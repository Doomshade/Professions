package git.doomshade.professions.utils;

import git.doomshade.professions.Professions;
import git.doomshade.professions.enums.SkillupColor;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.ItemType.Key;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ItemUtils {
    private static final List<Key> KEYS = Arrays.asList(Key.EXP, Key.LEVEL_REQ, Key.NAME, Key.ITEM_REQUIREMENTS, Key.LEVEL_REQ_COLOR);
    private static ItemUtils instance;

    static {
        instance = new ItemUtils();
    }

    public static ItemUtils getInstance() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    public static <A extends ItemType<?>> List<String> getItemTypeLore(A itemType) {
        Map<String, Object> map = getItemTypeMap(itemType.getClass(), itemType.getId());
        List<String> desc = (List<String>) map.get(Key.DESCRIPTION.toString());
        return getDescription(itemType, desc);
    }

    public static <A extends ItemType<?>> List<String> getDescription(A itemType, List<String> description) {
        return getDescription(itemType, description, null);
    }

    public static <A extends ItemType<?>> List<String> getDescription(A itemType, List<String> description, UserProfessionData upd) {
        Map<String, Object> map = getItemTypeMap(itemType.getClass(), itemType.getId());
        List<String> desc = new ArrayList<>(description);
        for (Key key : Key.values()) {
            if (key == Key.ITEM_REQUIREMENTS) {
                continue;
            }
            String regex = "\\{" + key + "\\}";
            Object mapObject = map.get(key.toString());

            String replacement = regex;
            if (key == Key.LEVEL_REQ_COLOR) {
                if (upd != null)
                    replacement = String.valueOf(SkillupColor.getSkillupColor(itemType.getLevelReq(), upd.getLevel()).getColor());
            } else {
                replacement = String.valueOf(mapObject);
            }
            for (int i = 0; i < desc.size(); i++) {
                String s = desc.get(i);
                if (s.isEmpty()) {
                    continue;
                }
                desc.set(i, ChatColor.translateAlternateColorCodes('&', (s.replaceAll(regex, replacement))));
            }
        }
        return desc;
    }

    public static <A extends ItemType<?>> Map<String, Object> getItemTypeMap(Class<A> clazz, int id) {
        File file = getFile(clazz);
        if (!file.exists()) {
            throw new IllegalStateException("Object not yet serialized!");
        }
        String itemId = String.valueOf(id);
        FileConfiguration loader = YamlConfiguration.loadConfiguration(file);
        if (!loader.isConfigurationSection(ItemType.KEY)) {
            throw new IllegalArgumentException(
                    clazz.getSimpleName() + " with id " + id + " not found in file! (" + ItemType.KEY + "." + id + ")");
        }
        ConfigurationSection itemsSection = loader.getConfigurationSection(ItemType.KEY);
        return ((MemorySection) itemsSection.get(itemId)).getValues(true);
    }

    public static <A extends ItemType<?>> File getFile(Class<?> clazz) {
        return new File(Professions.getInstance().getItemsFolder(), clazz.getSimpleName().toLowerCase() + ".yml");
    }

    public ItemStackBuilder itemStackBuilder(Material mat) {
        return new ItemStackBuilder(mat);
    }

    public class ItemStackBuilder {
        private Material mat;
        private int amount;
        private short damage;
        private ItemMeta meta;

        public ItemStackBuilder(Material mat) {
            this.mat = mat;
            this.amount = 1;
            this.damage = 0;
            this.meta = Bukkit.getItemFactory().getItemMeta(mat);
        }

        public ItemStackBuilder withLore(List<String> lore) {
            meta.setLore(lore);
            return this;
        }

        public ItemStackBuilder withDisplayName(String displayName) {
            meta.setDisplayName(displayName.isEmpty() ? "" : ChatColor.translateAlternateColorCodes('&', displayName));
            return this;
        }

        public ItemStackBuilder setAmount(int amount) {
            this.amount = amount;
            return this;
        }

        public ItemStackBuilder setDamage(short damage) {
            this.damage = damage;
            return this;
        }

        public ItemStack build() {
            ItemStack item = new ItemStack(mat, amount, damage);
            item.setItemMeta(meta);
            return item;
        }
    }


}
