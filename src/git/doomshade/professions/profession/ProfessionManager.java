package git.doomshade.professions.profession;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import git.doomshade.professions.Professions;
import git.doomshade.professions.profession.professions.alchemy.AlchemyProfession;
import git.doomshade.professions.profession.professions.alchemy.Potion;
import git.doomshade.professions.profession.professions.alchemy.PotionItemType;
import git.doomshade.professions.profession.professions.blacksmithing.BSItemType;
import git.doomshade.professions.profession.professions.crafting.CustomRecipe;
import git.doomshade.professions.profession.professions.enchanting.EnchantManager;
import git.doomshade.professions.profession.professions.enchanting.EnchantedItemItemType;
import git.doomshade.professions.profession.professions.enchanting.enchants.RandomAttributeEnchant;
import git.doomshade.professions.profession.professions.herbalism.Herb;
import git.doomshade.professions.profession.professions.herbalism.HerbItemType;
import git.doomshade.professions.profession.professions.herbalism.HerbalismProfession;
import git.doomshade.professions.profession.professions.jewelcrafting.Gem;
import git.doomshade.professions.profession.professions.jewelcrafting.GemItemType;
import git.doomshade.professions.profession.professions.jewelcrafting.JewelcraftingProfession;
import git.doomshade.professions.profession.professions.mining.MiningProfession;
import git.doomshade.professions.profession.professions.mining.Ore;
import git.doomshade.professions.profession.professions.mining.OreItemType;
import git.doomshade.professions.profession.professions.skinning.Mob;
import git.doomshade.professions.profession.professions.skinning.PreyItemType;
import git.doomshade.professions.profession.professions.smelting.BarItemType;
import git.doomshade.professions.profession.professions.smelting.SmeltingProfession;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.utils.ISetup;
import git.doomshade.professions.utils.IrremovableSet;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftShapedRecipe;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginManager;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * A manager regarding registration and queries of a {@link Profession}, {@link ItemType}, and {@link ItemTypeHolder}.
 *
 * @author Doomshade
 * @version 1.0
 */
public final class ProfessionManager implements ISetup {
    private static final ProfessionManager instance = new ProfessionManager();

    /**
     * Using IrremovableSet here so that the registered professions never get deleted (they are intended not to!)
     */
    private final IrremovableSet<Class<? extends Profession>> REGISTERED_PROFESSIONS = new IrremovableSet<>();

    @SuppressWarnings("rawtypes")
    private final HashMap<ItemTypeHolder<?>, Class<? extends ItemType>> ITEMS = new HashMap<>();
    private final PluginManager pm = Bukkit.getPluginManager();
    private final Professions plugin = Professions.getInstance();
    private Map<String, Profession> PROFESSIONS_ID = new HashMap<>();
    private Map<String, Profession> PROFESSIONS_NAME = new HashMap<>();

    private ProfessionManager() {
    }

    /**
     * @return the instance of this class
     */
    public static ProfessionManager getInstance() {
        return instance;
    }

    /**
     * @return all registered {@link ItemTypeHolder}s
     */
    public ImmutableSet<ItemTypeHolder<?>> getItemTypeHolders() {
        return ImmutableSet.copyOf(ITEMS.keySet());
    }

    /**
     * @param clazz the {@link ItemTypeHolder} class to look for
     * @param <A>   the {@link ItemTypeHolder}'s {@link ItemType}
     * @return instance of {@link ItemTypeHolder}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <A extends ItemType<?>> ItemTypeHolder<A> getItemTypeHolder(Class<A> clazz) throws IllegalArgumentException {
        for (Entry<ItemTypeHolder<?>, Class<? extends ItemType>> entry : ITEMS.entrySet()) {
            if (entry.getValue().equals(clazz)) {
                return (ItemTypeHolder<A>) entry.getKey();
            }
        }
        throw new IllegalArgumentException(clazz + " is not a registered item type holder!");
    }

    /**
     * @return all registered {@link Profession}s
     */
    @SuppressWarnings("rawtypes")
    public ImmutableSet<Class<? extends Profession>> getRegisteredProfessions() {
        return ImmutableSet.copyOf(REGISTERED_PROFESSIONS);
    }

    /**
     * @return a sorted {@link ImmutableMap} of {@link Profession}s by {@link Profession#getID()}
     */
    public ImmutableMap<String, Profession> getProfessionsById() {
        return ImmutableMap.copyOf(PROFESSIONS_ID);
    }

