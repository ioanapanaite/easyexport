package com.jodev.easyexport.model.page;

public class PagePaperSize {
    private PageAttribute width;
    private PageAttribute height;
    private PageFormat format;
    private PageOrientation orientation;
    private PageAttribute margin;
    private PageComponent header;
    private PageComponent footer;

    private PagePaperSize(Builder Builder) {
        this.width = Builder.width;
        this.height = Builder.height;
        this.format = Builder.pageFormat;
        this.orientation = Builder.pageOrientation;
        this.margin = Builder.pageMargin;
        this.header = Builder.header;
        this.footer = Builder.footer;
    }

    public PageAttribute getWidth() {
        return width;
    }

    public PageAttribute getHeight() {
        return height;
    }

    public PageFormat getFormat() {
        return format;
    }

    public PageOrientation getOrientation() {
        return orientation;
    }

    public PageAttribute getMargin() {
        return margin;
    }

    public PageComponent getHeader() {
        return header;
    }

    public PageComponent getFooter() {
        return footer;
    }

    public static class Builder {
        PageAttribute width;
        PageAttribute height;
        PageFormat pageFormat;
        PageOrientation pageOrientation;
        PageAttribute pageMargin;
        PageComponent header;
        PageComponent footer;

        public Builder withFormat(PageFormat pageFormat, PageOrientation pageOrientation) {
            this.pageFormat = pageFormat;
            this.pageOrientation = pageOrientation;
            return this;
        }

        public Builder withDimensions(PageAttribute width, PageAttribute height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder withPageMargin(PageAttribute pageMargin) {
            this.pageMargin = pageMargin;
            return this;
        }

        public Builder withHeader(PageComponent header) {
            this.header = header;
            return this;
        }

        public Builder withFooter(PageComponent footer) {
            this.footer = footer;
            return this;
        }

        public PagePaperSize build() {
            return new PagePaperSize(this);
        }
    }
}
