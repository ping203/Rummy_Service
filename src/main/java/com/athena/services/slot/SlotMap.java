package com.athena.services.slot;

import java.util.Random;
import java.util.Vector;

import com.athena.services.slot.SlotIcon;
import com.athena.services.slot.SlotRateTable;
import com.athena.services.slot.SlotConstants;

public class SlotMap {

	private final static int MAP_COLUMN = 5;
	private final static int MAP_ROW = 3;

	public SlotIcon[][] slotMap = new SlotIcon[MAP_ROW][MAP_COLUMN];
	private float[][] slotRateTable = new float[12][5];
	private String strResult = "";
	public Vector<Integer> listIconType = new Vector<Integer>();

	public SlotMap() {
		for (int i = 0; i < MAP_ROW; i++) {
			for (int j = 0; j < MAP_COLUMN; j++) {
				slotMap[i][j] = new SlotIcon(i, j);
			}
		}
		
		slotRateTable = SlotRateTable.rateTable1;
		strResult = "";

		this.setRandomTypeSlotIcon();
	}

	void setRandomTypeSlotIcon() {
		for (int i = 0; i < MAP_ROW; i++) {
			for (int j = 0; j < MAP_COLUMN; j++) {
				int type = getRandomType(slotMap[i][j]);
				System.out.println(type);
				slotMap[i][j].setIconType(type);
				strResult = strResult + slotMap[i][j].getIconType() + ";";
				listIconType.add(type);
			}
		}
	}

	public String getStrResult() {
		return strResult;
	}

	public void setStrResult(String strResult) {
		this.strResult = strResult;
	}

	public int getRandomType(SlotIcon icon) {
		float sumOfCol = 0;
		int type = 0;
		float sumCol = 0;
		int rateRableLength = slotRateTable.length;

		for (int i = 0; i < rateRableLength; i++) {
			sumOfCol = sumOfCol + slotRateTable[i][icon.getCol()];
		}

		float rand = new Random().nextFloat() * sumOfCol;

		while (type < (rateRableLength - 1)) {
			sumCol = (sumCol + slotRateTable[type][icon.getCol()]);
			if (sumCol > rand)
				break;
			type++;
		}

		/*
		 * check truong hop neu da co 1 con scatter trong cot thi goi lai ham
		 * nay
		 */

		if (type == SlotConstants.SLOT_TYPE_SCATTER) {
			for (int i = 0; i < 3; i++) {
				if (i != icon.getRow()) {
					if (slotMap[i][icon.getCol()].getIconType() == SlotConstants.SLOT_TYPE_SCATTER) {
						getRandomType(icon);
					}
				}
			}
		}

		return type;
	}
}
