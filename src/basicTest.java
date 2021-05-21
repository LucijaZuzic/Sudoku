import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;

class basicTest {

	boolean guess = false;
	
	String getCorrectSolution(String filename) throws FileNotFoundException {
		  File myObj = new File(filename);
		  java.util.Scanner myReader = new java.util.Scanner(myObj);
		  String correctSolution = "";
		  while (myReader.hasNextLine()) {
			  correctSolution += myReader.nextLine();
		  }
		  myReader.close(); 
		  return correctSolution;
	}
	
	String getCreateSolution(int size, String filename) {
		  ChangeBoxBorder changeBoxBorder = new ChangeBoxBorder(size, size, 1, size, false);
		  changeBoxBorder.readFile(filename);
		  CreateSudoku createSudoku = new CreateSudoku(changeBoxBorder.rows, changeBoxBorder.cols, changeBoxBorder.xLim, changeBoxBorder.yLim, changeBoxBorder.border, changeBoxBorder.boxNumber, changeBoxBorder.diagonalOn, changeBoxBorder.wrapAround, changeBoxBorder.sizeRelationships, changeBoxBorder.userInput, changeBoxBorder.lastUsedPath);
		  createSudoku.frame.setVisible(false);
		  createSudoku.checkBoxes();
		  createSudoku.useGuessing = guess;
		  createSudoku.isOnlyOneSolution();
		  String solution = "";
		  for (int row = 0; row < createSudoku.rows; row++){
			  for (int col = 0; col < createSudoku.cols; col++) {
					if (createSudoku.temporary[row * createSudoku.cols + col] < 10) {
						solution += String.valueOf(createSudoku.temporary[row * createSudoku.cols + col]);
					} else {
						char c = 'A';
						c += createSudoku.temporary[row * createSudoku.cols + col] - 10;
						solution += c;
					}
			  }
		  } 
		  createSudoku.writeToFile(filename);
		  return solution;
	}
	
	String getSolveSolution(int size, String filename) {
		  ChangeBoxBorder changeBoxBorder = new ChangeBoxBorder(size, size, 1, size, false);
		  changeBoxBorder.readFile(filename);
		  SolveSudoku solveSudoku = new SolveSudoku(changeBoxBorder.rows, changeBoxBorder.cols, changeBoxBorder.xLim, changeBoxBorder.yLim, changeBoxBorder.border, changeBoxBorder.boxNumber, changeBoxBorder.diagonalOn, changeBoxBorder.wrapAround, changeBoxBorder.sizeRelationships, changeBoxBorder.userInput, false);
		  solveSudoku.frame.setVisible(false);
		  solveSudoku.checkBoxes();
		  solveSudoku.useGuessing = guess;
		  solveSudoku.isOnlyOneSolution();
		  String solution = "";
		  for (int row = 0; row < solveSudoku.rows; row++){
			  for (int col = 0; col < solveSudoku.cols; col++) {
					if (solveSudoku.temporary[row * solveSudoku.cols + col] < 10) {
						solution += String.valueOf(solveSudoku.temporary[row * solveSudoku.cols + col]);
					} else {
						char c = 'A';
						c += solveSudoku.temporary[row * solveSudoku.cols + col] - 10;
						solution += c;
					}
			  }
		  } 
		  solveSudoku.writeToFile(filename);
		  return solution;
	}

	@Test
	void testCase() throws FileNotFoundException {
		for (int i = 1; i < 23; i++) {
			  String solutionFilename = "src/solutions/solution" + String.valueOf(i) + ".txt";
			  String testFilename = "src/testCases/testCase" + String.valueOf(i) + ".txt";
		      assertEquals(getCorrectSolution(solutionFilename), getCreateSolution(9, testFilename));
		      assertEquals(getCorrectSolution(solutionFilename), getSolveSolution(9, testFilename));
		}
	}
	
