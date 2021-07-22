package git.doomshade.professions.profession.professions.herbalism;

import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.api.spawn.ISpawnPoint;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.event.ProfessionEventWrapper;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.task.GatherTask;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.function.Consumer;
import java.util.logging.Level;

public class HerbalismProfession extends Profession {

    // TODO add this to config file
    public static final String MARKER_SET_ID = "Herbalism";

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
        if (!event.hasExtra(Location.class)) {
            throw new RuntimeException("No location given, this should not happen");
        }

        final Location location = event.getExtra(Location.class);
        final HerbItemType itemType = event.getItemType();
        final Herb herb = itemType.getObject();

        // this should not happen either but just making sure
        if (herb == null || location == null) {
            return;
        }

        final ISpawnPoint sp = herb.getSpawnPoint(location);
        final Player player = user.getPlayer();

        // end result action that will be called once the gathering is done/cancelled
        final Consumer<GatherTask.GatherResult> endResultAction = x -> {
            String msg = String.format("%s gathered %s on spawnpoint %s", player.getName(), herb.getName(),
                    sp.getLocation());
            final Messages.MessageBuilder messageBuilder = new Messages.MessageBuilder()
                    .itemType(itemType)
                    .userProfessionData(upd);
            switch (x) {
                case FULL_INVENTORY:
                    msg = msg.concat(" with full inventory");
                case SUCCESS:
                    if (addExp(event)) {
                        msg = msg.concat(Utils.getReceiveXp(event.getExp()));
                    }
                    break;
                case LOCATION_AIR:
                    msg = String.format("%s could not gather %s as the herb no longer existed", player.getName(),
                            herb.getName());
                    break;
                case CANCELLED_BY_DAMAGE:
                    player.sendMessage(messageBuilder
                            .message(Messages.HerbalismMessages.GATHERING_CANCELLED_BY_DAMAGE)
                            .build());
                    break;
                case CANCELLED_BY_MOVE:
                    player.sendMessage(messageBuilder
                            .message(Messages.HerbalismMessages.GATHERING_CANCELLED_BY_MOVEMENT)
                            .build());
                    break;
                case UNKNOWN:
                    break;
            }
            ProfessionLogger.log(msg, Level.CONFIG);
        };

        // create and run the task
        HerbGatherTask herbGatherTask =
                new HerbGatherTask(sp, upd, herb.getGatherItem(), endResultAction, itemType.getName(),
                        herb.getGatherTime() * 20);
        herbGatherTask.startTask();
    }

    @Override
    public boolean isSubprofession() {
        return false;
    }
}
