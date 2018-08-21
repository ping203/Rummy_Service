package com.athena.services.slot;

import java.awt.Point;
import java.util.Vector;

public class SlotPayLine {
	
	private static final Integer R1 = 0;
	private static final Integer R2 = 1;
	private static final Integer R3 = 2;
	
	private static final Integer C1 = 0;
	private static final Integer C2 = 1;
	private static final Integer C3 = 2;
	private static final Integer C4 = 3;
	private static final Integer C5 = 4;
	
	
	private static final Point[] line1 = { new Point(C1, R2), new Point(C2, R2), new Point(C3, R2), new Point(C4, R2), new Point(C5, R2) };

	private static final Point[] line2 = { new Point(C1, R1), new Point(C2, R1), new Point(C3, R1), new Point(C4, R1), new Point(C5, R1) };

	private static final Point[] line3 = { new Point(C1, R3), new Point(C2, R3), new Point(C3, R3), new Point(C4, R3), new Point(C5, R3) };

	private static final Point[] line4 = { new Point(C1, R1), new Point(C2, R2), new Point(C3, R3), new Point(C4, R2), new Point(C5, R1) }; /*v xuoi*/
	private static final Point[] line5 = { new Point(C1, R3), new Point(C2, R2), new Point(C3, R1), new Point(C4, R2), new Point(C5, R3) }; // v nguoc

	private static final Point[] line6 = { new Point(C1, R2), new Point(C2, R1), new Point(C3, R1), new Point(C4, R1), new Point(C5, R2) }; // /--\
	private static final Point[] line7 = { new Point(C1, R2), new Point(C2, R3), new Point(C3, R3), new Point(C4, R3), new Point(C5, R2) }; // \__/

	private static final Point[] line8 = { new Point(C1, R1), new Point(C2, R1), new Point(C3, R2), new Point(C4, R3), new Point(C5, R3) }; // --\__
	private static final Point[] line9 = { new Point(C1, R3), new Point(C2, R3), new Point(C3, R2), new Point(C4, R1), new Point(C5, R1) }; // __/--
	

	public Vector<Point[]> listPlayLine = new Vector<Point[]>();

	public SlotPayLine() {
		this.initListLine();
	}
	
	void initListLine()
	{
		listPlayLine.addElement(line1);
		listPlayLine.addElement(line2);
		listPlayLine.addElement(line3);
		listPlayLine.addElement(line4);
		listPlayLine.addElement(line5);
		listPlayLine.addElement(line6);
		listPlayLine.addElement(line7);
		listPlayLine.addElement(line8);
		listPlayLine.addElement(line9);
	}
}
