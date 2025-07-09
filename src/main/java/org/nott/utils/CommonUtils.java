package org.nott.utils;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author Nott
 * @date 2024-5-8
 */
@Slf4j
public class CommonUtils {

    public static String getHttpRequestBody(HttpServletRequest request){
        StringBuilder req = null;
        try {
            BufferedReader reader = request.getReader();
            req = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                req.append(line);
            }
        } catch (IOException e) {
            log.error("getHttpRequestBody error:{}",e.getMessage(),e);
        }
        return req.toString();
    }

    public static void writeHttpResp() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        if (requestAttributes == null) {
            log.error("requestAttributes is null, can not print to web");
            return;
        }
        HttpServletResponse response = requestAttributes.getResponse();
        if (response == null) {
            log.error("httpServletResponse is null, can not print to web");
            return;
        }

        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ServletOutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            outputStream.print("success");
        } catch (Exception e) {
            log.error("write web response error {}", e.getMessage(), e);
            throw new RuntimeException("io 异常", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
