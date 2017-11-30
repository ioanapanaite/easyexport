package com.jodev.easyexport.validation;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;

public class ValidationErrorContext extends RuntimeException{
    private List<ValidationErrorCode> errors = Lists.newArrayList();

    public void addError(ValidationErrorCode error) {
        errors.add(error);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<ValidationErrorCode> getErrors() {
        return errors;
    }

    @Override
    public String getMessage() {
        if(errors.isEmpty()) {
            return "No validation errors.";
        }
        return "Validation errors: " + errors.stream().map(ValidationErrorCode::getMessage).collect(Collectors.joining(",")) + ".";
    }
}
