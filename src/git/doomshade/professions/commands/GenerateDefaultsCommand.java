package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.item.ICraftable;
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.api.item.ItemTypeHolder;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * This command will generate all {@link ItemType} defaults
 *
 * @author Doomshade
 * @version 1.0
 */
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
            ItemType<?> itemType = itemTypeHolder.getItemType();


            Map<String, Object> map = ItemUtils.getItemTypeMap(itemType.getClass(), 0);

            // get the missing keys
            Set<FileEnum> missingKeys = Utils.getMissingKeysEnum(map, Strings.ItemTypeEnum.values());
            if (itemType instanceof ICraftable) {
                missingKeys.addAll(Utils.getMissingKeysEnum(map, Strings.ICraftableEnum.values()));
            }

            // do not uncomment, we want to check for items.object section too!
            /*if (missingKeys.isEmpty()) {
                continue;
            }*/

            File file = ItemUtils.getItemTypeFile(itemType.getClass());
            FileConfiguration loader = YamlConfiguration.loadConfiguration(file);

            // "items:"
            ConfigurationSection itemsSection = loader.getConfigurationSection(ItemType.KEY);

            for (String s : itemsSection.getKeys(false)) {

                // "items: '1':"
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(s);
                for (FileEnum en : missingKeys) {

                    // "items: '1': exp"
                    for (Map.Entry<?, Object> entry : en.getDefaultValues().entrySet()) {
                        if (!itemSection.isSet(entry.getKey().toString())) {
                            ProfessionLogger.log(String.format("Generated %s in file %s section %s", entry.getKey(), file.getName(), itemSection.getCurrentPath()), Level.INFO);
                        }
                        //
                        itemSection.addDefault(entry.getKey().toString(), entry.getValue());

                    }
                }


                // "items: '1': object:"
                ConfigurationSection objectSection = itemSection.isConfigurationSection(OBJECT) ? itemSection.getConfigurationSection(OBJECT) : itemSection.createSection(OBJECT);

                final Map<String, Object> serializedObject = itemType.getSerializedObject();
                if (serializedObject == null) {
                    ProfessionLogger.log("Object serialization not yet implemented for " + itemType.getClass().getSimpleName() + "!", Level.WARNING);
                } else {
                    for (Map.Entry<String, Object> entry : serializedObject.entrySet()) {
                        if (!objectSection.isSet(entry.getKey())) {
                            ProfessionLogger.log(String.format("Generated %s in file %s section %s", entry.getKey(), file.getName(), objectSection.getCurrentPath()), Level.INFO);
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
