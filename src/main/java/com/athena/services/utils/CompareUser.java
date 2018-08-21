package com.athena.services.utils;

import java.util.Comparator;

import com.athena.services.vo.UserInfo;

public class CompareUser implements Comparator<UserInfo>{
	@Override
	public int compare(UserInfo o1, UserInfo o2) {
		// TODO Auto-generated method stub
		if(o1.getAG() > o2.getAG())
			return -1;
		else
			return 1;
	}
}
