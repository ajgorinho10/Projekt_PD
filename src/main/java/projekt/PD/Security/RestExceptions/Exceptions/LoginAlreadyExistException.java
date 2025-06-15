package projekt.PD.Security.RestExceptions.Exceptions;

/*
 * Klasa LoginAlreadyExistException obsługuje wyjątek związany z próbą rejestracji nowego użytkownika
 * w przypadku gdy login instnieje już w bazie danych
 */

public class LoginAlreadyExistException extends RuntimeException {
    public LoginAlreadyExistException(String message) {
        super(message);
    }
}
