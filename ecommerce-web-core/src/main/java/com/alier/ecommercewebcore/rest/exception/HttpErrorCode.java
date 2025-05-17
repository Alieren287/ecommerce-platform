package com.alier.ecommercewebcore.rest.exception;

import com.alier.ecommercecore.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public interface HttpErrorCode extends ErrorCode {
    HttpStatus getHttpStatus();
}

