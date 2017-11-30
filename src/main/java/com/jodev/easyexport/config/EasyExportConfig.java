package com.jodev.easyexport.config;

import com.google.common.collect.Lists;

import java.util.List;

public class EasyExportConfig {

    private String phantomJsExecutablePath;

    private String phantomJsScriptPath;

    private String phantomJsHost;

    private List<Integer> phantomJsPortList;

    private Integer phantomJsNoOfAliveInstances;

    private Integer maxRequestsPerRenderer;

    private Integer maxRetriesPerRequest;

    private Integer requestTimeout;

    private String rendererLogFolderPath;

    private EasyExportConfig(Builder Builder) {
        this.phantomJsExecutablePath = Builder.phantomJsExecutablePath;
        this.phantomJsScriptPath = Builder.phantomJsScriptPath;
        this.phantomJsHost = Builder.phantomJsHost;
        this.phantomJsPortList = Builder.phantomJsPortList;
        this.phantomJsNoOfAliveInstances = Builder.phantomJsNoOfAliveInstances;
        this.maxRequestsPerRenderer = Builder.maxRequestsPerRenderer;
        this.maxRetriesPerRequest = Builder.maxRetriesPerRequest;
        this.requestTimeout = Builder.requestTimeout;
        this.rendererLogFolderPath = Builder.rendererLogFolderPath;
    }

    public String getPhantomJsExecutablePath() {
        return phantomJsExecutablePath;
    }

    public String getPhantomJsScriptPath() {
        return phantomJsScriptPath;
    }

    public String getPhantomJsHost() {
        return phantomJsHost;
    }

    public List<Integer> getPhantomJsPortList() {
        return phantomJsPortList;
    }

    public Integer getPhantomJsNoOfAliveInstances() {
        return phantomJsNoOfAliveInstances;
    }

    public Integer getMaxRequestsPerRenderer() {
        return maxRequestsPerRenderer;
    }

    public Integer getMaxRetriesPerRequest() {
        return maxRetriesPerRequest;
    }

    public Integer getRequestTimeout() {
        return requestTimeout;
    }

    public String getRendererLogFolderPath() {
        return rendererLogFolderPath;
    }

    public static class Builder {

        String phantomJsExecutablePath;

        String phantomJsScriptPath = this.getClass().getClassLoader().getResource("phantomjs/run.js").getPath();

        String phantomJsHost = "localhost";

        List<Integer> phantomJsPortList = Lists.newArrayList(9000,9001,9002,9003,9004,9005,9006,9007,9008,9009);

        Integer phantomJsNoOfAliveInstances = 3;

        Integer maxRequestsPerRenderer = 1;

        Integer maxRetriesPerRequest = 5;

        Integer requestTimeout = 60;

        String rendererLogFolderPath = "/tmp/easyexporter/logs";

        public Builder withPhantomJsExecutablePath(String path) {
            this.phantomJsExecutablePath = path;
            return this;
        }

        public Builder withPhantomJsScriptPath(String path) {
            this.phantomJsScriptPath = path;
            return this;
        }

        public Builder withPhantomJsHost(String host) {
            this.phantomJsHost = host;
            return this;
        }

        public Builder withPhantomJsPortList(Integer... portList) {
            this.phantomJsPortList = Lists.newArrayList(portList);
            return this;
        }

        public Builder withPhantomJsNoOfAliveInstances(Integer aliveInstances) {
            this.phantomJsNoOfAliveInstances = aliveInstances;
            return this;
        }

        public Builder withMaxRequestsPerRenderer(Integer maxRequestsPerRenderer) {
            this.maxRequestsPerRenderer = maxRequestsPerRenderer;
            return this;
        }

        public Builder withMaxRetriesPerRequest(Integer maxRetriesPerRequest) {
            this.maxRetriesPerRequest = maxRetriesPerRequest;
            return this;
        }

        public Builder withRequestTimeout(Integer requestTimeout) {
            this.requestTimeout = requestTimeout;
            return this;
        }

        public Builder withRendererLogFolderPath(String rendererLogFolderPath) {
            this.rendererLogFolderPath = rendererLogFolderPath;
            return this;
        }

        public EasyExportConfig build() {
            return new EasyExportConfig(this);
        }
    }
}
