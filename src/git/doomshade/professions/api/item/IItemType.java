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

package git.doomshade.professions.api.item;

import git.doomshade.professions.api.profession.Profession;
import git.doomshade.professions.api.user.IUserProfessionData;
import git.doomshade.professions.data.ProfessionExpSettings;
import git.doomshade.professions.enums.SkillupColor;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.trait.TrainerTrait;
import git.doomshade.professions.user.User;
import git.doomshade.professions.utils.Requirements;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

/**
 * <p>{@link ProfessionEvent} returns an instance of this to handle in a {@link Profession}</p>
 * <p>If you want to make your own type, make a class extend {@link ItemType} and override all constructors</p>
 * <p>To make a specialized item type (e.g. making this item craftable - yields a result in a time with
 * given prerequisites or trainable from an NPC with {@link TrainerTrait}) trait, see extensions</p>
 *
 * @param <T> the item type to look for in {@link ProfessionEvent}
 *
 * @author Doomshade
 * @see ItemType
 * @see CraftableItemType
 * @since 1.0
 */
public interface IItemType<T extends ConfigurationSerializable> {
    /**
     * Represents the number ID in the item type file.
     *
     * @return the ID number in the file
     *
     * @throws UnsupportedOperationException if this method is called on the example ItemType
     * @see ItemType#getExampleItemType(Class, ConfigurationSerializable)
     */
    int getFileId() throws UnsupportedOperationException;

    /**
     * Represents the config name of this item type in a "filename.fileId" format (filename without the .yml
     * extension).
     * <p>Note that this method was created for consistent IDs of item types, this is only a generated ID from the
     * file.</p>
     *
     * @return the config name
     */
    String getConfigName();

    /**
     * @return the name of this item type (used mainly for visual representation in an item)
     */
    String getName();

    /**
     * Sets the name of this item type
     *
     * @param name the name to set
     */
    void setName(String name);

    /**
     * @return the cost for training this item
     */
    int getTrainableCost();

    /**
     * Sets the cost of training this item
     *
     * @param cost the cost
     */
    void setTrainableCost(int cost);

    /**
     * @return {@code} true, if this item needs to be trained
     */
    boolean isTrainable();

    /**
     * Sets the training requirement of this item
     *
     * @param trainable the requirement
     */
    void setTrainable(boolean trainable);

    /**
     * @return the inventory requirements
     */
    Requirements getInventoryRequirements();

    /**
     * Sets the inventory requirements, overriding existing ones
     *
     * @param inventoryRequirements the inventory requirements
     */
    void setInventoryRequirements(Requirements inventoryRequirements);

    /**
     * @return the restricted worlds this item type will not be handled in events
     */
    List<String> getRestrictedWorlds();

    /**
     * Sets the restricted worlds in which this item type will not be handled in events
     *
     * @param restrictedWorlds the restricted worlds
     */
    void setRestrictedWorlds(List<String> restrictedWorlds);

    /**
     * @return {@code true} if this item type ignores the skillup color exp modifications
     *
     * @see ProfessionExpSettings
     */
    boolean isIgnoreSkillupColor();

    /**
     * Sets whether or not this item type should ignore the skillup color exp modifications
     *
     * @param ignoreSkillupColor whether or not to ignore skillup color
     *
     * @see ProfessionExpSettings
     */
    void setIgnoreSkillupColor(boolean ignoreSkillupColor);

    /**
     * @param upd the {@link User}'s {@link Profession} data to base the lore and {@link SkillupColor} around
     *
     * @return the itemstack (icon) representation of this item type used in a GUI
     */
    ItemStack getIcon(@Nullable IUserProfessionData upd);

    /**
     * @return the description of this item type (used mainly for visual representation in an item)
     */
    List<String> getDescription();

    /**
     * Sets the description of this item type
     *
     * @param description the description to set
     */
    void setDescription(List<String> description);

    /**
     * @return the material in a GUI (used for visual representation in an item)
     */
    ItemStack getGuiMaterial();

    /**
     * Sets the material of this item type in a GUI
     *
     * @param guiMaterial the material to set
     */
    void setGuiMaterial(ItemStack guiMaterial);

    /**
     * @return the object (or objective) of this item type
     */
    T getObject();

    /**
     * Sets the object (or objective) of this item type and also sets the name of this item type to {@code
     * item.toString()}.
     *
     * @param item the object to set
     */
    void setObject(T item);

    /**
     * @return the exp yield of this item type
     */
    int getExp();

    /**
     * Sets the exp yield of this item type
     *
     * @param exp the exp to set
     *
     * @throws IllegalArgumentException if the exp is < 0
     */
    void setExp(int exp) throws IllegalArgumentException;

    /**
     * You may override this method for more complex logic. This method is called during events, ensures that we got the
     * correct item type that gets further passed to profession
     *
     * @param object the object
     *
     * @return {@code true} if the object equals to this generic argument object
     */
    boolean equalsObject(T object);

    /**
     * @param professionLevel the profession level
     *
     * @return {@code true} if the profession level meets {@link #getLevelReq()}
     */
    boolean meetsLevelReq(int professionLevel);

    /**
     * Adds an inventory requirement for this item type
     *
     * @param item the inventory requirement to add
     */
    void addInventoryRequirement(ItemStack item);

    /**
     * @param player the player to check for
     *
     * @return {@code true} if the player meets requirements to proceed with the event, {@code false} otherwise. Does
     * not check for level requirements!
     */
    boolean meetsRequirements(Player player);

    /**
     * @return the level requirement of this item type
     */
    int getLevelReq();

    /**
     * Sets the level requirement of this item type
     *
     * @param levelReq the level to set
     */
    void setLevelReq(int levelReq);
}
