package git.doomshade.professions.task;

import git.doomshade.guiapi.CraftingItem;
import git.doomshade.guiapi.GUI;
import git.doomshade.professions.Profession;
import git.doomshade.professions.Professions;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.event.EventManager;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.profession.ICraftable;
import git.doomshade.professions.profession.professions.enchanting.EnchantingProfession;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
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
    private final Profession<?> prof;
    private final int slot;
    private final GUI gui;
    private ItemStack currentItem;
    private boolean repeat = false;
    private int repeatAmount;

    public CraftingTask(UserProfessionData upd, ItemStack currentItem, int slot, GUI gui) {
        this.upd = upd;
        this.currentItem = currentItem;
        this.slot = slot;
        this.gui = gui;
        this.user = upd.getUser();
        this.prof = upd.getProfession();
        this.repeatAmount = -1;
    }

    public void setCurrentItem(ItemStack currentItem) {
        this.currentItem = currentItem;
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
        for (ItemTypeHolder<?> entry : prof.getItems()) {
            for (ItemType<?> item : entry) {
                if (!(item instanceof ICraftable)) {
                    continue;
                }
                final ICraftable craftable = (ICraftable) item;
                if (!item.getIcon(upd).isSimilar(currentItem)) {
                    continue;
                }
                return craftable;
            }
        }
        return null;
    }

    private boolean hasInventorySpace() {
        if (user.getPlayer().getInventory().firstEmpty() == -1) {
            user.sendMessage(new Messages.MessageBuilder(Messages.Message.NO_INVENTORY_SPACE)
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
        final ICraftable craftable = getCraftable();
        if (craftable == null) {
            return;
        }

        if (!hasInventorySpace()) {
            return;
        }
        EventManager em = EventManager.getInstance();

        final ItemType<?> item = (ItemType<?>) craftable;
        if (!item.getIcon(upd).isSimilar(currentItem)) {
            return;
        }
        final Player player = user.getPlayer();
        if (!craftable.meetsCraftingRequirements(player)) {
            user.sendMessage(new Messages.MessageBuilder(Messages.Message.REQUIREMENTS_NOT_MET)
                    .setPlayer(user)
                    .setProfession(prof)
                    .build());
            return;
        }

        //final EnchantedItemType eit = em.getItemType(EnchantManager.getInstance().getEnchant(RandomAttributeEnchant.class), EnchantedItemType.class);
        final ProfessionEvent<ItemType<?>> pe = em.getEvent(item, user);
        if (!item.meetsLevelReq(upd.getLevel())) {
            pe.printErrorMessage(upd);
            return;
        }

        final CraftingItem craftingItem = new CraftingItem(currentItem, slot);

        Function<ItemStack, ?> func = craftable.getExtra();
        if (func != null)
            pe.addExtra(func.apply(currentItem));
        pe.addExtra(EnchantingProfession.ProfessionEventType.CRAFT);

        craftingItem.setEvent(CraftingItem.GUIEventType.CRAFTING_END_EVENT, (CraftingItem.Progress x) -> {
            if (!hasInventorySpace()) {
                return;
            }
            final ProfessionEvent<ItemType<?>> event = em.callEvent(pe);
            if (!event.isCancelled()) {
                craftable.removeCraftingRequirements(player);
                player.getInventory().addItem(craftable.getResult());
            }
            player.getWorld().playSound(player.getLocation(), craftable.getSounds().get(ICraftable.Sound.ON_CRAFT), 1, 1);

            if (repeat && repeatAmount != 0) {
                CraftingTask clone = clone();
                clone.setRepeatAmount(repeatAmount - 1);
                clone.runTask(Professions.getInstance());
            }
        });
        craftingItem.addProgress(craftingItem.new Progress(Professions.getInstance(),
                craftable.getCraftingTime(), gui, UPDATE_INTERVAL));

        player.getWorld().playSound(player.getLocation(), craftable.getSounds().get(ICraftable.Sound.CRAFTING), 1, 1);

    }

    @Override
    protected CraftingTask clone() {
        CraftingTask task = new CraftingTask(upd, currentItem, slot, gui);
        task.setRepeat(repeat);
        task.setRepeatAmount(repeatAmount);
        return task;
    }
}
