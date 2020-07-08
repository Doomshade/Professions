package git.doomshade.professions.trait;

import git.doomshade.guiapi.GUI;
import git.doomshade.professions.Professions;
import git.doomshade.professions.gui.trainergui.TrainerChooserGUI;
import git.doomshade.professions.gui.trainergui.TrainerGUI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.Optional;

@TraitName("professiontrainer")
public class TrainerTrait extends Trait {

    public static String KEY_TRAINER_ID = "trainerId";
    private String trainerId = "";

    public TrainerTrait() {
        super("professiontrainer");
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        this.trainerId = key.getString(TrainerChooserGUI.KEY_TRAINER);
        if (trainerId == null) {
            throw new NPCLoadException("Missing ID of trainer for " + this + " !");
        }
    }

    @EventHandler
    public void onRightClick(NPCRightClickEvent e) {
        if (!e.getNPC().equals(npc)) return;

        final Player player = e.getClicker();
        if (trainerId.isEmpty()) {
            final String s = "Could not resolve trainer ID, please contact an admin.";
            player.sendMessage(s);
            throw new RuntimeException(s);
        }
        final Optional<? extends GUI> opt = Professions.getGUIManager().getGui(TrainerGUI.class, player);

        opt.ifPresent(x -> {
            GUI gui = opt.get();
            gui.getContext().addContext(KEY_TRAINER_ID, trainerId);
            gui.setOnPostInit(t -> {
                gui.getInventory().setTitle(e.getNPC().getName());
                return null;
            });
            Professions.getGUIManager().openGui(gui);
        });
    }
}
