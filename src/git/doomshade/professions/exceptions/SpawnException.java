package git.doomshade.professions.exceptions;

import git.doomshade.professions.Professions;
import git.doomshade.professions.profession.utils.LocationElement;

import java.util.logging.Level;

public class SpawnException extends Exception {
    private SpawnExceptionReason reason = SpawnExceptionReason.UNKNOWN;
    private LocationElement locationElement = null;

    public SpawnException(Throwable cause) {
        this(cause, SpawnExceptionReason.UNKNOWN);
    }

    public SpawnException(Throwable cause, SpawnExceptionReason reason) {
        this(cause, reason, null);
    }

    public SpawnException(Throwable cause, SpawnExceptionReason reason, LocationElement locationElement) {
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
        Professions.log("Could not spawn " + locationElement);

        String reason = "Reason: ";
        switch (this.reason) {
            case UNKNOWN:
                break;
            case INVALID_MATERIAL:
                Professions.log(reason.concat("Invalid material given"), Level.WARNING);
                break;
            case INVALID_LOCATION:
                Professions.log(reason.concat("Invalid location given"), Level.WARNING);
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
