package git.doomshade.professions.profession.types.crafting;

import git.doomshade.professions.profession.types.ItemTypeHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftShapedRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Iterator;

public class CustomRecipeHolder extends ItemTypeHolder<CustomRecipe> {
    @Override
    public void init() {
        // TODO Auto-generated method stub

        ItemStack result = new ItemStack(Material.STONE);
        ItemMeta meta = result.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Test");
        meta.setLore(Arrays.asList("This", "is", "a fokin", "test"));
        result.setItemMeta(meta);
        ShapedRecipe recipe = new ShapedRecipe(result).shape("abc", "def", "ghi").setIngredient('e', Material.DIAMOND);
        ShapedRecipe recipee = new ShapedRecipe(new ItemStack(Material.STONE)).shape("abc", "def", "ghi")
                .setIngredient('e', Material.ARROW);
        ShapedRecipe recipeee = new ShapedRecipe(new ItemStack(Material.STONE)).shape("abc", "def", "ghi")
                .setIngredient('e', Material.CHEST);

        registerObject(new CustomRecipe(CraftShapedRecipe.fromBukkitRecipe(recipe), 500));
        registerObject(new CustomRecipe(CraftShapedRecipe.fromBukkitRecipe(recipee), 1500));
        registerObject(new CustomRecipe(CraftShapedRecipe.fromBukkitRecipe(recipeee), 2500));

        // clear these recipes if they exist, let the CustomRecipe class handle it!
        Iterator<Recipe> bukkitRecipes = Bukkit.getServer().recipeIterator();
        while (bukkitRecipes.hasNext()) {
            Recipe bukkitRecipe = bukkitRecipes.next();
            if (!(bukkitRecipe instanceof ShapedRecipe)) {
                continue;
            }
            CraftShapedRecipe bukkitShapedRecipe = CraftShapedRecipe.fromBukkitRecipe((ShapedRecipe) bukkitRecipe);
            for (CustomRecipe customRecipe : getRegisteredItemTypes()) {
                if (customRecipe.isValid(bukkitShapedRecipe)) {
                    bukkitRecipes.remove();
                }
            }
        }
    }

    public Class<CustomRecipe> getItemType() {
        // TODO Auto-generated method stub
        return CustomRecipe.class;
    }
}
