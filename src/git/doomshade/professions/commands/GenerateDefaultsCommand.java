package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.utils.FileEnum;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Strings;
import git.doomshade.professions.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This command will generate all but the object's {@link git.doomshade.professions.profession.types.ItemType} defaults
 *
 * @author Doomshade
 */
public class GenerateDefaultsCommand extends AbstractCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        for (ItemTypeHolder<?> itemTypeHolder : Professions.getProfessionManager().getItemTypeHolders()) {
            ItemType<?> itemType = itemTypeHolder.getItemTypeItem();

            Map<String, Object> map = ItemUtils.getItemTypeMap(itemType.getClass(), itemType.getId());

            Set<FileEnum> missingKeys = Utils.getMissingKeysEnum(map, Strings.ItemTypeEnum.values());

            File file = itemTypeHolder.getFile();
            FileConfiguration loader = YamlConfiguration.loadConfiguration(file);

            ConfigurationSection itemsSection = loader.getConfigurationSection(ItemType.KEY);
            Iterator<String> it = itemsSection.getKeys(false).iterator();
            while (it.hasNext()) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(it.next());
                for (FileEnum en : missingKeys) {
                    for (Map.Entry<Enum, Object> entry : en.getDefaultValues().entrySet()) {
                        itemSection.addDefault(entry.getKey().toString(), entry.getValue());
                    }
                }
            }
            loader.options().copyDefaults(true);
            try {
                loader.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "generateDefaults";
    }
}
