package git.doomshade.professions.data.cache;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CacheHeader implements Serializable {

    private final LocalDateTime time = LocalDateTime.now();
}
