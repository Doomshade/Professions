package git.doomshade.professions.profession.types.crafting.jewelcrafting;

import com.google.common.collect.Sets;
import git.doomshade.professions.Professions;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.utils.FileEnum;
import git.doomshade.professions.utils.GetSet;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Utils;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static git.doomshade.professions.profession.types.crafting.jewelcrafting.Gem.GemEnum.*;
import static git.doomshade.professions.profession.types.crafting.jewelcrafting.GemUtils.ACTIVE_GEM_NBT_TAG;
import static git.doomshade.professions.profession.types.crafting.jewelcrafting.GemUtils.IDS;


public class Gem implements ConfigurationSerializable {

    public static final HashMap<String, Gem> GEMS = new HashMap<>();
    // TODO settings
    public static final String EMPTY_GEM = "(Místo pro klenot)";
    private static final String EXAMPLE_GEM_ID = "example-gem-id";
    // stay here cuz of order!
    public static final Gem EXAMPLE_GEM = new Gem(EXAMPLE_GEM_ID, ItemUtils.EXAMPLE_RESULT, GemUtils.ADD_ATTRIBUTE_EFFECT, ChatColor.GREEN + "Test display name", Arrays.asList("poskozeni:50", "vitalita:30"), GemEquipmentSlot.ARMOR);
    private static final HashMap<UUID, Set<Gem>> ACTIVE_GEMS = new HashMap<>();
    private static final String GEM_NBT_TAG = "gemItemType";
    private static final HashSet<String> LOGGED_ERROR_GEMS = new HashSet<>();
    private final List<String> context;
    private final ItemStack gem;
    private final GemEffect gemEffect;
    private final String id;
    private final GemEquipmentSlot equipmentSlot;
    private String displayName;

    private Gem(String id, ItemStack gem, GemEffect gemEffect, String displayName, List<String> context, GemEquipmentSlot equipmentSlot) throws IllegalArgumentException {

        if (GEMS.containsKey(id)) {
            throw new IllegalArgumentException("Cannot register another gem with the same id! (" + id + ")");
        }

        this.equipmentSlot = equipmentSlot;
        if (equipmentSlot == null) {
            throw new IllegalArgumentException("Invalid equipment slot. Valid equipment slots: " + Arrays.toString(GemEquipmentSlot.values()));
        }
        this.id = id;
        this.context = context;
        this.gem = addNbtTag(gem);
        this.gemEffect = gemEffect;
        this.displayName = displayName;

        if (!id.equals(EXAMPLE_GEM_ID)) {
            GEMS.put(id, this);
        }
    }

    public static Optional<Gem> getGem(String id) {
        return Optional.ofNullable(GEMS.get(id));
    }

    /**
     * Checks for the NBT Tag of the ItemStack. If found, returns the gem
     *
     * @param item the item to check for
     * @return the gem if it actually is one
     */
    public static Optional<Gem> getGem(ItemStack item) {
        return getGem(item, GEM_NBT_TAG);
    }

    public static Optional<Gem> getGemInItem(ItemStack item) {
        return getGem(item, ACTIVE_GEM_NBT_TAG);
    }

    private static Optional<Gem> getGem(ItemStack item, String tag) {
        final Optional<Gem> empty = Optional.empty();

        if (item == null) {
            return empty;
        }
        net.minecraft.server.v1_9_R1.ItemStack itemStack = CraftItemStack.asNMSCopy(item);
        if (!itemStack.hasTag()) {
            return empty;
        }
        NBTTagCompound nbtTag = itemStack.getTag();

        if (!nbtTag.hasKey(tag)) {
            return empty;
        }

        final String id = nbtTag.getString(tag);
        final Gem value = GEMS.get(id);
        if (value == null) {
            if (LOGGED_ERROR_GEMS.add(id)) {
                final String s = "Found a gem but the gem is not registered! (" + id + ")";
                Professions.log(s, Level.WARNING);
                Professions.log(s, Level.CONFIG);
            }
            return empty;
        }

        return Optional.of(value);
    }

    public static boolean hasGem(ItemStack item) {
        if (item == null) {
            return false;
        }
        net.minecraft.server.v1_9_R1.ItemStack itemStack = CraftItemStack.asNMSCopy(item);
        return itemStack != null && itemStack.hasTag() && itemStack.getTag().hasKey(ACTIVE_GEM_NBT_TAG);
    }

