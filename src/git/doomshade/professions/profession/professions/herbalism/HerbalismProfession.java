package git.doomshade.professions.profession.professions.herbalism;

import git.doomshade.professions.Profession;
import git.doomshade.professions.Professions;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.profession.types.IGathering;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.task.GatherTask;
import git.doomshade.professions.user.UserProfessionData;
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


        // check for level requirements
        final UserProfessionData upd = getUserProfessionData(e.getPlayer());
        if (!playerMeetsLevelRequirements(e)) {
            e.printErrorMessage(upd);
            return;
        }

        // this should not happen but we will check for it anyways
        if (!e.hasExtra(Location.class)) {
            return;
        }
        final Location location = e.getExtra(Location.class);
        final HerbItemType itemType = getEvent(e, HerbItemType.class).getItemType();
        final Herb herb = itemType.getObject();

        // this should not happen either but just making sure
        if (herb == null || location == null) {
            return;
        }
        final HerbLocationOptions herbLocationOptions = herb.getHerbLocationOptions(location);
        final Player player = e.getPlayer().getPlayer();

        // end result action that will be called once the gathering is done/cancelled
        final Consumer<GatherTask.GatherResult> endResultAction = x -> {
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
                case CANCELLED_BY_DAMAGE:
                    player.sendMessage(new Messages.MessageBuilder(Messages.Message.GATHERING_CANCELLED_BY_DAMAGE)
                            .setItemType(e.getItemType())
                            .setUserProfessionData(upd)
                            .build());
                    break;
                case UNKNOWN:
                    break;
            }
            Professions.log(msg, Level.CONFIG);
        };

        // create and run the task
        HerbGatherTask herbGatherTask = new HerbGatherTask(herbLocationOptions, upd, herb.getGatherItem(), endResultAction, itemType.getName());
        herbGatherTask.runTaskLater(Professions.getInstance(), herb.getGatherTime() * 20);


    }
}
