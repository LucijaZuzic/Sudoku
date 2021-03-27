import java.util.TimerTask;


public class ScoreTask extends TimerTask {

	SolveSudoku s;
	
	public void setSudoku(SolveSudoku s1) {
		s = s1;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
        s.changeTime();
	}

}
