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

package git.doomshade.professions.api;

import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.api.item.ItemTypeHolder;
import git.doomshade.professions.data.ProfessionSettingsManager;
import git.doomshade.professions.data.ProfessionSpecificDefaultsSettings;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.event.ProfessionEventWrapper;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.listeners.ProfessionListener;
import git.doomshade.professions.profession.ProfessionManager;
import git.doomshade.professions.profession.professions.alchemy.AlchemyProfession;
import git.doomshade.professions.profession.professions.blacksmithing.BlacksmithingProfession;
import git.doomshade.professions.profession.professions.enchanting.EnchantingProfession;
import git.doomshade.professions.profession.professions.herbalism.HerbalismProfession;
import git.doomshade.professions.profession.professions.jewelcrafting.JewelcraftingProfession;
import git.doomshade.professions.profession.professions.mining.MiningProfession;
import git.doomshade.professions.profession.professions.smelting.SmeltingProfession;
import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.ISetup;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The class for a custom profession
 *
 * @author Doomshade
 * @version 1.0
 * @see AlchemyProfession
 * @see BlacksmithingProfession
 * @see EnchantingProfession
 * @see HerbalismProfession
 * @see JewelcraftingProfession
 * @see MiningProfession
 * @see SmeltingProfession
 * @since 1.0
 */
public abstract class Profession implements IProfession {

    protected final ProfessionUtils utils;
    final HashSet<ItemTypeHolder<?, ?>> items = new HashSet<>();
    private final HashSet<String> requiredPlugins = new HashSet<>();
    private final String name;
    private final ProfessionType pt;
    private final ItemStack icon;
    private final ProfessionSettingsManager professionSettings;

    public Profession() throws IllegalStateException {
        this(false);
    }

    private Profession(boolean ignoreInitializationError) throws IllegalStateException {
        // make sure only a single instance of this class is created
        ensureNotInitialized(ignoreInitializationError);

        // initialize settings AFTER initializing file as settings require the profession file!
        ProfessionSettingsManager settings = new ProfessionSettingsManager(this);
        try {
            settings.setup();
        } catch (Exception e) {
            ProfessionLogger.logError(e);
        }
        this.professionSettings = settings;
        ProfessionSpecificDefaultsSettings defaults = settings.getSettings(ProfessionSpecificDefaultsSettings.class);
        this.name = defaults.getName();
        this.icon = defaults.getIcon();
        this.pt = defaults.getProfessionType();
        this.utils = new ProfessionUtils(this);
    }

    private void ensureNotInitialized(boolean ignoreError) throws IllegalStateException {
        if (ProfessionManager.getInitedProfessions().contains(getClass()) && !ignoreError) {
            throw new IllegalStateException(
                    "Do not access professions by their constructor, use ProfessionManager#getProfession(String) " +
                            "instead");
        }

    }

    /**
     * @return the icon of this profession (for GUI purposes)
     */
    public ItemStack getIcon() {
        return icon;
    }

    /**
     * @return the handled {@link ItemType}'s holders
     */
    public final Iterable<ItemTypeHolder<?, ?>> getItems() {
        return items;
    }

    /**
     * @return the profession settings
     */
    public ProfessionSettingsManager getProfessionSettings() {
        return professionSettings;
    }

    @Override
    public final String toString() {
        String type = getProfessionType() != null ? getProfessionType().toString() + ", " : "";
        return getColoredName() + ChatColor.RESET + ", " + type;
    }

    /**
     * @return the profession type
     */
    public ProfessionType getProfessionType() {
        return pt;
    }

    /**
     * The colored name of this profession. Use this instead of getName() as that method does not translate the colour.
     *
     * @return the colored name of this profession
     */
    public final String getColoredName() {
        return ChatColor.translateAlternateColorCodes('&', getName());
    }

    /**
     * @return the name of this profession
     */
    public String getName() {
        return this.name;
    }

