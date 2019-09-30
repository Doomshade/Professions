package git.doomshade.professions.listeners;

import git.doomshade.professions.Professions;
import git.doomshade.professions.utils.ItemUtils;
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

public class UserListener implements Listener {
    private static ItemUtils utils = ItemUtils.getInstance();
    public static final ItemStack ZPET_BUTTON = utils.itemStackBuilder(Material.BOOK).withDisplayName(ChatColor.RED + "Zpet").withLore(new ArrayList<String>()).build();
    public static final ItemStack PREVIOUS_PAGE = utils.itemStackBuilder(Material.BOOK).withDisplayName("Predesla stranka").withLore(new ArrayList<String>()).build();
    public static final ItemStack NEXT_PAGE = utils.itemStackBuilder(Material.BOOK).withDisplayName("Dalsi stranka").withLore(new ArrayList<String>()).build();

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
            invContents = new ArrayList<ItemStack>(overLapping);
            inventoriez.put(i, inventory);
        }
        return inventoriez;
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        try {
            Professions.unloadUser(Professions.getUser(e.getPlayer()));
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Professions.loadUser(e.getPlayer());
    }
}
