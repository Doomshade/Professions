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

package git.doomshade.professions.trait;

import git.doomshade.guiapi.GUI;
import git.doomshade.guiapi.GUIManager;
import git.doomshade.professions.Professions;
import git.doomshade.professions.gui.trainer.TrainerChooserGUI;
import git.doomshade.professions.gui.trainer.TrainerGUI;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
@TraitName("professiontrainer")
public class TrainerTrait extends Trait {

    public static final String KEY_TRAINER_ID = "professions.trainerId";
    private String trainerId = "";

    public TrainerTrait() {
        super("professiontrainer");
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        this.trainerId = key.getString(KEY_TRAINER_ID, "");
        if (trainerId == null || trainerId.isEmpty()) {
            throw new NPCLoadException("Missing ID of trainer for " + this + " !");
        }
    }

    @Override
    public void save(DataKey key) {
        key.setString(KEY_TRAINER_ID, trainerId);
    }

    public String getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(String trainerId) {
        this.trainerId = trainerId;
    }

    public void openTrainerGUI(Player player) {
        final Optional<? extends GUI> opt = Professions.getGUIManager().getGui(TrainerGUI.class, player);

        opt.ifPresent(x -> {
            GUI gui = opt.get();
            gui.getContext().addContext(KEY_TRAINER_ID, trainerId);
            gui.setOnPostInit(t -> {
                gui.getInventory().setTitle(npc.getName());
                return null;
            });
            Professions.getGUIManager().openGui(gui);
        });
    }

    public void openTrainerChooserGUI(Player player) {
        final GUIManager guiManager = Professions.getGUIManager();
        final Optional<? extends GUI> opt = guiManager.getGui(TrainerChooserGUI.class, player);
        opt.ifPresent(x -> {
            GUI gui = opt.get();
            gui.getContext().addContext(TrainerChooserGUI.KEY_NPC, npc);
            guiManager.openGui(gui);
        });

    }
}
