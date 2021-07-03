package git.doomshade.professions.profession.professions.alchemy;

import com.sucy.skill.SkillAPI;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.api.types.CraftableItemType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PotionItemType extends CraftableItemType<Potion> {

    /**
     * Constructor for creation of the item type object
     *
     * @param object
     */
    public PotionItemType(Potion object) {
        super(object);
        setResult(Potion.EXAMPLE_POTION.getItem());
    }

    @Override
    public Map<String, Object> getSerializedObject() {
        if (getObject() != null) {
            return getObject().serialize();
        }
        return new HashMap<>();
    }

    @Override
    protected Potion deserializeObject(Map<String, Object> map) throws ProfessionObjectInitializationException {
        Potion potion = Potion.deserialize(map);
        final Optional<ItemStack> potionItem = potion.getPotionItem(potion.getItem());
        potionItem.ifPresent(this::setResult);
        return potion;
    }


    @Override
    public Function<ItemStack, ItemStack> getExtraInEvent() {
        return itemStack -> itemStack;
    }


    @Override
    public void onDisable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            //Potion.cache(p);
        }
        Potion.POTIONS.clear();
    }

    @Override
    public void onReload() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            //Potion.loadFromCache(p);
        }
    }

    @Override
    public void onLoad() {
        final Pattern ATTRIBUTE_PATTERN = Pattern.compile("([\\w]+):([0-9]+)");


        Potion.registerCustomPotionEffect((potionEffect, player, negated) -> {
            Matcher m = ATTRIBUTE_PATTERN.matcher(potionEffect);
            if (!m.find()) {
                return;
            }
            String attribute = m.group(1);
            int amount = Integer.parseInt(m.group(2));
            if (negated) {
                amount = -amount;
            }

            git.doomshade.loreattributes.Attribute laAttribute = git.doomshade.loreattributes.Attribute.parse(attribute);

            if (laAttribute != null) {
                git.doomshade.loreattributes.user.User.getUser(player).addCustomAttribute(laAttribute, amount);
            } else {
                com.sucy.skill.manager.AttributeManager.Attribute sapiAttribute = SkillAPI.getAttributeManager().getAttribute(attribute);
                if (sapiAttribute != null) {
                    SkillAPI.getPlayerData(player).addBonusAttributes(attribute, amount);
                }
            }

        });
    }
}
