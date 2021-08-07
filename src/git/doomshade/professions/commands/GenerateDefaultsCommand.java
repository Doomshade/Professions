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

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.api.item.ItemTypeHolder;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.FileEnum;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Permissions;
import git.doomshade.professions.utils.Strings;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

/**
 * This command will generate all {@link ItemType} defaults
 *
 * @author Doomshade
 * @version 1.0
 */
@SuppressWarnings("ALL")
public class GenerateDefaultsCommand extends AbstractCommand {
    private static final String OBJECT = String.valueOf(Strings.ItemTypeEnum.OBJECT.s);

    public GenerateDefaultsCommand() {
        setCommand("generate-defaults");
        setDescription("Generates the defaults of item types (does not override existing data).");
        setRequiresPlayer(false);
        addPermission(Permissions.ADMIN);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        for (ItemTypeHolder<?, ?> itemTypeHolder : Professions.getProfMan().getItemTypeHolders()) {
            final ItemType<?> itemType = itemTypeHolder.getExampleItemType();

            final Map<String, Object> map = ItemUtils.getItemTypeMap(itemType.getClass(), 0);

            // get the missing keys
            final Map<FileEnum, Object> missingKeys = Strings.getMissingKeysItemType(map, itemType);

            final File file = ItemUtils.getItemTypeFile(itemType.getClass());
            final FileConfiguration loader = YamlConfiguration.loadConfiguration(file);

            // "items:"
            final ConfigurationSection itemsSection = loader.getConfigurationSection(ItemType.KEY_ITEMS);

            for (String s : Objects.requireNonNull(itemsSection).getKeys(false)) {

                // "items: '1':"
                final ConfigurationSection itemSection = itemsSection.getConfigurationSection(s);

                // "items: '1': exp"
                for (Map.Entry<FileEnum, Object> entry : missingKeys.entrySet()) {
                    if (!Objects.requireNonNull(itemSection).isSet(entry.getKey().toString())) {
                        ProfessionLogger.log(
                                String.format("Generated %s in file %s section %s", entry.getKey(), file.getName(),
                                        itemSection.getCurrentPath()), Level.INFO);
                    }
                    //
                    itemSection.addDefault(entry.getKey().toString(), entry.getValue());

                }


                // "items: '1': object:"
                final ConfigurationSection objectSection =
                        Objects.requireNonNull(itemSection).isConfigurationSection(OBJECT) ?
                                itemSection.getConfigurationSection(OBJECT) :
                                itemSection.createSection(OBJECT);

                final Map<String, Object> serializedObject = itemType.serialize();
                if (serializedObject == null) {
                    ProfessionLogger.log(
                            "Object serialization not yet implemented for " + itemType.getClass().getSimpleName() + "!",
                            Level.SEVERE);
                } else {
                    for (Map.Entry<String, Object> entry : serializedObject.entrySet()) {
                        if (!Objects.requireNonNull(objectSection).isSet(entry.getKey())) {
                            ProfessionLogger.log(
                                    String.format("Generated %s in file %s section %s", entry.getKey(), file.getName(),
                                            objectSection.getCurrentPath()), Level.INFO);
                        }
                        objectSection.addDefault(entry.getKey(), entry.getValue());
                    }
                }
            }

            // now we copy the defaults and save to the file
            loader.options().copyDefaults(true);
            try {
                loader.save(file);
            } catch (IOException e) {
                ProfessionLogger.logError(e);
                return;
            }

        }

        sender.sendMessage("Defaults generated successfully");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "generateDefaults";
    }
}
