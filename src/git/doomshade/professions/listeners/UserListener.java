package git.doomshade.professions.listeners;

import git.doomshade.professions.Professions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static git.doomshade.professions.utils.ItemUtils.itemStackBuilder;

public class UserListener implements Listener {
    private static final ItemStack ZPET_BUTTON = itemStackBuilder(Material.BOOK).withDisplayName(ChatColor.RED + "Zpet").withLore(new ArrayList<>()).build();
    private static final ItemStack PREVIOUS_PAGE = itemStackBuilder(Material.BOOK).withDisplayName("Predesla stranka").withLore(new ArrayList<>()).build();
    private static final ItemStack NEXT_PAGE = itemStackBuilder(Material.BOOK).withDisplayName("Dalsi stranka").withLore(new ArrayList<>()).build();

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
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        try {
            Professions.unloadUser(Professions.getUser(e.getPlayer()));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        try {
            Professions.loadUser(e.getPlayer());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
