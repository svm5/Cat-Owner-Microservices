package labs.externalmicroservice.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        // Логируем полный stacktrace
        System.out.println("Unhandled exception" + ex);

        return ResponseEntity.status(500)
                .body(new ErrorResponse("Server error", ex.getMessage()));
    }

    record ErrorResponse(String error, String details) {}
}
