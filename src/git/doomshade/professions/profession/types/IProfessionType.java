package git.doomshade.professions.profession.types;

import git.doomshade.professions.event.ProfessionEvent;
import org.bukkit.event.Listener;

public interface IProfessionType extends Listener {

    String getDefaultName();

    <T extends ItemType<?>> void onEvent(ProfessionEvent<T> e);


}