    /**
     * @return a sorted {@link ImmutableMap} of {@link Profession}s by {@link Profession#getName()}
     */
    public ImmutableMap<String, Profession> getProfessionsByName() {
        return ImmutableMap.copyOf(PROFESSIONS_NAME);
    }

    public <T extends ItemTypeHolder<?>> void registerItemTypeHolderSupplier(Supplier<T> itemTypeHolder) throws IOException {
        registerItemTypeHolder(itemTypeHolder.get());
    }

    /**
     * @param itemTypeHolder the {@link ItemTypeHolder} to register
     * @param <T>            the {@link ItemTypeHolder}
     * @throws IOException ex
     */
    public <T extends ItemTypeHolder<?>> void registerItemTypeHolder(T itemTypeHolder) throws IOException {
        if (itemTypeHolder == null) return;
        itemTypeHolder.update();
        ITEMS.put(itemTypeHolder, itemTypeHolder.getItemType().getClass());
    }

    /**
     * @param name the {@link Profession#getName()} or {@link Profession#getID()} of {@link Profession}
     * @return the {@link Profession} if found, {@code null} otherwise
     */
    @Nullable
    public Profession getProfession(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        Profession prof = PROFESSIONS_ID.get(name.toLowerCase());
        if (prof == null) {
            prof = PROFESSIONS_NAME.get(ChatColor.stripColor(name.toLowerCase()));
        }

        if (prof == null) {
            try {
                prof = Utils.findInIterable(PROFESSIONS_ID.values(), x -> ChatColor.stripColor(x.getIcon().getItemMeta().getDisplayName()).equalsIgnoreCase(ChatColor.stripColor(name)));
            } catch (Utils.SearchNotFoundException e) {
                //e.printStackTrace();
            }
        }
        return prof;
    }

    /**
     * @param profession the {@link Profession} class
     * @return the {@link Profession} if found
     * @throws RuntimeException if the profession is not registered
     */
    public Profession getProfession(Class<? extends Profession> profession) throws RuntimeException {
        for (Profession prof : PROFESSIONS_ID.values()) {
            if (prof.getClass().getSimpleName().equals(profession.getSimpleName())) {
                return prof;
            }
        }
        throw new RuntimeException("Profession not registered!");
    }

    @Override
    public void setup() throws IOException {
        register();
        registerProfessions();
        //detectDuplicates();
    }

    /**
     * Detects config ID duplicates
     *
     * @deprecated as the config ID is automatically generated via file name + number id
     */
    @Deprecated
    private void detectDuplicates() {
        boolean loggedDuplicate = false;
        HashMap<String, ItemType<?>> map = new HashMap<>();
        LinkedList<ItemType<?>> duplicates = new LinkedList<>();
        for (ItemTypeHolder<?> holder : getItemTypeHolders()) {
            for (ItemType<?> itemType : holder) {
                final ItemType<?> put = map.putIfAbsent(itemType.getConfigName(), itemType);
                if (put != null) {
                    if (!loggedDuplicate) {
                        final String errorMsg = "Found duplicates of config names.";
                        Professions.log(errorMsg, Level.CONFIG);
                        Professions.log(errorMsg, Level.SEVERE);
                        loggedDuplicate = true;
                    }
                    duplicates.add(put);
                    duplicates.add(itemType);
                }
            }
        }

        StringBuilder duplicatesString = new StringBuilder();
        if (loggedDuplicate) {
            Professions.log("Duplicates:");
            Professions.log("Duplicates:", Level.CONFIG);

            for (ItemType<?> duplicate : duplicates) {
                duplicatesString.append("\n").append(duplicate.toCompactString());
            }
            System.out.println(duplicatesString);
            Professions.log(duplicatesString, Level.CONFIG);
        }
    }

    @Override
    public void cleanup() {
        PROFESSIONS_ID.clear();
        PROFESSIONS_NAME.clear();
        ITEMS.clear();
        Profession.INITED_PROFESSIONS.clear();
    }

    /**
     * Registers profession types and then item type holders in that order
     *
     * @throws IOException if an IO error occurs
     */
    private void register() throws IOException {
        registerItemTypeHolders();
    }

