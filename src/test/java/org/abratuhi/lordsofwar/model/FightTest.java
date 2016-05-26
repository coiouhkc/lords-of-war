package org.abratuhi.lordsofwar.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.abratuhi.lordsofwar.game.Game;
import org.abratuhi.lordsofwar.game.GameEngine;
import org.abratuhi.lordsofwar.game.GameState;
import org.abratuhi.lordsofwar.game.GameStateListWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * (C) Copyright 2015-current Alexei Bratuhin.
 *
 * @author bratuhia
 */
public class FightTest {

    public static final Logger LOG = Logger.getLogger(FightTest.class);

    static {
        Logger.getRootLogger().setLevel(Level.ERROR);
        LOG.setLevel(Level.INFO);
        //Logger.getLogger(Board.class).setLevel(Level.DEBUG);
		//Logger.getLogger(GameEngine.class).setLevel(Level.ERROR);
    }

    public static final int FIGHTS = 20;

    private GameEngine engine = new GameEngine();

    private boolean fight(Race race1, Race race2) throws IOException {
        GameStateListWrapper gsl = new GameStateListWrapper();
        GameState state = GameState.init(race1, race2);
        gsl.getStates().add(state);
        while(!state.isGameOver() && CollectionUtils.isNotEmpty(engine.getPossibleMovesForPlayer(state, state.getActivePlayer()))) {
			try {
				state = new Game().computeNextState(state, engine);
                gsl.getStates().add(new GameState(state));
			} catch (Exception e) {
				LOG.error(e);
				new ObjectMapper().writeValue(new File("error.state"), gsl);
				throw e;
			}
        }
        return state.isWinner1();
    }

    @Test
    public void testSingleFight() throws IOException {
        for (int i =0; i < FIGHTS; i++) {
            LOG.info("Take " + i);
            fight(Race.DWARF, Race.ORC);
        }
    }

    @Test
    public void testFightMatrix() throws InterruptedException {
        Map<Race, Map<Race, Float>> result = new HashMap<>();

        ExecutorService executorService = Executors.newFixedThreadPool(8);

        for (Race first : Race.values()) {
            for (Race second : Race.values()) {

                executorService.execute(() -> {
                    int player1wins = 0;
                    for (int i = 0; i < FIGHTS; i++) {
						boolean player1win = false;
						try {
							player1win = fight(first, second);
						} catch (IOException e) {
							LOG.error(e);
						}
						if (player1win) {
                            player1wins++;
                        }
                    }

                    Float ratio = player1wins * 1.0f / FIGHTS;

                    if (!result.containsKey(first)) { result.put(first, new HashMap<>()); }
                    result.get(first).put(second, ratio);

                    LOG.info(MessageFormat.format("{1} vs. {2} win ratio is {0, number, ##.##}", ratio, first, second));
                });

            }
        }

        executorService.shutdown();
        executorService.awaitTermination(12L, TimeUnit.HOURS);

        logResult(result);
    }

    private void logResult(Map<Race, Map<Race, Float>> result) {
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.rightPad("R1\\R2", 10));
        for (Race top: Race.values()) {
            sb.append(StringUtils.rightPad(top.name(), 10));
        }
        sb.append("\n");

        for (Race left: Race.values()) {
            sb.append(StringUtils.rightPad(left.name(), 10));
            for (Race top: Race.values()) {
                Float ratio = result.get(left).get(top);
                sb.append(StringUtils.rightPad(MessageFormat.format("{0, number, ##.##}", ratio), 10));
            }
            sb.append("\n");
        }

        LOG.info(MessageFormat.format("\n {0}", sb.toString()));

    }
}
