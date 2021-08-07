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

package git.doomshade.professions.task;

import git.doomshade.guiapi.CraftingItem;
import git.doomshade.guiapi.GUI;
import git.doomshade.professions.Professions;
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.item.ext.CraftableItemType;
import git.doomshade.professions.api.item.ICraftable;
import git.doomshade.professions.api.item.ext.ItemType;
import git.doomshade.professions.api.item.ItemTypeHolder;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.event.EventManager;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Function;

/**
 * Task for crafting items.
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class CraftingTask extends BukkitRunnable implements Cloneable {
    private static final int UPDATE_INTERVAL = 1;
    private final UserProfessionData upd;
    private final User user;
    private final Profession prof;
    private final int slot;
    private final GUI gui;
    private ItemStack currentItem;
    private boolean repeat = false;
    private int repeatAmount;
    private CraftableItemType<?> item;

    public CraftingTask(UserProfessionData upd, ItemStack currentItem, int slot, GUI gui) {
        this(upd, currentItem, slot, gui, true);
    }

    private CraftingTask(UserProfessionData upd, ItemStack currentItem, int slot, GUI gui, boolean update) {
        this.upd = upd;
        this.slot = slot;
        this.gui = gui;
        this.user = upd.getUser();
        this.prof = upd.getProfession();
        this.repeatAmount = -1;
        this.setCurrentItem(currentItem, update);
    }

    public void setCurrentItem(ItemStack currentItem) {
        setCurrentItem(currentItem, true);
    }

    private void setCurrentItem(ItemStack currentItem, boolean update) {
        this.currentItem = currentItem;

        if (update) {
            updateCraftable();
        }
    }

    private void setCraftable(CraftableItemType<?> craftable) {
        this.item = craftable;
    }

    private void updateCraftable() {
        this.item = null;
        for (ItemTypeHolder<?, ?> entry : prof.getItems()) {
            for (ItemType<?> item : entry) {
                if (!(item instanceof CraftableItemType)) {
                    continue;
                }
                final CraftableItemType<?> craftable = (CraftableItemType<?>) item;
                if (item.getIcon(upd).isSimilar(currentItem)) {
                    this.item = craftable;
                    break;
                }

            }
        }
    }

    public int getSlot() {
        return slot;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public void setRepeatAmount(int amount) {
        this.repeatAmount = amount;
    }

    public GUI getGui() {
        return gui;
    }

    public UserProfessionData getUpd() {
        return upd;
    }

    @Nullable
    public ICraftable getCraftable() {
        return item;
    }

    private boolean hasInventorySpace() {
        if (user.getPlayer().getInventory().firstEmpty() == -1) {
            user.sendMessage(new Messages.MessageBuilder(Messages.Global.NO_INVENTORY_SPACE)
                    .player(user).profession(upd.getProfession())
                    .professionType(upd.getProfession().getProfessionType())
                    .build());
            cancel();
            return false;
        }
        return true;
    }

    @Override
    public void run() {

        // item somehow no longer available
        if (item == null) {
            ProfessionLogger.log("A");
            return;
        }

        // player has no inventory space
        if (!hasInventorySpace()) {
            ProfessionLogger.log("B");
            return;
        }

        EventManager em = EventManager.getInstance();
        final Player player = user.getPlayer();
        final ProfessionEvent<ItemType<?>> pe = em.getEvent(item, user);

        // now check for level
        if (!item.meetsLevelReq(upd.getLevel())) {
            pe.printErrorMessage(upd);
            return;
        }


        // item available and has space -> check for requirements
        if (!item.meetsRequirements(player)) {
            user.sendMessage(new Messages.MessageBuilder(Messages.Global.REQUIREMENTS_NOT_MET)
                    .player(user)
                    .profession(prof)
                    .build());
            return;
        }

        //final EnchantedItemType eit = em.getItemType(EnchantManager.getInstance().getEnchant(RandomAttributeEnchant
        // .class), EnchantedItemType.class);


        // everything seems valid, start the crafting process
        final CraftingItem craftingItem = new CraftingItem(currentItem, slot);

        // the ItemType could require some extra function during crafting
        Function<ItemStack, ?> func = item.getExtraInEvent();
        if (func != null) {
            final Object appliedFunc = func.apply(currentItem);
            if (appliedFunc instanceof Collection) {
                pe.addExtras((Collection<?>) appliedFunc);
            } else {
                pe.addExtra(appliedFunc);
            }
        }


        // CraftingEvent is a functional interface with a CraftingItem.Progress class as an argument
        craftingItem.setEvent(CraftingItem.GUIEventType.CRAFTING_END_EVENT, x -> {
            if (!hasInventorySpace()) {
                return;
            }
            final ProfessionEvent<ItemType<?>> event = em.callEvent(pe);
            if (event.isCancelled()) {
                return;
            }
            item.consumeCraftingRequirements(player);
            player.getInventory().addItem(item.getResult());

            player.getWorld().playSound(player.getLocation(), item.getSounds().get(ICraftable.Sound.ON_CRAFT), 1, 1);

            if (repeat || repeatAmount > 0) {
                ProfessionLogger.log("Running anoda one");
                CraftingTask newTask = new CraftingTask(upd, currentItem, slot, gui, false);
                newTask.setRepeat(repeat);
                newTask.setRepeatAmount(repeatAmount - 1);
                newTask.setCraftable(item);
                newTask.runTask(Professions.getInstance());
            }
        });
        craftingItem.addProgress(craftingItem.new Progress(Professions.getInstance(),
                item.getCraftingTime(), gui, UPDATE_INTERVAL));

        player.getWorld().playSound(player.getLocation(), item.getSounds().get(ICraftable.Sound.CRAFTING), 1, 1);

    }
}
