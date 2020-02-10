package git.doomshade.professions.profession.professions.crafting;

import git.doomshade.professions.profession.types.ICrafting;
import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemorySection;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftShapedRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.util.*;
import java.util.Map.Entry;

public class CustomRecipe extends ItemType<CraftShapedRecipe> {
    private static final String RESULT = "result", SHAPE = "shape", INGREDIENTS = "ingredients";

    public CustomRecipe() {
        super();
    }

    public CustomRecipe(CraftShapedRecipe object, int exp) {
        super(object, exp);
    }

    @Override
    public boolean equalsObject(CraftShapedRecipe t) {

        CraftShapedRecipe current = getObject();
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
        CraftShapedRecipe recipe = getObject();
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
            map.put(x.charAt(0),
                    y == null ? null : ItemUtils.deserialize(((MemorySection) ingredients.get(x)).getValues(true)));
        });
        return map;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected CraftShapedRecipe deserializeObject(Map<String, Object> map) {
        Object o = map.get(RESULT);
        Map<String, Object> recipeDes;
        if (o instanceof Map) {
            recipeDes = (Map<String, Object>) o;
        } else if (o instanceof MemorySection) {
            recipeDes = ((MemorySection) o).getValues(true);
        } else {
            return null;
        }
        final ItemStack deserialize = ItemUtils.deserialize(recipeDes);
        if (deserialize == null) {
            return null;
        }
        ShapedRecipe recipe = new ShapedRecipe(deserialize).shape(((ArrayList<String>) map.get(SHAPE)).toArray(new String[0]));

        Map<String, Object> ingredDes = ((MemorySection) map.get(INGREDIENTS)).getValues(false);
        Map<Character, ItemStack> ingredients = deserializeIngredients(ingredDes);
        ingredients.forEach((x, y) -> {
            if (y != null)
                recipe.setIngredient(x, y.getData());
        });

        // must register the recipe
        Bukkit.addRecipe(recipe);
        return CraftShapedRecipe.fromBukkitRecipe(recipe);
    }

    @Override
    public Class<? extends IProfessionType> getDeclaredProfessionType() {
        return ICrafting.class;
    }

}
