package org.abratuhi.lordsofwar.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.abratuhi.lordsofwar.model.Board;
import org.abratuhi.lordsofwar.model.Card;
import org.abratuhi.lordsofwar.model.Move;
import org.abratuhi.lordsofwar.model.Player;
import org.abratuhi.lordsofwar.model.Race;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * (C) Copyright 2015-current Alexei Bratuhin.
 *
 * @author bratuhia
 */
public class Game {

    private final static Logger LOG = Logger.getLogger(Game.class);

    private GameEngine engine = new GameEngine();
    private LinkedList<GameState> states = new LinkedList<>();

    public static void main(String[] args) {

        final Game game = new Game();
        game.reset();

        JButton btnReset = new JButton("0");
        JButton btnBack = new JButton("<");
        JButton btnNext = new JButton(">");

        JPanel control = new JPanel();
        control.setLayout(new BoxLayout(control, BoxLayout.X_AXIS));
        control.add(Box.createHorizontalGlue());
        control.add(btnReset);
        control.add(btnBack);
        control.add(btnNext);
        control.add(Box.createHorizontalGlue());


        JPanel content = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);

                int cwidth = this.getWidth() / Board.WIDTH;
                int cheight = this.getHeight() / Board.HEIGHT;

				int [][] ama = game.getState().getBoard().getAttackMap(game.getState().getActivePlayer(), game.getState().getInactivePlayer());
				int [][] amp = game.getState().getBoard().getAttackMap(game.getState().getInactivePlayer(), game.getState().getActivePlayer());

