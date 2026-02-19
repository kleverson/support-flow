package br.com.supportflow.SupportFlow.common.util;

import br.com.supportflow.SupportFlow.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class Base64Utils {
    public static String resolveContentType(String base64) {
        if (base64 == null || base64.isBlank()) {
            throw new BusinessException("INVALID_FILE", "File payload is empty", HttpStatus.BAD_REQUEST);
        }
        if (base64.startsWith("data:")) {
            int start = "data:".length();
            int end = base64.indexOf(';');
            if (end > start) {
                return base64.substring(start, end);
            }
        }
        return "application/octet-stream";
    }

    public static String resolveFileName(String storagePath) {
        if (storagePath == null || storagePath.isBlank()) {
            return UUID.randomUUID().toString();
        }
        int idx = storagePath.lastIndexOf('/');
        if (idx < 0 || idx == storagePath.length() - 1) {
            return UUID.randomUUID().toString();
        }
        return storagePath.substring(idx + 1);
    }
}
