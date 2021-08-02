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

package git.doomshade.professions.profession.professions.herbalism.commands;

import git.doomshade.professions.api.spawn.ISpawnPoint;
import git.doomshade.professions.exceptions.SpawnException;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.command.CommandSender;

import java.util.function.Consumer;

@SuppressWarnings("ALL")
public class SpawnCommand extends AbstractSpawnCommand {

    private boolean force = false;

    public SpawnCommand() {
        setArg(true, "herb", "all / spawnpoint id");
        setArg(false, "forcespawn (bypass respawn timer and configuration in itemtype, default: false)");
        setCommand("spawn");
        setDescription("Spawns a herb");
        setRequiresPlayer(false);
        addPermission(Permissions.HELPER);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {

        if (args.length >= 5) {
            try {
                this.force = Boolean.parseBoolean(args[4]);
            } catch (Exception ignored) {
            }
        }
        super.onCommand(sender, args);
        this.force = false;
    }

    @Override
    protected Consumer<ISpawnPoint> consumer() {
        return x -> {
            try {
                if (force) {
                    x.forceSpawn();
                } else {
                    x.spawn();
                }
            } catch (SpawnException e) {
                e.printStackTrace();
            }
        };
    }

    @Override
    public String getID() {
        return "spawn";
    }
}
