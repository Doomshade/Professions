package git.doomshade.professions.profession.types;

import git.doomshade.professions.event.ProfessionEvent;
import org.bukkit.event.Listener;

public interface IProfessionType extends Listener, ProfessionEventable {

    String getDefaultName();

}
