package com.athena.services.vo;

import java.util.ArrayList;
import java.util.List;

public class DiemChi {
	
	public DiemChi(){
		Diem = 0;
		QuanBai = new ArrayList<Card>();
	}
	private int Diem;
	private List<Card> QuanBai;
	
	
	public int getDiem() {
		return Diem;
	}


	public void setDiem(int diem) {
		Diem = diem;
	}


	public List<Card> getQuanBai() {
		return QuanBai;
	}


	public void setQuanBai(List<Card> quanBai) {
		QuanBai = quanBai;
	}


	public int Comparer(DiemChi dc){
//		if(this==null)
//			return -1;
		if(dc==null)
			return 1;
		if(this.Diem > dc.getDiem())
			return 1;
		else if(this.Diem == dc.getDiem()){
			for(int i=0;i<QuanBai.size();i++){
				if(QuanBai.get(i).getN()==dc.getQuanBai().get(i).getN()){
					continue;
				}else{
					return this.QuanBai.get(i).Compare(dc.getQuanBai().get(i));
				}
			}
			return 0;
		} 
		return -1;
	}
}
