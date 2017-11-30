package com.jodev.easyexport.validation.validators;

import com.jodev.easyexport.config.EasyExportConfig;
import com.jodev.easyexport.validation.EasyExportValidator;
import com.jodev.easyexport.validation.ValidationErrorContext;
import org.springframework.stereotype.Component;

import static com.jodev.easyexport.validation.ValidationErrorCode.*;

@Component
public class EasyExportConfigValidator implements EasyExportValidator<EasyExportConfig> {


    @Override
    public void validate(EasyExportConfig config, ValidationErrorContext context) {
        if(config == null) {
            context.addError(NO_APP_CONFIG);
        } else {
            if(config.getPhantomJsExecutablePath() == null) {
                context.addError(NO_PHANTOM_EXECUTABLE);
            }
            if(config.getPhantomJsScriptPath() == null) {
                context.addError(NO_PHANTOM_SCRIPT);
            }
        }
    }
}
