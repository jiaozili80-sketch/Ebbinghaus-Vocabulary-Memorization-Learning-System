package com.ebbinghaus.vocab.exception;

import com.ebbinghaus.vocab.common.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResult<Void> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ApiResult.fail(400, ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ApiResult<Void> handleValidationException(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException validException) {
            String message = Optional.ofNullable(validException.getBindingResult().getFieldError())
                    .map(FieldError::getDefaultMessage)
                    .orElse("参数校验失败");
            return ApiResult.fail(400, message);
        }
        if (ex instanceof BindException bindException) {
            String message = Optional.ofNullable(bindException.getBindingResult().getFieldError())
                    .map(FieldError::getDefaultMessage)
                    .orElse("参数校验失败");
            return ApiResult.fail(400, message);
        }
        return ApiResult.fail(400, "参数校验失败");
    }

    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleException(Exception ex) {
        log.error("Unhandled backend exception", ex);
        return ApiResult.fail("系统异常，请稍后重试");
    }
}
