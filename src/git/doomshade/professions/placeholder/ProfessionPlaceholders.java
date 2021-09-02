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

package git.doomshade.professions.placeholder;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.Utils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import java.lang.reflect.Method;

/**
 * Placeholder expansion of PlaceholderAPI
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public final class ProfessionPlaceholders extends PlaceholderExpansion {

    private static final String SECONDARY = "secondary";
    private static final String PRIMARY = "primary";
    private static final ProfessionPlaceholders INSTANCE = new ProfessionPlaceholders();
    private final PluginDescriptionFile description = Professions.getInstance().getDescription();

    private ProfessionPlaceholders() {
    }

    public static ProfessionPlaceholders getInstance() {
        return INSTANCE;
    }

    @Override
    public String getIdentifier() {
        return "prof";
    }

    @Override
    public String getAuthor() {
        return description.getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return description.getVersion();
    }

    // must persist
    @Override
    public boolean persist() {
        return true;
    }

    // no requirements
    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player p, String id) {

        // we need player to be online in order for the placeholders to work
        if (p == null) {
            return "";
        }
        final User user = User.getUser(p);

        final UserProfessionData upd;
        final String sub;

        // can definitely be done better, but only two prof types -> cba
        if (id.startsWith(PRIMARY)) {

            // must add 1 ("primary" vs "primary_")
            sub = id.substring(id.indexOf(PRIMARY)) + 1;
            try {
                upd = (UserProfessionData) Utils.findInIterable(user.getProfessions(),
                        x -> x.getProfession().getProfessionType() == Profession.ProfessionType.PRIMARY);
            } catch (Utils.SearchNotFoundException e) {
                return "";
            }
        } else if (id.startsWith(SECONDARY)) {
            sub = id.substring(id.indexOf(SECONDARY)) + 1;
            try {
                upd = (UserProfessionData) Utils.findInIterable(user.getProfessions(),
                        x -> x.getProfession().getProfessionType() == Profession.ProfessionType.SECONDARY);
            } catch (Utils.SearchNotFoundException e) {
                return "";
            }
        } else {
            return null;
        }
        return resolveRequest(upd, sub);
    }

    /**
     * Resolves the placeholder based on profession data and the variable (request)
     *
     * @param upd     the user profession data
     * @param request the variable request
     *
     * @return replaced placeholder
     */
    private String resolveRequest(UserProfessionData upd, String request) {


        // resolve profession specific placeholders first
        final Profession prof = upd.getProfession();
        switch (request.toLowerCase()) {
            case "name":
                return prof.getColoredName();
            case "type":
                return prof.getProfessionType().toString();
        }


        // next resolve user specific placeholders
        // this does in a string: levelCap -> LevelCap
        final String methodName = ((Character) request.charAt(0)).toString().toUpperCase() + request.substring(1);

        try {

            // LevelCap -> getLevelCap
            final Method m = upd.getClass().getMethod("get" + methodName);
            final Object obj = m.invoke(upd);
            return obj.toString();
        } catch (Exception e) {
            // unknown method == unknown placeholder
            return null;
        }
    }
}
