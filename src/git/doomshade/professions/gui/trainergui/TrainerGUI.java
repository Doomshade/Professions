package git.doomshade.professions.gui.trainergui;

import git.doomshade.guiapi.*;
import git.doomshade.professions.Professions;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.data.TrainableSettings;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.ProfessionManager;
import git.doomshade.professions.api.types.ItemType;
import git.doomshade.professions.api.types.ItemTypeHolder;
import git.doomshade.professions.trait.TrainerTrait;
import git.doomshade.professions.api.user.User;
import git.doomshade.professions.api.user.UserProfessionData;
import git.doomshade.professions.utils.ISetup;
import git.doomshade.professions.utils.Range;
import git.doomshade.professions.utils.Utils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class TrainerGUI extends GUI implements ISetup {

    private static final HashMap<String, List<ItemType<?>>> CACHE = new HashMap<>();
    private static final HashMap<String, Profession> CACHE_PROFESSIONS = new HashMap<>();
    private static boolean inited = false;
    private String trainerId;
    private List<ItemType<?>> trainableItems = new ArrayList<>();
    private Profession eligibleProfession;

    protected TrainerGUI(Player guiHolder, GUIManager manager) {
        super(guiHolder, manager);
    }

    @Override
    public void init() throws GUIInitializationException {

        if (!inited) {
            inited = true;
            return;
        }
        this.trainerId = getContext().getContext(TrainerTrait.KEY_TRAINER_ID);

        if (CACHE.containsKey(trainerId)) {
            trainableItems = CACHE.get(trainerId);
            eligibleProfession = CACHE_PROFESSIONS.get(trainerId);
        } else {
            loadFromFile();
        }

        if (trainableItems == null || trainableItems.isEmpty()) {
            final String message = "Could not load trainer GUI somehow.. Call DANKSEJD";
            getHolder().sendMessage(message);
            Professions.log(message, Level.SEVERE);
            final GUIInitializationException ex = new GUIInitializationException();
            Professions.log(message + "\n" + Arrays.toString(ex.getStackTrace()), Level.CONFIG);
            throw ex;
        }

        GUIInventory.Builder builder = getInventoryBuilder();

        int pos = 0;

        User user = User.getUser(getHolder());
        UserProfessionData upd = user.getProfessionData(eligibleProfession);
        TrainableSettings settings = Settings.getSettings(TrainableSettings.class);
        for (ItemType<?> trainable : trainableItems) {
            final ItemStack guiMaterial = trainable.getIcon(upd);
            GUIItem item = new GUIItem(guiMaterial.getType(), pos++, guiMaterial.getAmount(), guiMaterial.getDurability());
            item.changeItem(this, () -> {
                ItemMeta meta = guiMaterial.getItemMeta();
                List<String> lore;
                if (meta.hasLore()) {
                    lore = meta.getLore();
                } else {
                    lore = new ArrayList<>();
                }

                lore.addAll(settings.calculateAdditionalLore(trainable, user, eligibleProfession));
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
        if (currentItem == null || currentItem.getType() == Material.AIR || !currentItem.hasItemMeta() || !currentItem.getItemMeta().hasDisplayName()) {
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
                return ChatColor.stripColor(icon.getItemMeta().getDisplayName()).equals(displayName)
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
                .setUserProfessionData(upd)
                .setItemType(itemType)
                .build());

    }


    // ve file: configs: - 'herb:all' / - 'herb:1-10'
    // 1) split ":"
    // 2) getId - herb -> get item type holder
    // 3) add and filter
    private void loadFromFile() throws GUIInitializationException {
        trainableItems.clear();
        eligibleProfession = null;

        File trainerFile = new File(Professions.getInstance().getTrainerFolder(), trainerId.concat(".yml"));
        FileConfiguration loader = YamlConfiguration.loadConfiguration(trainerFile);

        final ProfessionManager profMan = Professions.getProfessionManager();
        for (String key : loader.getStringList("configs")) {

            // 1) split
            String[] split = key.split(":");
            String configName = split[0];

            Range range;

            if (split.length == 1 || split[1].equalsIgnoreCase("all")) {
                range = new Range(-1);
            } else {
                try {
                    range = Range.fromString(split[1]);
                } catch (Exception e) {
                    Professions.logError(e);
                    return;
                }
            }

            // 2) get item type holder
            ItemTypeHolder<?> holder;
            try {
                holder = Utils.findInIterable(
                        profMan.getItemTypeHolders(),
                        x -> x.getFile().getName().substring(0, x.getFile().getName().lastIndexOf('.'))
                                .equalsIgnoreCase(configName));
            } catch (Utils.SearchNotFoundException e) {
                throw new RuntimeException(e);
            }

            // 3) add and filter
            if (range.getMin() == -1) {
                for (ItemType<?> itemType : holder) {
                    trainableItems.add(itemType);

                }
            } else {
                for (ItemType<?> itemType : holder) {
                    if (range.isInRange(itemType.getFileId(), true))
                        trainableItems.add(itemType);

                }
            }
            holder.sortItems(trainableItems);
        }


        // now professions
        final String profession = loader.getString("profession");
        if (profession == null) {
            Professions.log("Missing eligible profession in " + trainerFile.getName() + " file. (profession:___)", Level.WARNING);
            throw new GUIInitializationException();
        }
        this.eligibleProfession = Professions.getProfession(profession);
        CACHE.put(trainerId, trainableItems);
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
