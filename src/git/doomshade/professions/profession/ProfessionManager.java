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

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.api.item.ItemTypeHolder;
import git.doomshade.professions.api.profession.IProfessionManager;
import git.doomshade.professions.api.profession.Profession;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.profession.professions.alchemy.AlchemyProfession;
import git.doomshade.professions.profession.professions.alchemy.Potion;
import git.doomshade.professions.profession.professions.alchemy.PotionItemType;
import git.doomshade.professions.profession.professions.herbalism.Herb;
import git.doomshade.professions.profession.professions.herbalism.HerbItemType;
import git.doomshade.professions.profession.professions.herbalism.HerbalismProfession;
import git.doomshade.professions.profession.professions.jewelcrafting.Gem;
import git.doomshade.professions.profession.professions.jewelcrafting.GemItemType;
import git.doomshade.professions.profession.professions.jewelcrafting.JewelcraftingProfession;
import git.doomshade.professions.profession.professions.mining.MiningProfession;
import git.doomshade.professions.profession.professions.mining.Ore;
import git.doomshade.professions.profession.professions.mining.OreItemType;
import git.doomshade.professions.profession.professions.smelting.BarItemStack;
import git.doomshade.professions.profession.professions.smelting.BarItemType;
import git.doomshade.professions.profession.professions.smelting.SmeltingProfession;
import git.doomshade.professions.utils.ISetup;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.event.HandlerList;
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
	private static final ProfessionManager INSTANCE = new ProfessionManager();

	/**
	 * a set containing initialized professions - to ensure there's only one instance of a profession is reset on
	 * cleanup
	 */
	private final Set<Class<? extends Profession>> initedProfessions = new HashSet<>();

	/**
	 * a set containing registered professions as listeners - to ensure they only get registered once
	 */
	private final Set<Class<? extends Profession>> registeredProfessions = new HashSet<>();

	/**
	 * This needs to be raw because Java Generics
	 *
	 * @see #registerItemTypeHolder
	 */
	@SuppressWarnings("rawtypes")
	private final Map<Class<? extends ItemType>, ItemTypeHolder<?, ?>> items = new HashMap<>();
	private final PluginManager pm = Bukkit.getPluginManager();
	private Map<String, Profession> professionsIds = new HashMap<>();
	private Map<String, Profession> professionsName = new HashMap<>();

	private ProfessionManager() {
	}

	public Collection<Class<? extends Profession>> getInitedProfessions() {
		return Set.copyOf(getInstance().initedProfessions);
	}

	/**
	 * @return the instance of this class
	 */
	public static ProfessionManager getInstance() {
		return INSTANCE;
	}

	/**
	 * @return all registered {@link Profession}s
	 */
	public Set<Class<? extends Profession>> getRegisteredProfessions() {
		return Set.copyOf(registeredProfessions);
	}

	/**
	 * @return a sorted {@link Map} of {@link Profession}s by {@link Profession#getID()}
	 */
	public Map<String, Profession> getProfessionsById() {
		return Map.copyOf(professionsIds);
	}

	/**
	 * @return a sorted {@link Map} of {@link Profession}s by {@link Profession#getName()}
	 */
	public Map<String, Profession> getProfessionsByName() {
		return Map.copyOf(professionsName);
	}

	@Override
	public void setup() throws IOException {
		register();
		registerProfessions();
		//detectDuplicates();
	}

	@Override
	public void cleanup() {
		professionsIds.clear();
		professionsName.clear();
		items.clear();
		initedProfessions.clear();
	}

	/**
	 * Registers profession types and then item type holders in that order
	 */
	private void register() {
		registerItemTypeHolders();
	}

	/**
	 * Huge method for {@link ItemTypeHolder} registrations
	 *
	 * @see #registerItemTypeHolder(Class, ConfigurationSerializable, Consumer)
	 */
	private void registerItemTypeHolders() {

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
        /*registerItemTypeHolder(
                PreyItemType.class,
                new Mob(EntityType.SKELETON),
                x -> x.setName(ChatColor.YELLOW + "Kostlivec")
        );*/


		// HERBALISM
		registerItemTypeHolder(
				HerbItemType.class,
				Herb.EXAMPLE_HERB,
				x -> x.setName(ChatColor.DARK_AQUA + "Test gather item")
		);

		// ENCHANTING
        /*registerItemTypeHolder(
                EnchantedItemItemType.class,
                Enchants.EXAMPLE_ENCHANT,

                x -> {
                    x.addCraftingRequirement(ItemUtils.EXAMPLE_REQUIREMENT);
                    x.setName(ChatColor.RED + "Test random attribute enchantment");
                }
        );*/
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
				BarItemStack.getExampleBar(),
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
			Consumer<IType> additionalCommand) {
		ItemTypeHolder<T, IType> itemTypeHolder = new ItemTypeHolder<>(itemType, o, additionalCommand);
		items.put(itemTypeHolder.getExampleItemType().getClass(), itemTypeHolder);
		try {
			itemTypeHolder.update();
			ProfessionLogger.log(String.format("Successfully registered %s", itemType.getSimpleName()), Level.FINE);
		} catch (IOException e) {
			ProfessionLogger.logError(e);
		}
	}

	@Override
	public void unregisterProfession(final Profession prof) {
		initedProfessions.remove(prof.getClass());
		professionsIds.remove(prof.getID().toLowerCase());
		professionsName.remove(ChatColor.stripColor(prof.getColoredName().toLowerCase()));
		registeredProfessions.remove(prof.getClass());
		HandlerList.unregisterAll(prof);
	}

	@Override
	public void registerProfession(Profession prof) {
		registerProfession(prof, true);
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public <T extends ConfigurationSerializable, A extends ItemType<T>> ItemTypeHolder<T, A> getItemTypeHolder(
			Class<A> clazz) throws IllegalStateException {
		final ItemTypeHolder<?, ?> itHolder = items.get(clazz);
		if (itHolder == null) {
			throw new IllegalStateException(clazz + " is not a registered item type holder!");
		}
		return (ItemTypeHolder<T, A>) itHolder;
	}

	@Override
	public Optional<Profession> getProfession(Class<? extends Profession> profession) {
		for (Profession prof : professionsIds.values()) {
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
		Profession prof = professionsIds.get(id.toLowerCase());
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

		Profession prof = professionsName.get(ChatColor.stripColor(name.toLowerCase()));

		if (prof == null) {
			try {
				prof = Utils.findInIterable(professionsIds.values(),
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
	 * Registers a profession
	 *
	 * @param prof       the profession to register
	 * @param logMessage whether to announce the registration
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
					prof.getColoredName() + ChatColor.RESET, ChatColor.RESET, String.join(", ",
							prof.getRequiredPlugins()),
					String.join(", ", requiredPlugins)));
		}

		if (!initedProfessions.add(prof.getClass())) {
			throw new IllegalArgumentException(
					String.format("%s has already been registered!", prof.getColoredName() + ChatColor.RESET));
		}

		// make sure the profession is not already registered
		if (professionsIds.containsKey(prof.getID().toLowerCase())) {
			throw new IllegalArgumentException(
					String.format("%sERROR: %sA profession with name %s already exists! (%s)",
							ChatColor.DARK_RED, ChatColor.RED, prof.getName() + ChatColor.RESET, prof.getID()));
		}

		// finally the profession is registered
		professionsIds.put(prof.getID().toLowerCase(), prof);
		professionsName.put(ChatColor.stripColor(prof.getColoredName().toLowerCase()), prof);

		// if this is during onEnable, register the profession as a listener
		if (registeredProfessions.add(prof.getClass())) {
			ProfessionLogger.log(String.format("Registered events for %s", prof.getColoredName() + ChatColor.RESET),
					Level.INFO);
			pm.registerEvents(prof, Professions.getInstance());
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
	 * Registers current professions
	 *
	 * @see #registerProfession(Profession)
	 */
	private void registerProfessions() {
		registerProfession(new MiningProfession());
		registerProfession(new JewelcraftingProfession());
		//registerProfession(new EnchantingProfession(), false);
		//registerProfession(new SkinningProfession(), false);
		registerProfession(new SmeltingProfession());
		registerProfession(new HerbalismProfession());
		registerProfession(new AlchemyProfession());
		sortProfessions();
	}

	/**
	 * Sorts the professions ID and professions name maps for better visuals in chat
	 */
	private void sortProfessions() {
		Map<String, Profession> mapCopy = new HashMap<>(professionsIds);
		professionsIds = Utils.sortMapByValue(mapCopy, Comparator.comparing(Profession::getName));
		mapCopy = new HashMap<>(professionsName);
		professionsName = Utils.sortMapByValue(mapCopy, Comparator.comparing(Profession::getName));
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
		return Set.copyOf(items.values());
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
