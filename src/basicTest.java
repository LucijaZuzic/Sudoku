import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;

class basicTest {

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
		  CreateSudoku createSudoku = new CreateSudoku(changeBoxBorder.rows, changeBoxBorder.cols, changeBoxBorder.xLim, changeBoxBorder.yLim, changeBoxBorder.border, changeBoxBorder.boxNumber, changeBoxBorder.diagonalOn, changeBoxBorder.sizeRelationships, changeBoxBorder.userInput, changeBoxBorder.lastUsedPath);
		  createSudoku.frame.setVisible(false);
		  createSudoku.checkBoxes();
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
		  SolveSudoku solveSudoku = new SolveSudoku(changeBoxBorder.rows, changeBoxBorder.cols, changeBoxBorder.xLim, changeBoxBorder.yLim, changeBoxBorder.border, changeBoxBorder.boxNumber, changeBoxBorder.diagonalOn, changeBoxBorder.sizeRelationships, changeBoxBorder.userInput, false);
		  solveSudoku.frame.setVisible(false);
		  solveSudoku.checkBoxes();
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
		for (int i = 1; i < 15; i++) {
			  String solutionFilename = "src/solutions/solution" + String.valueOf(i) + ".txt";
			  String testFilename = "src/testCases/testCase" + String.valueOf(i) + ".txt";
		      assertEquals(getCorrectSolution(solutionFilename), getCreateSolution(9, testFilename));
		      assertEquals(getCorrectSolution(solutionFilename), getSolveSolution(9, testFilename));
		}
	}
	
	@Test
	void testCaseBoxes() throws FileNotFoundException {
		for (int i = 1; i < 3; i++) {
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
		for (int i = 1; i < 6; i++) {
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
		for (int i = 1; i < 2; i++) {
			  String solutionFilename = "src/solutions/solutionSixteen" + String.valueOf(i) + ".txt";
			  String testFilename = "src/testCases/testCaseSixteen" + String.valueOf(i) + ".txt";
		      assertEquals(getCorrectSolution(solutionFilename), getCreateSolution(16, testFilename));
		      assertEquals(getCorrectSolution(solutionFilename), getSolveSolution(16, testFilename));
		}
	}
	@Test
	void testCaseTwelve() throws FileNotFoundException {
		for (int i = 1; i < 3; i++) {
			  String solutionFilename = "src/solutions/solutionTwelve" + String.valueOf(i) + ".txt";
			  String testFilename = "src/testCases/testCaseTwelve" + String.valueOf(i) + ".txt";
		      assertEquals(getCorrectSolution(solutionFilename), getCreateSolution(12, testFilename));
		      assertEquals(getCorrectSolution(solutionFilename), getSolveSolution(12, testFilename));
		}
	}
}
