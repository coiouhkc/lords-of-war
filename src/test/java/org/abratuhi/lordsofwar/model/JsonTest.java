package org.abratuhi.lordsofwar.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.abratuhi.lordsofwar.game.Game;
import org.abratuhi.lordsofwar.game.GameEngine;
import org.abratuhi.lordsofwar.game.GameState;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * (C) Copyright 2015-current Alexei Bratuhin.
 *
 * @author bratuhia
 */
public class JsonTest {

	private static final Logger LOG = Logger.getLogger(JsonTest.class);

	private ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testToJson() throws JsonProcessingException {
		GameState state = GameState.init(Race.DWARF, Race.ORC);
		LOG.debug(mapper.writeValueAsString(state));
	}

	@Test
	public void testFromJson() throws IOException {
		GameEngine engine = new GameEngine();
		GameState state = GameState.init(Race.DWARF, Race.ORC);
		state = new Game().computeNextState(state, engine);
		GameState restate = mapper.readValue(mapper.writeValueAsString(state), GameState.class);
		assertEquals(state.getCmove(), restate.getCmove());
		assertEquals(state.getActivePlayer().getColor(), state.getActivePlayer().getColor());
	}
}
