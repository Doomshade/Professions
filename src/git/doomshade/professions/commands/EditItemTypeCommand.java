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

package git.doomshade.professions.commands;

import git.doomshade.professions.api.item.ext.ItemType;
import git.doomshade.professions.io.IOManager;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Permissions;
import git.doomshade.professions.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Command for editing certain parts of an item type in a file
 *
 * @author Doomshade
 * @version 1.0
 * @see ItemType
 * @since 1.0
 */
@SuppressWarnings("ALL")
public class EditItemTypeCommand extends AbstractCommand {

    static final Set<String> FILES = new HashSet<>();
    static final String ARG_FILE = "\"item type file\"";
    static final int[] I = {1};
    private static final Map<File, LinkedList<FileConfiguration>> UNDO = new TreeMap<>();

    public EditItemTypeCommand() {
        setArg(true, ARG_FILE,
                "path (ex. items.0.crafting-time)",
                "value\n" +
                        "for booleans: true or false\n" +
                        "for list: end each line with ';', ex. firstLine;secondLine;thirdLine\n" +
                        "for item: hand\n" +
                        "for item material: material\n" +
                        "for location: location");
        setCommand("edit");
        setDescription("Edits something in item type file");
        setRequiresPlayer(false);
        FILES.clear();
        FILES.addAll(Arrays.stream(Objects.requireNonNull(IOManager.getItemFolder().listFiles()))
                .map(x -> "\"".concat(x.getName()).concat("\""))
                .collect(Collectors.toSet()));
        addPermission(Permissions.ADMIN);
    }

    @Nullable
    static FileConfiguration getAndRemoveLastUndo(File file) {
        return getAndRemoveLastUndoWithMessage(file, null, "");
    }

