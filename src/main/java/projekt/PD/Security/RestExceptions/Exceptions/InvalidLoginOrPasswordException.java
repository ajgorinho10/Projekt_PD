package projekt.PD.Security.RestExceptions.Exceptions;

/*
 * Klasa InvalidLoginOrPasswordException obsługuje wyjątek związany z nieprawidłowym loginem lub hasłem.
 */

public class InvalidLoginOrPasswordException extends RuntimeException {
    public InvalidLoginOrPasswordException(String message) {
        super(message);
    }
}
