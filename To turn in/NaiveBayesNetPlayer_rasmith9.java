import java.util.List;

public class NaiveBayesNetPlayer_rasmith9 extends NannonPlayer {

	//For this player we use several independent random variables - the number of pieces at home and at safe, the piece on each board cell, and
	//the effect of each move. Board size and pieces default to 6 and 3, but can be passed in by using the constructor that passes in a 
	//NannonGameBoard object.
	private static int boardSize = 6;  // Default width of board.
	private static int pieces = 3;  // Default #pieces per player.

	int home_win[] = new int[pieces + 1]; 		// Holds p(homeX=? |  win).
	int home_lose[] = new int[pieces + 1]; 	// Holds p(homeX=? | !win).
	int safe_win[] = new int[pieces + 1];       	// NEED TO ALSO DO FOR ‘0’!
	int safe_lose[] = new int[pieces + 1];       	// Be sure to initialize!
	int board_win[][] = new int[boardSize][3]; 
	int board_lose[][] = new int[boardSize][3];
	int wins = 1;                            	// Remember m-estimates.
	int losses = 1;
	
	int hitOpponent_win = 1;
    int brokeMyPrime_win = 1;
    int extendsPrimeOfMine_win = 1;
    int createsPrimeOfMine_win = 1;
	int hitOpponent_lose = 1;
    int brokeMyPrime_lose = 1;
    int extendsPrimeOfMine_lose = 1;
    int createsPrimeOfMine_lose = 1;
    
    int bestCreatesPrimeOfMine = 0;
    double bestRatio = 0;
    
	@Override
	public String getPlayerName() {
		return "NaiveBayesNetPlayer_rasmith9";
	}
	
	public NaiveBayesNetPlayer_rasmith9() {
		if (boardSize == 0) {
			boardSize = 6;
		}
		if (pieces == 0) {
			pieces = 3;
		}
		initialize(boardSize, pieces);
	}
	public NaiveBayesNetPlayer_rasmith9(NannonGameBoard gameBoard) {
		super(gameBoard);
		int cells = NannonGameBoard.getCellsOnBoard();
		int pieces = NannonGameBoard.getPiecesPerPlayer();
		initialize(cells, pieces);
	}
	
