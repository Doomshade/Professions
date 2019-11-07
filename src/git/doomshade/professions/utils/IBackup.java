package git.doomshade.professions.utils;

import java.io.File;

public interface IBackup {

    /**
     * @return the files to backup
     */
    File[] getFiles();
}
