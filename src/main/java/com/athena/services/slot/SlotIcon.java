package com.athena.services.slot;

import com.athena.services.slot.SlotConstants;

//import com.athena.services.slot.SlotConstants;

public class SlotIcon {
	private int iconType;
	private int row;
	private int col;
	private final static float[] listGoldH1 = { 0, 5, 50, 250, 1000 };
	private final static float[] listGoldH2 = { 0, 4, 40, 200, 500 };
	private final static float[] listGoldH3 = { 0, 3, 30, 150, 300 };
	private final static float[] listGoldH4 = { 0, 0, 25, 100, 200 };
	private final static float[] listGoldH5 = { 0, 0, 20, 75, 150 };

	private final static float[] listGoldL1 = { 0, 0, 15, 50, 100 };
	private final static float[] listGoldL2 = { 0, 0, 10, 30, 80 };
	private final static float[] listGoldL3 = { 0, 0, 5, 20, 60 };
	private final static float[] listGoldL4 = { 0, 0, 4, 15, 50 };
	
	private final static float[] listGoldBonus = { 0, 0, 120, 600, 2400 };
	private final static float[] listGoldScatter = { 0, 0, 4.5f, 9, 18 };
	private final static float[] listGoldWild = { 0, 10, 100, 1000, 10000 };
	
	private static float[] listWinGold;

	public float getWinGold(int identity)
	{
		switch (this.iconType) {
		case SlotConstants.SLOT_TYPE_H1:
			listWinGold = listGoldH1;
			break;
		case SlotConstants.SLOT_TYPE_H2:
			listWinGold = listGoldH2;
			break;
		case SlotConstants.SLOT_TYPE_H3:
			listWinGold = listGoldH3;
			break;
		case SlotConstants.SLOT_TYPE_H4:
			listWinGold = listGoldH4;
			break;
		case SlotConstants.SLOT_TYPE_H5:
			listWinGold = listGoldH5;
			break;
		case SlotConstants.SLOT_TYPE_L1:
			listWinGold = listGoldL1;
			break;
		case SlotConstants.SLOT_TYPE_L2:
			listWinGold = listGoldL2;
			break;
		case SlotConstants.SLOT_TYPE_L3:
			listWinGold = listGoldL3;
			break;
		case SlotConstants.SLOT_TYPE_L4:
			listWinGold = listGoldL4;
			break;
		case SlotConstants.SLOT_TYPE_BONUS:
			listWinGold = listGoldBonus;
			break;
		case SlotConstants.SLOT_TYPE_SCATTER:
			listWinGold = listGoldScatter;
			break;
		case SlotConstants.SLOT_TYPE_WILD:
			listWinGold = listGoldWild;
			break;
		default:
			break;
		}
		
		return listWinGold[identity];
	}
	
	public SlotIcon(int row, int col) {
		this.row = row;
		this.col = col;
		this.iconType = -1;
	}
	
	public int getIconType() {
		return iconType;
	}

	public void setIconType(int iconType) {
		this.iconType = iconType;
	}
	
	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}
}
