package git.doomshade.professions.task;

import git.doomshade.professions.Professions;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupTask extends BukkitRunnable {

    private Map<File, List<File>> fileList;

    @Override
    public void run() {
        fileList = new HashMap<>(getAllFiles(Professions.getInstance().getDataFolder()));
        writeZipFile(new File(Professions.getInstance().getBackupFolder(), "backup-" + System.currentTimeMillis() + ".zip"));
    }

    private Map<File, List<File>> getAllFiles(File dir) {
        return getAllFiles(dir, new HashMap<>());
    }

    private Map<File, List<File>> getAllFiles(File dir, Map<File, List<File>> currentFiles) {
        if (dir == null) {
            return currentFiles;
        }
        for (File file : dir.listFiles()) {
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

    private void writeZipFile(File outputFile) {

        try {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
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
