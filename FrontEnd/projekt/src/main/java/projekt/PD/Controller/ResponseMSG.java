package projekt.PD.Controller;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class ResponseMSG<T> {
    public int status;
    public String message;
    public T data;
    public  LocalDateTime timestamp;

    public ResponseMSG(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
}
