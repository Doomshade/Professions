package git.doomshade.professions.task;

import git.doomshade.professions.Professions;
import git.doomshade.professions.utils.ExtendedBukkitRunnable;

import java.io.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Backs up the plugin into the backup directory.
 *
 * @author Doomshade
 * @version 1.0
 */
public class BackupTask extends ExtendedBukkitRunnable {
    // 1 hr
    private static final long BACKUP_DELAY = Duration.ofHours(1).getSeconds();

    private static final String BACKUP = "backup";
    private static final FilenameFilter FILE_FILTER = (dir, name) -> !(name.contains(BACKUP) || dir.getName().contains(BACKUP));

    /**
     * Defaults to failure
     */
    private Result result = Result.FAILURE;

    @Override
    public void run() {

        try {
            writeZipFile(getAllFiles(Professions.getInstance().getDataFolder()), new File(Professions.getInstance().getBackupFolder(), "backup-" + System.currentTimeMillis() + ".zip"));
            result = Result.SUCCESS;
        } catch (IOException e) {
            Professions.logError(e);
        }
    }

    /**
     * @return the result of backup
     */
    public Result getResult() {
        return result;
    }

    /**
     * Writes the
     *
     * @param outputFile
     * @throws IOException
     */
    private void writeZipFile(Collection<File> fileList, File outputFile) throws IOException {

        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(outputFile);
        ZipOutputStream zos = new ZipOutputStream(fos);

        for (File file : fileList) {
            try (FileInputStream fis = new FileInputStream(file)) {
                ZipEntry zipEntry = new ZipEntry(file.getAbsolutePath());
                zos.putNextEntry(zipEntry);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) != -1) {
                    zos.write(bytes, 0, length);
                }
                zos.closeEntry();
            }
        }

        zos.close();
        fos.close();
    }

    /**
     * @param dir the directory
     * @return the list of files in a directory
     */
    private Collection<File> getAllFiles(File dir) {
        return getAllFiles(dir, new ArrayList<>());
    }

    private Collection<File> getAllFiles(File dir, Collection<File> currentFiles) {
        if (dir == null) return currentFiles;

        final File[] files = dir.listFiles(FILE_FILTER);
        if (files == null) return currentFiles;

        for (File file : files) {
            if (file.isDirectory()) {
                getAllFiles(file, currentFiles);
            } else {
                currentFiles.add(file);
            }
        }
        return currentFiles;
    }

    @Override
    protected long delay() {
        return BACKUP_DELAY * 20L;
    }

    @Override
    protected long period() {
        return BACKUP_DELAY * 20L;
    }

    public enum Result {
        SUCCESS, FAILURE
    }
}
