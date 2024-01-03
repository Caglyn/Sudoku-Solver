import java.util.*;

public class SudokuSimulatedAnnealing {
    private static final int SIZE = 9;
    private static final int SUBGRID_SIZE = 3;
    private static final int MAX_ITERATIONS = 10000;
    private static final double INITIAL_TEMPERATURE = 1.0;
    private static final double COOLING_RATE = 0.99;

    private static Random random = new Random();
    private static int iterations;

    private static boolean startSudoku(int[][] board) {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= SIZE; i++) {
            numbers.add(i);
        }

        // Randomize numbers
        Collections.shuffle(numbers);

        // Check row, collumns and blocks
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == 0) {
                    for (int number : numbers) {
                        if (isValidPlacement(board, row, col, number)) {
                            board[row][col] = number;
                            if (startSudoku(board)) {
                                return true;
                            }
                            board[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean isValidPlacement(int[][] board, int row, int col, int number) {
        return !isNumberInBlock(board, row - row % SUBGRID_SIZE, col - col % SUBGRID_SIZE, number);
    }

    private static boolean isNumberInBlock(int[][] board, int startRow, int startCol, int number) {
        for (int row = startRow; row < startRow + SUBGRID_SIZE; row++) {
            for (int col = startCol; col < startCol + SUBGRID_SIZE; col++) {
                if (board[row][col] == number) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void solve(int[][] board) {
        double temperature = INITIAL_TEMPERATURE;
        int faultScore = calculateFaultScore(board);
        while (temperature > 0 && faultScore != 0) {
            for (int i = 0; i < MAX_ITERATIONS; i++) {
                iterations++;
                int[][] newPuzzle = generateCandidateSolution(board);
                faultScore=calculateFaultScore(newPuzzle);

                int oldScore = calculateFaultScore(board);
                int newScore = calculateFaultScore(newPuzzle);
                int delta = newScore - oldScore;

                if (random.nextDouble() < Math.exp(-delta / temperature)) {
                    board = newPuzzle;
                }

                if (iterations % 100000 == 0) {
                    System.out.println("Iteration: " + iterations + " - Fault Score: " + faultScore);
                }

                if (faultScore == 0) {
                    printSudoku(newPuzzle);
                    System.out.println("Fault score of final solution: " + faultScore);
                    System.out.println("Found in " + iterations + ". iteration");
                    return;
                }
            }
            temperature *= COOLING_RATE;
        }
    }

    private static int[][] generateCandidateSolution(int[][] board) {
        // Same board with the initial board
        int[][] initialBoard = new int[][]{
                {0, 0, 6, 0, 0, 0, 0, 0, 0},
                {0, 8, 0, 0, 5, 4, 2, 0, 0},
                {0, 4, 0, 0, 9, 0, 0, 7, 0},
                {0, 0, 7, 9, 0, 0, 3, 0, 0},
                {0, 0, 0, 0, 8, 0, 4, 0, 0},
                {6, 0, 0, 0, 0, 0, 1, 0, 0},
                {2, 0, 3, 0, 0, 0, 0, 0, 1},
                {0, 0, 0, 5, 0, 0, 0, 4, 0},
                {0, 0, 8, 3, 0, 0, 5, 0, 2}
        };
        int[][] candidateSolution = copyBoard(board);
        int row1, col1, row2, col2;

        // Randomize row & column blocks
        int blockRow = random.nextInt(3) * 3;
        int blockCol = random.nextInt(3) * 3;

        do {
            row1 = blockRow + random.nextInt(3);
            col1 = blockCol + random.nextInt(3);
        } while (initialBoard[row1][col1] != 0);

        do {
            row2 = blockRow + random.nextInt(3);
            col2 = blockCol + random.nextInt(3);
        } while (initialBoard[row2][col2] != 0 || (initialBoard[row1][col1] != initialBoard[row2][col2]));

        // Swap the row and columns
        int temp = candidateSolution[row1][col1];
        candidateSolution[row1][col1] = candidateSolution[row2][col2];
        candidateSolution[row2][col2] = temp;

        return candidateSolution;
    }

    private static int[][] copyBoard(int[][] original) {
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return copy;
    }

    public static void printSudoku(int[][] sudoku){
        for(int i = 0; i < sudoku.length; i++){
            if(i == 0 || i == 3 || i == 6){
                System.out.println("-------------------------");
            }
            for(int j = 0; j < sudoku.length; j++){
                if(j == 0 || j == 3 || j == 6){
                    System.out.print("| ");
                }
                System.out.print(sudoku[i][j] + " ");
            }
            System.out.println("|");
        }
        System.out.println("-------------------------");
    }

    private static int calculateFaultScore(int[][] board) {
        int faultScore = 0;

        // Check rows for duplicates
        for (int row = 0; row < SIZE; row++) {
            int[] count = new int[SIZE + 1];
            for (int col = 0; col < SIZE; col++) {
                int number = board[row][col];
                if (number != 0 && count[number]++ > 0) {
                    faultScore++;
                }
            }
        }

        // Check columns for duplicates
        for (int col = 0; col < SIZE; col++) {
            int[] count = new int[SIZE + 1];
            for (int row = 0; row < SIZE; row++) {
                int number = board[row][col];
                if (number != 0 && count[number]++ > 0) {
                    faultScore++;
                }
            }
        }

        // Check 3x3 blocks for duplicates
        for (int blockRow = 0; blockRow < SIZE; blockRow += 3) {
            for (int blockCol = 0; blockCol < SIZE; blockCol += 3) {
                int[] count = new int[SIZE + 1];
                for (int row = blockRow; row < blockRow + 3; row++) {
                    for (int col = blockCol; col < blockCol + 3; col++) {
                        int number = board[row][col];
                        if (number != 0 && count[number]++ > 0) {
                            faultScore++;
                        }
                    }
                }
            }
        }

        return faultScore;
    }

    public static void main(String[] args) {
        int[][] board = new int[][]{
                {0, 0, 6, 0, 0, 0, 0, 0, 0},
                {0, 8, 0, 0, 5, 4, 2, 0, 0},
                {0, 4, 0, 0, 9, 0, 0, 7, 0},
                {0, 0, 7, 9, 0, 0, 3, 0, 0},
                {0, 0, 0, 0, 8, 0, 4, 0, 0},
                {6, 0, 0, 0, 0, 0, 1, 0, 0},
                {2, 0, 3, 0, 0, 0, 0, 0, 1},
                {0, 0, 0, 5, 0, 0, 0, 4, 0},
                {0, 0, 8, 3, 0, 0, 5, 0, 2}
        };

        startSudoku(board);
        int[][] initialBoard = board;
        System.out.println("Initial Board:");
        printSudoku(initialBoard);
        System.out.println("Initial fault score: " + calculateFaultScore(initialBoard));

        solve(initialBoard);
    }
}