package jp.drmatching.common;

import jp.drmatching.laboratory.LaboratoryNotFoundException;
import jp.drmatching.researcher.ResearcherRegistrationConflictException;
import jp.drmatching.admin.AdminUnauthorizedException;
import jp.drmatching.admin.RegistrationReviewNotFoundException;
import jp.drmatching.auth.InvalidCredentialsException;
import jp.drmatching.researcher.InvalidResearcherPasswordException;
import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "timestamp", OffsetDateTime.now(), "status", 401, "message", exception.getMessage()));
    }

    @ExceptionHandler(InvalidResearcherPasswordException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPassword(InvalidResearcherPasswordException exception) {
        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", OffsetDateTime.now(), "status", 400, "message", exception.getMessage()));
    }

    @ExceptionHandler(AdminUnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleAdminUnauthorized(AdminUnauthorizedException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "timestamp", OffsetDateTime.now(), "status", 401, "message", exception.getMessage()));
    }

    @ExceptionHandler(RegistrationReviewNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRegistrationNotFound(
            RegistrationReviewNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "timestamp", OffsetDateTime.now(), "status", 404, "message", exception.getMessage()));
    }

    @ExceptionHandler(ResearcherRegistrationConflictException.class)
    public ResponseEntity<Map<String, Object>> handleRegistrationConflict(
            ResearcherRegistrationConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", OffsetDateTime.now(), "status", 409, "message", exception.getMessage()));
    }

    @ExceptionHandler(LaboratoryNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(LaboratoryNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "timestamp", OffsetDateTime.now(), "status", 404, "message", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst().orElse("入力内容を確認してください。");
        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", OffsetDateTime.now(), "status", 400, "message", message));
    }
}
