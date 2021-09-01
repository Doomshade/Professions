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

package git.doomshade.professions.gui.player;

import git.doomshade.guiapi.GUI;
import git.doomshade.guiapi.GUIClickEvent;
import git.doomshade.guiapi.GUIInventory.Builder;
import git.doomshade.guiapi.GUIItem;
import git.doomshade.guiapi.GUIManager;
import git.doomshade.professions.Professions;
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.item.ext.ItemType;
import git.doomshade.professions.api.item.ItemTypeHolder;
import git.doomshade.professions.data.GUISettings;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.listeners.PluginProfessionListener;
import git.doomshade.professions.task.CraftingTask;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class ProfessionGUI extends GUI {
    static final String POSITION_GUI = "position";
    private final int levelThreshold;
    private Profession prof;

    protected ProfessionGUI(Player guiHolder, GUIManager manager) {
        super(guiHolder, manager);
        levelThreshold = Settings.getSettings(GUISettings.class).getLevelThreshold();
    }

    @Override
    public void init() {
        this.prof = getContext().getContext(PlayerProfessionsGUI.ID_PROFESSION);
        Builder builder = getInventoryBuilder().size(9).title(prof.getColoredName());

        int pos = 0;
        User user = User.getUser(getHolder());
        UserProfessionData upd = user.getProfessionData(prof);
        List<String> lore = prof.getProfessionInformation(upd);
        final boolean profHasLore = lore != null && !lore.isEmpty();

        GUISettings settings = Settings.getSettings(GUISettings.class);

        String signName = settings.getSignName();

        for (ItemTypeHolder<?, ?> entry : prof.getItems()) {
            for (ItemType<?> item : entry) {
                if (pos == 5 && profHasLore) {
                    GUIItem infoItem = new GUIItem(Material.OAK_SIGN, pos, 1, (short) 0);
                    final ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(Material.OAK_SIGN);

                    Objects.requireNonNull(itemMeta).setDisplayName(signName);
                    itemMeta.setLore(lore);
                    infoItem.changeItem(this, () -> itemMeta);
                    builder = builder.withItem(infoItem);
                    pos++;
                    continue;
                }
                ItemStack icon = item.getIcon(upd);
                GUIItem guiItem = new GUIItem(icon.getType(), pos, icon.getAmount(), icon.getDurability());
                boolean hasRecipe = upd.hasTrained(item);
                boolean meetsLevel = item.meetsLevelReq(upd.getLevel() + levelThreshold);

                //if (item.isHiddenWhenUnavailable())
                guiItem.setHidden(!(hasRecipe && meetsLevel));
                /*else if (hasRecipe)
                    guiItem.setHidden(false);
                else
                    guiItem.setHidden(!meetsLevel);*/
                guiItem.changeItem(this, icon::getItemMeta);

                if (!guiItem.isHidden()) {
                    pos++;
                    builder = builder.withItem(guiItem);
                }
            }
        }
        setInventory(builder.build());
        setNextGui(TestThreeGui.class, Professions.getGUIManager());

    }

    @Override
    public void onGuiClick(GUIClickEvent e) {
        InventoryClickEvent event = e.getEvent();
        event.setCancelled(true);
        int slot = event.getSlot();
        ItemStack currentItem = event.getCurrentItem();
        User user = User.getUser(getHolder());
        UserProfessionData upd = user.getProfessionData(prof);
        if (currentItem == null || currentItem.getType() == Material.AIR) {
            return;
        }

        // the task handles the ICraftable's
        final CraftingTask task = new CraftingTask(upd, currentItem, slot, this);
        if (task.getCraftable() == null) {
            return;
        }
        final Professions plugin = Professions.getInstance();
        switch (event.getClick()) {
            case LEFT:
                task.setRepeat(false);
                task.runTask(plugin);
                break;
            case SHIFT_LEFT:
                task.setRepeat(true);
                task.runTask(plugin);
                break;
            case RIGHT:
                final Player player = user.getPlayer();
                player.closeInventory();
                final UUID uniqueId = player.getUniqueId();
                PluginProfessionListener.PENDING_REPEAT_AMOUNT.put(uniqueId, task);
                user.sendMessage(new Messages.MessageBuilder(Messages.Global.REPEAT_AMOUNT).player(user)
                        .profession(upd.getProfession())
                        .build());

                // cancel the task after one minute
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        PluginProfessionListener.PENDING_REPEAT_AMOUNT.remove(uniqueId);
                    }
                }.runTaskLater(plugin, 60 * 20L);
                break;
            case SHIFT_RIGHT:
                task.setRepeat(false);
                task.setRepeatAmount(64);
                task.runTask(plugin);
                break;
            default:
                break;
        }


    }


}
