package org.abratuhi.lordsofwar.game;

import org.abratuhi.lordsofwar.model.Board;
import org.abratuhi.lordsofwar.model.Card;
import org.abratuhi.lordsofwar.model.Move;
import org.abratuhi.lordsofwar.model.Player;
import org.abratuhi.lordsofwar.model.Rank;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * (C) Copyright 2015-current Alexei Bratuhin.
 *
 * @author bratuhia
 */
public class GameEngine {

    private final static Logger LOG = Logger.getLogger(GameEngine.class);

    private final float weightCardTaken = 1.0f;
    private final float weightCardLost = 1.0f;
    private final float weightCommandTaken = 2.0f;
    private final float weightCommandLost = 2.0f;
    private final float weightGeneralTaken = 3.0f;
    private final float weightGeneralLost = 3.0f;
    private final float weightCardAttack = 0.5f;
    private final float weightCardUnderAttack = 0.5f;
    private final float weightFieldAttack = 0.25f;


    public List<Move> getPossibleMovesForPlayer(GameState gs, Player p) {
        List<Move> moves = new ArrayList<>();

        switch(gs.getCmove()) {
            case 0:
                moves.add(getFirstMovePlayer1(p));
                break;
            case 1:
                moves.add(getFirstMovePlayer2(p));
                break;
            default:
                List<int[]> possibleFields = gs.getBoard().getAdjacentFree();
                List<Card> possibleCards = p.getHands();
                for (int[] xy: possibleFields) {
                    for(Card c: possibleCards) {
                        Move.Action put = new Move.Action(Move.ActionType.CARD_TO_BOARD, c, xy[0], xy[1]);

                        Move.Action take = new Move.Action(Move.ActionType.CARD_FROM_DECK);
                        moves.add(new Move(put, take));

						// FIXME: causes NPE during evaluation
//                      Player other = p.equals(gs.getActivePlayer())? gs.getInactivePlayer() : gs.getActivePlayer();
//                      int[][] amInactive = gs.getBoard().getAttackMapMelee(other);
//						p.getBoard().stream()
//							.filter(cc -> !cc.equals(c)
//									&& !gs.getBoard().isEngagedBy(cc, amInactive)
//									&& gs.getBoard().find0(cc) != null)
//							.forEach(cc -> {
//								Move.Action retreat = new Move.Action(Move.ActionType.CARD_FROM_BOARD, cc);
//								moves.add(new Move(put, retreat));
//							});
                    }
                }
                break;
        }

        return moves;
    }


    public Pair<GameState, Float> applyMoveOfActivePlayer(GameState gs, Move m) {
		gs.checkConstistency();

        GameState state = new GameState(gs);
        Float result = 0.0f;
        Player active = state.getActivePlayer();
        Player inactive = state.getInactivePlayer();

		// assert
		if (m.getTakeAction().getActionType().equals(Move.ActionType.CARD_FROM_BOARD)
				&& state.getBoard().find0(m.getTakeAction().getC()) == null) {
			LOG.error("Player has no card on board intended for take-back!");
		}

        //
        active.play(m.getPutAction().getC());
        state.getBoard().put0(m.getPutAction().getX(), m.getPutAction().getY(), m.getPutAction().getC());

        // compute damage
        int[][] amActive = state.getBoard().getAttackMap(active, inactive);
        int[][] amInactive = state.getBoard().getAttackMap(inactive, active);
        int[][] dmActive = state.getBoard().getDefenceMap(active);
        int[][] dmInactive = state.getBoard().getDefenceMap(inactive);

        for (int i = 0; i < Board.WIDTH; i++) {
            for (int j = 0; j < Board.HEIGHT; j++) {
                Card rc = state.getBoard().get0(i, j);
                if (rc == null) {
                    result += weightFieldAttack * amActive[i][j];
                } else {
                    if (active.hasOnBoard(rc)) {
                        result -= weightCardUnderAttack * amInactive[i][j];
                        if (dmActive[i][j] < amInactive[i][j]) {
                            result -= getLossWeightForCard(rc) * 1;

							assert (active.getBoard().contains(rc));
							assert (m.getTakeAction().getActionType().equals(Move.ActionType.CARD_FROM_DECK)
									|| (
									m.getTakeAction().getActionType().equals(Move.ActionType.CARD_FROM_BOARD)
									&& !m.getTakeAction().getC().equals(rc)
							));
							active.loose(rc);
                            state.getBoard().remove0(rc);

							if (m.getTakeAction().getActionType().equals(Move.ActionType.CARD_FROM_BOARD)
									&& state.getBoard().find0(m.getTakeAction().getC()) == null) {
								LOG.error("Player lost card intended for take-back!");
							}
                        }
                    } else {
                        result += weightCardAttack * dmInactive[i][j];
                        if (amActive[i][j] > dmInactive[i][j]) {
                            result += getWinWeightForCard(rc) * 1;

							assert (inactive.getBoard().contains(rc));
                            inactive.loose(rc);
                            state.getBoard().remove0(rc);
                        }
                    }
                }
            }
        }

        // draw card to reach required hand size
        switch (m.getTakeAction().getActionType()) {
            case CARD_FROM_DECK: active.draw(); break;
            case CARD_FROM_BOARD: {
                active.retreat(m.getTakeAction().getC());
                state.getBoard().remove0(m.getTakeAction().getC());
                break;
            }
            default: break;
        }

        // increment move
        state.setCmove(state.getCmove() + 1);

        //
        LOG.debug(String.format("Additional value of applying %s is %.2f", m, result));

		//
		gs.checkConstistency();

        //
        return Pair.of(state, result);
    }

    public float getLossWeightForCard(Card rc) {
        float lossWeight = weightCardLost;
        if (rc.getRank().equals(Rank.COMMAND)) {lossWeight = weightCommandLost;}
        if (rc.getRank().equals(Rank.GENERAL)) {lossWeight = weightGeneralLost;}
        return lossWeight;
    }

    public float getWinWeightForCard(Card rc) {
        float winWeight = weightCardTaken;
        if (rc.getRank().equals(Rank.COMMAND)) {winWeight = weightCommandTaken;}
        if (rc.getRank().equals(Rank.GENERAL)) {winWeight = weightGeneralTaken;}
        return winWeight;
    }

    public Move getFirstMovePlayer1(Player player) {
        Move.Action put = new Move.Action(Move.ActionType.CARD_TO_BOARD, player.getHands().get(0), Board.PLAYER1_FIRST_MOVE[0], Board.PLAYER1_FIRST_MOVE[1]);
        Move.Action take = new Move.Action(Move.ActionType.CARD_FROM_DECK);
        return new Move(put, take);
    }

    public Move getFirstMovePlayer2(Player player) {
        Move.Action put = new Move.Action(Move.ActionType.CARD_TO_BOARD, player.getHands().get(0), Board.PLAYER2_FIRST_MOVE[0], Board.PLAYER2_FIRST_MOVE[1]);
        Move.Action take = new Move.Action(Move.ActionType.CARD_FROM_DECK);
        return new Move(put, take);
    }
}
