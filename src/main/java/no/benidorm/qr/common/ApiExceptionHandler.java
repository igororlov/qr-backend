package no.benidorm.qr.common;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ProblemDetail> handleNotFound(NotFoundException ex, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(BadRequestException.class)
    ResponseEntity<ProblemDetail> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return problem(HttpStatus.FORBIDDEN, "Access denied", request);
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ProblemDetail> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return problem(HttpStatus.UNAUTHORIZED, "Invalid email or password", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        ProblemDetail detail = build(HttpStatus.BAD_REQUEST, "Validation failed", request);
        detail.setProperty("errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, HttpServletRequest request) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request);
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(build(status, message, request));
    }

    private ProblemDetail build(HttpStatus status, String message, HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
        detail.setProperty("timestamp", Instant.now());
        detail.setProperty("path", request.getRequestURI());
        return detail;
    }
}
