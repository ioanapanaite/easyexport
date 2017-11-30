package com.jodev.easyexport.validation;

public enum ValidationErrorCode {
    NO_APP_CONFIG("No application configuration defined"),
    NO_PHANTOM_EXECUTABLE("No PhantomJs executable path defined"),
    NO_PHANTOM_SCRIPT("No PhantomJs script path defined"),

    NO_PAGE_CONFIG("No page configuration defined"),
    FORMAT_IS_DEFINED("Page paper size format should not be defined"),
    ORIENTATION_IS_DEFINED("Page paper size orientation should not be defined"),
    EXPORT_TYPE_IS_NOT_DEFINED("Page export type is not defined");

    ValidationErrorCode(String message) {
        this.message = message;
    }

    private String message;

    public String getMessage() {
        return message;
    }
}
