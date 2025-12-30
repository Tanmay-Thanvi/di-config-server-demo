package com.deepintent.di_config_server_demo.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
@Order(1)
public class ConfigServerLoggingFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                   @NonNull HttpServletResponse response, 
                                   @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        if (isConfigServerEndpoint(path)) {
            ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
            
            try {
                filterChain.doFilter(request, responseWrapper);
            } finally {
                logRequest(request);
                logResponse(responseWrapper);
                responseWrapper.copyBodyToResponse();
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private boolean isConfigServerEndpoint(String path) {
        if (path == null || path.equals("/") || 
            path.startsWith("/actuator/") || 
            path.startsWith("/error")) {
            return false;
        }
        return path.startsWith("/") && 
               (path.contains("/") || path.endsWith(".yml") || path.endsWith(".properties"));
    }

    private void logRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        String queryString = request.getQueryString();
        
        log.info("==========================================");
        log.info("CONFIG SERVER REQUEST");
        log.info("==========================================");
        log.info("Method: {}", request.getMethod());
        log.info("Path: {}", path);
        if (queryString != null && !queryString.isEmpty()) {
            log.info("Query: {}", queryString);
        }
        
        String application = extractApplication(path);
        String profile = extractProfile(path);
        String label = extractLabel(path, request);
        
        if (application != null) {
            log.info("Application: {}", application);
        }
        if (profile != null) {
            log.info("Profile(s): {}", profile);
        }
        if (label != null) {
            log.info("Label: {}", label);
        }
    }

    private void logResponse(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        int status = response.getStatus();
        
        log.info("Response Status: {}", status);
        
        if (content.length > 0 && status == 200) {
            try {
                String responseBody = new String(content, StandardCharsets.UTF_8);
                logResponseVariables(responseBody);
            } catch (Exception e) {
                log.debug("Could not parse response body: {}", e.getMessage());
            }
        }
        
        log.info("==========================================");
    }

    private void logResponseVariables(String responseBody) {
        try {
            if (responseBody.trim().startsWith("{")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> jsonResponse = objectMapper.readValue(responseBody, Map.class);
                
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> propertySources = 
                    (java.util.List<Map<String, Object>>) jsonResponse.get("propertySources");
                
                if (propertySources != null && !propertySources.isEmpty()) {
                    log.info("--- Variables Provided ({} sources) ---", propertySources.size());
                    propertySources.forEach(source -> {
                        String sourceName = (String) source.get("name");
                        @SuppressWarnings("unchecked")
                        Map<String, Object> sourceProperties = (Map<String, Object>) source.get("source");
                        
                        if (sourceName != null) {
                            log.info("Source: {}", sourceName);
                            if (sourceProperties != null) {
                                sourceProperties.forEach((key, value) -> {
                                    String maskedValue = maskSensitive(key, String.valueOf(value));
                                    log.info("  {} = {}", key, maskedValue);
                                });
                            }
                        }
                    });
                } else {
                    log.info("--- Variables Provided (direct properties) ---");
                    jsonResponse.forEach((key, value) -> {
                        if (!key.equals("propertySources") && !key.equals("version") && 
                            !key.equals("state") && !key.equals("label")) {
                            String maskedValue = maskSensitive(key, String.valueOf(value));
                            log.info("  {} = {}", key, maskedValue);
                        }
                    });
                }
                
                Object name = jsonResponse.get("name");
                if (name != null) {
                    log.info("Application Name: {}", name);
                }
                
                Object profiles = jsonResponse.get("profiles");
                if (profiles != null) {
                    log.info("Active Profiles: {}", profiles);
                }
            } else if (responseBody.contains(":") || responseBody.contains("=")) {
                log.info("--- Variables Provided (YAML/Properties format) ---");
                String[] lines = responseBody.split("\n");
                int lineCount = 0;
                for (String line : lines) {
                    if (lineCount++ > 100) {
                        log.info("  ... (truncated, showing first 100 lines)");
                        break;
                    }
                    if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                        continue;
                    }
                    if (line.contains(":") || line.contains("=")) {
                        String[] parts = line.split("[:=]", 2);
                        if (parts.length == 2) {
                            String key = parts[0].trim();
                            String value = parts[1].trim();
                            String maskedValue = maskSensitive(key, value);
                            log.info("  {} = {}", key, maskedValue);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not parse response as JSON/YAML: {}", e.getMessage());
            log.info("--- Raw Response (first 500 chars) ---");
            String preview = responseBody.length() > 500 ? responseBody.substring(0, 500) + "..." : responseBody;
            log.info(preview);
        }
    }

    private String maskSensitive(String key, String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        
        String lowerKey = key.toLowerCase();
        if (lowerKey.contains("password") || 
            lowerKey.contains("token") || 
            lowerKey.contains("secret") ||
            (lowerKey.contains("key") && (lowerKey.contains("api") || lowerKey.contains("auth")))) {
            if (value.length() > 8) {
                return value.substring(0, 4) + "***" + value.substring(value.length() - 4);
            }
            return "***";
        }
        
        return value;
    }

    private String extractApplication(String path) {
        if (path == null || path.length() <= 1) {
            return null;
        }
        
        String[] parts = path.substring(1).split("/");
        if (parts.length > 0 && !parts[0].isEmpty()) {
            String firstPart = parts[0];
            if (firstPart.endsWith(".yml") || firstPart.endsWith(".properties")) {
                int dashIndex = firstPart.indexOf('-');
                if (dashIndex > 0) {
                    return firstPart.substring(0, dashIndex);
                }
            } else {
                return firstPart;
            }
        }
        return null;
    }

    private String extractProfile(String path) {
        if (path == null || path.length() <= 1) {
            return null;
        }
        
        String[] parts = path.substring(1).split("/");
        if (parts.length > 1) {
            return parts[1];
        } else if (parts.length == 1) {
            String firstPart = parts[0];
            if (firstPart.contains("-")) {
                int dashIndex = firstPart.indexOf('-');
                int dotIndex = firstPart.indexOf('.');
                if (dotIndex > dashIndex) {
                    return firstPart.substring(dashIndex + 1, dotIndex);
                }
            }
        }
        return null;
    }

    private String extractLabel(String path, HttpServletRequest request) {
        String[] parts = path.substring(1).split("/");
        if (parts.length > 2) {
            return parts[2];
        }
        String labelParam = request.getParameter("label");
        if (labelParam != null) {
            return labelParam;
        }
        return null;
    }
}

