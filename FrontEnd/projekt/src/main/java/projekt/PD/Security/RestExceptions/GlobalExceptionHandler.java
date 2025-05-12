package projekt.PD.Security.RestExceptions;


import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;

import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import projekt.PD.Security.RestExceptions.Exceptions.InvalidLoginOrPasswordException;
import projekt.PD.Security.RestExceptions.Exceptions.LoginAlreadyExistException;

import javax.security.auth.login.LoginException;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LoginAlreadyExistException.class)
    public ProblemDetail handleLoginException(LoginAlreadyExistException exception) {
        ProblemDetail errorDetail = null;
        errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());
        errorDetail.setProperty("description", "login");
        return errorDetail;
    }

    @ExceptionHandler(InvalidLoginOrPasswordException.class)
    public ProblemDetail handleInvalidLoginOrPasswordException(InvalidLoginOrPasswordException exception) {
        ProblemDetail errorDetail = null;
        errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());
        errorDetail.setProperty("description", "login or password");
        return errorDetail;
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ProblemDetail handleAuthorizationDeniedException(AuthorizationDeniedException exception) {
        ProblemDetail errorDetail = null;
        errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
        errorDetail.setProperty("description", "Nie masz uprawnień do tego zasobu");

        return errorDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        ProblemDetail errorDetail = null;
        errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, Objects.requireNonNull(exception.getBindingResult().getFieldError()).getDefaultMessage());
        errorDetail.setProperty("description", Objects.requireNonNull(exception.getBindingResult().getFieldError()).getField());
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
