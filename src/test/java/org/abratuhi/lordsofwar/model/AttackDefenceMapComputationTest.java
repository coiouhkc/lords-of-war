package org.abratuhi.lordsofwar.model;

import org.abratuhi.lordsofwar.game.GameEngine;
import org.abratuhi.lordsofwar.game.GameState;
import org.junit.Assert;
import org.junit.Test;

/**
 * (C) Copyright 2015-current Alexei Bratuhin.
 *
 * @author bratuhia
 */
public class AttackDefenceMapComputationTest {

	@Test
	public void testFirstMove() {
		Race race1 = Race.DWARF;
		Race race2 = Race.DWARF;

		GameEngine ge = new GameEngine();
		GameState gs = GameState.init(race1, race2);



		Assert.assertEquals(0, gs.getCmove());
		Assert.assertEquals(6, gs.getActivePlayer().getHands().size());
		Assert.assertEquals(0, gs.getActivePlayer().getBoard().size());
		Assert.assertEquals(0, gs.getActivePlayer().getLost().size());
		Assert.assertEquals(30, gs.getActivePlayer().getClosed().size());


		Move m = new Move(
				new Move.Action(
						Move.ActionType.CARD_TO_BOARD,
						gs.getActivePlayer().getHands().stream().filter(card -> card.getRank().equals(Rank.GENERAL)).findAny().get(),
						Board.PLAYER1_FIRST_MOVE[0],
						Board.PLAYER1_FIRST_MOVE[1]),
				new Move.Action(Move.ActionType.CARD_FROM_DECK));

		GameState gs1 = ge.applyMoveOfActivePlayer(new GameState(gs), m).getLeft();

		Assert.assertEquals(1, gs1.getCmove());
		Assert.assertEquals(6, gs1.getInactivePlayer().getHands().size());
		Assert.assertEquals(1, gs1.getInactivePlayer().getBoard().size());
		Assert.assertEquals(0, gs1.getInactivePlayer().getLost().size());
		Assert.assertEquals(29, gs1.getInactivePlayer().getClosed().size());

		int [][] ama = gs1.getBoard().getAttackMap(gs1.getActivePlayer(), gs1.getInactivePlayer());
		int [][] dma = gs1.getBoard().getDefenceMap(gs1.getActivePlayer());
		int [][] amp = gs1.getBoard().getAttackMap(gs1.getInactivePlayer(), gs1.getInactivePlayer());
		int [][] dmp = gs1.getBoard().getDefenceMap(gs1.getInactivePlayer());

		Assert.assertEquals(7, dmp[Board.PLAYER1_FIRST_MOVE[0]][Board.PLAYER1_FIRST_MOVE[1]]);

		Assert.assertEquals(4, amp[Board.PLAYER1_FIRST_MOVE[0]-1][Board.PLAYER1_FIRST_MOVE[1]-1]);
		Assert.assertEquals(3, amp[Board.PLAYER1_FIRST_MOVE[0]-1][Board.PLAYER1_FIRST_MOVE[1]]);
		Assert.assertEquals(5, amp[Board.PLAYER1_FIRST_MOVE[0]][Board.PLAYER1_FIRST_MOVE[1]-1]);


		Assert.assertNotNull(ama);
		Assert.assertNotNull(dma);
		Assert.assertNotNull(amp);
		Assert.assertNotNull(dmp);


	}
}
