package org.abratuhi.lordsofwar.game;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * (C) Copyright 2015-current Alexei Bratuhin.
 *
 * @author bratuhia
 */
public class GameStateListWrapper {

	@Getter @Setter private List<GameState> states = new ArrayList<>();
}
