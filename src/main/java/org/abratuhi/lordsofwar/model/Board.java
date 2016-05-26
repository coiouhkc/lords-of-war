package org.abratuhi.lordsofwar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * (C) Copyright 2015-current Alexei Bratuhin.
 *
 * @author bratuhia
 */
public class Board {

    private final static Logger LOG = Logger.getLogger(Board.class);

    public final static int WIDTH = 7;
    public final static int HEIGHT = 6;

    public final static int[] PLAYER1_FIRST_MOVE = new int[]{2, 3};
    public final static int[] PLAYER2_FIRST_MOVE = new int[]{4, 2};


    @Getter @Setter private Card[][] cards = new Card[WIDTH][HEIGHT];

    public Board() {}

    public Board(Card[][] cards) {
        for (int i=0; i<WIDTH; i++) {
            System.arraycopy(cards[i], 0, this.cards[i], 0, HEIGHT);
        }
    }

    public Board(Board b) {this(b.cards); }

    public boolean within0(int x, int y) {
        return (x>=0 && y >=0 && x<WIDTH && y<HEIGHT);
    }

    public Card get0(int x, int y) {
        return within0(x, y)? cards[x][y] : null;
    }

    public void set0(int x, int y, Card c) {
//        LOG.debug("set0: " + x + ", " + y + ": " + (c!=null? c.toString(): "null"));
		if (c!=null && get0(x, y) != null) {
			LOG.error("Card Override!");
		}
		if (within0(x, y)) cards[x][y] = c;
    }

    public void put0(int x, int y, Card c) {
        set0(x, y, c);
    }

    public void remove0(int x, int y) {put0(x, y, null);}
    public void remove0(Card c){
        Pair<Integer, Integer> xy = find0(c);
		remove0(xy.getLeft(), xy.getRight());
    }


    public Pair<Integer, Integer> find0(Card c) {
        for (int i=0; i<WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                if (cards[i][j] != null && cards[i][j].equals(c)) {
                    return Pair.of(i, j);
                }
            }
        }
        return null;
    }

	@JsonIgnore
    public List<int[]> getAdjacentFree() {
        Set<int[]> result = new HashSet<>();
        for (int i=0; i<WIDTH; i++) {
            for (int j=0; j<HEIGHT; j++) {
                if (cards[i][j] == null &&
                        (
                                get0(i-1,j-1) != null ||
                                get0(i-1,j) != null ||
                                get0(i-1,j+1) != null ||
                                get0(i,j-1) != null ||
                                get0(i,j+1) != null ||
                                get0(i+1,j-1) != null ||
                                get0(i+1,j) != null ||
                                get0(i+1,j+1) != null
                        )
                        ) {
                    result.add(new int[]{i, j});
                }
            }
        }
        return new ArrayList<>(result);
    }

	@JsonIgnore
    public int[][] getAttackMap(Player p, Player inactive) {
        return getAttackMapMeleeAndRanged(p, inactive);
    }

	@JsonIgnore
    public int[][] getAttackMapMelee(Player p) {
        int[][] am = new int[WIDTH][HEIGHT];
        for (int i=0; i<WIDTH; i++) {
            for (int j=0; j<HEIGHT; j++) {
                Card c = cards[i][j];
                if (c != null && p.hasOnBoard(c)) {
                    for (int[] melee: c.getMelee()) {
                        if (within0(i + melee[0], j + melee[1])) {
                            am[i + melee[0]][j + melee[1]] += melee[2];
                        }
                    }
                }
            }
        }
        return am;
    }

    public boolean isEngagedBy(Card c, Player p){
        return isEngagedBy(c, getAttackMapMelee(p));
    }

    public boolean isEngagedBy(Card c, int[][] am){
        Pair<Integer, Integer> xy = find0(c);
        int x = xy.getLeft();
        int y = xy.getRight();
        return am[x][y] != 0;
    }

	@JsonIgnore
    public int[][] getAttackMapMeleeAndRanged(Player p, Player inactive) {
        int [][] amInactive = getAttackMapMelee(inactive);
        int[][] am = new int[WIDTH][HEIGHT];
        for (int i=0; i<WIDTH; i++) {
            for (int j=0; j<HEIGHT; j++) {
                Card c = cards[i][j];
                if (c != null && p.hasOnBoard(c)) {
                    switch (c.getClazz()) {
                        case RANGED: {
                            // ranged units prefer to attack ranged if not engaged
                            if (!isEngagedBy(c, amInactive)) {
                                for (int[] ranged : c.getRanged()) {
                                    if (within0(i + ranged[0], j + ranged[1])) {
                                        am[i + ranged[0]][j + ranged[1]] += ranged[2];
                                    }
                                }
                            } else { // otherwise (if engaged) attack melee
                                for (int[] melee: c.getMelee()) {
                                    if (within0(i + melee[0], j + melee[1])) {
                                        am[i + melee[0]][j + melee[1]] += melee[2];
                                    }
                                }
                            }
                            break;
                        }
                        default: {
                            for (int[] melee: c.getMelee()) {
                                if (within0(i + melee[0], j + melee[1])) {
                                    am[i + melee[0]][j + melee[1]] += melee[2];
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        return am;
    }

	@JsonIgnore
    public int[][] getDefenceMap(Player p) {
        int[][] dm = new int[WIDTH][HEIGHT];
        for (int i=0; i<WIDTH; i++) {
            for (int j=0; j<HEIGHT; j++) {
                Card c = cards[i][j];
                if (c != null && p.hasOnBoard(c)) {
                    dm[i][j] = c.getHp();
                }
            }
        }
        return dm;
    }

	@JsonIgnore
	public String[][] getBoardMap() {
		String[][] m = new String[WIDTH][HEIGHT];
		for (int i=0; i<WIDTH; i++) {
			for (int j=0; j<HEIGHT; j++) {
				Card c = cards[i][j];
				if (c != null) {
					m[i][j] = c.toString();
				} else {
					m[i][j] = "-";
				}
			}
		}
		return m;
	}
}
