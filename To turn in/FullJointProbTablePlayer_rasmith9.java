import java.util.List;

public class FullJointProbTablePlayer_rasmith9 extends NannonPlayer {
	
	//These two 9-dimensional array represent the board state (relative to my player) on any of their moves. A board state in
	//count_given_move_in_win is incremented for each win in which the board state was encountered. Likewise, a board state in
	//count_given_move_in_lose is incremented for each loss in which the board state was encountered.
	//The first node represents the number of my pieces at home, the second node represents the number of my pieces at safe,
	//the third node represents the die value of my current roll, and the remaining 6 nodes represent the piece on each of the
	//six positions on the game board.
	int[][][][][][][][][] count_given_move_in_win = new int[4][4][7][3][3][3][3][3][3];
    int[][][][][][][][][] count_given_move_in_loss = new int[4][4][7][3][3][3][3][3][3];
    
    int bestHomePiecesValue = 0;
    int bestSafePiecesValue = 0;
    int bestDieValue = 0;
    int bestSpot1Value = 0;
    int bestSpot2Value = 0;
    int bestSpot3Value = 0;
    int bestSpot4Value = 0;
    int bestSpot5Value = 0;
    int bestSpot6Value = 0;
    double bestRatio = 0;
    
	int wins = 1;                            	// Remember m-estimates.
	int losses = 1;
    
	@Override
	public String getPlayerName() {
		return "FullJointProbTablePlayer_rasmith9";
	}
	
	public FullJointProbTablePlayer_rasmith9() {
		initialize();
		
	}
	
	//This player doesn't support non-traditional game board sizes.
	public FullJointProbTablePlayer_rasmith9(NannonGameBoard gameBoard) {
		super(gameBoard);
		initialize();
	}
	
