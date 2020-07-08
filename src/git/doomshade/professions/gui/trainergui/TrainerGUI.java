package git.doomshade.professions.gui.trainergui;

import git.doomshade.guiapi.GUI;
import git.doomshade.guiapi.GUIInitializationException;
import git.doomshade.guiapi.GUIManager;
import git.doomshade.professions.Professions;
import git.doomshade.professions.trait.TrainerTrait;
import org.bukkit.entity.Player;

import java.io.File;

public class TrainerGUI extends GUI {
    protected TrainerGUI(Player guiHolder, GUIManager manager) {
        super(guiHolder, manager);
    }

    @Override
    public void init() throws GUIInitializationException {
        final String trainerId = getContext().getContext(TrainerTrait.KEY_TRAINER_ID);
        File trainerFile = new File(Professions.getInstance().getTrainerFolder(), trainerId.concat(".yml"));
    }


}
