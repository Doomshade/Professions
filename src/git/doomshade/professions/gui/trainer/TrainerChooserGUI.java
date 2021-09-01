/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Jakub Šmrha
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package git.doomshade.professions.gui.trainer;

import git.doomshade.guiapi.*;
import git.doomshade.professions.io.IOManager;
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
import java.util.Objects;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class TrainerChooserGUI extends GUI {
    private static final String KEY_NAME = "name";
    public static final String KEY_NPC = "npc";
    private final Map<String, String> NAME_ID_MAP = new HashMap<>();

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
        File[] files = IOManager.getTrainerFolder().listFiles();

        if (files == null) {
            throw new GUIInitializationException();
        }
        GUIInventory.Builder builder = getInventoryBuilder().size(9).title("Trainer chooser");
        int position = 0;
        for (File file : files) {
            String id = file.getName().substring(0, file.getName().lastIndexOf('.'));

            FileConfiguration loader = YamlConfiguration.loadConfiguration(file);
            String name = Utils.translateColour(Objects.requireNonNull(loader.getString(KEY_NAME, "Trainer name")));

            NAME_ID_MAP.put(name, id);
            List<String> lore = Utils.translateLore(loader.getStringList("lore"));
            GUIItem item = new GUIItem(Material.CHEST, position++, 1, (short) 0);
            ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.CHEST);
            Objects.requireNonNull(meta).setDisplayName(name);
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

        String name = Objects.requireNonNull(item.getItemMeta()).getDisplayName();
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