	//@SuppressWarnings("unused")
	//We initialize our arrays so that every node combination has a value of 1 to avoid probabilities of 0 later.
	private void initialize() {
		int i;
		int j;
		int k;
		int l;
		int m;
		int n;
		int o;
		int p;
		int q;
		for (i = 0; i < 4; i++) {
			for (j = 0; j < 4; j++) {
				for (k = 0; k < 7; k++) {
					for (l = 0; l < 3; l++) {
						for (m = 0; m < 3; m++) {
							for (n = 0; n < 3; n++) {
								for (o = 0; o < 3; o++) {
									for (p = 0; p < 3; p++) {
										for (q = 0; q < 3; q++) {
											count_given_move_in_win[i][j][k][l][m][n][o][p][q] = 1;
											count_given_move_in_loss[i][j][k][l][m][n][o][p][q] = 1;
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	//@SuppressWarnings("unused") // This prevents a warning from the "if (false)" below.
	@Override
	public List<Integer> chooseMove(int[] boardConfiguration, List<List<Integer>> legalMoves) {
		
		// Below is some code you might want to use in your solution.
		//      (a) converts to zero-based counting for the cell locations
		//      (b) converts NannonGameBoard.movingFromHOME and NannonGameBoard.movingToSAFE to NannonGameBoard.cellsOnBoard,
		//          (so you could then make arrays with dimension NannonGameBoard.cellsOnBoard+1)
		//      (c) gets the current and next board configurations.
		
		double bestProbability = 0;
		List<Integer> bestMove = null;
		
		if (true && legalMoves != null) for (List<Integer> move : legalMoves) { // <----- be sure to drop the "false &&" !
			
			int fromCountingFromOne    = move.get(0);  // Convert below to an internal count-from-zero system.
			int   toCountingFromOne    = move.get(1);			
			int                 effect = move.get(2);  // See ManageMoveEffects.java for the possible values that can appear here.	
			
			// Note we use 0 for both 'from' and 'to' because one can never move FROM SAFETY or TO HOME, so we save a memory cell.
			int from = (fromCountingFromOne == NannonGameBoard.movingFromHOME ? 0 : fromCountingFromOne);
			int to   = (toCountingFromOne   == NannonGameBoard.movingToSAFETY ? 0 : toCountingFromOne);
			
			// The 'effect' of move is encoded in these four booleans:
		    boolean        hitOpponent = ManageMoveEffects.isaHit(      effect);  // Did this move 'land' on an opponent (sending it back to HOME)?
		    boolean       brokeMyPrime = ManageMoveEffects.breaksPrime( effect);  // A 'prime' is when two pieces from the same player are adjacent on the board;
		                                                                          // an opponent can NOT land on pieces that are 'prime' - so breaking up a prime of 
		                                                                          // might be a bad idea.
		    boolean extendsPrimeOfMine = ManageMoveEffects.extendsPrime(effect);  // Did this move lengthen (i.e., extend) an existing prime?
		    boolean createsPrimeOfMine = ManageMoveEffects.createsPrime(effect);  // Did this move CREATE a NEW prime? (A move cannot both extend and create a prime.)
		    
		    // Note that you can compute other effects than the four above (but you need to do it from the info in boardConfiguration, resultingBoard, and move).
		    
			// See comments in updateStatistics() regarding how to use these.
			int[] resultingBoard = gameBoard.getNextBoardConfiguration(boardConfiguration, move);  // You might choose NOT to use this - see updateStatistics().
			
			//we first retrieve the variables representing the board state (from my perspective) resulting from the move under consideration
			int whoseTurn = resultingBoard[0];
		    int homePieces;
		    int safePieces;
		    int die;
			
		    if (whoseTurn == 1) {
		    	homePieces = resultingBoard[1];
		    	safePieces = resultingBoard[3];
		    	die = resultingBoard[5];
		    }
		    else {
		    	homePieces = resultingBoard[2];
		    	safePieces = resultingBoard[4];
		    	die = resultingBoard[6];
		    }

		    int spot1 = resultingBoard[7];
		    int spot2 = resultingBoard[8];
		    int spot3 = resultingBoard[9];
		    int spot4 = resultingBoard[10];
		    int spot5 = resultingBoard[11];
		    int spot6 = resultingBoard[12];
		    
		    if (die == -1) {die = 0;}
		    
		    //once we have the board state under considerations, we compute the probability from our joint probability table by counting the number
		    //of times we won given the board state under consideration and finding the ratio of this to the total number of games that were played
		    //(won or lost) in which this state was encountered.
			double countOfWin = (double) this.count_given_move_in_win[homePieces][safePieces][die][spot1][spot2][spot3][spot4][spot5][spot6];
			double countOfWinAndLoss = (double) (countOfWin + this.count_given_move_in_loss[homePieces][safePieces][die][spot1][spot2][spot3][spot4][spot5][spot6]);
			double probabilityOfWin = countOfWin / countOfWinAndLoss;
			
			//we then track the likelihood that this move ends in a win, choosing the move that gives us the highest likelihood
			if (probabilityOfWin > bestProbability) {
				bestProbability = probabilityOfWin;
				bestMove = move;
			}
			
			if (bestProbability > bestRatio) {
				bestRatio = bestProbability;
				bestHomePiecesValue = homePieces;
				bestSafePiecesValue = safePieces;
				bestDieValue = die;
				bestSpot1Value = spot1;
				bestSpot2Value = spot2;
				bestSpot3Value = spot3;
				bestSpot4Value = spot4;
				bestSpot5Value = spot5;
				bestSpot6Value = spot6;
			}
			
			/* Here is what is in a board configuration vector.  There are also accessor functions in NannonGameBoard.java (starts at or around line 60).
			 
			   	boardConfiguration[0] = whoseTurn;        // Ignore, since it is OUR TURN when we play, by definition. (But needed to compute getNextBoardConfiguration.)
        		boardConfiguration[1] = homePieces_playerX; 
        		boardConfiguration[2] = homePieces_playerO;
        		boardConfiguration[3] = safePieces_playerX;
        		boardConfiguration[4] = safePieces_playerO;
        		boardConfiguration[5] = die_playerX;      // I added these early on, but never used them.
        		boardConfiguration[6] = die_playerO;      // Probably can be ignored since get the number of legal moves, which is more meaningful.
       
        		cells 7 to (6 + NannonGameBoard.cellsOnBoard) record what is on the board at each 'cell' (ie, board location).
        					- one of NannonGameBoard.playerX, NannonGameBoard.playerO, or NannonGameBoard.empty.
        		
			 */
			
			// DO SOMETHING HERE.             <-------------------------------------------------
		}
		
		if (bestMove == null) {
			return Utils.chooseRandomElementFromThisList(legalMoves); // In you own code you should of course get rid of this line.
		}
		else {
			return bestMove;
		}	
	}
	
	//@SuppressWarnings("unused") // This prevents a warning from the "if (false)" below.
	@Override
	public void updateStatistics(boolean didIwinThisGame, List<int[]> allBoardConfigurationsThisGameForPlayer,
			List<Integer> allCountsOfPossibleMovesForPlayer, List<List<Integer>> allMovesThisGameForPlayer) {
		
		// Do nothing with these in the random player (but hints are here for use in your players).	
		
		// However, here are the beginnings of what you might want to do in your solution (see comments in 'chooseMove' as well).
		if (true) { // <------------ Be sure to remove this 'false' *********************************************************************
			int numberOfMyMovesThisGame = allBoardConfigurationsThisGameForPlayer.size();	
			
			for (int myMove = 0; myMove < numberOfMyMovesThisGame; myMove++) {
				int[]         currentBoard        = allBoardConfigurationsThisGameForPlayer.get(myMove);
				int           numberPossibleMoves = allCountsOfPossibleMovesForPlayer.get(myMove);
				List<Integer> moveChosen          = allMovesThisGameForPlayer.get(myMove);
				int[]         resultingBoard      = (numberPossibleMoves < 1 ? currentBoard // No move possible, so board is unchanged.
						                                                     : gameBoard.getNextBoardConfiguration(currentBoard, moveChosen));
				
				// You should compute the statistics needed for a Bayes Net for any of these problem formulations:
				//
				//     prob(win | currentBoard and chosenMove and chosenMove's Effects)  <--- this is what I (Jude) did, but mainly because at that point I had not yet written getNextBoardConfiguration()
				//     prob(win | resultingBoard and chosenMove's Effects)               <--- condition on the board produced and also on the important changes from the prev board
				//
				//     prob(win | currentBoard and chosenMove)                           <--- if we ignore 'chosenMove's Effects' we would be more in the spirit of a State Board Evaluator (SBE)
				//     prob(win | resultingBoard)                                        <--- but it seems helpful to know something about the impact of the chosen move (ie, in the first two options)
				//
				//     prob(win | currentBoard)                                          <--- if you estimate this, be sure when CHOOSING moves you apply to the NEXT boards (since when choosing moves, one needs to score each legal move).
				
				if (numberPossibleMoves < 1) { continue; } // If NO moves possible, nothing to learn from (it is up to you if you want to learn for cases where there is a FORCED move, ie only one possible move).
	
				// Convert to our internal count-from-zero system.
				// A move is a list of three integers.  Their meanings should be clear from the variable names below.
				int fromCountingFromOne = moveChosen.get(0);  // Convert below to an internal count-from-zero system.
				int   toCountingFromOne = moveChosen.get(1);
				int              effect = moveChosen.get(2);  // See ManageMoveEffects.java for the possible values that can appear here. Also see the four booleans below.

				// Note we use 0 for both 'from' and 'to' because one can never move FROM SAFETY or TO HOME, so we save a memory cell.
				int from = (fromCountingFromOne == NannonGameBoard.movingFromHOME ? 0 : fromCountingFromOne);
				int to   = (toCountingFromOne   == NannonGameBoard.movingToSAFETY ? 0 : toCountingFromOne);
				
				// The 'effect' of move is encoded in these four booleans:
			    boolean        hitOpponent = ManageMoveEffects.isaHit(      effect); // Explained in chooseMove() above.
			    boolean       brokeMyPrime = ManageMoveEffects.breaksPrime( effect);
			    boolean extendsPrimeOfMine = ManageMoveEffects.extendsPrime(effect);
			    boolean createsPrimeOfMine = ManageMoveEffects.createsPrime(effect);
				
			    
			    //we first get whose turn it is - this will determine who's perspective we fill out our joint probability table with respect to
			    int whoseTurn = resultingBoard[0];
			    int homePieces;
			    int safePieces;
			    int die;
			    
			    //if it's X' turn, we'll grab X's resulting board values - otherwise, we'll grab O's
			    if (whoseTurn == 1) {
			    	homePieces = resultingBoard[1];
			    	safePieces = resultingBoard[3];
			    	die = resultingBoard[5];
			    }
			    else {
			    	homePieces = resultingBoard[2];
			    	safePieces = resultingBoard[4];
			    	die = resultingBoard[6];
			    }
			    
			    //these are what occupy the different positions on the board
			    int spot1 = resultingBoard[7];
			    int spot2 = resultingBoard[8];
			    int spot3 = resultingBoard[9];
			    int spot4 = resultingBoard[10];
			    int spot5 = resultingBoard[11];
			    int spot6 = resultingBoard[12];
			    
			    //we represent the die value of -1 by an index value of 0 in the third node of our arrays
			    if (die == -1) {die = 0;}
			    
			    //we increment the "count_given_move_in" arrays, choosing one based on whether we won or lost
			    if (didIwinThisGame == true) {
			    	count_given_move_in_win[homePieces][safePieces][die][spot1][spot2][spot3][spot4][spot5][spot6]++;
			    	wins++;
			    }
			    else if (didIwinThisGame == false) {
			    	count_given_move_in_loss[homePieces][safePieces][die][spot1][spot2][spot3][spot4][spot5][spot6]++;
			    	losses++;
			    }
				// DO SOMETHING HERE.  See chooseMove() for an explanation of what is stored in currentBoard and resultingBoard.
			}
		}
	}
	
	@Override
	public void reportLearnedModel() {
		Utils.println("\n-------------------------------------------------");
		Utils.println("\nThis is the learned model for " + getPlayerName() + ".");
		Utils.println("\n");
		Utils.println("\nBest home pieces value: " + Integer.toString(bestHomePiecesValue) + ".");
		Utils.println("\nBest safe pieces value: " + Integer.toString(bestSafePiecesValue) + ".");
		Utils.println("\nBest die value: " + Integer.toString(bestDieValue) + ".");
		Utils.println("\nBest cell 1 value: " + Integer.toString(bestSpot1Value) + ".");
		Utils.println("\nBest cell 2 value: " + Integer.toString(bestSpot2Value) + ".");
		Utils.println("\nBest cell 3 value: " + Integer.toString(bestSpot3Value) + ".");
		Utils.println("\nBest cell 4 value: " + Integer.toString(bestSpot4Value) + ".");
		Utils.println("\nBest cell 5 value: " + Integer.toString(bestSpot5Value) + ".");
		Utils.println("\nBest cell 6 value: " + Integer.toString(bestSpot6Value) + ".");
		Utils.println("\n");
		Utils.println("\nThese result in an optimal win-loss ratio value, equal numerically to " + bestRatio+ ".");
		Utils.println("\n-------------------------------------------------");
	}
}
