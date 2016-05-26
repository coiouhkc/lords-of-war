package org.abratuhi.lordsofwar.model;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * (C) Copyright 2015-current Alexei Bratuhin.
 *
 * @author bratuhia
 */
public class DeckFactoryTest {

    @Test
    public void testParseDwarves() throws IOException {
        List<Card> dwarves = new DeckFactory().parseFile("dwarves.csv", Race.DWARF);
        assertNotNull(dwarves);
        assertEquals(36, dwarves.size());
    }

    @Test
    public void testParseOrcs() throws IOException {
        List<Card> orcs = new DeckFactory().parseFile("orcs.csv", Race.ORC);
        assertNotNull(orcs);
        assertEquals(36, orcs.size());
    }

    @Test
    public void testParseTemplars() throws IOException {
        List<Card> templars = new DeckFactory().parseFile("templars.csv", Race.TEMPLAR);
        assertNotNull(templars);
        assertEquals(36, templars.size());
    }

    @Test
    public void testParseUndead() throws IOException {
        List<Card> undead = new DeckFactory().parseFile("undead.csv", Race.UNDEAD);
        assertNotNull(undead);
        assertEquals(36, undead.size());
    }

    @Test
    public void testParseElves() throws IOException {
        List<Card> elves = new DeckFactory().parseFile("elves.csv", Race.ELF);
        assertNotNull(elves);
        assertEquals(36, elves.size());
    }

    @Test
    public void testParseLizardmen() throws IOException {
        List<Card> lizardmen = new DeckFactory().parseFile("lizardmen.csv", Race.LIZARDMAN);
        assertNotNull(lizardmen);
        assertEquals(36, lizardmen.size());
    }
}
