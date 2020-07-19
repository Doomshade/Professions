package git.doomshade.professions.trait;

import git.doomshade.guiapi.GUI;
import git.doomshade.guiapi.GUIManager;
import git.doomshade.professions.Professions;
import git.doomshade.professions.gui.trainergui.TrainerChooserGUI;
import git.doomshade.professions.gui.trainergui.TrainerGUI;
import git.doomshade.professions.utils.Permissions;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.List;
import java.util.Optional;

@TraitName("professiontrainer")
public class TrainerTrait extends Trait {

    public static String KEY_TRAINER_ID = "trainerId";
    private String trainerId;

    public TrainerTrait() {
        super("professiontrainer");
    }

   /* @Override
    public void load(DataKey key) throws NPCLoadException {
        this.trainerId = key.getString(KEY_TRAINER_ID);
        if (trainerId == null || trainerId.isEmpty()) {
            throw new NPCLoadException("Missing ID of trainer for " + this + " !");
        }
    }

    @Override
    public void save(DataKey key) {
        key.setString(KEY_TRAINER_ID, trainerId);
    }*/


    @Override
    public void onSpawn() {
        this.trainerId = npc.data().get(KEY_TRAINER_ID);
    }

    @Override
    public void onAttach() {
        final String trainerId = npc.data().get(KEY_TRAINER_ID);
        if (trainerId != null && !trainerId.isEmpty()) {
            this.trainerId = trainerId;
            return;
        }

        // let's see if it was just attached
        List<String> selectors = npc.data().get("selectors");
        if (selectors == null) {
            return;
        }
        for (String selector : selectors) {
            Player player = Bukkit.getPlayer(selector);
            if (Permissions.has(player, Permissions.BUILDER)) {
                openTrainerChooserGUI(player);
                return;
            }
        }
    }

    @EventHandler
    public void onRightClick(NPCRightClickEvent e) {
        if (!e.getNPC().equals(npc)) return;

        final Player player = e.getClicker();
        if (trainerId == null || trainerId.isEmpty()) {
            final String s = "Could not resolve trainer ID, please contact an admin.";
            player.sendMessage(s);
            throw new RuntimeException(s);
        }
        openTrainerGUI(player);
    }

    public void openTrainerGUI(Player player) {
        final Optional<? extends GUI> opt = Professions.getGUIManager().getGui(TrainerGUI.class, player);

        opt.ifPresent(x -> {
            GUI gui = opt.get();
            gui.getContext().addContext(KEY_TRAINER_ID, trainerId);
            gui.setOnPostInit(t -> {
                gui.getInventory().setTitle(npc.getName());
                return null;
            });
            Professions.getGUIManager().openGui(gui);
        });
    }

    public void openTrainerChooserGUI(Player player) {
        final GUIManager guiManager = Professions.getGUIManager();
        final Optional<? extends GUI> opt = guiManager.getGui(TrainerChooserGUI.class, player);
        opt.ifPresent(x -> {
            GUI gui = opt.get();
            gui.getContext().addContext(TrainerChooserGUI.KEY_NPC, npc);
            guiManager.openGui(gui);
        });

    }
}
