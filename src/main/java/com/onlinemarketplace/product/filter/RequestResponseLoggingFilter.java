package com.onlinemarketplace.product.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(wrappedRequest, wrappedResponse);
        logRequest(wrappedRequest);
        logResponse(wrappedResponse);
        wrappedResponse.copyBodyToResponse();
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        logger.info("Request: {} {}, Headers: {}, Body: \n{}", request.getMethod(), request.getRequestURI(),
                request.getHeader("AUTHORIZATION"), new String(request.getContentAsByteArray(), StandardCharsets.UTF_8));
    }

    private void logResponse(ContentCachingResponseWrapper response) {
        logger.info("Response: {}, Headers: {}, Body: \n{}", response.getStatus(), response.getHeaderNames(),
                new String(response.getContentAsByteArray(), StandardCharsets.UTF_8));
    }

}

