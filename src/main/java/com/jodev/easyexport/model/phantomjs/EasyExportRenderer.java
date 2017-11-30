package com.jodev.easyexport.model.phantomjs;

import java.time.LocalDateTime;
import java.util.Optional;

public class EasyExportRenderer {

    private boolean started;
    private int port;
    private int noOfProcessedRequests;
    private Optional<Process> process = Optional.empty();
    private Optional<LocalDateTime> timeOfDeath = Optional.empty();

    public EasyExportRenderer(boolean started, int port, int noOfProcessedRequests, Optional<Process> process, Optional<LocalDateTime> timeOfDeath) {
        this.started = started;
        this.port = port;
        this.noOfProcessedRequests = noOfProcessedRequests;
        this.process = process;
        this.timeOfDeath = timeOfDeath;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getNoOfProcessedRequests() {
        return noOfProcessedRequests;
    }

    public void setNoOfProcessedRequests(int noOfProcessedRequests) {
        this.noOfProcessedRequests = noOfProcessedRequests;
    }

    public Optional<Process> getProcess() {
        return process;
    }

    public void setProcess(Optional<Process> process) {
        this.process = process;
    }

    public Optional<LocalDateTime> getTimeOfDeath() {
        return timeOfDeath;
    }

    public void setTimeOfDeath(Optional<LocalDateTime> timeOfDeath) {
        this.timeOfDeath = timeOfDeath;
    }

    public void incrementProcessedRequests() {
        this.noOfProcessedRequests++;
    }

    public boolean isUnusable(Integer maxAllowedRequests) {
        return this.started && this.process.isPresent() && noOfProcessedRequests >= maxAllowedRequests;
    }


}
