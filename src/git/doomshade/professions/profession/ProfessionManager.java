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

package git.doomshade.professions.profession;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import git.doomshade.professions.Professions;
import git.doomshade.professions.api.IProfessionManager;
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.item.ext.ItemType;
import git.doomshade.professions.api.item.ItemTypeHolder;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.profession.professions.alchemy.AlchemyProfession;
import git.doomshade.professions.profession.professions.alchemy.Potion;
import git.doomshade.professions.profession.professions.alchemy.PotionItemType;
import git.doomshade.professions.profession.professions.enchanting.EnchantedItemItemType;
import git.doomshade.professions.profession.professions.enchanting.Enchants;
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
import git.doomshade.professions.profession.professions.smelting.BarItemStack;
import git.doomshade.professions.profession.professions.smelting.BarItemType;
import git.doomshade.professions.profession.professions.smelting.SmeltingProfession;
import git.doomshade.professions.utils.ISetup;
import git.doomshade.professions.utils.IrremovableSet;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.PluginManager;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * A manager regarding registration and queries of {@link Profession}, {@link ItemType}, and {@link ItemTypeHolder}.
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public final class ProfessionManager implements ISetup, IProfessionManager {
    private static final ProfessionManager instance = new ProfessionManager();
    private static final HashSet<Class<? extends Profession>> INITED_PROFESSIONS = new HashSet<>();
    /**
     * Using IrremovableSet here so that the registered professions never get deleted (they are intended not to!)
     */
    private final IrremovableSet<Class<? extends Profession>> REGISTERED_PROFESSIONS = new IrremovableSet<>();
    @SuppressWarnings("rawtypes")
    private final HashMap<Class<? extends ItemType>, ItemTypeHolder<?, ?>> ITEMS = new HashMap<>();
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

    public static Collection<Class<? extends Profession>> getInitedProfessions() {
        return ImmutableSet.copyOf(INITED_PROFESSIONS);
    }

    /**
     * @return all registered {@link Profession}s
     */
    public Set<Class<? extends Profession>> getRegisteredProfessions() {
        return ImmutableSet.copyOf(REGISTERED_PROFESSIONS);
    }

    /**
     * @return a sorted {@link ImmutableMap} of {@link Profession}s by {@link Profession#getID()}
     */
    public Map<String, Profession> getProfessionsById() {
        return ImmutableMap.copyOf(PROFESSIONS_ID);
    }

    /**
     * @return a sorted {@link ImmutableMap} of {@link Profession}s by {@link Profession#getName()}
     */
    public Map<String, Profession> getProfessionsByName() {
        return ImmutableMap.copyOf(PROFESSIONS_NAME);
    }

    @Override
    public void setup() throws IOException {
        register();
        registerProfessions();
        //detectDuplicates();
    }

    @Override
    public void cleanup() {
        PROFESSIONS_ID.clear();
        PROFESSIONS_NAME.clear();
        ITEMS.clear();
        INITED_PROFESSIONS.clear();
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
     * @see #registerItemTypeHolder(Class, ConfigurationSerializable, Consumer)
     */
    private void registerItemTypeHolders() throws IOException {

        // MINING
        registerItemTypeHolder(
                OreItemType.class,
                Ore.EXAMPLE_ORE,
                ore -> {
                    ore.setName(ChatColor.GRAY + "Obsidian");
                    ore.addInventoryRequirement(ItemUtils.EXAMPLE_REQUIREMENT);
                }
        );

        // BLACKSMITHING
        /*registerItemTypeHolder(
                new ItemTypeHolder<>(BSItemType.class, ItemUtils.EXAMPLE_RESULT, bs -> {
                    bs.addCraftingRequirement(ItemUtils.EXAMPLE_REQUIREMENT);
                    bs.addInventoryRequirement(ItemUtils.EXAMPLE_REQUIREMENT);
                })
        );*/

        // HUNTING (no longer used)
        registerItemTypeHolder(
                PreyItemType.class,
                new Mob(EntityType.SKELETON),
                x -> x.setName(ChatColor.YELLOW + "Kostlivec")
        );


        // HERBALISM
        registerItemTypeHolder(
                HerbItemType.class,
                Herb.EXAMPLE_HERB,
                x -> x.setName(ChatColor.DARK_AQUA + "Test gather item")
        );

        // ENCHANTING
        registerItemTypeHolder(
                EnchantedItemItemType.class,
                Enchants.EXAMPLE_ENCHANT,
                /*(Supplier<Enchant>) () -> {
                    EnchantManager enchMan = EnchantManager.getInstance();
                    try {
                        enchMan.registerEnchant(new RandomAttributeEnchant(Utils.EXAMPLE_ID, Utils.EXAMPLE_NAME,
                                new ItemStack(Material.GLASS)));
                    } catch (Exception e) {
                        ProfessionLogger.logError(e);
                    }
                    return enchMan.getEnchant(RandomAttributeEnchant.class);
                },*/
                x -> {
                    x.addCraftingRequirement(ItemUtils.EXAMPLE_REQUIREMENT);
                    x.setName(ChatColor.RED + "Test random attribute enchantment");
                }
        );

        // CRAFTING
        /*registerItemTypeHolderSupplier(() -> {
            ShapedRecipe recipe = new ShapedRecipe(CustomRecipe.NMS_KEY, ItemUtils.EXAMPLE_RESULT)
                    .shape("abc", "def", "ghi")
                    .setIngredient('e', Material.DIAMOND);
            CustomRecipe cr = ItemType.getExampleItemType(CustomRecipe.class, recipe);
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
                ShapedRecipe shaped = (ShapedRecipe) bukkitRecipe;
                for (CustomRecipe customRecipe : itemTypeHolder) {
                    if (customRecipe.equalsObject(shaped)) {
                        bukkitRecipes.remove();
                    }
                }
            }
            return null;
            //return itemTypeHolder;
        });*/

        // SMELTING
        registerItemTypeHolder(
                BarItemType.class,
                BarItemStack.EXAMPLE_BAR,
                x -> {
                    x.addCraftingRequirement(ItemUtils.EXAMPLE_REQUIREMENT);
                    x.setName(ChatColor.BLUE + "Test bar");
                }
        );

        // ALCHEMY
        registerItemTypeHolder(
                PotionItemType.class,
                Potion.EXAMPLE_POTION
        );

        // JEWELCRAFTING
        registerItemTypeHolder(
                GemItemType.class,
                Gem.EXAMPLE_GEM
        );
    }

    @Override
    public <T extends ConfigurationSerializable, IType extends ItemType<T>> void registerItemTypeHolder(
            Class<IType> itemType, T o,
            Consumer<IType> additionalCommand) throws IOException {
        ItemTypeHolder<T, IType> itemTypeHolder = new ItemTypeHolder<>(itemType, o, additionalCommand);
        ITEMS.put(itemTypeHolder.getExampleItemType().getClass(), itemTypeHolder);
        itemTypeHolder.update();
    }

    @Override
    public void registerProfession(Profession prof) {
        registerProfession(prof, true);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <T extends ConfigurationSerializable, A extends ItemType<T>> ItemTypeHolder<T, A> getItemTypeHolder(
            Class<A> clazz) throws IllegalArgumentException {
        final ItemTypeHolder<?, ?> itHolder = ITEMS.get(clazz);
        if (itHolder == null) {
            throw new IllegalArgumentException(clazz + " is not a registered item type holder!");
        }
        return (ItemTypeHolder<T, A>) itHolder;
    }

    @Override
    public Optional<Profession> getProfession(Class<? extends Profession> profession) {
        for (Profession prof : PROFESSIONS_ID.values()) {
            if (prof.getClass().getSimpleName().equals(profession.getSimpleName())) {
                return Optional.of(prof);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Profession> getProfessionById(String id) {
        if (id == null || id.isEmpty()) {
            return Optional.empty();
        }
        Profession prof = PROFESSIONS_ID.get(id.toLowerCase());
        if (prof == null) {
            return Optional.empty();
        }
        return Optional.of(prof);
    }

    @Override
    public Optional<Profession> getProfessionByName(String name) {
        if (name == null || name.isEmpty()) {
            return Optional.empty();
        }

        Profession prof = PROFESSIONS_NAME.get(ChatColor.stripColor(name.toLowerCase()));

        if (prof == null) {
            try {
                prof = Utils.findInIterable(PROFESSIONS_ID.values(),
                        x -> x.getIcon() != null
                                && x.getIcon().getItemMeta() != null
                                && ChatColor.stripColor(x.getIcon().getItemMeta().getDisplayName())
                                .equalsIgnoreCase(ChatColor.stripColor(name)));
            } catch (Utils.SearchNotFoundException e) {
                return Optional.empty();
            }
        }
        return Optional.of(prof);
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
        Set<String> requiredPlugins = prof.getRequiredPlugins()
                .stream()
                .filter(plugin -> !Bukkit.getPluginManager().isPluginEnabled(plugin))
                .collect(Collectors.toSet());
        if (!requiredPlugins.isEmpty()) {
            throw new IllegalStateException(String.format(
                    "Could not load %s as some plugins are missing!%s\nRequired plugins: %s\nPlugins missing: %s",
                    prof.getColoredName(), ChatColor.RESET, String.join(", ", prof.getRequiredPlugins()),
                    String.join(", ", requiredPlugins)));
        }

        if (!INITED_PROFESSIONS.add(prof.getClass())) {
            throw new IllegalArgumentException(
                    String.format("%s %shas already been registered!", prof.getColoredName(), ChatColor.RESET));
        }

        // make sure the profession is not already registered
        if (PROFESSIONS_ID.containsKey(prof.getID().toLowerCase())) {
            throw new IllegalArgumentException(
                    String.format("%sERROR: %sA profession with name %s already exists! (%s)",
                            ChatColor.DARK_RED, ChatColor.RED, prof.getName() + ChatColor.RESET, prof.getID()));
        }

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
        if (logMessage) {
            ProfessionLogger.log("Registered " + prof.getColoredName() + ChatColor.RESET + " profession", Level.INFO);
        }
    }

    /**
     * Sorts the professions ID and professions name maps for better visuals in chat
     */
    private void sortProfessions() {
        Map<String, Profession> MAP_COPY = new HashMap<>(PROFESSIONS_ID);
        PROFESSIONS_ID = Utils.sortMapByValue(MAP_COPY, Comparator.comparing(Profession::getName));
        MAP_COPY = new HashMap<>(PROFESSIONS_NAME);
        PROFESSIONS_NAME = Utils.sortMapByValue(MAP_COPY, Comparator.comparing(Profession::getName));
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
        for (ItemTypeHolder<?, ?> holder : getItemTypeHolders()) {
            for (ItemType<?> itemType : holder) {
                final ItemType<?> put = map.putIfAbsent(itemType.getConfigName(), itemType);
                if (put != null) {
                    if (!loggedDuplicate) {
                        final String errorMsg = "Found duplicates of config names.";
                        ProfessionLogger.log(errorMsg, Level.CONFIG);
                        ProfessionLogger.log(errorMsg, Level.SEVERE);
                        loggedDuplicate = true;
                    }
                    duplicates.add(put);
                    duplicates.add(itemType);
                }
            }
        }

        StringBuilder duplicatesString = new StringBuilder();
        if (loggedDuplicate) {
            ProfessionLogger.log("Duplicates:");
            ProfessionLogger.log("Duplicates:", Level.CONFIG);

            for (ItemType<?> duplicate : duplicates) {
                duplicatesString.append("\n").append(duplicate.toCompactString());
            }
            System.out.println(duplicatesString);
            ProfessionLogger.log(duplicatesString, Level.CONFIG);
        }
    }

    /**
     * @return all registered {@link ItemTypeHolder}s
     */
    public Collection<ItemTypeHolder<?, ?>> getItemTypeHolders() {
        return ImmutableSet.copyOf(ITEMS.values());
    }

    /**
     * Sorts the map by value
     *
     * @param map the map to sort
     *
     * @return sorted map
     *
     * @see Utils#sortMapByValue(Map, Comparator)
     * @deprecated not used anymore
     */
    @Deprecated
    private Map<String, Profession> sortByValue(Map<String, Profession> map) {

        List<Entry<String, Profession>> list = new LinkedList<>(map.entrySet());

        list.sort(Comparator.comparing(o -> o.getValue().getName()));
        Map<String, Profession> sortedMap = new LinkedHashMap<>();
        for (Entry<String, Profession> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

}
