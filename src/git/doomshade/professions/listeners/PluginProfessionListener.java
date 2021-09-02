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

package git.doomshade.professions.listeners;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.item.ext.ItemType;
import git.doomshade.professions.data.ExpSettings;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.event.ProfessionExpGainEvent;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.task.CraftingTask;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class PluginProfessionListener implements Listener {
    public static final Map<UUID, CraftingTask> PENDING_REPEAT_AMOUNT = new HashMap<>();
    private static final Random RANDOM = new Random();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExpGain(ProfessionExpGainEvent e) {
        e.setExp(Settings.getSettings(ExpSettings.class).getExpMultiplier() * e.getExp());

        ItemType<?> source = e.getSource();
        if (source != null && source.isIgnoreSkillupColor()) {
            return;
        } //else {

        int rand = RANDOM.nextInt(100) + 1;
        final double chance = e.getSkillupColor().getChance();

        e.setCancelled(rand > chance);
        //}
    }


    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        final Player player = e.getPlayer();
        CraftingTask task = PENDING_REPEAT_AMOUNT.remove(player.getUniqueId());
        if (task == null) {
            return;
        }
        e.setCancelled(true);
        try {
            int amount = Integer.parseInt(e.getMessage());
            task.setRepeatAmount(amount);
            task.setRepeat(false);
            Professions.getGUIManager().openGui(task.getGui());
            task.setCurrentItem(task.getGui().getInventory().getContents().get(task.getSlot()).getItemStackCopy());
            task.runTask(Professions.getInstance());
        } catch (NumberFormatException e1) {
            player.sendMessage(new Messages.MessageBuilder(Messages.Global.INVALID_REPEAT_AMOUNT).player(player)
                    .profession(task.getUpd().getProfession())
                    .build());
        } catch (Exception e2) {
            player.sendMessage(ChatColor.RED +
                    "Nastala neočekávaná chyba. Kontaktuj prosím admina, napiš mu čas, kdy se stala, a pokus se " +
                    "popsat situaci, která nastala.");
            ProfessionLogger.logError(e2);
        }

    }


}