    public static void update(Player player) {

        Set<Gem> gems = new HashSet<>();

        // TODO unapply gem, který dřív byl aktivní!
        final PlayerInventory inventory = player.getInventory();
        for (ItemStack item : inventory.getArmorContents()) {
            if (item == null) {
                continue;
            }
            if (hasGem(item)) {
                final Optional<Gem> opt = getGemInItem(item);
                opt.ifPresent(gems::add);
            }
        }
        for (ItemStack item : Arrays.asList(inventory.getItemInMainHand(), inventory.getItemInOffHand(), inventory.getItem(9), inventory.getItem(10), inventory.getItem(11))) {
            if (item == null) {
                continue;
            }
            if (hasGem(item)) {
                final Optional<Gem> opt = getGemInItem(item);
                opt.ifPresent(gems::add);
            }
        }

        Set<Gem> activeGems = ACTIVE_GEMS.getOrDefault(player.getUniqueId(), new HashSet<>());
        final Sets.SetView<Gem> difference1 = Sets.difference(gems, activeGems);
        difference1.forEach(x -> x.apply(player));
        final Sets.SetView<Gem> difference2 = Sets.difference(activeGems, gems);
        difference2.forEach(x -> x.unApply(player));
    }

    public static void unApplyAll(Player player) {
        ACTIVE_GEMS.getOrDefault(player.getUniqueId(), new HashSet<>()).forEach(x -> x.unApply(player));
    }

    @SuppressWarnings("all")
    public static Gem deserialize(Map<String, Object> map) throws ProfessionObjectInitializationException {
        final Set<FileEnum> missingKeysEnum = Utils.getMissingKeysEnum(map, values());

        if (!missingKeysEnum.isEmpty()) {
            throw new ProfessionObjectInitializationException(GemItemType.class, missingKeysEnum.stream().map(Object::toString).collect(Collectors.toSet()));
        }
        GemEquipmentSlot equipmentSlot;
        try {
            equipmentSlot = GemEquipmentSlot.valueOf((String) map.get(EQUIPMENT_SLOT.s));
        } catch (IllegalArgumentException e) {
            Professions.log("Available equipment slots: " + Arrays.stream(GemEquipmentSlot.values()).map(Enum::name).collect(Collectors.joining(" ")));
            throw new ProfessionObjectInitializationException(GemItemType.class, Collections.singletonList(EQUIPMENT_SLOT.s), ProfessionObjectInitializationException.ExceptionReason.KEY_ERROR);
        }
        String id = (String) map.get(ID.s);
        String gemEffect = (String) map.get(GEM_EFFECT.s);
        MemorySection itemSection = (MemorySection) map.get(GEM.s);
        ItemStack item = ItemUtils.deserialize(itemSection.getValues(false));
        String displayName = (String) map.get(DISPLAY_NAME.s);
        displayName = displayName == null || displayName.isEmpty() ? displayName : ChatColor.translateAlternateColorCodes('&', displayName);

        List<String> context = (List<String>) map.get(GEM_EFFECT_CONTEXT.s);
        return new Gem(id, item, IDS.get(gemEffect), displayName, context, equipmentSlot);
    }

    public ItemStack getGem() {
        return gem;
    }

    public List<String> getContext() {
        return context;
    }

    private ItemStack addNbtTag(ItemStack gem) {
        net.minecraft.server.v1_9_R1.ItemStack itemStack = CraftItemStack.asNMSCopy(gem);
        NBTTagCompound nbtTag = itemStack.hasTag() ? itemStack.getTag() : new NBTTagCompound();
        nbtTag.setString(GEM_NBT_TAG, id);
        itemStack.setTag(nbtTag);
        return CraftItemStack.asBukkitCopy(itemStack);
    }

    public void apply(Player player) {
        final UUID uniqueId = player.getUniqueId();
        Set<Gem> gems = ACTIVE_GEMS.getOrDefault(uniqueId, new HashSet<>());
        if (gems.add(this)) {
            gemEffect.apply(this, player, false);
            ACTIVE_GEMS.put(uniqueId, gems);
        }
    }

