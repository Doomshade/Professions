package git.doomshade.professions.task;

import git.doomshade.guiapi.CraftingItem;
import git.doomshade.guiapi.GUI;
import git.doomshade.professions.Profession;
import git.doomshade.professions.Professions;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.event.EventManager;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.profession.professions.EnchantingProfession;
import git.doomshade.professions.profession.types.ICraftable;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.profession.types.enchanting.EnchantManager;
import git.doomshade.professions.profession.types.enchanting.EnchantedItemType;
import git.doomshade.professions.profession.types.enchanting.PreEnchantedItem;
import git.doomshade.professions.profession.types.enchanting.enchants.RandomAttributeEnchant;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

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

    @Override
    public void run() {
        if (user.getPlayer().getInventory().firstEmpty() == -1) {
            user.sendMessage(new Messages.MessageBuilder(Messages.Message.NO_INVENTORY_SPACE)
                    .setPlayer(user).setProfession(upd.getProfession())
                    .setProfessionType(upd.getProfession().getProfessionType())
                    .build());
            cancel();
            return;
        }
        EventManager em = EventManager.getInstance();
        for (ItemTypeHolder<?> entry : prof.getItems()) {
            for (ItemType<?> item : entry) {
                if (!(item instanceof ICraftable)) {
                    continue;
                }
                final ICraftable craftable = (ICraftable) item;
                if (!item.getIcon(upd).isSimilar(currentItem)) {
                    continue;
                }
                if (!craftable.meetsCraftingRequirements(user.getPlayer())) {
                    user.sendMessage(new Messages.MessageBuilder(Messages.Message.REQUIREMENTS_NOT_MET)
                            .setPlayer(user)
                            .setProfession(prof)
                            .build());
                    return;
                }

                final EnchantedItemType eit = em.getItemType(
                        EnchantManager.getInstance().getEnchant(RandomAttributeEnchant.class), EnchantedItemType.class);
                final ProfessionEvent<EnchantedItemType> pe = em.getEvent(eit, user);
                if (!item.meetsLevelReq(upd.getLevel())) {
                    pe.printErrorMessage(upd);
                    return;
                }

                final CraftingItem craftingItem = new CraftingItem(currentItem, slot);

                assert eit != null;
                pe.addExtra(new PreEnchantedItem(eit.getObject(), currentItem));
                pe.addExtra(EnchantingProfession.ProfessionEventType.CRAFT);

                craftingItem.setEvent(CraftingItem.GUIEventType.CRAFTING_END_EVENT, arg0 -> {
                    craftable.removeCraftingRequirements(user.getPlayer());
                    user.getPlayer().getInventory().addItem(craftable.getResult());
                    em.callEvent(pe);

                    // TODO fix repeat amount
                    if (repeat && repeatAmount != 0) {
                        CraftingTask clone = clone();
                        clone.setRepeatAmount(repeatAmount - 1);
                        clone.runTask(Professions.getInstance());
                    }
                });
                craftingItem.addProgress(craftingItem.new Progress(Professions.getInstance(),
                        craftable.getCraftingTime(), gui, UPDATE_INTERVAL));
                return;
            }
        }
    }

    @Override
    protected CraftingTask clone() {
        CraftingTask task = new CraftingTask(upd, currentItem, slot, gui);
        task.setRepeat(repeat);
        task.setRepeatAmount(repeatAmount);
        return task;
    }
}
