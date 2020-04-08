package git.doomshade.professions.profession.professions.herbalism;

import git.doomshade.professions.Profession;
import git.doomshade.professions.Professions;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.profession.types.IGathering;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.task.GatherTask;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.function.Consumer;
import java.util.logging.Level;

public class HerbalismProfession extends Profession<IGathering> {
    @Override
    public String getID() {
        return "herbalism";
    }

    @Override
    public void onLoad() {
        addItems(HerbItemType.class);
    }

    @Override
    @EventHandler
    public <T extends ItemType<?>> void onEvent(ProfessionEvent<T> e) {
        if (!isValidEvent(e, HerbItemType.class)) {
            return;
        }

        ProfessionEvent<HerbItemType> event = getEvent(e, HerbItemType.class);

        if (e.hasExtra(Location.class)) {
            final Location location = e.getExtra(Location.class);
            final Herb herb = event.getItemType().getObject();


            if (herb != null && location != null) {
                final HerbLocationOptions herbLocationOptions = herb.getHerbLocationOptions(location);
                final Player player = e.getPlayer().getPlayer();

                Consumer<GatherTask.GatherResult> endResultAction = x -> {
                    String msg = "";
                    switch (x) {
                        case SUCCESS:
                            msg = String.format("%s gathered %s on spawnpoint %s", player.getName(), herb.getName(), herbLocationOptions.location);
                            if (addExp(e)) {
                                msg = msg.concat(Utils.getReceiveXp(e.getExp()));
                            }
                            break;
                        case FULL_INVENTORY:
                            msg = String.format("%s could not gather %s as he had full inventory", player.getName(), herb.getName());
                            break;
                        case LOCATION_AIR:
                            msg = String.format("%s could not gather %s as the herb no longer existed", player.getName(), herb.getName());
                            break;
                    }
                    Professions.log(msg, Level.CONFIG);
                };

                HerbGatherTask herbGatherTask = new HerbGatherTask(herbLocationOptions, getUserProfessionData(e.getPlayer()), herb.getGatherItem(), endResultAction);
                herbGatherTask.runTaskLater(Professions.getInstance(), 5 * 20);
            }
        }
    }
}
