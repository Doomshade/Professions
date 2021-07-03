package git.doomshade.professions.task;

import git.doomshade.guiapi.CraftingItem;
import git.doomshade.guiapi.GUI;
import git.doomshade.professions.Professions;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.event.EventManager;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.api.types.ICraftable;
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.types.CraftableItemType;
import git.doomshade.professions.api.types.ItemType;
import git.doomshade.professions.api.types.ItemTypeHolder;
import git.doomshade.professions.api.user.User;
import git.doomshade.professions.api.user.UserProfessionData;
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

        if (update)
            updateCraftable();
    }

    private void setCraftable(CraftableItemType<?> craftable) {
        this.item = craftable;
    }

    private void updateCraftable() {
        this.item = null;
        for (ItemTypeHolder<?> entry : prof.getItems()) {
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
                    .setPlayer(user).setProfession(upd.getProfession())
                    .setProfessionType(upd.getProfession().getProfessionType())
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
            Professions.log("A");
            return;
        }

        // player has no inventory space
        if (!hasInventorySpace()) {
            Professions.log("B");
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
                    .setPlayer(user)
                    .setProfession(prof)
                    .build());
            return;
        }

        //final EnchantedItemType eit = em.getItemType(EnchantManager.getInstance().getEnchant(RandomAttributeEnchant.class), EnchantedItemType.class);


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
                Professions.log("Running anoda one");
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
