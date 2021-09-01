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

package git.doomshade.professions.profession.professions.crafting;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 * @deprecated not used
 */
@Deprecated
public class CustomRecipe /*extends ItemType<ShapedRecipe>*/ {
    /*public static final NamespacedKey NMS_KEY = new NamespacedKey(Professions.getInstance(), "recipe");
    private static final String RESULT = "result", SHAPE = "shape", INGREDIENTS = "ingredients";

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
                recipe.setIngredient(x, Objects.requireNonNull(y.getData()));
            }
        });

        // must register the recipe
        Bukkit.addRecipe(recipe);
        return recipe;
    }*/

}
