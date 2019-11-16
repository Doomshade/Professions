package git.doomshade.professions.task;

import git.doomshade.professions.Professions;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Backup task.
 *
 * @author Doomshade
 */
public class BackupTask extends BukkitRunnable {

    private Map<File, List<File>> fileList;
    private Result result = Result.FAILURE;

    @Override
    public void run() {
        fileList = new HashMap<>(getAllFiles(Professions.getInstance().getDataFolder()));

        try {
            writeZipFile(new File(Professions.getInstance().getBackupFolder(), "backup-" + System.currentTimeMillis() + ".zip"));
            result = Result.SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Result getResult() {
        return result;
    }

    private void writeZipFile(File outputFile) throws IOException {

        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(outputFile);
        ZipOutputStream zos = new ZipOutputStream(fos);

        for (Map.Entry<File, List<File>> file : fileList.entrySet()) {
            for (File filee : file.getValue())
                addToZip(file.getKey(), filee, zos);
        }

        zos.close();
        fos.close();
    }

    private Map<File, List<File>> getAllFiles(File dir) {
        return getAllFiles(dir, new HashMap<>());
    }

    private Map<File, List<File>> getAllFiles(File dir, Map<File, List<File>> currentFiles) {
        if (dir == null) {
            return currentFiles;
        }
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.getName().contains("backup")) {
                continue;
            }
            if (file.isDirectory()) {
                getAllFiles(file, currentFiles);

            } else {
                List<File> files = new ArrayList<>(currentFiles.getOrDefault(dir, new ArrayList<>()));
                files.add(file);
                currentFiles.put(dir, files);
            }
        }
        return currentFiles;
    }

    public enum Result {
        SUCCESS, FAILURE
    }

    private void addToZip(File dir, File file, ZipOutputStream zos) throws
            IOException {
        FileInputStream fis = new FileInputStream(file);

        ZipEntry zipEntry = new ZipEntry(dir + "/" + file.getName());
        zos.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }

        zos.closeEntry();
        fis.close();
    }
}