	//@SuppressWarnings("unused")
	//We initialize our arrays and variables so that every node combination has a value of 1 to avoid probabilities of 0 later.
	private void initialize(int cells, int pieces) {
		NaiveBayesNetPlayer_rasmith9.boardSize = cells;
		NaiveBayesNetPlayer_rasmith9.pieces = pieces;
		int home_win[] = new int[pieces + 1]; 		// Holds p(homeX=? |  win).
		int home_lose[] = new int[pieces + 1]; 	// Holds p(homeX=? | !win).
		int safe_win[] = new int[pieces + 1];       	// NEED TO ALSO DO FOR ‘0’!
		int safe_lose[] = new int[pieces + 1];       	// Be sure to initialize!
		int board_win[][] = new int[boardSize][3]; 
		int board_lose[][] = new int[boardSize][3]; 
		int wins = 1;                            	// Remember m-estimates.
		int losses = 1;
		
		int i;
		for (i = 0; i < pieces+1; i++) {
			home_win[i] = 1;
			home_lose[i] = 1;
			safe_win[i] = 1;
			safe_lose[i] = 1;
		}
		
		int j;
		int k;
		for (j = 0; j < boardSize; j++) {
			for (k = 0; k < 3; k++) {
				board_win[j][k] = 1; 
				board_lose[j][k] = 1; 
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
		    
		    double dWins = (double) wins;
		    double pHitOpponent_win = 1;
		    double pBrokeMyPrime_win = 1;
		    double pExtendsPrimeOfMine_win = 1;
		    double pCreatesPrimeOfMine_win = 1;
		    double pHome_win = (double) home_win[homePieces] / dWins;
			double pSafe_win = (double) safe_win[safePieces] / dWins;
			if (hitOpponent) {
				pHitOpponent_win = (double) hitOpponent_win / dWins;
			}
			if (brokeMyPrime) {
				pBrokeMyPrime_win = (double) brokeMyPrime_win / dWins;
			}
			if (extendsPrimeOfMine) {
				pExtendsPrimeOfMine_win = (double) extendsPrimeOfMine_win / dWins;
			}
			if (createsPrimeOfMine) {
				pCreatesPrimeOfMine_win = (double) createsPrimeOfMine_win / dWins;
			}
			
			int spot;
			int whatIsThere;
			double pBoard_win = 1;
	    	for (spot = 0; spot < boardSize; spot++) {
	    		whatIsThere = resultingBoard[spot+7];
	    		pBoard_win = pBoard_win * ((double) board_win[spot][whatIsThere] / dWins);
	    	}
			
			double dLosses = (double) losses;
			double pHome_lose = (double) home_lose[homePieces] / dLosses;
			double pSafe_lose = (double) safe_lose[safePieces] / dLosses;
		    double pHitOpponent_lose = 1;
		    double pBrokeMyPrime_lose = 1;
		    double pExtendsPrimeOfMine_lose = 1;
		    double pCreatesPrimeOfMine_lose = 1;
			if (hitOpponent) {
				pHitOpponent_lose = (double) hitOpponent_lose / dLosses;
			}
			if (brokeMyPrime) {
				pBrokeMyPrime_lose = (double) brokeMyPrime_lose / dLosses;
			}
			if (extendsPrimeOfMine) {
				pExtendsPrimeOfMine_lose = (double) extendsPrimeOfMine_lose / dLosses;
			}
			if (createsPrimeOfMine) {
				pCreatesPrimeOfMine_lose = (double) createsPrimeOfMine_lose / dLosses;
			}
	    	
			spot = 0;
			whatIsThere = 0;
			double pBoard_lose = 1;
	    	for (spot = 0; spot < boardSize; spot++) {
	    		whatIsThere = resultingBoard[spot+7];
	    		pBoard_lose = pBoard_lose * ((double) board_lose[spot][whatIsThere] / dLosses);
	    	}
			
	    	//with the naive assumption we can just multiply everything individually
	    	double likelihoodOfWin = pHome_win * pSafe_win * pHitOpponent_win * pBrokeMyPrime_win * pExtendsPrimeOfMine_win * pCreatesPrimeOfMine_win * pBoard_win;
	    	double likelihoodOfLoss = pHome_lose * pSafe_lose * pHitOpponent_lose * pBrokeMyPrime_lose * pExtendsPrimeOfMine_lose * pCreatesPrimeOfMine_lose * pBoard_lose;
	    	
	    	double pWin = dWins / (dWins + dLosses);
	    	double pLoss = dWins / (dWins + dLosses);
	    	
	    	double probabilityOfWin = likelihoodOfWin * pWin;
	    	double probabilityOfLoss = likelihoodOfLoss * pLoss;
	    	
	    	double oddsOfWin = probabilityOfWin / probabilityOfLoss;
			
			if (oddsOfWin > bestProbability) {
				bestProbability = oddsOfWin;
				bestMove = move;
			}
			
			if (bestProbability > bestRatio) {
				bestRatio = bestProbability;
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

			    int spot;
			    int whatIsThere;
			    
			    if (die == -1) {die = 0;}
			    
			    //we increment the appropriate array nodes and variables based on whether we won or lost
			    if (didIwinThisGame == true) {
			    	if (hitOpponent) {
			    		hitOpponent_win++;
			    	}
			    	if (brokeMyPrime) {
			    		brokeMyPrime_win++;
			    	}
			    	if (extendsPrimeOfMine) {
			    		extendsPrimeOfMine_win++;
			    	}
			    	if (createsPrimeOfMine) {
			    		createsPrimeOfMine_win++;
			    	}
			    	
			    	home_win[homePieces]++;
			    	safe_win[safePieces]++;
			    	wins++;
			    	
			    	for (spot = 0; spot < boardSize; spot++) {
			    		whatIsThere = resultingBoard[spot+7];
			    		board_win[spot][whatIsThere]++;
			    	}
			    }
			    else if (didIwinThisGame == false) {
			    	if (hitOpponent) {
			    		hitOpponent_lose++;
			    	}
			    	if (brokeMyPrime) {
			    		brokeMyPrime_lose++;
			    	}
			    	if (extendsPrimeOfMine) {
			    		extendsPrimeOfMine_lose++;
			    	}
			    	if (createsPrimeOfMine) {
			    		createsPrimeOfMine_lose++;
			    	}
			    	
			    	home_lose[homePieces]++;
			    	safe_lose[safePieces]++;
			    	losses++;
			    	
			    	for (spot = 0; spot < boardSize; spot++) {
			    		whatIsThere = resultingBoard[spot+7];
			    		board_lose[spot][whatIsThere]++;
			    	}
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
		Utils.println("\nBest home pieces value: " + Integer.toString(bestCreatesPrimeOfMine) + ".");
		Utils.println("\n");
		Utils.println("\nThese result in an optimal win-loss ratio value, equal numerically to " + bestRatio + ".");
		Utils.println("\n-------------------------------------------------");
	}
}
