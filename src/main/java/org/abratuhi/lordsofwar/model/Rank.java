package org.abratuhi.lordsofwar.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

/**
 * (C) Copyright 2015-current Alexei Bratuhin.
 *
 * @author bratuhia
 */
public enum Rank {

    RECRUIT("Recruit"),
    REGULAR("Regular"),
    VETERAN("Veteran"),
    ELITE("Elite"),
    SPECIAL("Special"),
    COMMAND("Command"),
    GENERAL("General");

    @Getter @Setter private String name;

    Rank(String s) {
        setName(s);
    }

    public static Rank fromString(String s) {
        return Arrays.asList(values()).stream().filter(r -> r.getName().equalsIgnoreCase(s)).findFirst().get();
    }
}
