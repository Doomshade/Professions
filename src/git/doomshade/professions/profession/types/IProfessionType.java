package git.doomshade.professions.profession.types;

import org.bukkit.event.Listener;

public interface IProfessionType extends Listener, IProfessionEventable {

    String getDefaultName();

}
