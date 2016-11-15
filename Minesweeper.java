import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class Minesweeper implements MouseListener {
	
	private static final int DEFAULT_WIDTH = 40;
	private static final int DEFAULT_HEIGHT = 40;
	private static final int BANNER_HEIGHT = 25;
	
	public static void main(String [] args) {
		final Minesweeper m = new Minesweeper(10, 10, 10);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				m.run();
			}
		});
	}
	
	private static enum MineState {
		Nothing,
		Mine,
		Flag,
		MineFlag,
		Question,
		MineQuestion,
		Exploded,
		Played;
		
		static boolean isMine(MineState state) {
			return state == Mine || state == MineFlag || state == MineQuestion;
		}
	}
	
	private int height;
	private int width;
	private int minesLeft;
	private int mineCount;
	private MineState[][] playedArray;
	
	private JFrame window;
	private JPanel mainPanel;
	private JPanel statusPanel;
	private JLabel mineCountLabel;
	private JButton[][] buttonArray;
	
	public Minesweeper(int height, int width, int mineCount) {
		this.height = height;
		this.width = width;
		this.minesLeft = mineCount;
		this.mineCount = mineCount;
		
		window = new JFrame("Minesweeper");
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(height, width));
		statusPanel = new JPanel();
		mineCountLabel = new JLabel(getMineLabelText());
		statusPanel.add(mineCountLabel);
		
		buttonArray = new JButton[height][width];
		playedArray = new MineState[height][width];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				JButton mineButton = new JButton();
				buttonArray[i][j] = mineButton;
				mainPanel.add(mineButton);
				mineButton.addMouseListener(this);
				playedArray[i][j] = MineState.Nothing;
			}
		}
		
		while (mineCount > 0) {
			int row = (int) (Math.random() * height);
			int col = (int) (Math.random() * width);
			if (playedArray[row][col] == MineState.Nothing) {
				playedArray[row][col] = MineState.Mine;
				mineCount --;
			}
		}
		
		window.getContentPane().add(mainPanel, BorderLayout.CENTER);
		window.getContentPane().add(statusPanel, BorderLayout.SOUTH);
		
		window.setSize(DEFAULT_WIDTH * width, BANNER_HEIGHT + DEFAULT_HEIGHT * height);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void run() {
		window.setVisible(true);
	}
	
	@Override
	public void mouseClicked(MouseEvent me) {
		Object source = me.getSource();
		int row = 0;
		int col = 0;
		
		LocateButton:
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (source == buttonArray[i][j]) {
					row = i;
					col = j;
					break LocateButton;
				}
			}
		}
		
		if (me.getButton() == MouseEvent.BUTTON1) {
			playSpot(row, col);
		}
		else if (me.getButton() == MouseEvent.BUTTON3) {
			placeFlag(row, col);
		}
	}
	
	@Override
	public void mousePressed(MouseEvent me) {}
	
	@Override
	public void mouseReleased(MouseEvent me) {}
	
	@Override
	public void mouseEntered(MouseEvent me) {}
	
	@Override
	public void mouseExited(MouseEvent me) {}

	private void playSpot(int row, int col) {
		MineState state = playedArray[row][col];
		switch(state) {
		case Nothing:
		case Flag:
		case Question:
			int surroundingMineCount = getSurroundingMineCount(row, col);
			buttonArray[row][col].setText("" + surroundingMineCount);
			playedArray[row][col] = MineState.Played;
			if (surroundingMineCount == 0) {
				floodFillZero(row, col);
			}
			if (checkWin()) {
				winGame();
			}
			break;
		case Mine:
		case MineFlag:
		case MineQuestion:
			buttonArray[row][col].setText("X");
			playedArray[row][col] = MineState.Exploded;
			loseGame();
			break;
		default:
			break;
		}
		buttonArray[row][col].setEnabled(false);
	}
	
	private int getSurroundingMineCount(int row, int col) {
		int acc = 0;
		for (int[] c : new int[][] { {-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1} }) {
			int dRow = row + c[0];
			int dCol = col + c[1];
			if (dRow >= 0 && dRow <= height - 1 && dCol >= 0 && dCol <= width - 1) {
				acc += MineState.isMine(playedArray[dRow][dCol]) ? 1 : 0;
			}
		}
		return acc;
	}
	
	private void placeFlag(int row, int col) {
		int acc = 0;
		switch (playedArray[row][col]) {
		case Nothing:
			buttonArray[row][col].setText("F");
			playedArray[row][col] = MineState.Flag;
			acc = -1;
			break;
		case Mine:
			buttonArray[row][col].setText("F");
			playedArray[row][col] = MineState.MineFlag;
			acc = -1;
			break;
		case Flag:
			buttonArray[row][col].setText("?");
			playedArray[row][col] = MineState.Question;
			acc = 1;
			break;
		case MineFlag:
			buttonArray[row][col].setText("?");
			playedArray[row][col] = MineState.MineQuestion;
			acc = 1;
			break;
		case Question:
			buttonArray[row][col].setText("");
			playedArray[row][col] = MineState.Nothing;
			break;
		case MineQuestion:
			buttonArray[row][col].setText("");
			playedArray[row][col] = MineState.Mine;
			break;
		default:
			break;
		}
		minesLeft += acc;
		mineCountLabel.setText(getMineLabelText());
	}
	
	private String getMineLabelText() {
		return "Mines: " + minesLeft;
	}
	
	private void loseGame() {
		for (int i = 0; i < height; i ++) {
			for (int j = 0; j < width; j ++) {
				buttonArray[i][j].setEnabled(false);
				if (MineState.isMine(playedArray[i][j])) {
					buttonArray[i][j].setText("M");
				}
				else if (playedArray[i][j] == MineState.Flag || playedArray[i][j] == MineState.Question) {
					buttonArray[i][j].setText("");
				}
			}
		}
		mineCountLabel.setText("Game Over...");
	}
	
	private void winGame() {
		for (int i = 0; i < height; i ++) {
			for (int j = 0; j < width; j ++) {
				buttonArray[i][j].setEnabled(false);
				if (MineState.isMine(playedArray[i][j])) {
					buttonArray[i][j].setText("M");
				}
			}
		}
		mineCountLabel.setText("You Win!!!");
	}
	
	private boolean checkWin() {
		int totalSpots = height * width;
		int winSpots = totalSpots - mineCount;
		int acc = 0;
		for (int i = 0; i < height; i ++) {
			for (int j = 0; j < width; j ++) {
				acc += playedArray[i][j] == MineState.Played ? 1 : 0;
			}
		}
		return acc == winSpots;
	}
	
	private void floodFillZero(int row, int col) {
		for (int[] c : new int[][] { {-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1} }) {
			int dRow = row + c[0];
			int dCol = col + c[1];
			if (dRow >= 0 && dRow <= height - 1 && dCol >= 0 && dCol <= width - 1 && playedArray[dRow][dCol] == MineState.Nothing) {
				int surroundingMineCount = getSurroundingMineCount(dRow, dCol);
				buttonArray[dRow][dCol].setEnabled(false);
				buttonArray[dRow][dCol].setText("" + surroundingMineCount);
				playedArray[dRow][dCol] = MineState.Played;
				if (surroundingMineCount == 0) {
					floodFillZero(dRow, dCol);
				}
			}
		}
	}

}
