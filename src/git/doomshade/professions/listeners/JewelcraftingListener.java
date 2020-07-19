package git.doomshade.professions.listeners;

import git.doomshade.professions.Professions;
import git.doomshade.professions.profession.professions.jewelcrafting.Gem;
import git.doomshade.professions.utils.GetSet;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class JewelcraftingListener implements Listener {

    private static final HashMap<UUID, Gem> RIGHT_CLICK = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            final Player player = event.getPlayer();
            final PlayerInventory inventory = player.getInventory();
            final ItemStack hand = inventory.getItemInMainHand();
            final Optional<Gem> opt = Gem.getGem(hand);
            final UUID uniqueId = player.getUniqueId();

            // right clicked with a gem in hand
            if (opt.isPresent()) {
                if (RIGHT_CLICK.containsKey(uniqueId)) {
                    return;
                }
                player.sendMessage("Nyní klikni na předmět s místem pro klenot");
                RIGHT_CLICK.put(uniqueId, opt.get());
            }

            // did not, check if player clicked before
            else {
                final Gem gem = RIGHT_CLICK.remove(uniqueId);

                // did not
                if (gem == null) {
                    return;
                }

                // did but no item in hand afterwards
                if (hand == null) {
                    player.sendMessage("Musíš mít v ruce nějaký předmět s místem pro klenot");
                    return;
                }

                final ItemStack itemStack = new ItemStack(Material.BARRIER);


                // clicked a gem before and has an item in hand -> try to insert
                GetSet<ItemStack> gs = new GetSet<>(hand);
                Gem.InsertResult result = gem.insert(gs);


                // TODO messages
                switch (result) {
                    case INVALID_ITEM:
                        player.sendMessage("Neplatný předmět");
                        break;
                    case NO_GEM_SPACE:
                        player.sendMessage("V předmětu není místo pro klenot");
                        break;
                    case SUCCESS:

                        ItemStack[] armorContents = inventory.getArmorContents();
                        ItemStack[] copy = Arrays.copyOf(armorContents, armorContents.length);
                        for (int i = 0; i < copy.length; i++) {
                            ItemStack item = copy[i];
                            if (item == null) {
                                copy[i] = itemStack;
                            }
                        }
                        inventory.setArmorContents(copy);
                        inventory.removeItem(gem.getGem());
                        inventory.setItemInMainHand(gs.get());
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                inventory.setArmorContents(armorContents);
                            }
                        }.runTaskLater(Professions.getInstance(), 1);
                        player.sendMessage("Přidán gem do předmětu");
                        break;
                }

            }
        }
    }
}
