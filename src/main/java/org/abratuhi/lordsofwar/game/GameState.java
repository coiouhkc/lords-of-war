package org.abratuhi.lordsofwar.game;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.abratuhi.lordsofwar.model.Board;
import org.abratuhi.lordsofwar.model.DeckFactory;
import org.abratuhi.lordsofwar.model.Player;
import org.abratuhi.lordsofwar.model.Race;
import org.apache.commons.collections4.CollectionUtils;

/**
 * (C) Copyright 2015-current Alexei Bratuhin.
 *
 * @author bratuhia
 */
public class GameState {

    private static final int CARD_HAND_SIZE = 6;

    @Getter @Setter private Player player1;
    @Getter @Setter private Player player2;
    @Getter @Setter private Board board;
    @Getter @Setter private int cmove;

    public GameState() {}

    public GameState(Player player1, Player player2, Board board, int cmove) {
        this();
        this.player1 = new Player(player1);
        this.player2 = new Player(player2);
        this.board = new Board(board);
        this.cmove = cmove;
    }

    public GameState(GameState gs) {
        this(gs.getPlayer1(), gs.getPlayer2(), gs.getBoard(), gs.getCmove());
    }


    @JsonIgnore
    public Player getActivePlayer() {
        switch (getCmove() % 2) {
            case 0:
                return getPlayer1();
            case 1:
                return getPlayer2();
            default:
                return getPlayer1(); // o rly ?!
        }
    }

    @JsonIgnore
    public Player getInactivePlayer() {
        return getActivePlayer().equals(getPlayer1()) ? getPlayer2() : getPlayer1();
    }

	@JsonIgnore
    public boolean isGameOver() {
        return isLost(player1) || isLost(player2);
    }

	@JsonIgnore
    public boolean isLost(Player p) {
        return p.countLost() >= 20 || p.countLostCommand() >= 4 || CollectionUtils.isEmpty(p.getHands());
    }

    @JsonIgnore
    public Player getWinner() {
        return  (!isLost(player1)) ? player1: player2;
    }

	@JsonIgnore
    public boolean isWinner1() {
        return !isLost(player1);
    }

    public static GameState init(Race race1, Race race2) {
        Player player1 = new Player();
        Player player2 = new Player();
        Board board = new Board();

        player1.setColor(new int[]{255, 0, 0});
        player1.initWithDeckAndHandsize(new DeckFactory().deck(race1), CARD_HAND_SIZE);

        player2.setColor(new int[]{0, 0, 255});
        player2.initWithDeckAndHandsize(new DeckFactory().flipdeck(new DeckFactory().deck(race2)), CARD_HAND_SIZE);

        int cmove = 0;

        return new GameState(player1, player2, board, cmove);
    }


    public void checkConstistency() {
        getActivePlayer().getBoard().forEach(card -> {assert getBoard().find0(card) != null; });
        getInactivePlayer().getBoard().forEach(card -> {assert getBoard().find0(card) != null; });
    }
}
