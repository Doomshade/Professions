package git.doomshade.professions.profession.professions.crafting;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.exceptions.InitializationException;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.util.*;
import java.util.Map.Entry;

public class CustomRecipe extends ItemType<ShapedRecipe> {
    public static final NamespacedKey NMS_KEY = new NamespacedKey(Professions.getInstance(), "recipe");
    private static final String RESULT = "result", SHAPE = "shape", INGREDIENTS = "ingredients";

    /**
     * Constructor for creation of the item type object
     *
     * @param object
     */
    public CustomRecipe(ShapedRecipe object) {
        super(object);
    }

    @Override
    public boolean equalsObject(ShapedRecipe t) {

        ShapedRecipe current = getObject();
        if (current == null) {
            return false;
        }
        Collection<ItemStack> ingredients = current.getIngredientMap().values();
        Collection<ItemStack> otherIngredients = t.getIngredientMap().values();

        List<ItemStack> items = Arrays.asList(ingredients.toArray(new ItemStack[0]));
        List<ItemStack> otherItems = Arrays.asList(otherIngredients.toArray(new ItemStack[0]));

        for (int i = 0; i < Math.min(items.size(), otherItems.size()); i++) {
            ItemStack item = items.get(i);
            ItemStack otherItem = otherItems.get(i);
            if (item == null && otherItem == null) {
                continue;
            }
            if (item != null && otherItem != null) {
                if (!item.isSimilar(otherItem)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return current.getResult().isSimilar(t.getResult());
    }

    @Override
    public Map<String, Object> getSerializedObject() {
        Map<String, Object> map = new HashMap<>();
        ShapedRecipe recipe = getObject();
        if (recipe == null) {
            return map;
        }
        map.put(RESULT, recipe.getResult().serialize());
        map.put(SHAPE, recipe.getShape());
        map.put(INGREDIENTS, serializeIngredients(recipe.getIngredientMap()));
        return map;
    }

    private Map<String, Object> serializeIngredients(Map<Character, ItemStack> ingredients) {
        Map<String, Object> map = new HashMap<>();
        int abc = 'a';

        for (Entry<Character, ItemStack> entry : ingredients.entrySet()) {
            Character x = entry.getKey();
            ItemStack y = entry.getValue();
            if (x != null)
                map.put(String.valueOf(x), y == null ? null : y.serialize());
            else
                map.put(String.valueOf((char) abc++), y == null ? null : y.serialize());
        }
        return map;
    }

    private Map<Character, ItemStack> deserializeIngredients(Map<String, Object> ingredients) {
        Map<Character, ItemStack> map = new HashMap<>();
        ingredients.forEach((x, y) -> {
            try {
                map.put(x.charAt(0),
                        y == null ? null : ItemUtils.deserialize(((MemorySection) ingredients.get(x)).getValues(true)));
            } catch (ConfigurationException | InitializationException e) {
                ProfessionLogger.logError(e, false);
            }
        });
        return map;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ShapedRecipe deserializeObject(Map<String, Object> map) throws ProfessionObjectInitializationException {
        Object o = map.get(RESULT);
        Map<String, Object> recipeDes;
        if (o instanceof Map) {
            recipeDes = (Map<String, Object>) o;
        } else if (o instanceof MemorySection) {
            recipeDes = ((MemorySection) o).getValues(true);
        } else {
            return null;
        }
        final ItemStack deserialize;
        try {
            deserialize = ItemUtils.deserialize(recipeDes);
        } catch (ConfigurationException | InitializationException e) {
            ProfessionLogger.logError(e, false);
            throw new ProfessionObjectInitializationException("Could not deserialize recipe ItemStack from file");
        }
        ShapedRecipe recipe = new ShapedRecipe(NMS_KEY, deserialize)
                .shape(((ArrayList<String>) map.get(SHAPE)).toArray(new String[0]));

        Map<String, Object> ingredDes = ((MemorySection) map.get(INGREDIENTS)).getValues(false);
        Map<Character, ItemStack> ingredients = deserializeIngredients(ingredDes);
        ingredients.forEach((x, y) -> {
            if (y != null) {
                recipe.setIngredient(x, y.getData());
            }
        });

        // must register the recipe
        Bukkit.addRecipe(recipe);
        return recipe;
    }

}
