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
import git.doomshade.professions.Professions;
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.Range;
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.api.item.ItemTypeHolder;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.data.TrainableSettings;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.io.IOManager;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.profession.ProfessionManager;
import git.doomshade.professions.trait.TrainerTrait;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.ISetup;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * @param <T>
 * @param <Type>
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class TrainerGUI<T extends ConfigurationSerializable, Type extends ItemType<T>> extends GUI implements ISetup {

    private static final HashMap<String, List<ItemType<?>>> CACHE = new HashMap<>();
    private static final HashMap<String, Profession> CACHE_PROFESSIONS = new HashMap<>();
    private static boolean inited = false;
    private String trainerId;
    private List<Type> trainableItems = new ArrayList<>();
    private Profession eligibleProfession;

    protected TrainerGUI(Player guiHolder, GUIManager manager) {
        super(guiHolder, manager);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void init() throws GUIInitializationException {

        if (!inited) {
            inited = true;
            return;
        }
        this.trainerId = getContext().getContext(TrainerTrait.KEY_TRAINER_ID);

        if (!CACHE.isEmpty()) {
            trainableItems = (List<Type>) CACHE.get(trainerId);
            eligibleProfession = CACHE_PROFESSIONS.get(trainerId);
        } else {
            loadFromFile();
        }

        if (trainableItems == null || trainableItems.isEmpty()) {
            final String message = "Could not load trainer GUI somehow.. Call DANKSEJD";
            getHolder().sendMessage(message);
            ProfessionLogger.log(message, Level.SEVERE);
            final GUIInitializationException ex = new GUIInitializationException();
            ProfessionLogger.logError(ex, true);
            //ProfessionLogger.log(message + "\n" + Arrays.toString(ex.getStackTrace()), Level.CONFIG);
            throw ex;
        }

        GUIInventory.Builder builder = getInventoryBuilder();

        int pos = 0;

        User user = User.getUser(getHolder());
        UserProfessionData upd = user.getProfessionData(eligibleProfession);
        TrainableSettings settings = Settings.getSettings(TrainableSettings.class);
        for (ItemType<?> trainable : trainableItems) {
            final ItemStack guiMaterial = trainable.getIcon(upd);
            GUIItem item =
                    new GUIItem(guiMaterial.getType(), pos++, guiMaterial.getAmount(), guiMaterial.getDurability());
            item.changeItem(this, () -> {
                ItemMeta meta = guiMaterial.getItemMeta();
                List<String> lore;
                if (Objects.requireNonNull(meta).hasLore()) {
                    lore = meta.getLore();
                } else {
                    lore = new ArrayList<>();
                }

                Objects.requireNonNull(lore)
                        .addAll(settings.calculateAdditionalLore(trainable, user, eligibleProfession));
                meta.setLore(lore);
                return meta;
            });

            builder = builder.withItem(item);
        }
        setInventory(builder.build());
    }

    @Override
    public void onGuiClick(GUIClickEvent e) {
        final InventoryClickEvent event = e.getEvent();
        event.setCancelled(true);
        final ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || currentItem.getType() == Material.AIR || !currentItem.hasItemMeta() ||
                !Objects.requireNonNull(
                        currentItem.getItemMeta()).hasDisplayName()) {
            return;
        }

        HumanEntity he = event.getWhoClicked();
        if (!(he instanceof Player)) {
            return;
        }
        Player player = (Player) he;

        User user = User.getUser(player);
        if (!user.hasProfession(eligibleProfession)) {
            // TODO send message
            return;
        }

        UserProfessionData upd = user.getProfessionData(eligibleProfession);
        ItemType<?> itemType;
        try {
            String displayName = ChatColor.stripColor(currentItem.getItemMeta().getDisplayName());
            Material mat = currentItem.getType();
            itemType = Utils.findInIterable(trainableItems, x ->
            {
                final ItemStack icon = x.getIcon(upd);
                return ChatColor.stripColor(Objects.requireNonNull(icon.getItemMeta()).getDisplayName())
                        .equals(displayName)
                        && icon.getType() == mat;
            });
        } catch (Utils.SearchNotFoundException ex) {
            return;
        }
        if (upd.hasTrained(itemType)) {
            // TODO send message
            return;
        }

        upd.train(itemType);

        // log
        user.sendMessage(new Messages.MessageBuilder(Messages.Global.SUCCESSFULLY_TRAINED)
                .userProfessionData(upd)
                .itemType(itemType)
                .build());

    }


    // ve file: configs: - 'herb:all' / - 'herb:1-10'
    // 1) split ":"
    // 2) getId - herb -> get item type holder
    // 3) add and filter
    @SuppressWarnings("unchecked")
    private void loadFromFile() throws GUIInitializationException {
        trainableItems.clear();
        eligibleProfession = null;

        File trainerFile = new File(IOManager.getTrainerFolder(), trainerId.concat(".yml"));
        FileConfiguration loader = YamlConfiguration.loadConfiguration(trainerFile);

        final ProfessionManager profMan = Professions.getProfMan();
        for (String key : loader.getStringList("configs")) {

            // 1) split
            String[] split = key.split(":");
            String configName = split[0];

            Range range;

            if (split.length == 1 || split[1].equalsIgnoreCase("all")) {
                range = new Range(-1);
            } else {
                try {
                    range = Range.fromString(split[1]).orElseThrow(() -> new IllegalArgumentException(
                            String.format("Could not get " +
                                    "range from '%s'", split[1])));
                } catch (Exception e) {
                    ProfessionLogger.logError(e);
                    return;
                }
            }

            // 2) get item type holder
            ItemTypeHolder<T, Type> holder;
            try {
                holder = (ItemTypeHolder<T, Type>) Utils.findInIterable(
                        profMan.getItemTypeHolders(),
                        x -> {
                            final String s = ItemUtils.getItemTypeFile(x.getExampleItemType().getClass()).getName();
                            return s.substring(0, s.lastIndexOf('.'))
                                    .equalsIgnoreCase(configName);
                        });
            } catch (Utils.SearchNotFoundException e) {
                throw new RuntimeException(e);
            }

            // 3) add and filter
            if (range.getMin() == -1) {
                for (Type itemType : holder) {
                    trainableItems.add(itemType);

                }
            } else {
                for (Type itemType : holder) {
                    if (range.isInRange(itemType.getFileId(), true)) {
                        trainableItems.add(itemType);
                    }

                }
            }
            holder.sortItems(trainableItems);
        }


        // now professions
        final String profession = loader.getString("profession");
        if (profession == null) {
            ProfessionLogger.log("Missing eligible profession in " + trainerFile.getName() + " file. (profession:___)",
                    Level.WARNING);
            throw new GUIInitializationException();
        }
        this.eligibleProfession = Professions.getProfMan().getProfessionById(profession)
                .orElseThrow(GUIInitializationException::new);
        CACHE.put(trainerId, (List<ItemType<?>>) trainableItems);
        CACHE_PROFESSIONS.put(trainerId, eligibleProfession);
    }

    @Override
    public void setup() {

    }

    @Override
    public void cleanup() {
        CACHE.clear();
        CACHE_PROFESSIONS.clear();
    }
}
