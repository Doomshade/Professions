package git.doomshade.professions.exceptions;

import java.io.IOException;

public class ConfigurationException extends IOException {
    private String message;

    public ConfigurationException() {
        super();
    }

    public ConfigurationException(String message) {
        super(message);
        this.message = message;
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }

    public void append(String message) {
        this.message += " " + message;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + ": " + message;
    }

}
