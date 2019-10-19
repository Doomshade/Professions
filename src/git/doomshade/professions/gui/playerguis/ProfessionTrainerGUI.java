package git.doomshade.professions.gui.playerguis;

import git.doomshade.guiapi.*;
import git.doomshade.guiapi.GUIInventory.Builder;
import git.doomshade.professions.Profession;
import git.doomshade.professions.Professions;
import git.doomshade.professions.data.ProfessionSettings;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.profession.types.Trainable;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ProfessionTrainerGUI extends GUI {
    public static final String KEY_PROFESSION = "profession";
    private Profession<?> prof;
    private List<Trainable> items;

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
        this.prof = (Profession<?>) getContext().getContext(KEY_PROFESSION);
        if (prof == null) {
            throw new GUIInitializationException();
        }

        Builder builder = getInventoryBuilder().title("KEK");

        items = new ArrayList<>();
        int position = 0;

        User user = User.getUser(getHolder());
        UserProfessionData upd = user.getProfessionData(prof);

        ProfessionSettings settings = Settings.getInstance().getProfessionSettings();

        for (ItemTypeHolder<?> itemTypes : prof.getItems()) {
            for (ItemType<?> itemType : itemTypes) {
                if (itemType instanceof Trainable) {
                    Trainable trainable = (Trainable) itemType;
                    if (trainable.isTrainable()) {
                        GUIItem item = new GUIItem(itemType.getGuiMaterial(), position++);
                        item.changeItem(this, new Supplier<ItemMeta>() {

                            @Override
                            public ItemMeta get() {
                                ItemMeta meta = itemType.getIcon(upd).getItemMeta();
                                List<String> lore;
                                if (meta.hasLore()) {
                                    lore = meta.getLore();
                                } else {
                                    lore = new ArrayList<>();
                                }
                                if (trainable.hasTrained(upd)) {
                                    lore.addAll(settings.getTrainedLore(trainable));
                                } else {
                                    lore.addAll(settings.getNotTrainedLore(trainable));
                                }
                                meta.setLore(lore);
                                return meta;
                            }

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
        Economy econ = Professions.getEconomy();

        for (Trainable trainable : items) {
            if (trainable.hasTrained(upd)) {
                continue;
            }
            ItemStack icon = ((ItemType<?>) trainable).getIcon(upd);
            if (areSimilar(icon, item)) {
                if (!econ.has(getHolder(), trainable.getCost())) {
                    user.sendMessage("Nem� cash");
                    return;
                }
                trainable.train(upd);
                String name;
                ItemMeta meta;
                if (!icon.hasItemMeta()) {
                    name = icon.getType().toString();
                } else {
                    meta = icon.getItemMeta();
                    if (meta.hasDisplayName()) {
                        name = meta.getDisplayName();
                    } else {
                        name = icon.getType().toString();
                    }
                }
                upd.getUser().sendMessage("Vytr�noval jsi " + name);
                getManager().openGui(this);
                return;
            }
        }
    }
}
