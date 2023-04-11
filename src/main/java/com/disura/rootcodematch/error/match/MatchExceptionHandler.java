package com.disura.rootcodematch.error.match;

import com.disura.rootcodematch.controller.AppResponse;
import com.disura.rootcodematch.controller.MatchController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice(basePackageClasses = {MatchController.class})
@ResponseStatus
@Slf4j
public class MatchExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(MatchException.class)
    public ResponseEntity<AppResponse> matchException(MatchException exception, WebRequest request) {
        log.error(exception.getMessage(), exception);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        AppResponse error = new AppResponse(false, exception.getMessage(), null);
        return ResponseEntity.status(status).body(error);
    }

}
