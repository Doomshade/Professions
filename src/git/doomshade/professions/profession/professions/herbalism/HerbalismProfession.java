/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Jakub Šmrha
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package git.doomshade.professions.profession.professions.herbalism;

import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.item.ext.ItemType;
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

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class HerbalismProfession extends Profession {

    // TODO add this to config file
    public static final String MARKER_SET_ID = "Herbalism";

    @Override
    public String getID() {
        return "herbalism";
    }

    @Override
    public <T extends ItemType<?>> void onEvent(ProfessionEventWrapper<T> e) {
        ProfessionLogger.log("Handling Herbalism event...", Level.FINEST);
        final ProfessionEvent<HerbItemType> event = utils.getEventUnsafe(e);

        // check for level requirements
        final User user = event.getPlayer();
        final UserProfessionData upd = utils.getUserProfessionData(user);
        if (!utils.playerMeetsLevelRequirements(event)) {
            event.printErrorMessage(upd);
            ProfessionLogger.log(String.format("Player %s did not meet requirements to gather %s",
                    user.getPlayer().getName(), event.getItemType().getObject().getName()), Level.FINEST);
            return;
        }

        // this should not happen, but we will check for it anyways
        if (!event.hasExtra(Location.class)) {
            throw new IllegalStateException("No location given, this should not happen");
        }

        final Location location = event.getExtra(Location.class);
        final HerbItemType itemType = event.getItemType();
        final Herb herb = itemType.getObject();

        // this should not happen either but just making sure
        if (herb == null || location == null) {
            ProfessionLogger.log(
                    String.format("Either herb or location was null, this should not happen! (herb=%s, location=%s)",
                            herb, location),
                    Level.WARNING);
            return;
        }

        final ISpawnPoint sp = herb.getSpawnPoint(location);
        final Player player = user.getPlayer();

        if (GatherTask.isActive(player)) {
            ProfessionLogger.log(String.format("Player %s did not have an active gather task for %s",
                    user.getPlayer().getName(), herb.getName()), Level.FINEST);
            return;
        }

        // end result action that will be called once the gathering is done/cancelled
        final Consumer<GatherTask.GatherResult> endResultAction = x -> {
            String msg = String.format("%s gathered %s on spawnpoint %s", player.getName(), herb.getName(),
                    sp.toString());
            final Messages.MessageBuilder messageBuilder = new Messages.MessageBuilder()
                    .itemType(itemType)
                    .userProfessionData(upd);
            switch (x) {
                case FULL_INVENTORY:
                    msg = msg.concat(" with full inventory");
                case SUCCESS:
                    if (utils.addExp(event)) {
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
                        herb.getGatherTime() * Utils.TICKS);
        ProfessionLogger.log("Starting " + herbGatherTask, Level.FINEST);
        herbGatherTask.startTask();
    }

    @Override
    public boolean isSubprofession() {
        return false;
    }

    @Override
    public void onLoad() {
        utils.addItems(HerbItemType.class);
    }
}
