package com.athena.services.slot;

public class SlotRateTable {
	public final static float[][] rateTable1 = { { 5, 5, 6, 6, 6 }, //bonus
								  			   { 9, 8, 7, 7, 6 }, //wild
								  			   { 6, 6, 6, 6, 6 }, //scatter
								  			   { 8, 7, 2, 2, 1 }, //h1
								  			   { 8, 7, 3, 3, 2 }, //h1
								  			   { 9, 8, 4, 4, 2 }, //h3
								  			   { 10, 9, 10, 6, 5 }, //h4
								  			   { 11, 10, 12, 8, 10 }, //h5
 								  			   { 12, 13, 13, 14, 16 }, //l1
								  			   { 15, 17, 17, 19, 20 }, //l2
								  			   { 17, 19, 19, 21, 22 }, //l3
								  			   { 20, 21, 21, 24, 24 }}; //l4
	
	public final static float[][] rateTable2 = { { 5, 5, 6, 6, 6 }, //bonus
			   								   { 9, 8, 7, 7, 6 }, //wild
			   								   { 6, 6, 6, 6, 6 }, //scatter
			   								   { 8, 7, 2, 2, 1 }, //h1
			   								   { 8, 7, 3, 3, 2 }, //h1
			   								   { 9, 8, 4, 4, 2 }, //h3
			   								   { 10, 9, 10, 6, 5 }, //h4
			   								   { 12.16f, 13.3f, 13.3f, 14.82f, 15.58f }, //h5
			   								   { 15.36f, 16.8f, 16.8f, 18.72f, 19.68f }, //l1
			   								   { 15, 17, 17, 19, 20 }, //l2
			   								   { 17, 19, 19, 21, 22 }, //l3
			   								   { 20, 21, 21, 24, 24 }}; //l4

}
