package git.doomshade.professions.gui.playerguis;

import git.doomshade.guiapi.*;
import git.doomshade.guiapi.CraftingItem.GUIEventType;
import git.doomshade.guiapi.CraftingItem.Progress;
import git.doomshade.guiapi.GUIInventory.Builder;
import git.doomshade.guiapi.event.CraftingEvent;
import git.doomshade.professions.Profession;
import git.doomshade.professions.Professions;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.event.EventManager;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.profession.professions.EnchantingProfession.ProfessionEventType;
import git.doomshade.professions.profession.types.CraftableItemType;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.profession.types.crafting.ICrafting;
import git.doomshade.professions.profession.types.enchanting.EnchantManager;
import git.doomshade.professions.profession.types.enchanting.EnchantedItemType;
import git.doomshade.professions.profession.types.enchanting.PreEnchantedItem;
import git.doomshade.professions.profession.types.enchanting.enchants.RandomAttributeEnchant;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map.Entry;

public class ProfessionGUI extends GUI {
    static final String POSITION_GUI = "position";
    private static final int INTERVAL = 1;
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
                guiItem.changeItem(this, () -> icon.getItemMeta());

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
        for (ItemTypeHolder<?> entry : prof.getItems()) {
            for (ItemType<?> item : entry) {
                if (!(item instanceof CraftableItemType)) {
                    continue;
                }
                CraftableItemType<?> craftable = (CraftableItemType<?>) item;
                if (!craftable.getIcon(upd).isSimilar(currentItem)) {
                    continue;
                }
                if (!craftable.meetsCraftingRequirements(getHolder())) {
                    getHolder().sendMessage("Nemáš requirementy boi");
                    return;
                }
                EventManager em = EventManager.getInstance();
                EnchantedItemType eit = em.getItemType(
                        EnchantManager.getInstance().getEnchant(RandomAttributeEnchant.class), EnchantedItemType.class);
                ProfessionEvent<EnchantedItemType> pe = em.getEvent(eit, user);
                if (!craftable.meetsLevelReq(user.getProfessionData(prof).getLevel())) {
                    pe.printErrorMessage(upd);
                    return;
                }

                CraftingItem craftingItem = new CraftingItem(currentItem, slot);

                pe.addExtra(new PreEnchantedItem(eit.getObject(), currentItem));
                pe.addExtra(ProfessionEventType.CRAFT);

                craftingItem.setEvent(GUIEventType.CRAFTING_END_EVENT, arg0 -> {
                    craftable.removeCraftingRequirements(getHolder());
                    getHolder().getInventory().addItem(craftable.getResult());
                    em.callEvent(pe);
                });
                craftingItem.addProgress(craftingItem.new Progress(Professions.getInstance(),
                        craftable.getCraftingTime(), this, INTERVAL));

                return;

            }
        }

    }

}
