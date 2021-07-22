package git.doomshade.professions.profession.professions.herbalism;

import git.doomshade.professions.api.spawn.ISpawnPoint;
import git.doomshade.professions.task.GatherTask;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class HerbGatherTask extends GatherTask {

    private static final BossBarOptions bossBarOptions = new BossBarOptions();

    static {
        bossBarOptions.useBossBar = true;
        bossBarOptions.barColor = BarColor.GREEN;
        bossBarOptions.barStyle = BarStyle.SOLID;
    }

    public HerbGatherTask(ISpawnPoint location, UserProfessionData gatherer, ItemStack result,
                          Consumer<GatherResult> endResultAction, String title, long gatherTime) {
        super(location, gatherer, result, endResultAction, bossBarOptions, gatherTime);
        bossBarOptions.title = title;
        setOnGathererDamaged(x -> true);
        setOnMoved(x -> x >= 5d);
    }
}
