package git.doomshade.professions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import git.doomshade.professions.profession.professions.alchemy.AlchemyProfession;
import git.doomshade.professions.profession.professions.alchemy.Potion;
import git.doomshade.professions.profession.professions.alchemy.PotionItemType;
import git.doomshade.professions.profession.professions.crafting.CustomRecipe;
import git.doomshade.professions.profession.professions.enchanting.EnchantManager;
import git.doomshade.professions.profession.professions.enchanting.EnchantedItemItemType;
import git.doomshade.professions.profession.professions.enchanting.EnchantingProfession;
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
import git.doomshade.professions.profession.professions.skinning.SkinningProfession;
import git.doomshade.professions.profession.professions.smelting.BarItemType;
import git.doomshade.professions.profession.professions.smelting.SmeltingProfession;
import git.doomshade.professions.profession.types.*;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

/**
 * A manager regarding registration and queries of a {@link Profession}, {@link IProfessionType}, {@link ItemType} and {@link ItemTypeHolder}.
 *
 * @author Doomshade
 * @see Profession
 * @see IProfessionType
 * @see ItemType
 * @see ItemTypeHolder
 */
public final class ProfessionManager implements ISetup {
    private static final ProfessionManager instance = new ProfessionManager();
    @SuppressWarnings("rawtypes")

    private final IrremovableSet<Class<? extends Profession>> REGISTERED_PROFESSIONS = new IrremovableSet<>();
    private final HashSet<Class<? extends IProfessionType>> PROFESSION_TYPES = new HashSet<>();
    private final HashMap<ItemTypeHolder<?>, Class<? extends ItemType>> ITEMS = new HashMap<>();
    private final PluginManager pm = Bukkit.getPluginManager();
    private final Professions plugin = Professions.getInstance();
    private Map<String, Profession<? extends IProfessionType>> PROFESSIONS_ID = new HashMap<>();
    private Map<String, Profession<? extends IProfessionType>> PROFESSIONS_NAME = new HashMap<>();

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
    public <A extends ItemType<?>> ItemTypeHolder<A> getItemTypeHolder(Class<A> clazz) {
        for (Entry<ItemTypeHolder<?>, Class<? extends ItemType>> entry : ITEMS.entrySet()) {
            if (entry.getValue().equals(clazz)) {
                return (ItemTypeHolder<A>) entry.getKey();
            }
        }
        throw new RuntimeException(clazz + " is not a registered item type holder!");
    }

