package com.jodev.easyexport.model.page;

public enum PageExportType {
    PDF(".pdf"),
    PNG(".png"),
    JPEG(".jpeg"),
    BPM(".bpm"),
    PPM(".ppm"),
    GIF(".gif");

    PageExportType(String extensionString) {
        this.extensionString = extensionString;
    }

    private String extensionString;

    public String extension() {
        return this.extensionString;
    }
}
