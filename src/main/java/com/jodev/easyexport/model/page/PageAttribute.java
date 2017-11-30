package com.jodev.easyexport.model.page;

public class PageAttribute {
    private int value;
    private PageUnit unit;

    public PageAttribute(int value, PageUnit unit) {
        this.value = value;
        this.unit = unit;
    }

    public int getValue() {
        return value;
    }

    public PageUnit getUnit() {
        return unit;
    }
}