    /**
     * Huge method for {@link ItemTypeHolder} registrations
     *
     * @throws IOException if an IO error occurs
     * @see #registerItemTypeHolder(ItemTypeHolder)
     */
    private void registerItemTypeHolders() throws IOException {

        // MINING
        registerItemTypeHolderSupplier(() -> {
            OreItemType ore = ItemType.getExampleItemType(OreItemType.class, Ore.EXAMPLE_ORE);
            ore.setName(ChatColor.GRAY + "Obsidian");
            ore.addInventoryRequirement(ItemUtils.EXAMPLE_REQUIREMENT);
            return new ItemTypeHolder<>(ore);
        });

        // BLACKSMITHING
        registerItemTypeHolderSupplier(() -> {
            BSItemType bs = ItemType.getExampleItemType(BSItemType.class, ItemUtils.EXAMPLE_RESULT);
            bs.addCraftingRequirement(ItemUtils.EXAMPLE_REQUIREMENT);
            bs.addInventoryRequirement(ItemUtils.EXAMPLE_REQUIREMENT);
            return null;
            //return new ItemTypeHolder<>(bs);
        });

        // HUNTING (no longer used)
        registerItemTypeHolderSupplier(() -> {
            PreyItemType preyItemType = ItemType.getExampleItemType(PreyItemType.class, new Mob(EntityType.SKELETON));
            preyItemType.setName(ChatColor.YELLOW + "Kostlivec");
            return null;
            //return new ItemTypeHolder<>(preyItemType));
        });

        // HERBALISM
        registerItemTypeHolderSupplier(() -> {
            HerbItemType herb = ItemType.getExampleItemType(HerbItemType.class, Herb.EXAMPLE_HERB);
            herb.setName(ChatColor.DARK_AQUA + "Test gather item");
            return new ItemTypeHolder<>(herb);
        });

        // ENCHANTING
        registerItemTypeHolderSupplier(() -> {
            EnchantManager enchm = EnchantManager.getInstance();
            try {
                enchm.registerEnchant(new RandomAttributeEnchant(new ItemStack(Material.GLASS)));
            } catch (Exception e) {
                Professions.logError(e);
            }
            RandomAttributeEnchant ench = enchm.getEnchant(RandomAttributeEnchant.class);
            EnchantedItemItemType eit = ItemType.getExampleItemType(EnchantedItemItemType.class, ench);
            eit.addCraftingRequirement(ItemUtils.EXAMPLE_REQUIREMENT);
            eit.setName(ChatColor.RED + "Test random attribute enchantment");
            return new ItemTypeHolder<>(eit);
        });

        // CRAFTING
        registerItemTypeHolderSupplier(() -> {
            ShapedRecipe recipe = new ShapedRecipe(ItemUtils.EXAMPLE_RESULT).shape("abc", "def", "ghi").setIngredient('e', Material.DIAMOND);
            CustomRecipe cr = ItemType.getExampleItemType(CustomRecipe.class, CraftShapedRecipe.fromBukkitRecipe(recipe));
            cr.setName(ChatColor.DARK_GREEN + "Test recipe");
            final ItemTypeHolder<CustomRecipe> itemTypeHolder = new ItemTypeHolder<>(cr);
            itemTypeHolder.registerObject(cr);

            // clear these recipes if they exist, let the CustomRecipe class handle it!
            final Server server = Bukkit.getServer();

            // this is not a huge performance issue (even though it runs in O(n^2), the amount of recipes is negligible
            Iterator<Recipe> bukkitRecipes = server.recipeIterator();
            while (bukkitRecipes.hasNext()) {
                Recipe bukkitRecipe = bukkitRecipes.next();
                if (!(bukkitRecipe instanceof ShapedRecipe)) {
                    continue;
                }
                CraftShapedRecipe bukkitShapedRecipe = CraftShapedRecipe.fromBukkitRecipe((ShapedRecipe) bukkitRecipe);
                for (CustomRecipe customRecipe : itemTypeHolder.getRegisteredItemTypes()) {
                    if (customRecipe.equalsObject(bukkitShapedRecipe)) {
                        bukkitRecipes.remove();
                    }
                }
            }
            return null;
            //return itemTypeHolder;
        });

        // SMELTING
        registerItemTypeHolderSupplier(() -> {
            BarItemType barItemType = ItemType.getExampleItemType(BarItemType.class, ItemUtils.EXAMPLE_RESULT);
            barItemType.addCraftingRequirement(ItemUtils.EXAMPLE_REQUIREMENT);
            barItemType.setName(ChatColor.BLUE + "Test bar");
            return new ItemTypeHolder<>(barItemType);
        });

        // ALCHEMY
        registerItemTypeHolderSupplier(() -> {
            final PotionItemType potionItemType = ItemType.getExampleItemType(PotionItemType.class, Potion.EXAMPLE_POTION);
            return new ItemTypeHolder<>(potionItemType);
        });

        // JEWELCRAFTING
        registerItemTypeHolderSupplier(() -> {
            final GemItemType gemItemType = ItemType.getExampleItemType(GemItemType.class, Gem.EXAMPLE_GEM);
            return new ItemTypeHolder<>(gemItemType);
        });
    }

