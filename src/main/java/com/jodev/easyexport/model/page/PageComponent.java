package com.jodev.easyexport.model.page;

import static com.jodev.easyexport.util.EasyExportUtil.encodeURIComponent;

public class PageComponent {
    public static String PAGE_NUM = "%pageNum%";
    public static String NUM_PAGES = "%numPages%";

    public PageComponent(PageAttribute height, String content) {
        this.content = encodeURIComponent(content);
        this.height = height;
    }

    private String content;

    private PageAttribute height;

    public String getContent() {
        return content;
    }

    public PageAttribute getHeight() {
        return height;
    }
}
