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

package git.doomshade.professions.listeners;

import git.doomshade.guiapi.GUI;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class UserListener implements Listener {
    /*private static final ItemStack ZPET_BUTTON = itemStackBuilder(Material.BOOK).withDisplayName(ChatColor.RED +
    "Zpet").withLore(new ArrayList<>()).build();
    private static final ItemStack PREVIOUS_PAGE = itemStackBuilder(Material.BOOK).withDisplayName("Predesla
    stranka").withLore(new ArrayList<>()).build();
    private static final ItemStack NEXT_PAGE = itemStackBuilder(Material.BOOK).withDisplayName("Dalsi stranka")
    .withLore(new ArrayList<>()).build();

    public static LinkedHashMap<Integer, Inventory> getGuiWithPages(List<ItemStack> invContents, String invName) {
        return getGuiWithPages(invContents, invName, true);
    }

    public static LinkedHashMap<Integer, Inventory> getGuiWithPages(List<ItemStack> invContents, String invName,
                                                                    boolean addZpetButton) {
        ArrayList<ItemStack> overLapping;
        int size = 9 - invContents.size() % 9 + invContents.size();
        int invAmount = 1;
        while (size > 51) {
            size -= 54;
            invAmount++;
        }
        LinkedHashMap<Integer, Inventory> inventoriez = new LinkedHashMap<>();
        for (int i = 0; i < invAmount; i++) {
            overLapping = new ArrayList<>();
            Inventory inventory = Bukkit.createInventory(null,
                    i == invAmount - 1 ? invContents.size() + 3 + (9 - (invContents.size() + 3) % 9) : 54, invName);
            if (invAmount != 1) {
                int prevPageIndex = inventory.getSize() - 3;
                int nextPageIndex = inventory.getSize() - 2;
                inventory.setItem(prevPageIndex, PREVIOUS_PAGE);
                inventory.setItem(nextPageIndex, NEXT_PAGE);
            }
            if (addZpetButton) {
                int zpetButtonIndex = inventory.getSize() - 1;
                inventory.setItem(zpetButtonIndex, ZPET_BUTTON);
            }
            for (ItemStack itemStack : invContents) {
                if (inventory.firstEmpty() != -1) {
                    inventory.addItem(itemStack);
                    continue;
                }
                overLapping.add(itemStack);
            }
            invContents = new ArrayList<>(overLapping);
            inventoriez.put(i, inventory);
        }
        return inventoriez;
    }*/

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        try {
            User.unloadUser(User.getUser(e.getPlayer()));
        } catch (IOException e1) {
            ProfessionLogger.logError(e1);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        final Player player = e.getPlayer();
        try {
            User.loadUser(player);
        } catch (IOException ex) {
            ProfessionLogger.logError(ex);
        }

        //
        // TODO
        // ((CraftPlayer) player).getHandle().addEffect(new MobEffect(MobEffects.FASTER_DIG, 60, 0, false, false));

    }

    private static final HashMap<UUID, HashMap<ValidInputType, GUI>> PLAYER_INPUT = new HashMap<>();

    public static void askUser(Player user, String message, ValidInputType type, GUI gui) {
        user.closeInventory();
        PLAYER_INPUT.put(user.getUniqueId(), new HashMap<>() {
            {
                put(type, gui);
            }
        });
        user.sendMessage(message);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        HashMap<ValidInputType, GUI> onIn;
        if ((onIn = PLAYER_INPUT.remove(e.getPlayer().getUniqueId())) != null) {
            GUI gui = onIn.values().iterator().next();
            gui.onCustomEvent(e.getMessage());
        }
    }

    public enum ValidInputType {
        STRING,
        INTEGER,
        DOUBLE,
        ITEMSTACK,
        BOOLEAN,
        MATERIAL,
        LIST
    }
}
