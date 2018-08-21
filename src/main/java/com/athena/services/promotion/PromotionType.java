package com.athena.services.promotion;

public class PromotionType {
	// 1. UserMSG
	// 2. TemPromotionDetails
	// 3. AdminPromotion
	public static final int TYPE_NOT_ENOUGH_GOLD 		= 0; // het tien
	public static final int TYPE_FORTUITY 				= 1; // dot xuat
	public static final int TYPE_UP_VIP 				= 2; // len vip
	public static final int TYPE_COUNT_TIME 			= 3; // online dem gio
	public static final int TYPE_ROTATION				= 4; // vong quay may man
	public static final int TYPE_WATCH_VIDEO			= 5; // xem video
	
	public static final int TYPE_GIFT_CODE				= 6; // gift code
	public static final int TYPE_INVITE_FACEBOOK		= 7; // moi ban facebook
	
	public static final int TYPE_COMPENSATION			= 15; // den bu
	public static final int TYPE_BONUS_CASHIN_BY_DAY	= 16; // Vào mỗi Sunday hoặc Thursday Nạp đủ 50K IDR được tặng 5M chip.
	public static final int TYPE_DAILY					= 17; // dang nhap hang ngay
	
	public static final int TYPE_INVITE_SHARE_FACEBOOK	= 23; // moi ban chia se qua facebook
	public static final int TYPE_BONUS_CASHIN_BY_VIP	= 24; // nap tien khuyen mai theo vip
	public static final int TYPE_GIFT_CODE_INVITE		= 100; // gift invite friend
}
