package com.jodev.easyexport.service.impl;

import com.google.common.collect.Maps;
import com.jodev.easyexport.config.EasyExportConfig;
import com.jodev.easyexport.config.EasyExportPageConfig;
import com.jodev.easyexport.model.page.PageExportType;
import com.jodev.easyexport.model.phantomjs.EasyExportRenderer;
import com.jodev.easyexport.service.EasyExporter;
import com.jodev.easyexport.validation.validators.EasyExportConfigValidator;
import com.jodev.easyexport.validation.validators.EasyExportPageConfigValidator;
import com.twitter.finagle.builder.ClientBuilder;
import com.twitter.finagle.http.Request;
import com.twitter.util.Await;
import com.twitter.util.Duration;
import com.twitter.util.Future;
import com.twitter.util.FutureEventListener;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.jodev.easyexport.util.EasyExportSynchronizationUtil.*;
import static com.jodev.easyexport.util.EasyExportUtil.getQueryString;
import static com.jodev.easyexport.util.EasyExportSynchronizationUtil.STOP_RENDERERS_CONDITION;

@Service
public class EasyPhantomJsExporter implements EasyExporter {
    private static Logger LOGGER = LoggerFactory.getLogger(EasyPhantomJsExporter.class);

    private static Map<Integer, EasyExportRenderer> easyExportRenderers = Maps.newHashMap();
    private static int requestsInProgress = 0;
    private EasyExportConfig config;

    @Autowired
    private EasyExportConfigValidator configValidator;
    @Autowired
    private EasyExportPageConfigValidator pageConfigValidator;

    @Override
    public void init(EasyExportConfig configuration) {
        configValidator.validateAndThrow(configuration);
        if(config != null) {
            LOGGER.error("EasyExport already initialized!");
            return;
        }
        config = configuration;
        config.getPhantomJsPortList().forEach(
                port -> easyExportRenderers.put(port, new EasyExportRenderer(false, port, 0, Optional.empty(), Optional.empty()))
        );
        easyExportRenderers.keySet().stream().limit(config.getPhantomJsNoOfAliveInstances()).forEach(this::startRenderer);
        LOGGER.info("EasyExport initialized...");
    }


