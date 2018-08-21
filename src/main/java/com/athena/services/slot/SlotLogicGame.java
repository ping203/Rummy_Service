package com.athena.services.slot;

import com.athena.services.slot.SlotPayLine;

import com.athena.services.slot.SlotConstants;
import com.athena.services.slot.SlotIcon;
import com.athena.services.slot.SlotLogicGame;

import java.awt.Point;
import java.util.Vector;

import com.athena.services.slot.SlotMap;

public class SlotLogicGame {
	
	private static SlotLogicGame instance = null;
	private int mTotalWin;
	private static int mTotalBonus;
	private int mTotalScatter;
	private final static float[] listGoldScatter = { 0, 0, 4.5f, 9, 18 };
	public SlotPayLine mSlotPayLine;
	private int mUnit;
	
	private Vector<Integer> mLineWin = new Vector<Integer>();
	private Vector<Integer> mBonusLine = new Vector<Integer>();
	private Vector<Float> mListGoldWinPerLine = new Vector<Float>();
	private Vector<Integer> mListNumIconWinPerLine = new Vector<Integer>();

	private SlotLogicGame() {
		mSlotPayLine = new SlotPayLine();
		mTotalWin = 0;
		mTotalBonus = 0;
		mTotalScatter = 0;
		mUnit = 0;
		mLineWin.clear();
		mListGoldWinPerLine.clear();
		mListNumIconWinPerLine.clear();
		mBonusLine.clear();
	}

	public void resetValue()
	{
		mSlotPayLine = new SlotPayLine();
		mTotalWin = 0;
		mTotalBonus = 0;
		mTotalScatter = 0;
		mUnit = 0;
		mLineWin.clear();
		mListGoldWinPerLine.clear();
		mListNumIconWinPerLine.clear();
		mBonusLine.clear();
	}
	
	public static SlotLogicGame getInstance() {
		if (instance == null) {
			instance = new SlotLogicGame();
		}
		return instance;
	}

	public void checkSlotMap(int totalRow, int Unit, SlotMap map) {
		for (int i = 0; i < totalRow; i++) {
			if (checkLineWin(i, map)) {

			}

			if (checkBonus(i, map)) {

			}

			this.mTotalScatter = this.checkScatter(map);
		}
	}

	private Boolean checkBonus(int identityOfLine, SlotMap map) {
		int count = 0;

		Point[] line = mSlotPayLine.listPlayLine.get(identityOfLine);

		SlotIcon icon = null;

		for (int i = 0; i < 5; i++) {
			icon = map.slotMap[line[i].y][line[i].x];

			if (icon.getIconType() == SlotConstants.SLOT_TYPE_BONUS) {
				count++;
			}
		}

		if (count >= 3) {
			System.out.println("bonus on line : " + identityOfLine);
//			mTotalWin += (icon.getWinGold(count - 1) * mUnit);
			mBonusLine.addElement(identityOfLine);
			return true;
		}

		return false;
	}

	private Integer checkScatter(SlotMap map) {

		int numOfScatter = 0;

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 5; j++) {
				if (map.slotMap[i][j].getIconType() == SlotConstants.SLOT_TYPE_SCATTER) {
					numOfScatter++;
				}
			}
		}

		if (numOfScatter > 5)
			numOfScatter = 5;

		if (numOfScatter >= 3) {
			mTotalWin += (listGoldScatter[numOfScatter - 1] * mUnit);
		}

		return numOfScatter;
	}

	private Boolean checkLineWin(int identityOfLine, SlotMap map) {
		int count = 1;
		int minColToWin = 3;
		int maxColToWin = 4;
		int winIconType = -1;

		Point[] line = mSlotPayLine.listPlayLine.get(identityOfLine);

		SlotIcon firstIcon = map.slotMap[line[0].y][line[0].x];
		SlotIcon nextIcon = null;

		if (firstIcon.getIconType() == SlotConstants.SLOT_TYPE_H1
				|| firstIcon.getIconType() == SlotConstants.SLOT_TYPE_H2
				|| firstIcon.getIconType() == SlotConstants.SLOT_TYPE_H3) {

			minColToWin = 2;
		}

		while (count <= maxColToWin) {
			nextIcon = map.slotMap[line[count].y][line[count].x];
			if (nextIcon.getIconType() == SlotConstants.SLOT_TYPE_BONUS)
				break;

			if (firstIcon.getIconType() == SlotConstants.SLOT_TYPE_WILD) {
				count++;
				firstIcon = nextIcon;
			} else {
				if (nextIcon.getIconType() == firstIcon.getIconType()
						|| nextIcon.getIconType() == SlotConstants.SLOT_TYPE_WILD) {
					count++;
					if (nextIcon.getIconType() != SlotConstants.SLOT_TYPE_WILD) {
						firstIcon = nextIcon;
					}
				} else
					break;
			}
		}

		if (count >= minColToWin) {
			mLineWin.addElement(identityOfLine + 1);
			mListGoldWinPerLine.addElement(firstIcon.getWinGold(count - 1) * mUnit);
			mListNumIconWinPerLine.addElement(count);
			mTotalWin += (firstIcon.getWinGold(count - 1) * mUnit);
			winIconType = firstIcon.getIconType();
			System.out.println("line win: " + (identityOfLine + 1) + "   win icon type : " + winIconType
					+ "    total win : " + count);
			return true;
		} else
			return false;
	}
	
	public Vector<Integer> getBonusLine() {
		return mBonusLine;
	}

	public void setBonusLine(Vector<Integer> mBonusLine) {
		this.mBonusLine = mBonusLine;
	}

	public Vector<Integer> getListNumIconWinPerLine() {
		return mListNumIconWinPerLine;
	}

	public void setListNumIconWinPerLine(Vector<Integer> mListNumIconWinPerLine) {
		this.mListNumIconWinPerLine = mListNumIconWinPerLine;
	}

	public Vector<Float> getListGoldWinPerLine() {
		return mListGoldWinPerLine;
	}

	public void setListGoldWinPerLine(Vector<Float> mListGoldWinPerLine) {
		this.mListGoldWinPerLine = mListGoldWinPerLine;
	}

	public Vector<Integer> getLineWin() {
		return mLineWin;
	}

	public void setLineWin(Vector<Integer> mLineWin) {
		this.mLineWin = mLineWin;
	}

	public int getmUnit() {
		return mUnit;
	}

	public void setmUnit(int mUnit) {
		this.mUnit = mUnit;
	}

	public int getTotalScatter() {
		return mTotalScatter;
	}

	public void setTotalScatter(int mTotalScatter) {
		this.mTotalScatter = mTotalScatter;
	}
	
	public Integer getmTotalWin() {
		return mTotalWin;
	}

	public void setmTotalWin(int mTotalWin) {
		this.mTotalWin = mTotalWin;
	}

	public static int getmTotalBonus() {
		return mTotalBonus;
	}

	public static void setmTotalBonus(int mTotalBonus) {
		SlotLogicGame.mTotalBonus = mTotalBonus;
	}
}