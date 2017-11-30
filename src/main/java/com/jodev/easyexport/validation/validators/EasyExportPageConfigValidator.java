package com.jodev.easyexport.validation.validators;

import com.jodev.easyexport.config.EasyExportPageConfig;
import com.jodev.easyexport.validation.EasyExportValidator;
import com.jodev.easyexport.validation.ValidationErrorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.jodev.easyexport.validation.ValidationErrorCode.EXPORT_TYPE_IS_NOT_DEFINED;
import static com.jodev.easyexport.validation.ValidationErrorCode.NO_PAGE_CONFIG;

@Component
public class EasyExportPageConfigValidator implements EasyExportValidator<EasyExportPageConfig> {

    @Autowired
    private PagePaperSizeValidator paperSizeValidator;

    @Override
    public void validate(EasyExportPageConfig config, ValidationErrorContext context) {
        if (config == null) {
            context.addError(NO_PAGE_CONFIG);
        } else {
            if(config.getPageExportType() == null) {
                context.addError(EXPORT_TYPE_IS_NOT_DEFINED);
            }
            paperSizeValidator.validate(config.getPagePaperSize(), context);
        }
    }
}
