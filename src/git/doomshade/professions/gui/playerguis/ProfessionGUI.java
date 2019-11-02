package git.doomshade.professions.gui.playerguis;

import git.doomshade.guiapi.*;
import git.doomshade.guiapi.GUIInventory.Builder;
import git.doomshade.professions.Profession;
import git.doomshade.professions.Professions;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.listeners.PluginProfessionListener;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.profession.types.crafting.ICrafting;
import git.doomshade.professions.task.CraftingTask;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class ProfessionGUI extends GUI {
    static final String POSITION_GUI = "position";
    private final int levelThreshold;
    private Profession<?> prof;

    protected ProfessionGUI(Player guiHolder, GUIManager manager) {
        super(guiHolder, manager);
        levelThreshold = Settings.getInstance().getProfessionSettings().getLevelThreshold();
    }

    @Override
    public void init() throws GUIInitializationException {
        this.prof = (Profession<?>) getContext().getContext(PlayerProfessionsGUI.ID_PROFESSION);
        Builder builder = getInventoryBuilder().size(9).title(prof.getColoredName());

        int pos = 0;
        User user = User.getUser(getHolder());
        UserProfessionData upd = user.getProfessionData(prof);
        for (ItemTypeHolder<?> entry : prof.getItems()) {
            for (ItemType<?> item : entry) {
                ItemStack icon = item.getIcon(upd);
                GUIItem guiItem = new GUIItem(icon.getType(), pos);
                boolean hasRecipe = upd.hasExtra(icon.getItemMeta().getDisplayName());
                boolean meetsLevel = item.meetsLevelReq(upd.getLevel() + levelThreshold);
                if (item.isHiddenWhenUnavailable())
                    guiItem.setHidden(!(hasRecipe && meetsLevel));
                else if (hasRecipe)
                    guiItem.setHidden(false);
                else
                    guiItem.setHidden(!meetsLevel);
                guiItem.changeItem(this, icon::getItemMeta);

                if (!guiItem.isHidden()) {
                    pos++;
                    builder = builder.withItem(guiItem);
                }
            }
        }
        setInventory(builder.build());
        setNextGui(TestThreeGui.class, Professions.getManager());

    }

    @Override
    public void onGuiClick(GUIClickEvent e) {
        InventoryClickEvent event = e.getEvent();
        event.setCancelled(true);
        int slot = event.getSlot();
        ItemStack currentItem = event.getCurrentItem();
        User user = User.getUser(getHolder());
        UserProfessionData upd = user.getProfessionData(prof);
        if (currentItem == null || currentItem.getType() == Material.AIR
                || !(prof.getType() instanceof ICrafting || prof instanceof ICrafting)) {
            return;
        }
        final CraftingTask task = new CraftingTask(upd, currentItem, slot, this);
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
                user.sendMessage(Messages.getInstance().MessageBuilder().setMessage(Messages.Message.REPEAT_AMOUNT).setPlayer(user).setProfession(upd.getProfession()).build());

                // cancel the task after one minute
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        PluginProfessionListener.PENDING_REPEAT_AMOUNT.remove(uniqueId);
                    }
                }.runTaskLater(plugin, 60 * 20L);
                break;
            case SHIFT_RIGHT:
                task.setRepeat(true);
                task.setRepeatAmount(64);
                task.runTask(plugin);
                break;
            default:
                break;
        }


    }


}
