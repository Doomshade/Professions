package git.doomshade.professions.gui.playerguis;

import git.doomshade.guiapi.*;
import git.doomshade.guiapi.GUIInventory.Builder;
import git.doomshade.professions.Profession;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.data.TrainableSettings;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.profession.ITrainable;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ProfessionTrainerGUI extends GUI {
    public static final String KEY_PROFESSION = "profession";
    private Profession<?> prof;
    private List<ITrainable> items;

    protected ProfessionTrainerGUI(Player guiHolder, GUIManager manager) {
        super(guiHolder, manager);
    }

    private static boolean areSimilar(ItemStack one, ItemStack two) {
        if (one == null || two == null) {
            return false;
        }

        if (!one.hasItemMeta() && !two.hasItemMeta()) {
            return one.isSimilar(two);
        }
        ItemMeta oneMeta = one.getItemMeta();
        ItemMeta twoMeta = two.getItemMeta();

        boolean displayName = true;
        boolean lore = true;

        if (oneMeta.hasDisplayName() && twoMeta.hasDisplayName()) {
            displayName = oneMeta.getDisplayName().equals(twoMeta.getDisplayName());
        }

        return displayName && lore;
    }

    @Override
    public void init() throws GUIInitializationException {
        this.prof = getContext().getContext(KEY_PROFESSION);
        if (prof == null) {
            throw new GUIInitializationException();
        }

        Builder builder = getInventoryBuilder().title("KEK");

        items = new ArrayList<>();
        int position = 0;

        User user = User.getUser(getHolder());
        UserProfessionData upd = user.getProfessionData(prof);

        TrainableSettings settings = Settings.getSettings(TrainableSettings.class);

        for (ItemTypeHolder<?> itemTypes : prof.getItems()) {
            for (ItemType<?> itemType : itemTypes) {
                if (itemType instanceof ITrainable) {
                    ITrainable trainable = (ITrainable) itemType;
                    if (trainable.isTrainable()) {
                        final short durability = itemType.getGuiMaterial().getDurability();
                        GUIItem item = new GUIItem(itemType.getGuiMaterial().getType(), position++, 1, durability);
                        item.changeItem(this, () -> {
                            ItemMeta meta = itemType.getIcon(upd).getItemMeta();
                            List<String> lore;
                            if (meta.hasLore()) {
                                lore = meta.getLore();
                            } else {
                                lore = new ArrayList<>();
                            }
                            if (upd.hasTrained(trainable)) {
                                lore.addAll(settings.getTrainedLore(trainable));
                            } else {
                                lore.addAll(settings.getNotTrainedLore(trainable));
                            }
                            meta.setLore(lore);
                            return meta;
                        });
                        items.add(trainable);
                        builder = builder.withItem(item);
                    }
                }
            }
        }

        setInventory(builder.build());
    }

    @Override
    public void onGuiClick(GUIClickEvent e) {
        InventoryClickEvent event = e.getEvent();
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null) {
            return;
        }
        User user = User.getUser(getHolder());
        UserProfessionData upd = user.getProfessionData(prof);

        for (ITrainable trainable : items) {
            if (!(trainable instanceof ItemType)) {
                throw new IllegalStateException();
            }

            if (upd.hasTrained(trainable)) {
                continue;
            }
            ItemType<?> itemType = (ItemType<?>) trainable;
            ItemStack icon = itemType.getIcon(upd);
            if (!areSimilar(icon, item)) {
                continue;
            }
            if (!upd.train(trainable)) {
                user.sendMessage(new Messages.MessageBuilder(Messages.Message.NOT_ENOUGH_MONEY_TO_TRAIN)
                        .setItemType(itemType)
                        .setUserProfessionData(upd)
                        .build());
                return;
            }
            String name;
            if (!icon.hasItemMeta()) {
                name = icon.getType().toString();
            } else {
                ItemMeta meta = icon.getItemMeta();
                if (meta.hasDisplayName()) {
                    name = meta.getDisplayName();
                } else {
                    name = icon.getType().toString();
                }
            }
            upd.getUser().sendMessage("Vytrenoval jsi " + name);
            getManager().openGui(this);
            return;

        }
    }
}
