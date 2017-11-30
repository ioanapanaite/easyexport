package com.jodev.easyexport.validation;

public interface EasyExportValidator<T> {

    /**
     * Validates the given object and adds the validation errors to the context if the case
     *
     * @param object The object to be validated
     * @param context The context that contains all the validation errors
     */
    void validate(T object, ValidationErrorContext context);

    /**
     * Vallidates the given object, adds the validation errors to the context if the case and then throws the errors if the context is not emoty
     *
     * @param object The object to be validated
     * @param context The context that contains all the validation errors
     *
     * @throws ValidationErrorContext The context containing all the validation errors
     */
    default void validateAndThrow(T object, ValidationErrorContext context) {
        this.validate(object, context);
        if(context.hasErrors()) {
            throw context;
        }
    }

    /**
     * Vallidates the given object, adds the validation errors to a new context if the case and then throws the errors if the context is not emoty
     *
     * @param object The object to be validated
     *
     * @throws ValidationErrorContext The context containing all the validation errors
     */
    default void validateAndThrow(T object) {
        ValidationErrorContext context = new ValidationErrorContext();
        this.validateAndThrow(object, context);
    }
}
