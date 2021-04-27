import java.util.TimerTask;


public class ScoreTask extends TimerTask {

	SolveSudoku solveSudoku;
	
	public void setSudoku(SolveSudoku newSolveSudoku) {
		solveSudoku = newSolveSudoku;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub

		if (solveSudoku.timerStopped) {
			return;
		}
        solveSudoku.changeTime();
	}

}