                for (int i = 0; i < Board.WIDTH; i++) {
                    for (int j = 0; j < Board.HEIGHT; j++) {
                        // compute mid
                        int midx = i * cwidth + cwidth / 2;
                        int midy = j * cheight + cheight / 2;

                        // draw card border
                        g.drawRect(i * cwidth, j * cheight, cwidth, cheight);

						// init font
						Font ff = g.getFont();

						// draw active/passive attack
						ff = g.getFont();
						g.setFont(ff.deriveFont((float) ff.getSize() / 1.5f));
						g.drawString(String.valueOf(ama[i][j] + "/" + amp[i][j]), midx - ff.getSize(), midy - ff.getSize());
						g.setFont(ff);

                        // draw card content
                        Card c = game.getState().getBoard().get0(i, j);
                        if (c != null) {
                            Player p = game.getState().getPlayer1().hasOnBoard(c) ? game.getState().getPlayer1() : game.getState().getPlayer2();

                            // draw player id
                            Color cc = g.getColor();
							int[] raceColor = game.getColor(c.getRace(), p.equals(game.getState().getActivePlayer()));
                            g.setColor(new Color(raceColor[0], raceColor[1], raceColor[2]));
                            g.fillOval(midx - cwidth / 20, midy - cheight / 20, cwidth / 10, cheight / 10);
                            g.setColor(cc);

                            // draw hp
                            g.setFont(ff.deriveFont((float) ff.getSize() * 1.5f));
                            g.drawString(String.valueOf(c.getHp()), midx, midy);
                            g.setFont(ff);

                            // draw melee attack
                            for (int[] meleeTriple : c.getMelee()) {
                                if (meleeTriple[2] > 0) {
                                    g.drawString(String.valueOf(meleeTriple[2]),
                                            midx + meleeTriple[0] * cwidth / 2 + (meleeTriple[0] > 0 ? -g.getFont().getSize() : 0),
                                            midy + meleeTriple[1] * cheight / 2 + (meleeTriple[1] < 0 ? g.getFont().getSize() : 0));
                                }
                            }

                            // draw ranged attack
                            ff = g.getFont();
                            g.setFont(ff.deriveFont((float) ff.getSize() / 1.5f));
                            c.getRanged().stream().filter(rangedTriple -> rangedTriple[2] > 0).forEach(rangedTriple -> {
                                g.drawString(String.valueOf(rangedTriple[2]), midx + rangedTriple[0] * cwidth / 10, midy + rangedTriple[1] * cheight / 10);
                            });
                            g.setFont(ff);

                        }
                    }
                }
            }
        };

        final JTextField fldCommand = new JTextField();
        final JButton btnEnter = new JButton("\u23CE");

        fldCommand.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    game.handleCommand(fldCommand.getText());
                    fldCommand.setText("");
					content.repaint();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                // do nothing
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // do nothing
            }
        });

        btnEnter.addActionListener(e -> {
            game.handleCommand(fldCommand.getText());
            fldCommand.setText("");
        });

        JPanel command = new JPanel();
        command.setLayout(new BoxLayout(command, BoxLayout.X_AXIS));
        command.add(fldCommand);
        command.add(btnEnter);


        JFrame jf = new JFrame();
        jf.setTitle("Lords of War :: Simulator");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setLayout(new BorderLayout());
        jf.setSize(new Dimension(1024, 768));
        jf.setResizable(true);

        jf.add(control, BorderLayout.NORTH);
        jf.add(content, BorderLayout.CENTER);
        jf.add(command, BorderLayout.SOUTH);


        jf.setVisible(true);


        btnReset.addActionListener(e -> {
            game.reset();
            jf.repaint();
        });

        btnBack.addActionListener(e -> {
            game.prevState();
            jf.repaint();
        });

        btnNext.addActionListener(e -> {
            GameState state = game.getState();
            GameEngine engine = game.getEngine();

            if (state.isGameOver()) {
                return;
            }

            GameState nextstate = game.computeNextState(state, engine);
            game.addState(nextstate);

            if (nextstate.isGameOver()) {
                LOG.debug("Gameover: winner is " + (nextstate.isWinner1()? "player1" : "player2"));
            }

            jf.repaint();
        });
    }

    private void reset() {
        GameState state = GameState.init(Race.DWARF, Race.ORC);
        states = new LinkedList<>();
        states.add(state);
    }

    private void prevState() {
        if (states.size() > 1) {
            states.removeLast();
        }
    }

    private GameState nextState() {
        GameState next = new GameState(getState());
        states.addLast(next);
        return next;
    }

    public GameState computeNextState(final GameState gs, final GameEngine ge) {
        Player active = gs.getActivePlayer();
        List<Move> moves = ge.getPossibleMovesForPlayer(gs, active);
        Move nextmove = moves.parallelStream().max((m1, m2) -> {
			Pair<GameState, Float> res1 = ge.applyMoveOfActivePlayer(new GameState(gs), m1);
			Pair<GameState, Float> res2 = ge.applyMoveOfActivePlayer(new GameState(gs), m2);
			return (res1.getRight().compareTo(res2.getRight()));
        }).get();


        if (nextmove != null) {
            if (CollectionUtils.isNotEmpty(active.getHands())) {
                Pair<GameState, Float> res = ge.applyMoveOfActivePlayer(new GameState(gs), nextmove);
               return res.getLeft();
            }
        }

        return null;
    }

    private void addState(GameState state) {
        states.addLast(state);
    }

    private void handleCommand(String command) {
        LOG.debug("Command: " + command);
		String cmd = command;
		if (cmd.split("\\s+").length > 1) {
			cmd = cmd.split("\\s+")[0];
		}
        switch (cmd) {
			case "am_a":
				LOG.debug("\n" + Arrays.deepToString(getState().getBoard().getAttackMap(getState().getActivePlayer(), getState().getInactivePlayer())).replaceAll("], ", "], \n"));
				break;
			case "am_p":
				LOG.debug("\n" + Arrays.deepToString(getState().getBoard().getAttackMap(getState().getInactivePlayer(), getState().getActivePlayer())).replaceAll("], ", "], \n"));
				break;
			case "dm_a":
				LOG.debug("\n" + Arrays.deepToString(getState().getBoard().getDefenceMap(getState().getActivePlayer())).replaceAll("], ", "], \n"));
				break;
			case "dm_p":
				LOG.debug("\n" + Arrays.deepToString(getState().getBoard().getDefenceMap(getState().getInactivePlayer())).replaceAll("], ", "], \n"));
				break;
			case "b":
				LOG.debug("\n" + Arrays.deepToString(getState().getBoard().getBoardMap()).replaceAll("], ", "], \n"));
				break;
			case "load":
				String path = command.split("\\s+")[1];
				try {
					GameStateListWrapper gsl = new ObjectMapper().readValue(FileUtils.readFileToString(new File(path)), GameStateListWrapper.class);
					this.reset();
					this.states.addAll(gsl.getStates());

				} catch (IOException e) {
					LOG.error(e);
				}
				break;
			case "stats":
				LOG.debug("Stats: ");
				LOG.debug("Active: " + getState().getActivePlayer().getStats());
				LOG.debug("Passive: " + getState().getInactivePlayer().getStats());
			default: break;
		}
    }

    private GameState getState() {
        return states.getLast();
    }

    private GameEngine getEngine() {
        return engine;
    }

	private int[] getColor (Race race, boolean isActive) {
		switch (race) {
			case DWARF: return new int[]{255, 0, 0};
			case ELF: return new int[]{0, 255, 0};
			case LIZARDMAN: return new int[]{0, 0, 255};
			case ORC: return new int[]{255, 0, 255 };
			case TEMPLAR: return new int[]{255, 255, 0};
			case UNDEAD: return new int[]{0, 255, 255};
		}
		return new int[]{127, 127, 127};
	}

}
