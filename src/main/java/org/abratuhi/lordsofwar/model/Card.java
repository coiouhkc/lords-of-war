package org.abratuhi.lordsofwar.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * (C) Copyright 2015-current Alexei Bratuhin.
 *
 * @author bratuhia
 */
public class Card {

    @Getter @Setter private int id;
    @Getter @Setter private String name;
    @Getter @Setter private Race race;
    @Getter @Setter private Rank rank;
    @Getter @Setter private Clazz clazz;
    @Getter @Setter private int hp;
    @Getter @Setter private List<int[]> melee; // list of triplets (relative-x, relative-y, value)
    @Getter @Setter private List<int[]> ranged; // list of triplets (relative-x, relative-y, value)

    public Card() {}

    public Card(String id, String name, String rank, String clazz, String hp, List<int[]> melee, List<int[]> ranged) {
        this(Integer.parseInt(id), name, Rank.fromString(rank), Clazz.fromString(clazz), Integer.parseInt(hp), melee, ranged);
    }

    public Card(Race race, String id, String name, String rank, String clazz, String hp, List<int[]> melee, List<int[]> ranged) {
        this(Integer.parseInt(id), name, Rank.fromString(rank), Clazz.fromString(clazz), Integer.parseInt(hp), melee, ranged);
        setRace(race);
    }

    public Card(int id, String name, Rank rank, Clazz clazz, int hp, List<int[]> melee, List<int[]> ranged) {
        setId(id);
        setName(name);
        setRank(rank);
        setClazz(clazz);
        setHp(hp);
        setMelee(melee);
        setRanged(ranged);
    }

    public Card(Race race, int id, String name, Rank rank, Clazz clazz, int hp, List<int[]> melee, List<int[]> ranged) {
        this(id, name, rank, clazz, hp, melee, ranged);
        setRace(race);
    }

    public Card(Card c) {
        this(c.id, c.name, c.rank, c.clazz, c.hp, c.melee, c.ranged);
    }

    public Card flip() {
        return new Card(race, id, name, rank, clazz, hp, melee.stream().map(a -> new int[]{a[0], -a[1], a[2]}).collect(Collectors.toList()), ranged.stream().map(a -> new int[]{a[0], -a[1], a[2]}).collect(Collectors.toList()));
    }

    @Override
    public String toString() {
        return String.format("[%s (%d) %s %s]", race, id, rank, clazz);
    }
}