    /**
     * Compares the profession types and then the names
     *
     * @param o the profession to compare
     *
     * @return {@inheritDoc}
     */
    @Override
    public int compareTo(Profession o) {
        int compare = getProfessionType().compareTo(o.getProfessionType());
        if (compare == 0) {
            return ChatColor.stripColor(getColoredName()).compareTo(ChatColor.stripColor(o.getColoredName()));
        }
        return compare;
    }

    /**
     * Called after being loaded from a file
     */
    public void onLoad() {
    }

    /**
     * Called when a user levels up
     */
    public void onLevelUp(UserProfessionData upd) {

    }

    /**
     * @return the required plugin IDs
     */
    public final Set<String> getRequiredPlugins() {
        return requiredPlugins;
    }

    /**
     * Adds a plugin requirement
     *
     * @param plugin the plugin ID
     */
    protected void addRequiredPlugin(String plugin) {
        requiredPlugins.add(plugin);
    }

    /**
     * Handles the called profession event from {@link ProfessionListener} <br> Cancels the event if the player does not
     * have this profession and is a rank lower than builder <br> If the player has this profession and the profession
     * event is correct, {@link #onEvent(ProfessionEventWrapper)} is called
     *
     * @param event   the profession event
     * @param <IType> the item type argument of the event (this prevents wildcards)
     */
    @EventHandler
    public final <IType extends ItemType<?>> void handleEvent(ProfessionEvent<IType> event) {

        // the player has no profession but has a rank builder+ -> do not cancel the event
        if (!utils.playerHasProfession(event)) {

            // cancels the event if the player is a rank lower than builder
            event.setCancelled(!Permissions.has(event.getPlayer().getPlayer(), Permissions.BUILDER));
            return;
        }
        for (ItemTypeHolder<?, ?> ith : items) {
            for (ItemType<?> it : ith) {
                if (it.getClass().equals(event.getItemType().getClass())) {
                    onEvent(new ProfessionEventWrapper<>(event));
                    return;
                }
            }
        }
    }

    @Nullable
    public List<String> getProfessionInformation(UserProfessionData upd) {
        return null;
    }

    /**
     * @return a collection of subprofessions of this profession
     */
    public Collection<Class<? extends Profession>> getSubprofessions() {
        return null;
    }

    /**
     * The profession types
     */
    public enum ProfessionType implements ISetup {
        PRIMARY("Primary"),
        SECONDARY("Secondary");

        private String name;

        ProfessionType(String name) {
            this.name = name;
        }

        /**
         * @param professionType the string
         *
         * @return the converted profession type from a string
         */
        public static ProfessionType fromString(String professionType) {
            for (ProfessionType type : values()) {
                if (type.name.equalsIgnoreCase(professionType) || type.name().equalsIgnoreCase(professionType)) {
                    return type;
                }
            }
            String sb = Arrays.stream(values())
                    .map(type -> type.ordinal() + "=" + type)
                    .collect(Collectors.joining("", professionType + " is not a valid profession type! (", ")"));
            throw new IllegalArgumentException(sb);
        }

        /**
         * @param id the id
         *
         * @return the converted profession type from an id based on ordinal() method
         */
        public static ProfessionType fromId(int id) {
            for (ProfessionType type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            String sb = Arrays.stream(values())
                    .map(type -> type.ordinal() + "=" + type)
                    .collect(Collectors.joining("", id + " is not a valid profession id type! (", ")"));
            throw new IllegalArgumentException(sb);
        }

        @Override
        public String toString() {
            return String.valueOf(name.toCharArray()[0]).toUpperCase() + name.toLowerCase().substring(1);
        }

        @Override
        public void setup() {
            PRIMARY.name = new Messages.MessageBuilder(Messages.Global.PROFTYPE_PRIMARY).build();
            SECONDARY.name = new Messages.MessageBuilder(Messages.Global.PROFTYPE_SECONDARY).build();
        }
    }
}
