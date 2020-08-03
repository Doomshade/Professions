package git.doomshade.professions.trait;

import git.doomshade.guiapi.GUI;
import git.doomshade.guiapi.GUIManager;
import git.doomshade.professions.Professions;
import git.doomshade.professions.gui.trainergui.TrainerChooserGUI;
import git.doomshade.professions.gui.trainergui.TrainerGUI;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.entity.Player;

import java.util.Optional;

@TraitName("professiontrainer")
public class TrainerTrait extends Trait {

    public static String KEY_TRAINER_ID = "professions.trainerId";
    private String trainerId = "";

    public TrainerTrait() {
        super("professiontrainer");
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        this.trainerId = key.getString(KEY_TRAINER_ID, "");
        if (trainerId == null || trainerId.isEmpty()) {
            throw new NPCLoadException("Missing ID of trainer for " + this + " !");
        }
    }

    @Override
    public void save(DataKey key) {
        key.setString(KEY_TRAINER_ID, trainerId);
    }

    public String getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(String trainerId) {
        this.trainerId = trainerId;
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
