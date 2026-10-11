
/**

$$$License$$$

**/

package org.bamboomy.c44.bot.player;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;
import org.bamboomy.c44.bot.board.Board;
import org.bamboomy.c44.bot.board.Place;
import org.bamboomy.c44.bot.move.Check;
import org.bamboomy.c44.bot.move.Move;
import org.bamboomy.c44.bot.piecez.King;
import org.bamboomy.c44.bot.piecez.Piece;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Player {

	private static final int CORES;

	static {

		CORES = Runtime.getRuntime().availableProcessors();
	}

	private static int NB_OF_THREADS = (int) (CORES * 1.5);

	private static int NB_OF_BOARDZ;

	private final Semaphore available = new Semaphore(NB_OF_THREADS);

	private ArrayList<Move> movez = new ArrayList<Move>();

	private Move bestMove;

	private double maxValue;

	// WARNING!!! -> as a king can only take 2 steps (with this value set to 7),
	// if you change this value
	// you should also adapt the check algorithm...
	// also (if you use a search tree) a depth of 7 is probably the deepest you want
	// to go for...
	// take into account that this includes 2 moves of a human (or other bot with a
	// different algorithm)
	// so their choice will differ from the one you calculate either way...
	private static final int MAX_DEPTH = 3;

	private int totalNodes = 0;

	private String[] movezStringz;

	private String output = "";

	private static final boolean DEBUG = false;

	private static final boolean DEBUG_MATE = true;

	@Getter
	private final Color color;

	@Getter
	private final Board board;

	@Getter
	protected final ArrayList<Piece> piecez = new ArrayList<Piece>();

	@Setter
	@Getter
	private King king;

	private int calculated = 0;

	@Getter
	private final Alliance alliance;

	@Getter
	private HashSet<Piece> preventPieces = new HashSet<Piece>();

	@Setter
	@Getter
	private boolean dead = false;

	final static Logger logger = Logger.getLogger(Player.class);

	private int numberOfMove;

	public static boolean MATE_PROFILE = false;

	public Player(int i, Board board) {

		System.out.println("I have: " + NB_OF_THREADS + " threads, jej :)");

		color = Color.getBySeq(i);

		this.board = board;

		if (board.getAlliance().isInAlliance(color)) {

			alliance = board.getAlliance();

		} else {

			alliance = board.getAlliance().getOtherAlliance();
		}

		this.numberOfMove = Integer.parseInt(board.getNumberOfMove());
	}

	public void startCalculation() {

		calculated = 0;

		for (Piece piece : piecez) {

			if (piece.getMovez() != null) {

				movez.addAll(piece.getMovez());

				System.out.println(piece.getMovez().size() + " movez added...");
			}
		}

		NB_OF_BOARDZ = Math.min(NB_OF_THREADS, movez.size());

		(new Thread(new Calculator(movez, board))).start();
	}

	class Calculator implements Runnable {

		private ArrayList<Move> movez;

		private ArrayList<Integer> availableBoardIndexes = new ArrayList<Integer>();
		private ArrayList<Integer> busyBoardIndexes = new ArrayList<Integer>();

		private Board[] boardz = new Board[NB_OF_BOARDZ];

		private Board board;

		public Calculator(ArrayList<Move> movez, Board board) {
			this.movez = new ArrayList<>(movez);
			this.board = board;

			init();
		}

		public void init() {

			bestMove = null;
			maxValue = -Integer.MAX_VALUE;

			for (int i = 0; i < NB_OF_BOARDZ; i++) {

				boardz[i] = new Board(board);
			}
		}

		public synchronized void releaseIndex(int index) {

			if (DEBUG) {

				// log.debug("releasing:" + index);
			}

			for (int i = 0; i < busyBoardIndexes.size(); i++) {

				if (busyBoardIndexes.get(i) == index) {

					boardz[busyBoardIndexes.get(i)] = new Board(board);

					availableBoardIndexes.add(busyBoardIndexes.remove(i));

					return;
				}
			}

			throw new RuntimeException("couldn't find busy index");
		}

		@Override
		public void run() {

			try {

				ArrayList<Thread> threadz = new ArrayList<>();

				for (int i = 0; i < NB_OF_BOARDZ; i++) {

					availableBoardIndexes.add(i);
				}

				// ArrayList<Move> filteredMoves = filterMovez(movez, color.getSeq());

				movezStringz = new String[movez.size()];

				try {

					int counter = 0;

					while (!movez.isEmpty()) {

						log.debug("waiting for next calculator...");

						available.acquire();

						log.debug("going forth with:" + availableBoardIndexes.get(0));

						MoveCalculator calculator = new MoveCalculator(boardz[availableBoardIndexes.get(0)],
								movez.remove(0), board.getColor(), board.getAlliance(), availableBoardIndexes.get(0),
								this, counter++);

						busyBoardIndexes.add(availableBoardIndexes.remove(0));

						Thread thread = new Thread(calculator);
						threadz.add(thread);
						thread.start();
					}

					for (Thread thread : threadz) {
						thread.join();
					}

					System.out.println("maxValue is: " + maxValue);

					board.getPoller().movePiece(bestMove.getPiece().getMd5(), bestMove.getTo().getMd5());

					// bestMove.execute(false);

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (ArrayIndexOutOfBoundsException a) {

				// ignored

			} catch (Throwable t) {

				handleException(t);
			}
		}
	}

	class MoveCalculator implements Runnable {

		private static final int CHECK_MATE = 100_000;

		private Board board;
		private Color color;
		private Move move;
		private Alliance alliance;
		private int index;
		private Calculator calculator;

		private Move[] movesToDepth = new Move[MAX_DEPTH];

		private int nodes = 0;

		private int moveCounter;

		public MoveCalculator(Board board, Move move, Color color, Alliance alliance, int index, Calculator calculator,
				int moveCounter) {
			this.board = board;
			this.color = color;
			this.alliance = alliance;
			this.move = move;
			this.index = index;
			this.calculator = calculator;
			this.moveCounter = moveCounter;
		}

		private void init() {

			board.execute(move);

			Alliance otherAlliance = alliance.getOtherAlliance();

			if (!board.getPlayers()[alliance.getOtherColor(color).getSeq()].isDead()) {

				board.getPlayers()[alliance.getOtherColor(color).getSeq()].calculateMovez();
			}

			board.getPlayers()[color.getSeq()].calculateMovez();

			if (!board.getPlayers()[otherAlliance.getOne().getSeq()].isDead()) {

				board.getPlayers()[otherAlliance.getOne().getSeq()].calculateMovez();
				board.getPlayers()[otherAlliance.getOne().getSeq()].calculateKingMovez();
			}

			if (!board.getPlayers()[otherAlliance.getTwo().getSeq()].isDead()) {

				board.getPlayers()[otherAlliance.getTwo().getSeq()].calculateMovez();
				board.getPlayers()[otherAlliance.getTwo().getSeq()].calculateKingMovez();
			}
		}

		@Override
		public void run() {

			try {

				System.out.println(index);

				ArrayList<Color> otherColors = new ArrayList<Color>();

				double evaluation = 0;

				init();

				Alliance otherAlliance = alliance.getOtherAlliance();

				otherColors.add(otherAlliance.getOne());
				otherColors.add(otherAlliance.getTwo());

				for (Color color : otherColors) {

					if (isPat(color)) {

						evaluation = -50;
					}
				}

				if (evaluation == 0) {

					for (Color color : otherColors) {

						if (isMate(color) == CHECK_MATE) {

							if (DEBUG_MATE) {

								System.out.println("already mate...");
							}

							evaluation = CHECK_MATE * 777;

							break;

						} else {

							if (DEBUG_MATE) {

								System.out.println("not already mate...");
							}
						}
					}
				}

				if (numberOfMove > 7 || MATE_PROFILE) {

					if (evaluation == 0) {

						evaluation = canMate(otherAlliance.getOne());

						if (evaluation == CHECK_MATE) {

							if (DEBUG_MATE) {

								System.out.println(otherAlliance.getOne() + ": -> whole...");
							}
						}
					}

					if (evaluation == 0) {

						evaluation = canMate(otherAlliance.getTwo());

						if (evaluation == CHECK_MATE) {

							if (DEBUG_MATE) {

								System.out.println(otherAlliance.getTwo() + ": -> whole...");
							}
						}
					}
				}

				if (evaluation == 0) {

					System.out.println("Considering other moves...");

					board = new Board(Player.this.board);

					init();

					evaluation = evaluate(1, -Integer.MAX_VALUE, Integer.MAX_VALUE, ((color.getSeq() + 1) % 4));
				}

				move.setMovesToDepth(movesToDepth);

				recieveMove(mapMove(move), evaluation, moveCounter, nodes);

				board = null;// nullify board so it can be garbage collected...

				calculator.releaseIndex(index);

				available.release();

			} catch (ArrayIndexOutOfBoundsException a) {

				// ignored

			} catch (Throwable t) {

				handleException(t);
			}
		}

		private boolean isPat(Color fooColor) {

			if (board.getPlayers()[fooColor.getSeq()].isDead()) {

				return false;
			}

			board.getPlayers()[fooColor.getSeq()].king.clearChecks();

			board.getPlayers()[fooColor.getSeq()].calculateMovez();
			board.getPlayers()[fooColor.getSeq()].calculateKingMovez();

			return !king.isCheck() && !board.getPlayers()[fooColor.getSeq()].canMove();
		}

		private double canMate(Color foo) {

			if (isDead(foo)) {

				return 0;
			}

			ArrayList<Integer> allTurns = new ArrayList<>();

			allTurns.add(foo.getSeq());
			allTurns.add(color.getSeq());

			int depth = 2;

			/*
			 * if (!isDead(alliance.getOtherColor(color))) {
			 * 
			 * allTurns.add(alliance.getOtherColor(color).getSeq());
			 * 
			 * depth = 1; }
			 */

			allTurns.sort(Integer::compare);

			ArrayList<Integer> realTurns = new ArrayList<>();

			boolean collect = false;

			while (realTurns.size() != allTurns.size()) {

				for (Integer integer : allTurns) {

					if (integer == foo.getSeq()) {

						collect = true;
					}

					if (collect) {

						realTurns.add(integer);

						if (realTurns.size() == allTurns.size()) {

							break;
						}
					}
				}
			}

			if (isMate(foo) == CHECK_MATE || canMateIn(depth, foo, realTurns)) {

				if (isMate(foo) == CHECK_MATE) {

					if (DEBUG_MATE) {

						System.out.println("half mate");
					}

				} else {

					if (DEBUG_MATE) {

						System.out.println("half can mate in 2");
					}
				}

				return CHECK_MATE;
			}

			if (DEBUG_MATE) {

				System.out.println("half: not mate! ");
			}

			return 0;
		}

		private boolean canMateIn(int steps, Color foo, ArrayList<Integer> turns) {

			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < steps; i++) {

				sb.append("-");
			}

			String indent = sb.toString();

			if (steps == 0) {

				return false;
			}

			ArrayList<Move> moves = new ArrayList<Move>();

			if (!board.getPlayers()[turns.get(0)].isDead()) {

				board.getPlayers()[turns.get(0)].calculateMovez();
				board.getPlayers()[turns.get(0)].calculateKingMovez();

				for (Piece piece : board.getPlayers()[turns.get(0)].piecez) {

					moves.addAll(piece.getMovez());
				}

				for (Move fooMove : moves) {

					fooMove.execute(false);

					if (!calculateOne(steps, foo, turns, indent, fooMove, false)) {

						return false;
					}

					fooMove.rollback();
				}

			} else {

				if (!calculateOne(steps, foo, turns, indent, null, board.getPlayers()[turns.get(0)].isDead())) {

					return false;
				}
			}

			System.out.println(indent + "Can mate in: " + steps + "!!!");

			return true;
		}

		private boolean calculateOne(int steps, Color foo, ArrayList<Integer> turns, String indent, Move fooMove,
				boolean oneDead) {

			RecursiveMateState recursiveMateState = new RecursiveMateState();

			if (!board.getPlayers()[turns.get(1)].isDead()) {

				board.getPlayers()[turns.get(1)].calculateMovez();
				board.getPlayers()[turns.get(1)].calculateKingMovez();

				ArrayList<Move> myMovez = new ArrayList<Move>();

				for (Piece piece : board.getPlayers()[turns.get(1)].piecez) {

					myMovez.addAll(piece.getMovez());
				}

				for (Move move : myMovez) {

					move.execute(false);

					recursiveMateState = calculateTwo(steps, foo, turns, indent, move, recursiveMateState, oneDead);

					if (!recursiveMateState.isResult()) {

						return false;
					}

					if (recursiveMateState.isOneMate()) {

						break;
					}
				}

				if (recursiveMateState.getCounter() == myMovez.size() || !recursiveMateState.isMateFound()) {

					if (DEBUG_MATE) {

						System.out.println(indent + "not mate!");
					}

					if (fooMove != null) {

						fooMove.rollback();
					}

					return false;

				} else {

					if (DEBUG_MATE) {

						System.out.println(
								indent + "counter = " + recursiveMateState.getCounter() + "(" + myMovez.size() + ")");
					}
				}

			} else {

				if (oneDead) {

					throw new RuntimeException("two dead players can't be here (neither)!!!");
				}

				if (!board.getPlayers()[turns.get(2)].isDead()) {

					try {

						board.getPlayers()[turns.get(2)].calculateMovez();
						board.getPlayers()[turns.get(2)].calculateKingMovez();

					} catch (ArrayIndexOutOfBoundsException e) {

						System.out.println("currentPlace.getX()");

						return false;
					}

					ArrayList<Move> allianceMovez = new ArrayList<Move>();

					for (Piece piece : board.getPlayers()[turns.get(2)].piecez) {

						allianceMovez.addAll(piece.getMovez());
					}

					for (Move allianceMove : allianceMovez) {

						allianceMove.execute(false);

						if (isMate(foo) == CHECK_MATE || canMateIn(steps - 1, foo, turns)) {

							allianceMove.rollback();

							if (DEBUG_MATE) {

								if (isMate(foo) == CHECK_MATE) {

									System.out.println(indent + "mate");

								} else {

									System.out.println(indent + "Would mate in: " + (steps - 1));
								}
							}

							return true;
						}

						allianceMove.rollback();
					}

					return false;

				} else {

					throw new RuntimeException("two dead players can't be here!!!");
				}
			}

			return true;
		}

		private RecursiveMateState calculateTwo(int steps, Color foo, ArrayList<Integer> turns, String indent,
				Move move, RecursiveMateState recursiveMateState, boolean oneDead) {

			if (turns.size() == 3 && !board.getPlayers()[turns.get(2)].isDead()) {

				try {

					board.getPlayers()[turns.get(2)].calculateMovez();
					board.getPlayers()[turns.get(2)].calculateKingMovez();

				} catch (ArrayIndexOutOfBoundsException e) {

					System.out.println("currentPlace.getX()");

					recursiveMateState.setResult(false);

					return recursiveMateState;
				}

				ArrayList<Move> allianceMovez = new ArrayList<Move>();

				for (Piece piece : board.getPlayers()[turns.get(2)].piecez) {

					allianceMovez.addAll(piece.getMovez());
				}

				for (Move allianceMove : allianceMovez) {

					allianceMove.execute(false);

					if (isMate(foo) == CHECK_MATE || canMateIn(steps - 1, foo, turns)) {

						allianceMove.rollback();

						if (DEBUG_MATE) {

							if (isMate(foo) == CHECK_MATE) {

								System.out.println(indent + "mate");

							} else {

								System.out.println(indent + "Would mate in: " + (steps - 1));
							}
						}

						recursiveMateState.setOneMate(true);

						break;
					}

					allianceMove.rollback();
				}

				move.rollback();

				recursiveMateState.setCounter(recursiveMateState.getCounter() + 1);

				return recursiveMateState;

			} else {

				if (oneDead) {

					throw new RuntimeException("two dead players can't be in calculateTwo!!!");
				}

				if (isMate(foo) == CHECK_MATE || canMateIn(steps - 1, foo, turns)) {

					if (DEBUG_MATE) {

						if (isMate(foo) == CHECK_MATE) {

							System.out.println(indent + "mate");

						} else {

							System.out.println(indent + "Would mate in: " + (steps - 1));
						}
					}

					recursiveMateState.setOneMate(true);
					recursiveMateState.setMateFound(true);
				}

				move.rollback();

				return recursiveMateState;
			}
		}

		private double isMate(Color fooColor) {

			if (isDead(fooColor)) {

				return 0;
			}

			board.getPlayers()[fooColor.getSeq()].calculateMovez();
			board.getPlayers()[fooColor.getSeq()].calculateKingMovez();

			board.getPlayers()[fooColor.getSeq()].calculateChecks(board.getPlayers()[fooColor.getSeq()].alliance,
					board.getPlayers()[fooColor.getSeq()]);

			if (board.getPlayers()[fooColor.getSeq()].getKing().isCheck()
					&& !board.getPlayers()[fooColor.getSeq()].canPrevent()) {

				return CHECK_MATE;
			}

			return 0;
		}

		double evaluate(int depth, double alpha, double beta, int playerIndex) {

			double finalEvaluation;

			while (board.getPlayers()[playerIndex].isDead()
					|| alliance.isInAlliance(board.getPlayers()[playerIndex].getColor())) {

				playerIndex = (playerIndex + 1) % 4;
			}

			ArrayList<Move> moves = new ArrayList<Move>();

			if (depth >= MAX_DEPTH) {

				double leaf = calculateBoardValue(color, alliance);

				return leaf;

			} else {

				// hier...
				board.getPlayers()[playerIndex].calculateMovez();
				board.getPlayers()[playerIndex].calculateKingMovez();

				for (Piece piece : board.getPlayers()[playerIndex].piecez) {

					moves.addAll(piece.getMovez());
				}

				// ArrayList<Move> filteredMoves = filterMovez(moves, currentPlayerIndex);

				finalEvaluation = calculateFinalEvaluation(moves, depth, alpha, beta, playerIndex);
			}

			return finalEvaluation;
		}

		private double calculateFinalEvaluation(ArrayList<Move> filteredMoves, int depth, double alpha, double beta,
				int playerIndex) {

			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < depth; i++) {

				sb.append("-");
			}

			double finalEvaluation;

			boolean isMax = alliance.isInAlliance(Color.getBySeq(playerIndex));

			if (isMax) {

				finalEvaluation = -Integer.MAX_VALUE;

			} else {

				finalEvaluation = Integer.MAX_VALUE;
			}

			Collections.shuffle(filteredMoves);

			for (Move move : filteredMoves) {

				logger.debug(sb.toString() + "a: " + alpha + ", b: " + beta);

				move.execute(false);

				double evaluation;

				try {

					evaluation = evaluate(depth + 1, alpha, beta, ((playerIndex + 1) % 4));

				} finally {

					move.rollback();
				}

				logger.debug(sb.toString() + "e: " + evaluation);

				movesToDepth[depth] = move;

				if (evaluation == CHECK_MATE) {

					return CHECK_MATE;
				}

				if (isMax) {

					finalEvaluation = Math.max(finalEvaluation, evaluation);

					if (finalEvaluation >= beta) {

						logger.debug(sb.toString() + "beta-cut");

						break;
					}

					alpha = Math.max(finalEvaluation, alpha);

				} else {

					finalEvaluation = Math.min(finalEvaluation, evaluation);

					if (finalEvaluation <= alpha) {

						logger.debug(sb.toString() + "alpha-cut");

						break;
					}

					beta = Math.min(finalEvaluation, beta);
				}
			}

			logger.debug(sb.toString() + "fe: " + finalEvaluation);

			return finalEvaluation;
		}

		public double calculateBoardValue(Color color, Alliance alliance) {

			/*
			 * nodes++;
			 * 
			 * board.clear();
			 * 
			 * for (Player player : board.getPlayers()) {
			 * 
			 * player.propagateValues(); }
			 */

			return board.value(alliance);
		}

	}

	public void calculateMovez() {

		for (Piece piece : piecez) {

			if (!(piece instanceof King) && !piece.isRemoved()) {

				piece.calculateMovez();
			}
		}
	}

	public boolean canMove() {

		boolean canMove = false;

		for (Piece piece : piecez) {

			if (piece.getMovez().size() > 0) {

				canMove = true;

				break;
			}
		}

		return canMove;
	}

	public void calculateChecks(Alliance alliance, Player checkedPlayer) {

		ArrayList<Player> others = new ArrayList<Player>();

		for (Player player : board.getPlayers()) {

			if (!alliance.isInAlliance(player.getColor())) {

				others.add(player);
			}
		}

		updateChecks(others.toArray(new Player[0]), checkedPlayer);
	}

	public boolean canPrevent() {

		ArrayList<Move> moves = new ArrayList<Move>();

		preventPieces = new HashSet<Piece>();

		calculateMovez();
		calculateKingMovez();

		for (Piece piece : piecez) {

			piece.setPreventingMoves(new ArrayList<Move>());

			moves.addAll(piece.getMovez());
		}

		for (Move move : moves) {

			move.execute(false);

			king.clearChecks();
			calculateChecks(alliance, this);

			if (!king.isCheck()) {

				move.getPiece().getPreventingMoves().add(move);

				preventPieces.add(move.getPiece());
			}

			move.rollback();
		}

		return preventPieces.size() != 0;
	}

	public void calculateKingMovez() {

		king.prepareMovez();

		ArrayList<Player> others = new ArrayList<>();

		for (Player player : board.getPlayers()) {

			if (!alliance.isInAlliance(player.getColor())) {

				others.add(player);
			}
		}

		ArrayList<Move> movez = new ArrayList<>();

		for (Place possiblePlace : king.getPossibleMoves()) {

			Move move = new Move(king.getCurrentPlace(), possiblePlace, king, "m");

			move.execute(false);

			king.clearChecks();

			for (Player player : others) {

				for (Piece piece : player.getPiecez()) {

					piece.updateChecks(this);
				}
			}

			if (!king.isCheck()) {

				movez.add(move);
			}

			move.rollback();
		}

		king.setMovez(movez);
	}

	private synchronized void recieveMove(Move move, double value, int moveCounter, int nodes) {

		calculated++;

		totalNodes += nodes;

		if (value > maxValue) {

			bestMove = move;

			maxValue = value;
		}

		movezStringz[moveCounter] = "(" + value + "(" + nodes + ")" + move.getTo().getMd5().substring(0, 4) + ")";

		output = "total: " + totalNodes + "\n\n";

		for (int i = 0; i < movezStringz.length; i++) {

			output += movezStringz[i] + " ";

			for (int j = 0; j < 3; j++) {

				i++;

				if (i >= movezStringz.length) {

					break;
				}

				output += movezStringz[i] + " ";
			}

			output += "\n";
		}

		output += "\n";

		output += calculated + "/" + movez.size() + " moves calculated...\n\n";

		System.out.println(output);

		board.getPoller().output(output);
	}

	public void propagateValues() {

		for (Piece piece : piecez) {

			if (!piece.isRemoved()) {

				piece.propagateValues();
			}
		}
	}

	public boolean defends(Place to) {

		for (Piece piece : piecez) {

			if (piece.defends(to)) {

				return true;
			}
		}

		return false;
	}

	public boolean attacks(Place to) {

		for (Piece piece : piecez) {

			if (piece.attacks(to)) {

				return true;
			}
		}

		return false;
	}

	public void calculateOffence() {

		for (Piece piece : piecez) {

			piece.calculateOffence();
		}
	}

	public void calculateDefence(Place to) {

		for (Piece piece : piecez) {

			piece.calculateDefence(to);
		}
	}

	public Piece getPieceByIdentifier(String identifier) {

		for (Piece piece : piecez) {

			if (piece.getIdentifier().equalsIgnoreCase(identifier)) {

				return piece;
			}
		}

		return null;
	}

	public void updateChecks(Player[] others, Player checkedPlayer) {

		for (Player player : others) {

			for (Piece piece : player.getPiecez()) {

				if (!piece.isRemoved()) {

					piece.updateChecks(checkedPlayer);
				}
			}
		}
	}

	public void setCheck(Piece piece) {

		king.addCheck(new Check(piece));
	}

	private synchronized Move mapMove(Move move) {

		for (Move gottenMove : movez) {

			if (gottenMove.getPiece().getMd5().equalsIgnoreCase(move.getPiece().getMd5())
					&& gottenMove.getTo().getMd5().equalsIgnoreCase(move.getTo().getMd5())) {

				return gottenMove;
			}

			if (gottenMove.getPiece().getMd5().equalsIgnoreCase(move.getPiece().getMd5())
					&& gottenMove.getTo().getX() == move.getTo().getX()
					&& gottenMove.getTo().getY() == move.getTo().getY()) {

				return gottenMove;
			}
		}

		throw new RuntimeException("didn't find move...");
	}

	private boolean isDead(Color color) {

		return board.getPlayers()[color.getSeq()].isDead();
	}

	private void handleException(Throwable t) {

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		t.printStackTrace(pw);

		t.printStackTrace();

		board.getPoller().exception(sw.toString());
	}

}
