package com.athena.services.handler;

public class EVT_ID {
	
	// 1 -> 100 : game action 
	public static final int CREATE_TABLE 						= 1;
	public static final int PLAYNOW_CHANGE_TABLE		 		= 2;
	public static final int PLAYNOW_BY_MARK_TYPE 				= 3;
	public static final int CREATE_TABLE_TYPE   				= 4;
	public static final int PLAYNOW_CHANGE_TABLE_BY_TYPE 		= 5;
	public static final int CREATE_TABLE_WITH_NUM_SEAT 			= 6;
	public static final int PLAYNOW_POKER_TEXAS 				= 7;
	public static final int JOIN_TABLE_WITH_ID 					= 8;
	public static final int CREATE_TABLE_WITH_NUM_SEAT_9K 		= 9;
	public static final int PLAYNOW_WITH_GAMEID 				= 10;
	
	// 100 -> promotion
	public static final int PROMOTION_INVITE_FRIEND_FACEBBOK	= 100;
	public static final int PROMOTION_FACEBBOK_NEW_DEVICE		= 101;
	public static final int PROMOTION_INVITE_FACEBOOK_NEW		= 102;
	
	// 200 - get user info
	public static final int GET_STATUS							= 200;
	public static final int UPDATE_STATUS						= 201;
	public static final int REGISTER_ACC						= 202;
	
	// 300 - bank
	public static final int BANK_GET_INFO						= 300;
	public static final int SEND_BANK							= 301;
	public static final int BANK_WITHDRAW						= 302;
	public static final int BANK_SEND_OTHER_USER				= 303;
	public static final int BANK_YOU_GET_CHIP_FROM_ORTHER_USER	= 304;
	
	// 400 - get top
	public static final int GET_TOP_VIP							= 400;
	public static final int GET_TOP_RICH						= 401;
	
	// 500 - get tranfer history
	public static final int GET_TRANSFER_HISTORY				= 500;
	
	// 600 - room action
	public static final int GET_LIST_TABLE						= 600;
	
	// 700 - room action
	public static final int CHANGE_HAPPY_HOURS					= 700;
}