    @Override
    public void destroy() {
        LOCK.lock();
        try {
            if(requestsInProgress != 0) {
                STOP_RENDERERS_CONDITION.await();
            }
            LOGGER.info("Stopping exporter...");
            easyExportRenderers.values().forEach(renderer -> {
                 if(stopRenderer(renderer.getPort())) {
                     easyExportRenderers.remove(renderer.getPort());
                 }
            });
        } catch (InterruptedException e) {
            LOGGER.error("Error awaiting for all renderers to close: ", e.getMessage());
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public File render(EasyExportPageConfig pageConfig, String url, String resultPath) {
        if(config == null) {
            LOGGER.error("EasyExport not initialized! EasyExporter.init should be called before rendering an url!");
            return null;
        }
        try {
            pageConfigValidator.validateAndThrow(pageConfig);

            File resultFile = initResultFile(resultPath, pageConfig.getPageExportType());

            Map<String, String> params = Maps.newHashMap();
            params.put("url", url);
            params.put("file", resultFile.getAbsolutePath());
            params.put("config", pageConfig.getJson());

            callRenderer(url, params);
            while(!resultFile.exists())
            {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    LOGGER.error("Error on writing the file for url " + url + ": " + e.getMessage());
                }
            }
            return resultFile;
        } catch (Exception e) {
            LOGGER.error("Error getting the response from phantomjs server for rendering the url " + url + ": " + e.getMessage());
            return null;
        }
    }


    private File initResultFile(String resultPath, PageExportType type) throws IOException {
        File resultFile;
        if(resultPath == null) {
            resultFile = File.createTempFile("com/jodev/easyexport",type.extension());
            resultFile.deleteOnExit();
        } else {
            resultFile = new File(resultPath);
        }
        return resultFile;
    }

    private void callRenderer(String url, Map<String, String> params) throws Exception {
        com.twitter.finagle.Service<HttpRequest, HttpResponse> client =
                ClientBuilder.safeBuild(
                        ClientBuilder.get()
                                .codec(com.twitter.finagle.http.Http.get().enableTracing(true))
                                .keepAlive(true)
                                .hosts(getAvailableHosts())
                                .hostConnectionLimit(1000)
                                .requestTimeout(Duration.fromSeconds(config.getRequestTimeout()))
                                .tcpConnectTimeout(Duration.fromSeconds(10))
                                .retries(config.getMaxRetriesPerRequest()));
        Request request = Request.apply(getQueryString(params));

        requestsInProgress++;
        Future<HttpResponse> response = client.apply(request);
        response.addEventListener(
            new FutureEventListener<HttpResponse>() {
                public void onSuccess(HttpResponse response) {
                    requestsInProgress--;
                    signalConditions();
                    if(response.headers().contains("port")) {
                        consumeFreeSlot(Integer.parseInt(response.headers().get("port")));
                    }
                    LOGGER.info("Url " + url + " rendered with success.");
                }

                public void onFailure(Throwable cause) {
                    requestsInProgress--;
                    checkIfTheRendererCrashed();
                    throw new RuntimeException(cause.getMessage());
                }
            });
        Await.result(response);
    }

    private void checkIfTheRendererCrashed() {
        LOCK.lock();
        try {
            easyExportRenderers.values().stream().filter(renderer -> {
                try {
                    if(renderer.isStarted() && renderer.getProcess().isPresent()) {
                        renderer.getProcess().get().exitValue();
                        return true;
                    }
                    return false;
                } catch (Exception e) {
                    return false;
                }
            }).forEach(renderer -> {
                easyExportRenderers.put(renderer.getPort(), new EasyExportRenderer(false, renderer.getPort(), 0, Optional.empty(), Optional.empty()));
                startNextAvailablePort();
            });
        } finally {
            LOCK.unlock();
        }

    }

    private boolean startRenderer(int port) {
        LOCK.lock();
        try {
            LOGGER.info("Starting renderer at port: " + port + "...");
            if(!easyExportRenderers.containsKey(port)) {
                LOGGER.info("!Renderer not allowed to start at the port " + port + "!");
                return false;
            } else if(easyExportRenderers.get(port).isStarted()) {
                LOGGER.info("!Renderer is already started at the port " + port + "!");
                return false;
            }

            ProcessBuilder processBuilder = new ProcessBuilder(
                    config.getPhantomJsExecutablePath(),
                    "--ignore-ssl-errors=true",
                    "--ssl-protocol=any",
                    "--web-security=false",
                    config.getPhantomJsScriptPath(),
                    String.valueOf(port),
                    config.getRendererLogFolderPath()
            );
            Process process = processBuilder.start();

            EasyExportRenderer renderer = easyExportRenderers.get(port);
            renderer.setStarted(true);
            renderer.setProcess(Optional.of(process));
            easyExportRenderers.put(port, renderer);

            LOGGER.info("Started renderer at port: " + port + "...");
            NEW_RENDERER_IS_OPEN.signalAll();
            return true;
        } catch (IOException e) {
            LOGGER.error("Could not start renderer at port: " + port + ". Error: " + e.getMessage());
            return false;
        } finally {
            LOCK.unlock();
        }
    }

    private boolean stopRenderer(int port) {
        LOCK.lock();
        try {
            EasyExportRenderer renderer = easyExportRenderers.get(port);

            if(!renderer.isStarted() && !renderer.getProcess().isPresent() && !renderer.getTimeOfDeath().isPresent()) {
                LOGGER.info("!Renderer from port " + port + " is already stopped!");
                return false;
            }

            LOGGER.info("Stopping renderer from port: " + port + "...");
            Process processToKill = renderer.getProcess().get();
            processToKill.destroy();
            processToKill.waitFor();
            processToKill.exitValue();

            renderer.setProcess(Optional.empty());
            renderer.setTimeOfDeath(Optional.empty());
            easyExportRenderers.put(port, renderer);

            NEW_RENDERER_CAN_START_CONDITION.signalAll();
            LOGGER.info("Renderer at port " + port + " was successfully killed...");
            return true;
        } catch (Throwable e) {
            LOGGER.error("Error removing renderer from port: " + port, e);
            return false;
        } finally {
            LOCK.unlock();
        }
    }

    private String getAvailableHosts() {
        LOCK.lock();
        try {
            long startedWorkers = getNoOfStartedRenderers();
            if (startedWorkers != 0 && requestsInProgress >= startedWorkers * config.getMaxRequestsPerRenderer()) {
                LOGGER.info("Maximum requests to be processed -> " + startedWorkers * config.getMaxRequestsPerRenderer());
                LOGGER.info("Client is waiting for signal that the exporter can process requests. -> " + requestsInProgress);
                OPEN_FOR_PROCESSING_CONDITION.await();
                LOGGER.info("Client received signal that the exporter can process requests. -> " + requestsInProgress);
            }
            if (easyExportRenderers.values().stream().allMatch(renderer -> renderer.isUnusable(config.getMaxRequestsPerRenderer()))) {
                LOGGER.info("Client does not have available hosts and is waiting for a new one to open. -> " + startedWorkers * config.getMaxRequestsPerRenderer());
                NEW_RENDERER_IS_OPEN.await();
                LOGGER.info("Client received signal that there is a new open host. -> " + startedWorkers * config.getMaxRequestsPerRenderer());
            }
            return easyExportRenderers.values()
                    .stream()
                    .filter(EasyExportRenderer::isStarted)
                    .map(worker -> config.getPhantomJsHost() + ":" + worker.getPort())
                    .collect(Collectors.joining(","));
        } catch (InterruptedException e) {
            LOGGER.error("Error on awaiting signal that exporter can start processing request!");
            return null;
        } finally {
            LOCK.unlock();
        }
    }

    private void consumeFreeSlot(int port) {
        LOCK.lock();
        try {
            EasyExportRenderer renderer = easyExportRenderers.get(port);
            renderer.incrementProcessedRequests();
            if(renderer.isUnusable(config.getMaxRequestsPerRenderer())) {
                markRendererStopped(port);
                startNextAvailablePort();
            }
        } finally {
            LOCK.unlock();
        }
    }

    private void markRendererStopped(int port) {
        LOCK.lock();
        try {
            EasyExportRenderer renderer = easyExportRenderers.get(port);
            renderer.setStarted(false);
            renderer.setTimeOfDeath(Optional.of(LocalDateTime.now()));
            renderer.setNoOfProcessedRequests(0);
            easyExportRenderers.put(port, renderer);
            LOGGER.info("Renderer has been marked as stopped at port " + port + "...");
        } finally {
            LOCK.unlock();
        }
    }

    private void startNextAvailablePort() {
        LOCK.lock();
        try {
            if (easyExportRenderers.values().stream().noneMatch(renderer -> !renderer.isStarted() && !renderer.getProcess().isPresent())) {
                LOGGER.info("Waiting for signal to start new renderer...");
                NEW_RENDERER_CAN_START_CONDITION.await();
                LOGGER.info("Signal received to start new renderer...");
            }
            EasyExportRenderer rendererToStart = easyExportRenderers.values()
                                                    .stream()
                                                    .filter(renderer -> !renderer.isStarted() && !renderer.getProcess().isPresent())
                                                    .findFirst()
                                                    .get();
            LOGGER.info("New renderer to be started at port " + rendererToStart.getPort() + "...");
            startRenderer(rendererToStart.getPort());
        } catch (InterruptedException e) {
            LOGGER.error("Error while awaiting signal to open new renderer!");
        } finally {
            LOCK.unlock();
        }
    }

    private void signalConditions() {
        LOCK.lock();
        try {
            if (requestsInProgress == 0) {
                STOP_RENDERERS_CONDITION.signalAll();
                OPEN_FOR_PROCESSING_CONDITION.signalAll();
                LOGGER.debug("Exporter can be stopped...");
                LOGGER.debug("Exporter has free slots and can process requests...");
            } else if(requestsInProgress < getNoOfStartedRenderers() * config.getMaxRequestsPerRenderer()){
                OPEN_FOR_PROCESSING_CONDITION.signalAll();
                LOGGER.debug("Exporter has free slots and can process requests...");
            } else {
                LOGGER.debug("Exporter still processes requests and does not have free slots...");
            }
        } finally {
            LOCK.unlock();
        }
    }

    private long getNoOfStartedRenderers() {
        LOCK.lock();
        try {
            return easyExportRenderers.values().stream().filter(EasyExportRenderer::isStarted).count();
        } finally {
            LOCK.unlock();
        }

    }

    @Scheduled(fixedRate = 60000)
    private void recycleRenderer() {
        easyExportRenderers.values()
                .stream()
                .filter(renderer -> renderer.getTimeOfDeath().isPresent())
                .forEach(renderer -> {
                    if(stopRenderer(renderer.getPort())) {
                        renderer.setTimeOfDeath(Optional.empty());
                        easyExportRenderers.put(renderer.getPort(), renderer);
                    }
                });
    }

}