	@Test
	void testCaseGuessing() throws FileNotFoundException {
		guess = true;
		for (int i = 1; i < 2; i++) {
			  String solutionFilename = "src/solutions/solutionGuessing" + String.valueOf(i);
			  String testFilename = "src/testCases/testCaseGuessing" + String.valueOf(i) + ".txt";
			  String[] correct = new String[6];
			  for (int j = 1; j < 7; j++) {
				  correct[j - 1] = getCorrectSolution(solutionFilename + "Version" + String.valueOf(j) + ".txt");
			  }
			  String sol = getCreateSolution(9, testFilename);
			  int x = 0;
			  String lastCorrect = "";
			  while (x < 1000) {
				  sol = getCreateSolution(9, testFilename);
				  boolean found = false;
				  for (int j = 0; j < 6; j++) {
					  if (correct[j].compareTo(sol) == 0) {
						  found = true;
						  lastCorrect = correct[j];
						  break;
					  }
				  }
				  if (found) {
					  break;
				  }
				  x++;
			  }
		      assertEquals(lastCorrect, sol);
			  sol = getSolveSolution(9, testFilename);
			  x = 0;
			  lastCorrect = "";
			  while (x < 1000) {
				  sol = getSolveSolution(9, testFilename);
				  boolean found = false;
				  for (int j = 0; j < 6; j++) {
					  if (correct[j].compareTo(sol) == 0) {
						  found = true;
						  lastCorrect = correct[j];
						  break;
					  }
				  }
				  if (found) {
					  break;
				  }
				  x++;
			  }
		      assertEquals(lastCorrect, sol);
		}
		guess = false;
	}
	
	@Test
	void testCaseBoxes() throws FileNotFoundException {
		for (int i = 1; i < 6; i++) {
			  String solutionFilename = "src/solutions/solutionBoxes" + String.valueOf(i) + ".txt";
			  String testFilename = "src/testCases/testCaseBoxes" + String.valueOf(i) + ".txt";
		      assertEquals(getCorrectSolution(solutionFilename), getCreateSolution(9, testFilename));
		      assertEquals(getCorrectSolution(solutionFilename), getSolveSolution(9, testFilename));
		}
	}
	
	@Test
	void testCaseBoxesDiagonal() throws FileNotFoundException {
		for (int i = 1; i < 3; i++) {
			  String solutionFilename = "src/solutions/solutionBoxesDiagonal" + String.valueOf(i) + ".txt";
			  String testFilename = "src/testCases/testCaseBoxesDiagonal" + String.valueOf(i) + ".txt";
		      assertEquals(getCorrectSolution(solutionFilename), getCreateSolution(9, testFilename));
		      assertEquals(getCorrectSolution(solutionFilename), getSolveSolution(9, testFilename));
		}
	}
	
	@Test
	void testCaseSix() throws FileNotFoundException {
		for (int i = 1; i < 11; i++) {
			  String solutionFilename = "src/solutions/solutionSix" + String.valueOf(i) + ".txt";
			  String testFilename = "src/testCases/testCaseSix" + String.valueOf(i) + ".txt";
		      assertEquals(getCorrectSolution(solutionFilename), getCreateSolution(6, testFilename));
		      assertEquals(getCorrectSolution(solutionFilename), getSolveSolution(6, testFilename));
		}
	}

	@Test
	void testCaseLargerSmaller() throws FileNotFoundException {
		for (int i = 1; i < 13; i++) {
			  String solutionFilename = "src/solutions/solutionLargerSmaller" + String.valueOf(i) + ".txt";
			  String testFilename = "src/testCases/testCaseLargerSmaller" + String.valueOf(i) + ".txt";
		      assertEquals(getCorrectSolution(solutionFilename), getCreateSolution(9, testFilename));
		      assertEquals(getCorrectSolution(solutionFilename), getSolveSolution(9, testFilename));
		}
	}
	
	@Test
	void testCaseSixteen() throws FileNotFoundException {
		for (int i = 1; i < 11; i++) {
			  String solutionFilename = "src/solutions/solutionSixteen" + String.valueOf(i) + ".txt";
			  String testFilename = "src/testCases/testCaseSixteen" + String.valueOf(i) + ".txt";
		      assertEquals(getCorrectSolution(solutionFilename), getCreateSolution(16, testFilename));
		      assertEquals(getCorrectSolution(solutionFilename), getSolveSolution(16, testFilename));
		}
	}
	
