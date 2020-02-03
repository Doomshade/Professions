package git.doomshade.professions.trait;

import git.doomshade.guiapi.GUI;
import git.doomshade.professions.Profession;
import git.doomshade.professions.Professions;
import git.doomshade.professions.gui.playerguis.ProfessionTrainerGUI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.event.EventHandler;

import java.util.Optional;

/**
 * Trait for custom profession trainers.
 *
 * @author Doomshade
 */
@TraitName(value = "professiontrainer")
public class ProfessionTrainerTrait extends Trait {
    private static final String KEY_PROFESSION = "profession";

    private Profession<?> profession;

    public ProfessionTrainerTrait() {
        super("professiontrainer");

    }

    @EventHandler
    public void onRightClick(NPCRightClickEvent e) {
        if (e.getNPC() != npc) {
            return;
        }


        if (profession == null) {
            throw new IllegalStateException("Could not find a profession!");
        }

        Optional<? extends GUI> opt = Professions.getGUIManager().getGui(ProfessionTrainerGUI.class, e.getClicker());
        if (opt.isPresent()) {
            GUI gui = opt.get();
            gui.getContext().addContext(ProfessionTrainerGUI.KEY_PROFESSION, profession);
            gui.setOnPostInit(t -> {
                gui.getInventory().setTitle(e.getNPC().getName());
                return null;
            });
            Professions.getGUIManager().openGui(gui);
        }
    }


    @Override
    public void load(DataKey key) {
        this.profession = Professions.getProfession(key.getString(KEY_PROFESSION));

    }

    @Override
    public void save(DataKey key) {
        key.setString(KEY_PROFESSION, profession.getID());
    }


    @Override
    public void onAttach() {
        this.profession = Professions.getProfession("enchanting");
    }
}
