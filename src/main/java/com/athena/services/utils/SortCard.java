package com.athena.services.utils;

import java.util.Comparator;

import com.athena.services.vo.Card;

public class SortCard implements Comparator<Card> {

	@Override
	public int compare(Card o1, Card o2) {
		// TODO Auto-generated method stub
		if(o1.getN() > o2.getN()) return -1;
		else if(o1.getN() == o2.getN()){
			if(o1.getS() > o2.getS()){
				return -1;
			} else {
				return 1;
			}
		} 
		return 1;
	}
	
}
