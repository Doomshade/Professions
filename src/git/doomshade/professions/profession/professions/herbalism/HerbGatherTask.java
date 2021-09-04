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

import git.doomshade.professions.api.item.object.spawn.ISpawnPoint;
import git.doomshade.professions.task.GatherTask;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class HerbGatherTask extends GatherTask {

    private static final BossBarOptions BOSS_BAR_OPTIONS = new BossBarOptions();
    // TODO add to settings
    private static final double MAX_DISTANCE = 5d;

    static {
        BOSS_BAR_OPTIONS.useBossBar = true;
        BOSS_BAR_OPTIONS.barColor = BarColor.GREEN;
        BOSS_BAR_OPTIONS.barStyle = BarStyle.SOLID;
    }

    public HerbGatherTask(ISpawnPoint location, UserProfessionData gatherer, ItemStack result,
                          Consumer<GatherResult> endResultAction, String title, long gatherTime) {
        super(location, gatherer, result, endResultAction, BOSS_BAR_OPTIONS, gatherTime);
        BOSS_BAR_OPTIONS.title = title;
        setOnGathererDamaged(x -> true);
        setOnMoved(x -> x >= MAX_DISTANCE);
    }
}
