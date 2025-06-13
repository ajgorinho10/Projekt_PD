package projekt.PD.Security.RestExceptions.Exceptions;

public class LoginAlreadyExistException extends RuntimeException {
    public LoginAlreadyExistException(String message) {
        super(message);
    }
}