    /**
     * @return all registered {@link IProfessionType}s
     */
    public ImmutableSet<Class<? extends IProfessionType>> getProfessionTypes() {
        return ImmutableSet.copyOf(PROFESSION_TYPES);
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
    public ImmutableMap<String, Profession<? extends IProfessionType>> getProfessionsById() {
        return ImmutableMap.copyOf(PROFESSIONS_ID);
    }

    /**
     * @return a sorted {@link ImmutableMap} of {@link Profession}s by {@link Profession#getName()}
     */
    public ImmutableMap<String, Profession<? extends IProfessionType>> getProfessionsByName() {
        return ImmutableMap.copyOf(PROFESSIONS_NAME);
    }

    /**
     * @param itemTypeHolder the {@link ItemTypeHolder} to register
     * @param <T>            the {@link ItemTypeHolder}
     * @throws IOException ex
     */
    public <T extends ItemTypeHolder<?>> void registerItemTypeHolder(T itemTypeHolder) throws IOException {
        itemTypeHolder.update();
        ITEMS.put(itemTypeHolder, itemTypeHolder.getItemType().getClass());
    }

    /**
     * @param name the {@link Profession#getName()} or {@link Profession#getID()} of {@link Profession}
     * @return the {@link Profession} if found, {@code null} otherwise
     */
    @Nullable
    public Profession<?> getProfession(String name) {
        if (name.isEmpty()) {
            return null;
        }
        Profession<?> prof = PROFESSIONS_ID.get(name.toLowerCase());
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
     * @return the {@link Profession} if found, {@code null} otherwise
     */
    @Nullable
    public Profession<? extends IProfessionType> getProfession(Class<? extends Profession<?>> profession) {
        for (Profession<? extends IProfessionType> prof : PROFESSIONS_ID.values()) {
            if (prof.getClass().getSimpleName().equals(profession.getSimpleName())) {
                return prof;
            }
        }
        return null;
    }

    /**
     * @param clazz the {@link IProfessionType} class
     * @see IProfessionType for already registered profession types.
     * Registers a custom {@link IProfessionType}.
     */
    private void registerProfessionType(Class<? extends IProfessionType> clazz) {
        PROFESSION_TYPES.add(clazz);
    }

    @Override
    public void setup() throws IOException {
        register();
        registerProfessions();
    }

    @Override
    public void cleanup() {
        PROFESSION_TYPES.clear();
        PROFESSIONS_ID.clear();
        PROFESSIONS_NAME.clear();
        ITEMS.clear();
        Profession.INITED_PROFESSIONS.clear();
    }

    private void register() throws IOException {
        registerProfessionTypes();
        registerItemTypeHolders();
    }

    private void registerItemTypeHolders() throws IOException {
        {
            OreItemType ore = ItemType.getExampleItemType(OreItemType.class, Ore.EXAMPLE_ORE);
            ore.setName(ChatColor.GRAY + "Obsidian");
            registerItemTypeHolder(new ItemTypeHolder<>(ore));
        }
        {
            PreyItemType preyItemType = ItemType.getExampleItemType(PreyItemType.class, new Mob(EntityType.SKELETON));
            preyItemType.setName(ChatColor.YELLOW + "Kostlivec");
            registerItemTypeHolder(new ItemTypeHolder<>(preyItemType));
        }
        {
            HerbItemType herb = ItemType.getExampleItemType(HerbItemType.class, Herb.EXAMPLE_HERB);
            herb.setName(ChatColor.DARK_AQUA + "Test gather item");
            registerItemTypeHolder(new ItemTypeHolder<>(herb));
        }
        {
            EnchantManager enchm = EnchantManager.getInstance();
            try {
                enchm.registerEnchant(new RandomAttributeEnchant(new ItemStack(Material.GLASS)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            RandomAttributeEnchant ench = enchm.getEnchant(RandomAttributeEnchant.class);
            EnchantedItemItemType eit = ItemType.getExampleItemType(EnchantedItemItemType.class, ench);
            ItemStack craftRequirement = new ItemStack(Material.GLASS);
            ItemMeta craftRequirementMeta = craftRequirement.getItemMeta();
            craftRequirementMeta.setDisplayName(ChatColor.WHITE + "Sklo");
            craftRequirementMeta.setLore(ImmutableList.of("Japato"));
            craftRequirement.setItemMeta(craftRequirementMeta);
            eit.addCraftingRequirement(craftRequirement);
            eit.setName(ChatColor.RED + "Test random attribute enchantment");
            registerItemTypeHolder(new ItemTypeHolder<>(eit));
        }
        {
            ShapedRecipe recipe = new ShapedRecipe(ItemUtils.EXAMPLE_RESULT).shape("abc", "def", "ghi").setIngredient('e', Material.DIAMOND);
            CustomRecipe cr = ItemType.getExampleItemType(CustomRecipe.class, CraftShapedRecipe.fromBukkitRecipe(recipe));
            cr.setName(ChatColor.DARK_GREEN + "Test recipe");
            final ItemTypeHolder<CustomRecipe> itemTypeHolder = new ItemTypeHolder<>(cr);
            itemTypeHolder.registerObject(cr);

            // clear these recipes if they exist, let the CustomRecipe class handle it!
            final Server server = Bukkit.getServer();
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
            registerItemTypeHolder(itemTypeHolder);
        }
        {
            BarItemType barItemType = ItemType.getExampleItemType(BarItemType.class, ItemUtils.EXAMPLE_RESULT);
            barItemType.addCraftingRequirement(ItemUtils.EXAMPLE_REQUIREMENT);
            barItemType.setName(ChatColor.BLUE + "Test bar");
            registerItemTypeHolder(new ItemTypeHolder<>(barItemType));
        }
        {
            final PotionItemType potionItemType = ItemType.getExampleItemType(PotionItemType.class, Potion.EXAMPLE_POTION);
            registerItemTypeHolder(new ItemTypeHolder<>(potionItemType));
        }
        {
            final GemItemType gemItemType = ItemType.getExampleItemType(GemItemType.class, Gem.EXAMPLE_GEM);
            registerItemTypeHolder(new ItemTypeHolder<>(gemItemType));
        }
    }


    private void registerProfessionTypes() {
        registerProfessionType(IMining.class);
        registerProfessionType(IHunting.class);
        registerProfessionType(IGathering.class);
        registerProfessionType(IEnchanting.class);
        registerProfessionType(ICrafting.class);
    }

    private void registerProfessions() {
        registerProfession(new MiningProfession(), false);
        registerProfession(new JewelcraftingProfession(), false);
        registerProfession(new EnchantingProfession(), false);
        registerProfession(new SkinningProfession(), false);
        registerProfession(new SmeltingProfession(), false);
        registerProfession(new HerbalismProfession(), false);
        registerProfession(new AlchemyProfession(), false);
        sortProfessions();
    }

    private void registerProfession(Profession<? extends IProfessionType> prof, boolean sayMessage) {
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
        if (!Profession.INITED_PROFESSIONS.contains(prof.getClass())) {
            try {
                throw new IllegalAccessException("If you want to override constructors, make sure to call super() !");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        PROFESSIONS_ID.forEach((y, x) -> {
            if (x.getID().equalsIgnoreCase(prof.getID())) {
                throw new IllegalArgumentException(ChatColor.DARK_RED + "ERROR:" + ChatColor.RED + " A profession with name "
                        + prof.getName() + ChatColor.RESET + " already exists! (" + prof.getID() + ")");
            }
        });
        PROFESSIONS_ID.put(prof.getID().toLowerCase(), prof);
        PROFESSIONS_NAME.put(ChatColor.stripColor(prof.getColoredName().toLowerCase()), prof);
        if (!REGISTERED_PROFESSIONS.contains(prof.getClass())) {
            pm.registerEvents(prof, plugin);
            REGISTERED_PROFESSIONS.add(prof.getClass());
        }

        /*Professions._log("Could not update " + prof.getID() + " profession. Reason:", Level.WARNING);
        e.printStackTrace();
        */
        prof.onLoad();
        if (sayMessage)
            Professions.log("Registered " + prof.getColoredName() + ChatColor.RESET + " profession", Level.INFO);
    }

    public void registerProfession(Profession<? extends IProfessionType> prof) {
        registerProfession(prof, true);
    }

    private void sortProfessions() {
        Map<String, Profession<? extends IProfessionType>> MAP_COPY = new HashMap<>(PROFESSIONS_ID);
        PROFESSIONS_ID = sortByValue(MAP_COPY);
        MAP_COPY = new HashMap<>(PROFESSIONS_NAME);
        PROFESSIONS_NAME = sortByValue(MAP_COPY);
    }

    private Map<String, Profession<? extends IProfessionType>> sortByValue(Map<String, Profession<? extends IProfessionType>> unsortMap) {

        List<Entry<String, Profession<? extends IProfessionType>>> list = new LinkedList<>(unsortMap.entrySet());

        list.sort(Comparator.comparing(o -> o.getValue().getName()));
        Map<String, Profession<? extends IProfessionType>> sortedMap = new LinkedHashMap<>();
        for (Entry<String, Profession<? extends IProfessionType>> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

}
