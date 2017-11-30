package com.jodev.easyexport.config;


import com.google.gson.Gson;
import com.jodev.easyexport.model.page.*;

import javax.servlet.http.Cookie;
import java.util.List;

import static com.jodev.easyexport.util.EasyExportUtil.encodeURIComponent;

public class EasyExportPageConfig {
    private String pageCookiesString;
    private PageExportType pageExportType;
    private PageViewport pageViewport;
    private PagePaperSize pagePaperSize;
    private PageBasicAuthentication pageBasicAuthentication;

    private EasyExportPageConfig(Builder Builder) {
        this.pageCookiesString = encodeURIComponent(new Gson().toJson(Builder.pageCookies));
        this.pageExportType = Builder.pageExportType;
        this.pageViewport = Builder.pageViewport;
        this.pagePaperSize = Builder.pagePaperSize;
        this.pageBasicAuthentication = Builder.pageBasicAuthentication;
    }

    public String getPageCookiesString() {
        return pageCookiesString;
    }

    public PageExportType getPageExportType() {
        return pageExportType;
    }

    public PageViewport getPageViewport() {
        return pageViewport;
    }

    public PagePaperSize getPagePaperSize() {
        return pagePaperSize;
    }

    public PageBasicAuthentication getPageBasicAuthentication() {
        return pageBasicAuthentication;
    }

    public String getJson() {
        return new Gson().toJson(this);
    }

    public static class Builder {
        private List<Cookie> pageCookies;
        private PageExportType pageExportType;
        private PageViewport pageViewport;
        private PagePaperSize pagePaperSize;
        private PageBasicAuthentication pageBasicAuthentication;

        public Builder withPageCookies(List<Cookie> cookieList) {
            this.pageCookies = cookieList;
            return this;
        }

        public Builder exportType(PageExportType exportType) {
            this.pageExportType = exportType;
            return this;
        }

        public Builder pageViewportSize(int width, int height) {
            this.pageViewport = new PageViewport(width, height);
            return this;
        }

        public Builder pagePaperSize(PagePaperSize pagePaperSize) {
            this.pagePaperSize = pagePaperSize;
            return this;
        }

        public Builder withBasicAuthentication(PageBasicAuthentication authentication){
            this.pageBasicAuthentication = authentication;
            return this;
        }

        public EasyExportPageConfig build() {
            return new EasyExportPageConfig(this);
        }
    }
}
