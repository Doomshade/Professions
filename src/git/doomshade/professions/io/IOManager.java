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

package git.doomshade.professions.io;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.task.BackupTask;
import git.doomshade.professions.user.User;
import git.doomshade.professions.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 *
 */
public class IOManager {
    private static final Professions plugin = Professions.getInstance();

    static PrintStream fos = null;
    private static final File CACHE_FOLDER = new File(plugin.getDataFolder(), "cache");
    private static final File PLAYER_FOLDER = new File(plugin.getDataFolder(), "playerdata");
    private static final File DATA_FOLDER = new File(plugin.getDataFolder(), "data");
    private static final File BACKUP_FOLDER = new File(plugin.getDataFolder(), "backup");
    private static final File LOGS_FOLDER;
    private static final File LOG_FILE;

    static {
        LOGS_FOLDER = new File(plugin.getDataFolder(), "logs");
        LOG_FILE = new File(getLogsFolder(),
                String.format("%s.txt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH_mm"))));
    }

    private static final File FILTERED_LOGS_FOLDER = new File(plugin.getDataFolder(), "filtered logs");
    private static final File ITEM_FOLDER = new File(plugin.getDataFolder(), "itemtypes");
    private static final File PROFESSION_FOLDER = new File(plugin.getDataFolder(), "professions");
    private static final File TRAINER_FOLDER = new File(plugin.getDataFolder(), "trainer gui");

    public static final String LANG_PATH = "lang/";
    private static final File LANG_FOLDER = new File(plugin.getDataFolder(), "lang");

    private static boolean FIRST_BACKUP = true;

    private static final HashMap<String, File> FILE_CACHE = new HashMap<>();


    /**
     * Creates a folder if possible
     *
     * @param file the folder to create
     *
     * @return the same file
     */
    private static File getFolder(File file) {
        if (!file.isDirectory()) {
            file.mkdirs();
        }
        return file;
    }

    private static File getFile(File dir, String name) {
        File folder = getFolder(dir);
        File f = new File(folder, name);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                ProfessionLogger.logError(e);
            }
        }
        return f;
    }

    /**
     * @return the {@link User} folder directory
     */
    public static File getPlayerFolder() {
        return getFolder(PLAYER_FOLDER);
    }

    /**
     * @return the cache folder directory
     */
    public static File getCacheFolder() {
        return getFolder(CACHE_FOLDER);
    }

    /**
     * @return the folder with additional data that does not belong to any other file (user, itemtype, ...)
     */
    public static File getAdditionalDataFolder() {
        return getFolder(DATA_FOLDER);
    }

    /**
     * @return the backup folder directory
     */
    public static File getBackupFolder() {
        return getFolder(BACKUP_FOLDER);
    }

    /**
     * @return the logs folder directory
     */
    public static File getLogsFolder() {
        return getFolder(LOGS_FOLDER);
    }

    /**
     * @return the filtered logs folder directory
     */
    public static File getFilteredLogsFolder() {
        return getFolder(FILTERED_LOGS_FOLDER);
    }

    /**
     * @return the {@link ItemType} folder directory
     */
    public static File getItemFolder() {
        return getFolder(ITEM_FOLDER);
    }

    /**
     * @return the {@link Profession} folder directory
     */
    public static File getProfessionFolder() {
        return getFolder(PROFESSION_FOLDER);
    }

    public static File getProfessionFile(Profession prof) {
        return getProfessionFile(prof.getClass());
    }

    public static File getProfessionFile(Class<? extends Profession> clazz) {
        String fileName = clazz.getSimpleName().toLowerCase().replace("profession", "");
        final File dir = IOManager.getProfessionFolder();
        final String fName = fileName.concat(Utils.YML_EXTENSION);
        return getFile(dir, fName);
    }

    /**
     * @return the folder with trainer GUI
     */
    public static File getTrainerFolder() {
        return getFolder(TRAINER_FOLDER);
    }

    /**
     * @return the lang folder directory
     */
    public static File getLangFolder() {
        return getFolder(LANG_FOLDER);
    }

    public static File getLogFile() {
        return LOG_FILE;
    }

    public static void setupFiles() {
        Professions plugin = Professions.getInstance();

        // data folder
        if (!plugin.getDataFolder().isDirectory()) {
            plugin.getDataFolder().mkdir();
        }

        // log
        if (!LOG_FILE.exists()) {
            try {
                LOG_FILE.createNewFile();
                fos = new PrintStream(LOG_FILE);
            } catch (IOException e) {
                ProfessionLogger.logError(e);
            }
        }

        // patterns
        plugin.saveResource(LANG_PATH.concat("patterns.properties"), true);
        plugin.saveResource(LANG_PATH.concat("placeholders.properties"), true);

        // lang
        plugin.saveResource(LANG_PATH.concat("lang_cs.yml"), false);
        plugin.saveResource(LANG_PATH.concat("lang_cs_D.yml"), false);
        plugin.saveResource(LANG_PATH.concat("lang_en.yml"), false);

        // config
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
    }

    /**
     * Saves only user files (currently)
     *
     * @throws IOException ex
     */
    public static void saveFiles() throws IOException {
        User.saveUsers();
    }

    /**
     * Flushes the log file
     */
    public static void saveLogFile() {
        if (fos != null) {
            fos.flush();
        }
    }

    public static void closeLogFile() {
        if (fos != null) {
            saveLogFile();
            fos.close();
        }
    }

    /**
     * Forces the backup of plugin.
     *
     * @return the result of backup
     */
    public static BackupTask.Result backup() {
        BackupTask task = new BackupTask();
        task.startTask();
        FIRST_BACKUP = false;
        return task.getResult();
    }

    /**
     * Backs up a plugin if it has not been backed up before
     *
     * @return the result of backup or {@code null}, if backed up before
     */
    public static BackupTask.Result backupFirst() {
        return FIRST_BACKUP ? backup() : null;
    }
}
