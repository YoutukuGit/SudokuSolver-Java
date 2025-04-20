
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


//该程序回溯算法的时间复杂度在理论上极高：O(9^N)，其中N为空白格数量
//但由于数独规模固定(9x9)且合法输入通常有唯一解，实际运行效率可接受
//空间复杂度为常数级O(1)，适用于标准数独求解
//查重算法的时空复杂度均为O(1)

public class SudokuSolver {
    //建立二维数组，存放81个文本框，以便查找
    JTextField[][] sudokuInputFields = new JTextField[10][10];
    int[][] sudokuCells = new int[10][10];
    
    public SudokuSolver(){
        createGUI();
    }
    private void createGUI(){
        JFrame frame = new JFrame("标准数独求解器");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //定义panel布局为9x9方阵，以便放入81个文本框
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(9, 9));

        for(int i = 1; i <= 9; i++){
            for(int j = 1; j <= 9; j++){
            JTextField field = new JTextField(3);
            gridPanel.add(field);
            sudokuInputFields[i][j] = field;
            }
        }

        JButton solveButton = new JButton("开始求解");
        solveButton.addActionListener((ActionEvent e)->{
            readSudokuFromUI(sudokuCells);
            if(solveSudokuRecursively(sudokuCells, 1, 1)) { // 从(1,1)开始求解
                updateUIFromSudoku(sudokuCells);
            } else {
                JOptionPane.showMessageDialog(frame, "无解！");
            }
        });

        JButton resetButton = new JButton("清空");
        resetButton.addActionListener((ActionEvent e)->{
            clearSudoku();
        });

        JPanel controlButtonPanel = new JPanel();
        controlButtonPanel.setLayout(new GridLayout(1, 2));
        controlButtonPanel.add(solveButton);
        controlButtonPanel.add(resetButton);

        //定义frame布局为竖直
        frame.add(gridPanel, BorderLayout.CENTER);
        frame.add(controlButtonPanel, BorderLayout.SOUTH);
        
        frame.pack();
        frame.setVisible(true);
    }

    //行列检查中不需要跳过与自身的比较
    //因为比较是通过布尔数组实现，而非宫格检查中的遍历对比
    private boolean hasRowConflict(int[][] sudoku, int row, int currentColumn){
        boolean[] seen = new boolean[10];
        for(int j = 1; j <= 9; j++){
            if(sudoku[row][j] <1 || sudoku[row][j] >9){
                continue;
            }
            /*if(j == currentColumn){
                continue;
            }*/
            if(seen[sudoku[row][j]]){      
                return true;    
            }
            seen[sudoku[row][j]] = true;   
        }
        return false;
    }

    private boolean hasColumnConflict(int[][] sudoku, int currentRow, int col){
        boolean[] seen = new boolean[10];
        for(int i = 1; i <= 9; i++){
            if(sudoku[i][col] <1 || sudoku[i][col] >9){
                continue;
            }
            /*if(i == currentRow){
                continue;
            }*/
            if(seen[sudoku[i][col]]){
                return true;
            }
            seen[sudoku[i][col]] = true;
        }
        return false;
    }

    private boolean hasBlockConflict(int[][] sudoku, int i, int j){
        //判断某元素所属宫格，确定起始行和起始列，并跳过自身位置比较
        int num = sudoku[i][j];
        
        int blockRowStart = ((i - 1) / 3) * 3 + 1; // 起始行1,4,7
        int blockColStart = ((j - 1) / 3) * 3 + 1; // 起始列1,4,7
        for(int row = blockRowStart; row < blockRowStart + 3; row++){
            for(int col = blockColStart; col < blockColStart + 3; col++){
                if(row == i && col == j){
                    continue;
                }
                if(sudoku[row][col] <1 || sudoku[row][col] >9){
                    continue;
                }
                if(num == sudoku[row][col]){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasConflict(int[][] sudoku, int row, int col){
        return hasRowConflict(sudoku,row,col) || 
            hasColumnConflict(sudoku,row,col) || 
            hasBlockConflict(sudoku,row, col);
    }

    //1.遍历所有单元格，找到第一个空白格(值为0)
    //2.尝试填入1-9中的数字
    //3.若数字不冲突，递归处理下一个单元格
    //4.若所有数字均冲突，回溯到上一个单元格，重置当前值为0
    //5.重复直到所有单元格填满或无解
    private boolean solveSudokuRecursively(int[][] sudoku, int row, int col){
        if(row > 9){
            return true;
        }
        //计算下一个单元格位置
        int nextrow = (col == 9) ? row+1 : row;
        int nextcol = (col == 9) ? 1 : col+1;
        if(sudoku[row][col] != 0 ){
            //跳过已填数字，直接处理下一个单元格
            return solveSudokuRecursively(sudoku, nextrow, nextcol);
        }
        for(int n = 1; n <= 9; n++){
            sudoku[row][col] = n;
            if(!hasConflict(sudoku, row, col)){
                if(solveSudokuRecursively(sudoku, nextrow, nextcol)){
                    return true;    //求解成功
                }
            }
            sudoku[row][col] = 0;   //回溯
        }
        return false;   //无解
    }

    private void readSudokuFromUI(int[][] sudoku){
        for(int i = 1; i <= 9; i++){
            for(int j = 1; j <= 9; j++){
                String text = sudokuInputFields[i][j].getText().trim(); // 去除前后空格
                try {
                    sudoku[i][j] = Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    sudoku[i][j] = 0; // 空白单元格及无效输入均视为输入0
                }
            }
        }
    }

    private void updateUIFromSudoku(int[][] sudoku){
        for(int i = 1; i <= 9; i++){
            for(int j = 1; j <= 9; j++){
                sudokuInputFields[i][j].setText(String.valueOf(sudoku[i][j]));
            }
        }
    }
    
    private void clearSudoku(){
        for(int i = 1; i <= 9; i++){
            for(int j = 1; j <= 9; j++){
                sudokuInputFields[i][j].setText("");
                sudokuCells[i][j] = 0;
            }
        }
    }
    public static void main(String[] args) throws Exception {
        new SudokuSolver();
    }
}
