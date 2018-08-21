package com.athena.services.vo;

public class Card {
	public Card(){
		
	}
	public Card(int s,int n,int i){
		this.S = s;
		this.N = n;
		this.I = i;
	}
	public int Compare(Card c1){
		if(this.N > c1.getN())
			return 1;
		else if(this.N == c1.getN())
			return 0;
		else 
			return -1;
	}
	private int S;
	private int N;
	private int I;
	
	public int getI() {
		return I;
	}
	public void setI(int i) {
		I = i;
	}
	public int getS() {
		return S;
	}
	public void setS(int s) {
		S = s;
	}
	public int getN() {
		return N;
	}
	public void setN(int n) {
		N = n;
	}
}
