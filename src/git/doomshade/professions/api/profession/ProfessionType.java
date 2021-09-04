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

package git.doomshade.professions.api.profession;

import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.utils.ISetup;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * The profession types
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public enum ProfessionType implements ISetup {
    PRIMARY("Primary"),
    SECONDARY("Secondary");

    private String name;

    ProfessionType(String name) {
        this.name = name;
    }

    /**
     * @param professionType the string
     *
     * @return the converted profession type from a string
     */
    public static ProfessionType fromString(String professionType) {
        for (ProfessionType type : values()) {
            if (type.name.equalsIgnoreCase(professionType) || type.name().equalsIgnoreCase(professionType)) {
                return type;
            }
        }
        String sb = Arrays.stream(values())
                .map(type -> type.ordinal() + "=" + type)
                .collect(Collectors.joining("", professionType + " is not a valid profession type! (", ")"));
        throw new IllegalArgumentException(sb);
    }

    /**
     * @param id the id
     *
     * @return the converted profession type from an id based on ordinal() method
     */
    public static ProfessionType fromId(int id) {
        for (ProfessionType type : values()) {
            if (type.ordinal() == id) {
                return type;
            }
        }
        String sb = Arrays.stream(values())
                .map(type -> type.ordinal() + "=" + type)
                .collect(Collectors.joining("", id + " is not a valid profession id type! (", ")"));
        throw new IllegalArgumentException(sb);
    }

    @Override
    public String toString() {
        return String.valueOf(name.toCharArray()[0]).toUpperCase() + name.toLowerCase().substring(1);
    }

    @Override
    public void setup() {
        PRIMARY.name = new Messages.MessageBuilder(Messages.Global.PROFTYPE_PRIMARY).build();
        SECONDARY.name = new Messages.MessageBuilder(Messages.Global.PROFTYPE_SECONDARY).build();
    }
}
