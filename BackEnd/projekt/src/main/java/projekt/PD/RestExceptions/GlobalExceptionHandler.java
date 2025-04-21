package projekt.PD.RestExceptions;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import projekt.PD.Controller.ResponseMSG;

import java.util.HashMap;
import java.util.Map;


@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseMSG<ErrorResponse>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getBody().getDetail(),
                "Złe zapytanie do serwera"
        );

        Map<String, String> details = new HashMap<>();

        for(FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            details.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        errorResponse.setDetails(details);
        errorResponse.setPath("/users");


        return new ResponseEntity<>(new ResponseMSG<>(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.toString(), errorResponse), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseMSG<ErrorResponse>> handleException(Exception ex) {

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                "Złe zapytanie do serwera"
        );

        return new ResponseEntity<>(new ResponseMSG<>(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.toString(), errorResponse), HttpStatus.BAD_REQUEST);
    }
}
