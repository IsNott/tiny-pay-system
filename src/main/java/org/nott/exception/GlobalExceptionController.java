package org.nott.exception;

import org.apache.commons.lang3.StringUtils;
import org.nott.common.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author Nott
 * @date 2024-5-7
 */

@RestControllerAdvice
public class GlobalExceptionController {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionController.class);

    @ExceptionHandler(value = PayException.class)
    public R<?> payExceptionHandler(PayException e){
        log.error("Catch payException {}", e.getMessage(), e);
        return R.failure(StringUtils.isNotEmpty(e.getMessage()) ? e.getMessage() : "Something wrong at pay process,please retry later.");
    }

    @ExceptionHandler(value = RuntimeException.class)
    public R<?> runExceptionHandler(RuntimeException e){
        log.error("Catch runException {}", e.getMessage(), e);
        return R.failure();
    }
}
