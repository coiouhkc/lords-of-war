package org.abratuhi.lordsofwar.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
/**
 * (C) Copyright 2015-current Alexei Bratuhin.
 *
 * @author bratuhia
 */
public class Move {

    public enum ActionType {
        CARD_FROM_DECK,
        CARD_FROM_BOARD,
        CARD_TO_BOARD
    }

    public static class Action {
        @Getter @Setter private ActionType actionType;
        @Getter @Setter private Card c;
        @Getter @Setter private int x;
        @Getter @Setter private int y;

        public Action() {}

        public Action(ActionType actionType) {this(); setActionType(actionType);}

        public Action(ActionType actionType, Card c) {this(actionType); setC(c);}

        public Action(ActionType actionType, Card c, int x, int y) {this(actionType); setC(c); setX(x); setY(y);}

        @Override public String toString() {return String.format("[action: %s, %s, %d, %d]", actionType, c, x, y);}
    }

    @Getter @Setter private List<Action> actions;

    public Move() {}

    public Move(Action a1, Action a2) {
        this();
        setActions(new ArrayList<>());
        actions.add(a1);
        actions.add(a2);
    }

    public Action getPutAction() {
        return actions.get(0);
    }

    public Action getTakeAction() {
        return actions.get(1);
    }

    @Override
    public String toString() {
        return String.format("[move: %s, %s]", getPutAction(), getTakeAction());
    }

}