    public void unApply(Player player) {
        final UUID uniqueId = player.getUniqueId();
        Set<Gem> gems = ACTIVE_GEMS.getOrDefault(uniqueId, new HashSet<>());
        if (gems.remove(this)) {
            gemEffect.apply(this, player, true);
            ACTIVE_GEMS.put(uniqueId, gems);
        }
    }

    public boolean isActive(Player player) {
        return ACTIVE_GEMS.getOrDefault(player.getUniqueId(), new HashSet<>()).contains(this);
    }

    public boolean insert(GetSet<ItemStack> getSet, boolean ignoreMisto) {

        ItemStack item = getSet.t;

        // firstly check whether or not the item has lore
        if (item == null || (!ignoreMisto && !item.hasItemMeta())) {
            return false;
        }

        final ItemMeta itemMeta = item.getItemMeta();

        if (!ignoreMisto && !itemMeta.hasLore()) {
            return false;
        }

        if (!ignoreMisto) {
            AtomicInteger i = new AtomicInteger(-1);
            final List<String> lore = itemMeta.getLore();

            // look for the gem slot, return false if not found
            try {
                Utils.findInIterable(lore, x -> {
                    i.getAndIncrement();
                    return !x.isEmpty() && ChatColor.stripColor(x).equalsIgnoreCase(EMPTY_GEM);
                });
            } catch (Utils.SearchNotFoundException e) {
                return false;
            }

            lore.set(i.get(), displayName);
        }

        // gem slot found, time to add NBT tag and replace the gem line
        net.minecraft.server.v1_9_R1.ItemStack itemStack = CraftItemStack.asNMSCopy(item);

        NBTTagCompound nbtTag = itemStack.hasTag() ? itemStack.getTag() : new NBTTagCompound();
        String theTag = nbtTag.hasKey(ACTIVE_GEM_NBT_TAG) ? nbtTag.getString(ACTIVE_GEM_NBT_TAG).concat(":") : "";


        nbtTag.setString(ACTIVE_GEM_NBT_TAG, theTag.concat(id));
        itemStack.setTag(nbtTag);

        getSet.t = CraftItemStack.asBukkitCopy(itemStack);

        return true;
    }

    @Override
    public Map<String, Object> serialize() {
        return new HashMap<String, Object>() {
            {
                put(ID.s, id);
                put(GEM_EFFECT.s, gemEffect.toString());
                put(GEM_EFFECT_CONTEXT.s, null);
                put(GEM.s, ItemUtils.serialize(gem));
                put(DISPLAY_NAME.s, displayName);
                put(EQUIPMENT_SLOT.s, equipmentSlot.name());
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gem gem1 = (Gem) o;
        return gem.equals(gem1.gem) &&
                gemEffect.equals(gem1.gemEffect) &&
                id.equals(gem1.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gem, gemEffect, id);
    }

    @Override
    public String toString() {
        return "Gem{" +
                "gemEffect=" + gemEffect +
                ", id='" + id + '\'' +
                ", equipmentSlot=" + equipmentSlot +
                ", displayName='" + displayName + '\'' +
                '}';
    }

    enum GemEnum implements FileEnum {
        ID("id"),
        GEM_EFFECT("gem-effect"),
        GEM_EFFECT_CONTEXT("gem-effect-context"),
        GEM("item"),
        DISPLAY_NAME("gem-name"),
        EQUIPMENT_SLOT("equipment-slot");

        private final String s;

        GemEnum(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return s;
        }

        @Override
        public EnumMap<?, Object> getDefaultValues() {
            return new EnumMap<GemEnum, Object>(GemEnum.class) {
                {
                    put(ID, "some-id");
                    put(GEM_EFFECT, "add");
                    put(GEM_EFFECT_CONTEXT, new ArrayList<String>());
                    put(GEM, ItemUtils.EXAMPLE_RESULT.serialize());
                    put(DISPLAY_NAME, "&cNejhorší gem I");
                    put(EQUIPMENT_SLOT, GemEquipmentSlot.MAINHAND.name());
                }
            };
        }
    }

    public enum GemEquipmentSlot {
        MAINHAND, OFFHAND, ARMOR
    }
}
