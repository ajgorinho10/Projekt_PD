package projekt.PD.Security.RestExceptions.Exceptions;

public class InvalidLoginOrPasswordException extends RuntimeException {
    public InvalidLoginOrPasswordException(String message) {
        super(message);
    }
}