	@Test
	void testCaseSixteenDiagonal() throws FileNotFoundException {
		for (int i = 1; i < 7; i++) {
			  String solutionFilename = "src/solutions/solutionSixteenDiagonal" + String.valueOf(i) + ".txt";
			  String testFilename = "src/testCases/testCaseSixteenDiagonal" + String.valueOf(i) + ".txt";
		      assertEquals(getCorrectSolution(solutionFilename), getCreateSolution(16, testFilename));
		      assertEquals(getCorrectSolution(solutionFilename), getSolveSolution(16, testFilename));
		}
	}
	@Test
	void testCaseEight() throws FileNotFoundException {
		for (int i = 1; i < 2; i++) {
			  String solutionFilename = "src/solutions/solutionEight" + String.valueOf(i) + ".txt";
			  String testFilename = "src/testCases/testCaseEight" + String.valueOf(i) + ".txt";
		      assertEquals(getCorrectSolution(solutionFilename), getCreateSolution(8, testFilename));
		      assertEquals(getCorrectSolution(solutionFilename), getSolveSolution(8, testFilename));
		}
	}
	@Test
	void testCaseTen() throws FileNotFoundException {
		for (int i = 1; i < 2; i++) {
			  String solutionFilename = "src/solutions/solutionTen" + String.valueOf(i) + ".txt";
			  String testFilename = "src/testCases/testCaseTen" + String.valueOf(i) + ".txt";
		      assertEquals(getCorrectSolution(solutionFilename), getCreateSolution(10, testFilename));
		      assertEquals(getCorrectSolution(solutionFilename), getSolveSolution(10, testFilename));
		}
	}
	@Test
	void testCaseFifteen() throws FileNotFoundException {
		for (int i = 1; i < 2; i++) {
			  String solutionFilename = "src/solutions/solutionFifteen" + String.valueOf(i) + ".txt";
			  String testFilename = "src/testCases/testCaseFifteen" + String.valueOf(i) + ".txt";
		      assertEquals(getCorrectSolution(solutionFilename), getCreateSolution(15, testFilename));
		      assertEquals(getCorrectSolution(solutionFilename), getSolveSolution(15, testFilename));
		}
	}
	
	@Test
	void testCaseTwelve() throws FileNotFoundException {
		for (int i = 1; i < 4; i++) {
			  String solutionFilename = "src/solutions/solutionTwelve" + String.valueOf(i) + ".txt";
			  String testFilename = "src/testCases/testCaseTwelve" + String.valueOf(i) + ".txt";
		      assertEquals(getCorrectSolution(solutionFilename), getCreateSolution(12, testFilename));
		      assertEquals(getCorrectSolution(solutionFilename), getSolveSolution(12, testFilename));
		}
	}
	
	@Test
	void testCaseTwentyFive() throws FileNotFoundException {
		for (int i = 1; i < 3; i++) {
			  String solutionFilename = "src/solutions/solutionTwentyFive" + String.valueOf(i) + ".txt";
			  String testFilename = "src/testCases/testCaseTwentyFive" + String.valueOf(i) + ".txt";
		      assertEquals(getCorrectSolution(solutionFilename), getCreateSolution(25, testFilename));
		      assertEquals(getCorrectSolution(solutionFilename), getSolveSolution(25, testFilename));
		}
	}
	@Test
	void testCaseToroidal() throws FileNotFoundException {
		for (int i = 1; i < 2; i++) {
			  String solutionFilename = "src/solutions/solutionToroidal" + String.valueOf(i) + ".txt";
			  String testFilename = "src/testCases/testCaseToroidal" + String.valueOf(i) + ".txt";
		      assertEquals(getCorrectSolution(solutionFilename), getCreateSolution(9, testFilename));
		      assertEquals(getCorrectSolution(solutionFilename), getSolveSolution(9, testFilename));
		}
	}
	@Test
	void testCaseToroidalSeven() throws FileNotFoundException {
		for (int i = 1; i < 2; i++) {
			  String solutionFilename = "src/solutions/solutionToroidalSeven" + String.valueOf(i) + ".txt";
			  String testFilename = "src/testCases/testCaseToroidalSeven" + String.valueOf(i) + ".txt";
		      assertEquals(getCorrectSolution(solutionFilename), getCreateSolution(7, testFilename));
		      assertEquals(getCorrectSolution(solutionFilename), getSolveSolution(7, testFilename));
		}
	}
}
