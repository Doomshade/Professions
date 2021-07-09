package git.doomshade.professions.profession.professions.herbalism;

import git.doomshade.professions.Professions;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.event.ProfessionEventWrapper;
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.task.GatherTask;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.function.Consumer;
import java.util.logging.Level;

public class HerbalismProfession extends Profession {
    @Override
    public String getID() {
        return "herbalism";
    }

    @Override
    public void onLoad() {
        addItems(HerbItemType.class);
    }

    @Override
    public <T extends ItemType<?>> void onEvent(ProfessionEventWrapper<T> e) {

        final ProfessionEvent<HerbItemType> event = getEventUnsafe(e, HerbItemType.class);

        // check for level requirements
        final User user = event.getPlayer();
        final UserProfessionData upd = getUserProfessionData(user);
        if (!playerMeetsLevelRequirements(event)) {
            event.printErrorMessage(upd);
            return;
        }

        // this should not happen but we will check for it anyways
        if (!event.hasExtra(Location.class)) throw new RuntimeException("No location given, this should not happen");

        final Location location = event.getExtra(Location.class);
        final HerbItemType itemType = event.getItemType();
        final Herb herb = itemType.getObject();

        // this should not happen either but just making sure
        if (herb == null || location == null) return;

        final HerbSpawnPoint herbLocationOptions = herb.getSpawnPoints(location);
        final Player player = user.getPlayer();

        // end result action that will be called once the gathering is done/cancelled
        final Consumer<GatherTask.GatherResult> endResultAction = x -> {
            String msg = String.format("%s gathered %s on spawnpoint %s", player.getName(), herb.getName(), herbLocationOptions.location);
            final Messages.MessageBuilder messageBuilder = new Messages.MessageBuilder()
                    .setItemType(itemType)
                    .setUserProfessionData(upd);
            switch (x) {
                case FULL_INVENTORY:
                    msg = msg.concat(" with full inventory");
                case SUCCESS:
                    if (addExp(event)) {
                        msg = msg.concat(Utils.getReceiveXp(event.getExp()));
                    }
                    break;
                case LOCATION_AIR:
                    msg = String.format("%s could not gather %s as the herb no longer existed", player.getName(), herb.getName());
                    break;
                case CANCELLED_BY_DAMAGE:
                    player.sendMessage(messageBuilder
                            .setMessage(Messages.HerbalismMessages.GATHERING_CANCELLED_BY_DAMAGE)
                            .build());
                    break;
                case CANCELLED_BY_MOVE:
                    player.sendMessage(messageBuilder
                            .setMessage(Messages.HerbalismMessages.GATHERING_CANCELLED_BY_MOVEMENT)
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

    @Override
    public boolean isSubprofession() {
        return false;
    }
}
