package git.doomshade.professions.exceptions;

import git.doomshade.professions.api.spawn.ILocationElement;
import git.doomshade.professions.io.ProfessionLogger;

import java.util.logging.Level;

public class SpawnException extends Exception {
    private SpawnExceptionReason reason = SpawnExceptionReason.UNKNOWN;
    private ILocationElement locationElement = null;

    public SpawnException(Throwable cause) {
        this(cause, SpawnExceptionReason.UNKNOWN);
    }

    public SpawnException(Throwable cause, SpawnExceptionReason reason) {
        this(cause, reason, null);
    }

    public SpawnException(Throwable cause, SpawnExceptionReason reason, ILocationElement locationElement) {
        super(cause);
        this.reason = reason;
        this.locationElement = locationElement;
    }

    protected SpawnException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public void printStackTrace() {
        String locationElement = this.locationElement == null ? "some element in the world" : this.locationElement.toString();
        ProfessionLogger.log("Could not spawn " + locationElement);

        String reason = "Reason: ";
        switch (this.reason) {
            case UNKNOWN:
                break;
            case INVALID_MATERIAL:
                ProfessionLogger.log(reason.concat("Invalid material given"), Level.WARNING);
                break;
            case INVALID_LOCATION:
                ProfessionLogger.log(reason.concat("Invalid location given"), Level.WARNING);
                break;
        }
        super.printStackTrace();
    }

    public enum SpawnExceptionReason {
        UNKNOWN,
        INVALID_MATERIAL,
        INVALID_LOCATION
    }
}