    @Nullable
    private static FileConfiguration getAndRemoveLastUndoWithMessage(File file, CommandSender sender, String message) {
        LinkedList<FileConfiguration> pastUndoes = UNDO.getOrDefault(file, new LinkedList<>());
        FileConfiguration lastUndo = pastUndoes.pollLast();
        UNDO.put(file, pastUndoes);
        if (sender != null && message != null) {
            sender.sendMessage(message);
        }
        return lastUndo;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        File file = getFile(args);
        String fileName = file.getName();
        if (!file.exists()) {
            sender.sendMessage(fileName + " file does not exist! Make sure to put \" or ' around the file name.");
            return;
        }
        FileConfiguration loader = YamlConfiguration.loadConfiguration(file);
        final String path = args[I[0]++];
        if (!loader.getKeys(true).contains(path)) {
            sender.sendMessage(path + " does not exist in " + fileName + "! Creating new path.");
        }
        String[] values = String.join(" ", Arrays.asList(args).subList(I[0], args.length)).split(";");
        if (values.length == 0) {
            sender.sendMessage("Cannot set " + path +
                    " to empty value like that. If you really want to set it to empty, set the value to NULL");
            return;
        }
        String setValue = "";

        FileConfiguration loaderCopy = new YamlConfiguration();
        try {
            loaderCopy.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            sender.sendMessage("Error loading " + file.getName() + ". Check console for error output.");
            ProfessionLogger.logError(e);
            return;
        }
        saveUndo(file, loaderCopy);

        if (values.length > 1) {
            List<String> list = Arrays.asList(values);
            final String delimiter = "\n- ";
            setValue = delimiter.concat(String.join(delimiter, list));
            loader.set(path, list);
        } else {
            String value = values[0];
            if (value.equalsIgnoreCase("null")) {
                loader.set(path, "");
            } else {
                setValue = value;

                // check whether or not the value is a number
                try {
                    int number = Integer.parseInt(value);
                    loader.set(path, number);
                } catch (NumberFormatException e) {
                    try {
                        double number = Double.parseDouble(value);
                        loader.set(path, number);
                    } catch (NumberFormatException ex) {
                        // not a number nor a list, must be a string or a boolean

                        String valueCopy = value.toLowerCase();
                        switch (valueCopy) {
                            case "true":
                                loader.set(path, true);
                                break;
                            case "false":
                                loader.set(path, false);
                                break;
                            case "hand":
                                if (!(sender instanceof Player)) {
                                    getAndRemoveLastUndoWithMessage(file, sender,
                                            "You must be a player to set an item to a path!");
                                    return;
                                }
                                ItemStack hand = ((Player) sender).getInventory().getItemInMainHand();
                                String materialName;
                                materialName = hand.getType().name();
                                setValue = "item ";
                                if (hand.hasItemMeta() && Objects.requireNonNull(hand.getItemMeta()).hasDisplayName()) {
                                    setValue += hand.getItemMeta().getDisplayName();
                                } else {
                                    setValue += materialName;
                                }
                                loader.set(path, ItemUtils.serialize(hand));
                                break;
                            case "material":
                                if (!(sender instanceof Player)) {
                                    getAndRemoveLastUndoWithMessage(file, sender,
                                            "You must be a player to set a material to a path!");
                                    return;
                                }
                                hand = ((Player) sender).getInventory().getItemInMainHand();
                                materialName = hand.getType().name();
                                setValue = "material " + materialName;
                                loader.set(path, materialName);
                                break;
                            case "location":
                                if (!(sender instanceof Player)) {
                                    getAndRemoveLastUndoWithMessage(file, sender,
                                            "You must be a player to set a location to a path!");
                                    return;
                                }
                                setValue = "your current location";
                                loader.set(path, ((Player) sender).getLocation().getBlock().getLocation().serialize());
                                break;
                            default:
                                loader.set(path, value);
                                break;
                        }
                    }
                }

            }
        }
        try {
            loader.save(file);
            sender.sendMessage(String.format("Successfully set %s to %s", path, setValue));

            final CommandHandler handler = CommandHandler.getInstance(CommandHandler.class);
            try {
                if (handler != null) {
                    final UndoEditCommand acmd = handler.getCommand(UndoEditCommand.class);
                    String msg = handler.infoMessage(acmd)
                            .replaceAll("<" + acmd.getArgs().get(true).get(0) + ">", "\"" + fileName + "\"");
                    sender.sendMessage("To undo, use command:\n " + msg);
                }
            } catch (Utils.SearchNotFoundException e) {
                ProfessionLogger.logError(e);
            }
        } catch (IOException e) {
            sender.sendMessage("Unexpected error occurred. Check console for further logs.");
            ProfessionLogger.logError(e);
        }
    }

    static File getFile(String[] args) {
        I[0] = 1;
        String fileName = Arrays.asList(args).subList(1, args.length).stream().map(new Function<String, String>() {
            private boolean found = false;

            @Override
            public String apply(String s) {
                if (found) {
                    return "";
                }
                I[0]++;
                if (s.endsWith("\"") || s.endsWith("'")) {
                    found = true;
                }
                return s;
            }
        }).collect(Collectors.joining(" ")).trim().replaceAll("\"", "");
        return new File(IOManager.getItemFolder(), fileName);
    }

    private static void saveUndo(File file, FileConfiguration loader) {
        LinkedList<FileConfiguration> pastUndoes = UNDO.getOrDefault(file, new LinkedList<>());
        pastUndoes.add(loader);
        UNDO.put(file, pastUndoes);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        File file = getFile(args);
        if (args.length == I[0]) {
            list.addAll(FILES.stream().filter(x -> x.startsWith(args[I[0] - 1])).collect(Collectors.toSet()));
        } else if (args.length > I[0] && file.exists()) {
            FileConfiguration loader = YamlConfiguration.loadConfiguration(file);
            list.addAll(loader.getKeys(true)
                    .stream()
                    .filter(x -> x.startsWith(args[I[0]].replaceAll("\"", "")))
                    .collect(Collectors.toSet()));
        }

        return list.isEmpty() ? null : list;
    }

    @Override
    public String getID() {
        return "edit";
    }
}
