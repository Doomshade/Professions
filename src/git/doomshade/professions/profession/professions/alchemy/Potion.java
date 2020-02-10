package git.doomshade.professions.profession.professions.alchemy;

import com.google.common.collect.ImmutableSet;
import git.doomshade.professions.Professions;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.utils.FileEnum;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Utils;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.NBTTagString;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

import static git.doomshade.professions.profession.professions.alchemy.Potion.PotionEnum.*;

public class Potion implements ConfigurationSerializable {

    private static final HashSet<CustomPotionEffect> CUSTOM_POTION_EFFECTS = new HashSet<>();

    private static final String TEST_POTION_ID = "test_potion";
    public static final Potion EXAMPLE_POTION = new Potion(
            Arrays.asList("vyhybani", "poskozeni"),
            5,
            TEST_POTION_ID,
            PotionType.FIRE_RESISTANCE,
            ItemUtils.itemStackBuilder(Material.POTION).withDisplayName("&aSome bottle").build());

    private static final String NBT_KEY = "profession_potion";

    // TODO clearing them currently in onDisable
    static final HashSet<Potion> POTIONS = new HashSet<>();
    private static final String SPLIT_CHAR = ":";

    private final ArrayList<String> potionEffects = new ArrayList<>();
    private final int duration;
    private final String potionId;
    private final PotionType potionType;
    private final ItemStack potion;

    private Potion(Collection<String> potionEffects, int duration, String potionId, PotionType potionType, ItemStack potion) {
        this.duration = duration;
        this.potionId = potionId;
        this.potionType = potionType;
        this.potionEffects.addAll(potionEffects);
        this.potion = potion;

        if (!potionId.equals(TEST_POTION_ID))
            POTIONS.add(this);
    }

    private static File getFile(Player player) {
        return new File(Professions.getInstance().getCacheFolder(), player.getUniqueId().toString().concat(".bin"));
    }

    @Deprecated
    public static void cache(Player player) {
        /*HashSet<PotionTask> potionTasks = ACTIVE_POTIONS.get(player.getUniqueId());
        if (potionTasks == null || potionTasks.isEmpty()) {
            return;
        }
        File file = getFile(player);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            for (PotionTask task : potionTasks) {
                oos.writeObject(task);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    @Deprecated
    public static void loadFromCache(Player player) {
        /*File file = getFile(player);
        if (!file.exists()) {
            return;
        }
        HashSet<PotionTask> potionTasks = new HashSet<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            while (ois.available() > 0) {
                potionTasks.add((PotionTask) ois.readObject());
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            ACTIVE_POTIONS.put(player.getUniqueId(), potionTasks);
        }*/
    }

    public static ImmutableSet<Potion> getPotions() {
        return ImmutableSet.copyOf(POTIONS);
    }

    /**
     * Retrieves an instance from memory of the potion if it exists.
     *
     * @param potion the item stack to check for
     * @return a potion if the item is valid, {@code null} otherwise
     */
    @Nullable
    public static Potion getItem(ItemStack potion) {
        if (potion == null || potion.getType() != Material.POTION) {
            return null;
        }
        net.minecraft.server.v1_9_R1.ItemStack nms = CraftItemStack.asNMSCopy(potion);
        if (!nms.hasTag()) {
            return null;
        }
        NBTTagCompound nbt = nms.getTag();
        if (!nbt.hasKey(NBT_KEY)) {
            return null;
        }
        String potionId = nbt.getString(NBT_KEY);
        try {
            return Utils.findInIterable(POTIONS, x -> x.potionId.equals(potionId));
        } catch (Utils.SearchNotFoundException e) {
            Professions.log(POTIONS);
            throw new IllegalStateException("Found " + potionId + " in-game, but its data is not loaded!");
        }
    }

    public static void registerCustomPotionEffect(CustomPotionEffect effect) {
        CUSTOM_POTION_EFFECTS.add(effect);
    }

