package git.doomshade.professions.listeners;

import git.doomshade.guiapi.GUI;
import git.doomshade.professions.Professions;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class UserListener implements Listener {
    /*private static final ItemStack ZPET_BUTTON = itemStackBuilder(Material.BOOK).withDisplayName(ChatColor.RED + "Zpet").withLore(new ArrayList<>()).build();
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
    }*/

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
        final Player player = e.getPlayer();
        try {
            Professions.loadUser(player);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        //
        // TODO
        // ((CraftPlayer) player).getHandle().addEffect(new MobEffect(MobEffects.FASTER_DIG, 60, 0, false, false));

    }

    private static final HashMap<UUID, HashMap<ValidInputType, GUI>> PLAYER_INPUT = new HashMap<>();

    public static void askUser(Player user, String message, ValidInputType type, GUI gui) {
        user.closeInventory();
        PLAYER_INPUT.put(user.getUniqueId(), new HashMap<ValidInputType, GUI>() {
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
        STRING, INTEGER, DOUBLE, ITEMSTACK, BOOLEAN, MATERIAL, LIST
    }
}
