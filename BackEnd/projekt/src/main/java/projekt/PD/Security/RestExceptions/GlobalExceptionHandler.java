package projekt.PD.Security.RestExceptions;


import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import projekt.PD.Security.RestExceptions.Exceptions.LoginAlreadyExistException;

import javax.security.auth.login.LoginException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LoginAlreadyExistException.class)
    public ProblemDetail handleLoginException(LoginAlreadyExistException exception) {
        ProblemDetail errorDetail = null;
        errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());
        errorDetail.setProperty("description", "login");
        return errorDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleSecurityException(Exception exception) {
        ProblemDetail errorDetail = null;
        errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500), exception.getMessage());
        errorDetail.setProperty("description", "Nie znany błąd");

        return errorDetail;
    }

}
