package org.abratuhi.lordsofwar.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * (C) Copyright 2015-current Alexei Bratuhin.
 *
 * @author bratuhia
 */
public class Player {

    private final static Logger LOG = Logger.getLogger(Player.class);
    @Getter @Setter private int[] color = new int[] {0, 0, 0};

    @Getter @Setter private List<Card> closed = new ArrayList<>(); // cards in closed deck
    @Getter @Setter private List<Card> hands = new ArrayList<>(); // cards on hands
    @Getter @Setter private List<Card> board = new ArrayList<>(); // cards on board
    @Getter @Setter private List<Card> lost = new ArrayList<>(); // lost cards

    public Player() {}

    public Player(Player p) {
        this();
        this.color = p.getColor();
        this.closed = new ArrayList<>(p.getClosed());
        this.hands = new ArrayList<>(p.getHands());
        this.board = new ArrayList<>(p.getBoard());
        this.lost = new ArrayList<>(p.getLost());
    }

    public boolean has(Card c) {
        return (CollectionUtils.isNotEmpty(closed) && closed.contains(c))
                || (CollectionUtils.isNotEmpty(hands) && hands.contains(c))
                || (CollectionUtils.isNotEmpty(board) && board.contains(c))
                || (CollectionUtils.isNotEmpty(lost) && lost.contains(c));
    }

	public boolean hasOnBoard(Card c) {
		return (CollectionUtils.isNotEmpty(board) && board.contains(c));
	}

    public void initWithDeckAndHandsize(List<Card> deck, int handsize) {
        Card general = deck.stream().filter(c -> c.getRank().equals(Rank.GENERAL)).collect(Collectors.toList()).get(0);
        deck.remove(general);
        Collections.shuffle(deck);
        List<Card> hand = new ArrayList<>();
        hand.add(general);
        while (hand.size() < handsize) {
            hand.add(deck.remove(0));
        }
        Collections.shuffle(hand);

        closed = deck;
        hands = hand;
        board = new ArrayList<>();
        lost = new ArrayList<>();
    }

    public void play(Card c) {
        if (hands.remove(c)) {
            board.add(c);
        } else {
			LOG.error("Player could not play card: " + c.toString());
		}
    }

    public void draw() {
        if(CollectionUtils.isNotEmpty(closed)) {
            hands.add(closed.remove(0));
        }
    }

    public void retreat(Card c) {
        if (CollectionUtils.isNotEmpty(board) && board.remove(c)) {
            hands.add(c);
        } else {
			LOG.error("Player could not retreat card: " + c.toString());
		}
    }

    public void loose(Card c) {
        if (board.remove(c)) {
            lost.add(c);
        } else {
            LOG.error("Player could not loose card: " + c.toString());
        }
    }

    public int countLost() {
        return lost.size();
    }

    public int countLostCommand() {
        return lost.stream().filter(c -> c.getRank().equals(Rank.GENERAL) || c.getRank().equals(Rank.COMMAND)).collect(Collectors.toList()).size();
    }

    public String getStats() {
        return MessageFormat.format("Closed {0}, Hands {1}, Board {2}, Lost {3}", closed.size(), hands.size(), board.size(), lost.size());
    }
}
