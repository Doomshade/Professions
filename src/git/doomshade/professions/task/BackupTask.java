/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Jakub Šmrha
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package git.doomshade.professions.task;

import git.doomshade.professions.Professions;
import git.doomshade.professions.io.IOManager;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.ExtendedBukkitRunnable;
import git.doomshade.professions.utils.Utils;

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
 * @since 1.0
 */
public class BackupTask extends ExtendedBukkitRunnable {
    // 1 hr
    private static final long BACKUP_DELAY = Duration.ofHours(1).getSeconds();

    private static final String BACKUP = "backup";
    private static final FilenameFilter FILE_FILTER =
            (dir, name) -> !(name.contains(BACKUP) || dir.getName().contains(BACKUP));
    private static final int BUFSIZE = 1024;

    /**
     * Defaults to failure
     */
    private Result result = Result.FAILURE;

    @Override
    public void run() {

        try {
            writeZipFile(getAllFiles(Professions.getInstance().getDataFolder()),
                    new File(IOManager.getBackupFolder(), "backup-" + System.currentTimeMillis() + ".zip"));
            result = Result.SUCCESS;
        } catch (IOException e) {
            ProfessionLogger.logError(e);
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
     *
     * @throws IOException
     */
    private void writeZipFile(Collection<File> fileList, File outputFile) throws IOException {

        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            try (ZipOutputStream zos = new ZipOutputStream(fos)) {
                for (File file : fileList) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        ZipEntry zipEntry = new ZipEntry(file.getAbsolutePath());
                        zos.putNextEntry(zipEntry);
                        byte[] bytes = new byte[BUFSIZE];
                        int length;
                        while ((length = fis.read(bytes)) != -1) {
                            zos.write(bytes, 0, length);
                        }
                        zos.closeEntry();
                    }
                }

            }
        }
    }

    /**
     * @param dir the directory
     *
     * @return the list of files in a directory
     */
    private Collection<File> getAllFiles(File dir) {
        return getAllFiles(dir, new ArrayList<>());
    }

    private Collection<File> getAllFiles(File dir, Collection<File> currentFiles) {
        if (dir == null) {
            return currentFiles;
        }

        final File[] files = dir.listFiles(FILE_FILTER);
        if (files == null) {
            return currentFiles;
        }

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
        return BACKUP_DELAY * Utils.TICKS;
    }

    @Override
    protected long period() {
        return BACKUP_DELAY * Utils.TICKS;
    }

    public enum Result {
        SUCCESS,
        FAILURE
    }
}
