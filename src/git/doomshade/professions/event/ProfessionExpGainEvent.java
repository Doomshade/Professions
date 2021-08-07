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

package git.doomshade.professions.event;

import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.enums.SkillupColor;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player receives exp
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class ProfessionExpGainEvent extends AbstractProfessionEvent implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    private ItemType<?> source;
    private double exp;

    protected ProfessionExpGainEvent(UserProfessionData data) {
        super(data);
    }

    public ProfessionExpGainEvent(UserProfessionData data, ItemType<?> source, double exp) {
        this(data);
        this.source = source;
        this.exp = exp;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public double getExp() {
        return exp;
    }

    public void setExp(double exp) {
        this.exp = exp;
    }

    public ItemType<?> getSource() {
        return source;
    }

    public SkillupColor getSkillupColor() {
        return getUserProfessionData().getSkillupColor(getSource());
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

}