    /**
     * Registers current professions
     *
     * @see #registerProfession(Profession)
     */
    private void registerProfessions() {
        registerProfession(new MiningProfession(), false);
        registerProfession(new JewelcraftingProfession(), false);
        //registerProfession(new EnchantingProfession(), false);
        //registerProfession(new SkinningProfession(), false);
        registerProfession(new SmeltingProfession(), false);
        registerProfession(new HerbalismProfession(), false);
        registerProfession(new AlchemyProfession(), false);
        sortProfessions();
    }

    /**
     * Registers a profession
     *
     * @param prof       the profession to register
     * @param logMessage whether or not to announce the registration
     */
    private void registerProfession(Profession prof, boolean logMessage) {

        // required plugins of the profession
        Set<String> requiredPlugins = new HashSet<>();
        for (String plugin : prof.getRequiredPlugins()) {
            if (!Bukkit.getPluginManager().isPluginEnabled(plugin)) {
                requiredPlugins.add(plugin);
            }
        }
        if (!requiredPlugins.isEmpty()) {
            throw new IllegalStateException(String.format("Could not load %s as some plugins are missing!%s\nRequired plugins: %s\nPlugins missing: %s",
                    prof.getColoredName(), ChatColor.RESET, String.join(", ", prof.getRequiredPlugins()), String.join(", ", requiredPlugins)));
        }

        // the constructor was not overwritten correctly
        if (!Profession.INITED_PROFESSIONS.contains(prof.getClass())) {
            throw new IllegalStateException("If you want to override constructors, make sure to call super() !");
        }

        // make sure the profession is not already registered
        PROFESSIONS_ID.forEach((y, x) -> {
            if (x.getID().equalsIgnoreCase(prof.getID())) {
                throw new IllegalArgumentException(ChatColor.DARK_RED + "ERROR:" + ChatColor.RED + " A profession with name "
                        + prof.getName() + ChatColor.RESET + " already exists! (" + prof.getID() + ")");
            }
        });

        // finally the profession is registered
        PROFESSIONS_ID.put(prof.getID().toLowerCase(), prof);
        PROFESSIONS_NAME.put(ChatColor.stripColor(prof.getColoredName().toLowerCase()), prof);

        // if this is during onEnable, register the profession as a listener
        if (!REGISTERED_PROFESSIONS.contains(prof.getClass())) {
            pm.registerEvents(prof, plugin);
            REGISTERED_PROFESSIONS.add(prof.getClass());
        }

        /*Professions._log("Could not update " + prof.getID() + " profession. Reason:", Level.WARNING);
        e.printStackTrace();
        */

        // lastly call #onLoad
        prof.onLoad();
        if (logMessage)
            Professions.log("Registered " + prof.getColoredName() + ChatColor.RESET + " profession", Level.INFO);
    }

    /**
     * Registers a profession
     * <p>IMPORTANT! Make sure you only create a SINGLE instance of the profession, multiple instances are disallowed and WILL throw an exception!</p>
     *
     * @param prof the profession to register
     */
    public void registerProfession(Profession prof) {
        registerProfession(prof, true);
    }

    /**
     * Sorts the professions ID and professions name maps for better visuals in chat
     * <p>Note that this is not a necessary method, but adds something extra to it</p>
     */
    private void sortProfessions() {
        Map<String, Profession> MAP_COPY = new HashMap<>(PROFESSIONS_ID);
        PROFESSIONS_ID = sortByValue(MAP_COPY);
        MAP_COPY = new HashMap<>(PROFESSIONS_NAME);
        PROFESSIONS_NAME = sortByValue(MAP_COPY);
    }

    /**
     * Sorts the map by value
     *
     * @param unsortMap the map to sort
     * @return sorted map
     */
    private Map<String, Profession> sortByValue(Map<String, Profession> unsortMap) {

        List<Entry<String, Profession>> list = new LinkedList<>(unsortMap.entrySet());

        list.sort(Comparator.comparing(o -> o.getValue().getName()));
        Map<String, Profession> sortedMap = new LinkedHashMap<>();
        for (Entry<String, Profession> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

}
