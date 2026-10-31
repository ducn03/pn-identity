package com.premiernuts.identity.core.exception;

import com.premiernuts.identity.core.utils.MessageHelper;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PnException extends RuntimeException {

    private String errorCode;
    private String errorMessage;
    private List<String> errors;

    public PnException(String errorCode) {
        super(MessageHelper.getMessage(errorCode));
        this.errorCode = errorCode;
        this.errorMessage = MessageHelper.getMessage(errorCode);
    }

    public PnException(String errorCode, List<String> errors) {
        super(MessageHelper.getMessage(errorCode));
        this.errorCode = errorCode;
        this.errorMessage = MessageHelper.getMessage(errorCode);
        this.errors = errors;
    }

    public PnException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public PnException(String errorCode, String errorMessage, List<String> errors) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errors = errors;
    }
}
