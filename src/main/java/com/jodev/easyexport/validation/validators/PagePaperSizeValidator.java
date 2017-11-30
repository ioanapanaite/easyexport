package com.jodev.easyexport.validation.validators;

import com.jodev.easyexport.model.page.PagePaperSize;
import com.jodev.easyexport.validation.EasyExportValidator;
import com.jodev.easyexport.validation.ValidationErrorCode;
import com.jodev.easyexport.validation.ValidationErrorContext;
import org.springframework.stereotype.Component;

@Component
public class PagePaperSizeValidator implements EasyExportValidator<PagePaperSize>{

    @Override
    public void validate(PagePaperSize pagePaperSize, ValidationErrorContext context) {
        if(pagePaperSize == null) {
            return;
        }
        if(pagePaperSize.getWidth() != null || pagePaperSize.getHeight() != null) {
            if(pagePaperSize.getFormat() != null) {
                context.addError(ValidationErrorCode.FORMAT_IS_DEFINED);
            }
            if(pagePaperSize.getOrientation() != null) {
                context.addError(ValidationErrorCode.ORIENTATION_IS_DEFINED);
            }
        }
    }
}
