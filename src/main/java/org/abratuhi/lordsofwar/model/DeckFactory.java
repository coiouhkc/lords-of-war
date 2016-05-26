package org.abratuhi.lordsofwar.model;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * (C) Copyright 2015-current Alexei Bratuhin.
 *
 * @author bratuhia
 */
public class DeckFactory {

    public List<int[]> parseAttack(String s) {
        return Arrays.asList(s.replaceAll("\\s", "").split(";"))
                .stream()
                .map(field ->
                        StringUtils.isNotEmpty(field) && StringUtils.containsNone(field, "_")? new int[]{
                                Integer.parseInt(field.substring(1, field.length()-1).split(",")[0]),
                                Integer.parseInt(field.substring(1, field.length()-1).split(",")[1]),
                                Integer.parseInt(field.substring(1, field.length()-1).split(",")[2])
                        } : null
                )
                .filter(obj -> obj != null)
                .collect(Collectors.toList());
    }

    public List<Card> parseFile(String file, Race race) throws IOException {
        return parse(CSVParser.parse(new File(getClass().getClassLoader().getResource(file).getFile()), Charset.forName("UTF-8"), CSVFormat.RFC4180.withHeader()), race);
    }

    public List<Card> parseString(String csv, Race race) throws IOException {
        return parse(CSVParser.parse(csv, CSVFormat.RFC4180.withHeader()), race);
    }

    public List<Card> parse(CSVParser parser, Race race) throws IOException {
        return parser.getRecords()
                .stream()
                .map(csvrec -> new Card(
                        race,
                        csvrec.get("Id"),
                        csvrec.get("Name"),
                        csvrec.get("Rank"),
                        csvrec.get("Class"),
                        csvrec.get("Hp"),
                        parseAttack(csvrec.get("Melee")),
                        parseAttack(csvrec.get("Ranged"))))
                .collect(Collectors.toList());
    }

    public List<Card> deck(Race race) {
        try {
            switch (race) {
                case DWARF:
                    return parseFile("dwarves.csv", Race.DWARF);
                case ORC:
                    return parseFile("orcs.csv", Race.ORC);
                case TEMPLAR:
                    return parseFile("templars.csv", Race.TEMPLAR);
                case UNDEAD:
                    return parseFile("undead.csv", Race.UNDEAD);
                case ELF:
                    return parseFile("elves.csv", Race.ELF);
                case LIZARDMAN:
                    return parseFile("lizardmen.csv", Race.LIZARDMAN);
                default:
                    return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    public List<Card> flipdeck(List<Card> deck) {
        return deck.stream().map(Card::flip).collect(Collectors.toList());
    }
}
