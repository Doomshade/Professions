package git.doomshade.professions.gui.trainergui;

import git.doomshade.guiapi.*;
import git.doomshade.professions.Professions;
import git.doomshade.professions.trait.TrainerTrait;
import git.doomshade.professions.utils.Utils;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainerChooserGUI extends GUI {
    private static final String KEY_NAME = "name";
    public static final String KEY_NPC = "npc";
    private Map<String, String> NAME_ID_MAP = new HashMap<>();

    protected TrainerChooserGUI(Player guiHolder, GUIManager manager) {
        super(guiHolder, manager);
    }

    @Override
    public void init() throws GUIInitializationException {
        NPC selectedNpc = getContext().getContext(KEY_NPC);
        if (!selectedNpc.hasTrait(TrainerTrait.class)) {
            getHolder().sendMessage(selectedNpc.getName() + ChatColor.RESET + " does not have Trainer Trait!");
            throw new GUIInitializationException();
        }
        final Professions plugin = Professions.getInstance();
        File[] files = plugin.getTrainerFolder().listFiles();

        if (files == null) throw new GUIInitializationException();
        GUIInventory.Builder builder = getInventoryBuilder().size(9).title("Trainer chooser");
        int position = 0;
        for (File file : files) {
            String id = file.getName().substring(0, file.getName().lastIndexOf('.'));

            FileConfiguration loader = YamlConfiguration.loadConfiguration(file);
            String name = Utils.translateName(loader.getString(KEY_NAME, "Trainer name"));

            NAME_ID_MAP.put(name, id);
            List<String> lore = Utils.translateLore(loader.getStringList("lore"));
            GUIItem item = new GUIItem(Material.CHEST, position++, 1, (short) 0);
            ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.CHEST);
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.changeItem(this, () -> meta);
            builder = builder.withItem(item);
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
        final HumanEntity he = event.getWhoClicked();
        final NPC npc = CitizensAPI.getDefaultNPCSelector().getSelected(he);
        if (npc == null) {
            he.sendMessage("You must have an NPC selected to set the trait (/npc sel)");
            return;
        }

        if (!npc.hasTrait(TrainerTrait.class)) {
            he.sendMessage("This NPC (" + npc.getName() + ChatColor.RESET + ") does not have Trainer Trait.");
            return;
        }

        String name = item.getItemMeta().getDisplayName();
        String id = NAME_ID_MAP.get(name);
        if (id == null) {
            he.sendMessage("Could not retrieve trainer ID with the trainer name " + name);
            return;
        }

        TrainerTrait trait = npc.getTrait(TrainerTrait.class);
        trait.setTrainerId(id);

        he.sendMessage("Successfully set to a " + id + " trainer.");
    }
}
