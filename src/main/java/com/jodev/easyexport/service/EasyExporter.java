package com.jodev.easyexport.service;

import com.jodev.easyexport.config.EasyExportConfig;
import com.jodev.easyexport.config.EasyExportPageConfig;

import java.io.File;

public interface EasyExporter {

    /**
     * Inits the renderers pool that will further export the desired URLs. It will start a number of PhantomJs processes that will run at their specific ports.
     * Must ALWAYS be called before exporting any URL.
     *
     * @param configuration The configuration specific for the application and renderers. If the configuration is not valid, a runtime exception will be thrown.
     */
    void init(EasyExportConfig configuration);

    /**
     * This method takes care of safely terminating any process that is currently rendering an URL.
     * It is recommended to be used before shutting down the application in which com.jodev.easyexport is used.
     *
     */
    void destroy();

    /**
     * Exports the given url based on the defined configuration and saves it at the given result path
     *
     * @param pageConfig The page configuration that describes the attributes of the exported page
     * @param url The url to be exported
     * @param resultPath The path where the result file will be saved. If null, then the created file will be just a temporary one that will be deleted once the application shuts down.
     *
     * @return The exported file
     */
    File render(EasyExportPageConfig pageConfig, String url, String resultPath);
}
