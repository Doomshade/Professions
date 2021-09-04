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

package git.doomshade.professions.api.profession;

import git.doomshade.professions.api.item.ItemTypeHolder;
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.data.ProfessionSettingsManager;
import git.doomshade.professions.data.ProfessionSpecificDefaultsSettings;
import git.doomshade.professions.data.Settings;
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
import java.util.logging.Level;
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
    final Set<ItemTypeHolder<?, ?>> items = new HashSet<>();
    private final Set<String> requiredPlugins = new HashSet<>();
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
        ProfessionSettingsManager settings = Settings.getProfessionSettingsManager(this);
        try {
            settings.setup();
        } catch (Exception e) {
            ProfessionLogger.logError(e);
        }
        this.professionSettings = settings;

        ProfessionSpecificDefaultsSettings defaults = settings.getDefaultsSettings();
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

    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public final Iterable<ItemTypeHolder<?, ?>> getItems() {
        return items;
    }

    @Override
    public ProfessionSettingsManager getProfessionSettings() {
        return professionSettings;
    }

    @Override
    public final String toString() {
        String type = getProfessionType() != null ? getProfessionType().toString() + ", " : "";
        return getColoredName() + ChatColor.RESET + ", " + type;
    }

    @Override
    public ProfessionType getProfessionType() {
        return pt;
    }

    @Override
    public final String getColoredName() {
        return ChatColor.translateAlternateColorCodes('&', getName());
    }

    @Override
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

    @Override
    public void onLoad() {
    }

    @Override
    public void onLevelUp(UserProfessionData upd) {

    }

    @Override
    public final Collection<String> getRequiredPlugins() {
        return Collections.unmodifiableCollection(requiredPlugins);
    }

    @Override
    public final void addRequiredPlugin(String plugin) {
        requiredPlugins.add(plugin);
    }

    @Override
    @EventHandler
    public final <IType extends ItemType<?>> void handleEvent(ProfessionEvent<IType> event) {
        final Class<? extends IProfession>[] profs = event.getProfessions();

        if (profs == null || profs.length == 0) {
            ProfessionLogger.logError(new IllegalStateException(
                    String.format("Received empty profession list for %s event", event.getItemType().getName())), true);
            return;
        }

        // check whether the event was called for this profession
        if (Arrays.stream(profs).noneMatch(prof -> prof.equals(getClass()))){
            return;
        }
        // the player has no profession but has a rank builder+ -> do not cancel the event
        if (!utils.playerHasProfession(event)) {

            // cancels the event if the player is a rank lower than builder
            final boolean b = !Permissions.has(event.getPlayer().getPlayer(), Permissions.BUILDER);
            ProfessionLogger.log(String.format("Setting cancelled of event %s to %s",
                    event.getItemType().getName(), b), Level.FINEST);
            event.setCancelled(b);
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

    @Override
    @Nullable
    public List<String> getProfessionInformation(UserProfessionData upd) {
        return null;
    }

    @Override
    public Collection<Class<? extends Profession>> getSubprofessions() {
        return null;
    }

}
