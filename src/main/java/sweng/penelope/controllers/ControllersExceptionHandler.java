package sweng.penelope.controllers;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ControllersExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintValidation(ConstraintViolationException ex) {
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        StringBuilder sb = new StringBuilder();

        violations.forEach(violation -> {
            sb.append(violation.getMessage() + "\n");
        });

        return ResponseEntity.badRequest().body(sb.toString());
    }
}