    void addAttributes(final Player player, final boolean negate) {
        //final PotionMeta itemMeta = (PotionMeta) potion.getItemMeta();
        //itemMeta.getCustomEffects().forEach(x -> x.getType().createEffect(duration, 0).apply(player));


        potionEffects.forEach(x -> {
            for (CustomPotionEffect potionEffect : CUSTOM_POTION_EFFECTS) {
                potionEffect.apply(x, player, negate);
            }
        });

    }

    @SuppressWarnings("all")
    static Potion deserialize(Map<String, Object> map) throws ProfessionObjectInitializationException {
        Set<String> missingKeys = Utils.getMissingKeys(map, PotionEnum.values());
        if (!missingKeys.isEmpty()) {
            throw new ProfessionObjectInitializationException(PotionItemType.class, missingKeys);
        }

        ArrayList<String> potionEffects = (ArrayList<String>) map.get(POTION_EFFECTS.s);
        int duration = (int) map.get(POTION_DURATION.s);
        String potionId = (String) map.get(POTION_FLAG.s);
        PotionType potionType = PotionType.valueOf((String) map.get(POTION_TYPE.s));
        MemorySection mem = (MemorySection) map.get(POTION.s);
        ItemStack potion = ItemUtils.deserialize(mem.getValues(false));
        return new Potion(potionEffects, duration, potionId, potionType, potion);
    }

    ItemStack getItem() {
        return potion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Potion potion = (Potion) o;
        return potionId.equals(potion.potionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(potionId);
    }

    @Override
    public Map<String, Object> serialize() {
        return new HashMap<String, Object>() {
            {
                put(POTION_EFFECTS.s, potionEffects);
                put(POTION_DURATION.s, duration);
                put(POTION_FLAG.s, potionId);
                put(POTION_TYPE.s, potionType.name());
                put(POTION.s, ItemUtils.serialize(potion));
            }
        };
    }

    public Optional<ItemStack> getPotionItem(ItemStack item) {
        if (item.getType() != Material.POTION || !item.hasItemMeta()) {
            return Optional.empty();
        }
        ItemStack clone = item.clone();
        PotionMeta meta = (PotionMeta) clone.getItemMeta();
        meta.setBasePotionData(new PotionData(potionType));
        clone.setItemMeta(meta);
        net.minecraft.server.v1_9_R1.ItemStack nms = CraftItemStack.asNMSCopy(clone);
        NBTTagCompound nbt = nms.hasTag() ? nms.getTag() : new NBTTagCompound();
        nbt.set(NBT_KEY, new NBTTagString(potionId));
        nms.setTag(nbt);
        return Optional.of(CraftItemStack.asBukkitCopy(nms));
    }

    @Override
    public String toString() {
        return "Potion{" +
                "potionEffects=" + potionEffects +
                ", duration=" + duration +
                ", potionId='" + potionId + '\'' +
                ", potionType=" + potionType +
                ", potion=" + potion +
                '}';
    }

    public String getPotionId() {
        return potionId;
    }

    int getDuration() {
        return duration;
    }

    enum PotionEnum implements FileEnum {
        POTION_EFFECTS("potion-effects"), POTION_DURATION("duration"), POTION_FLAG("id"), POTION_TYPE("potion-type"), POTION("potion");

        private final String s;

        PotionEnum(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return s;
        }

        @Override
        public EnumMap<PotionEnum, Object> getDefaultValues() {
            return new EnumMap<PotionEnum, Object>(PotionEnum.class) {
                {
                    put(POTION_EFFECTS, Arrays.asList(
                            String.format("kriticky_utok%s10", SPLIT_CHAR),
                            String.format("vyhybani%s5", SPLIT_CHAR),
                            String.format("sance_na_kriticky_zasah%s80", SPLIT_CHAR),
                            String.format("poskozeni%s40", SPLIT_CHAR),
                            String.format("zivoty%s30", SPLIT_CHAR)));
                    put(POTION_DURATION, 80);
                    put(POTION_FLAG, "potions unique flag");
                    put(POTION_TYPE, PotionType.FIRE_RESISTANCE.name());
                    put(POTION, EXAMPLE_POTION.serialize());
                }
            };
        }
    }
}
