package com.athena.services.utils;

import java.util.Collections;
import java.util.List;
import com.athena.services.vo.Card;


public class LuckyFunction {
	
	public static int Check_5Quan(List<Card> lsCard){
		//lsCard = lsCard.OrderBy(c => c.N).ThenBy(c => c.S).ToList();
		Collections.sort(lsCard,new SortCardASC());
        int dc = 0;
        dc = CheckStraightFlush(lsCard);
        if (dc != 0) return 10000;//10000,0000
        dc = CheckFourOfAKind(lsCard);
        if (dc != 0) return 3000;//3000,0000
        dc = CheckFullHouse(lsCard);
        if (dc != 0) return 1000;//1000,0000
        dc = CheckFlush(lsCard);
        if (dc != 0) return 300;//300,0000
        dc = CheckStraight(lsCard);
        if (dc != 0) return 50;//50,0000
        dc = CheckThreeOfAKind(lsCard);
        if (dc != 0) return 20;//20,0000
        dc = CheckTwoPair(lsCard);
        if (dc != 0) return 5;//5,0000
        dc = CheckOnePair(lsCard);
        if (dc != 0) return 1;//1,0000
        dc = CheckJacksOrBetter(lsCard);
        return 0;
	}

    //region thung pha sanh
    public static int CheckStraightFlush(List<Card> lsCard)
    {
        if(lsCard.get(4).getN() == 14)
        {
            if (lsCard.get(4).getN()-13 == lsCard.get(0).getN() - 1 &&
            lsCard.get(4).getS() == lsCard.get(0).getS() &&
            lsCard.get(0).getN() == lsCard.get(1).getN() - 1 &&
            lsCard.get(0).getS() == lsCard.get(1).getS() &&
            lsCard.get(1).getN() == lsCard.get(2).getN() - 1 &&
            lsCard.get(1).getS() == lsCard.get(2).getS() &&
            lsCard.get(2).getN() == lsCard.get(3).getN() - 1 &&
            lsCard.get(2).getS() == lsCard.get(3).getS())
            {
                return 9;
            }
        }
        if (lsCard.get(0).getN() == lsCard.get(1).getN() - 1 &&
            lsCard.get(0).getS() == lsCard.get(1).getS() &&
            lsCard.get(1).getN() == lsCard.get(2).getN() - 1 &&
            lsCard.get(1).getS() == lsCard.get(2).getS() &&
            lsCard.get(2).getN() == lsCard.get(3).getN() - 1 &&
            lsCard.get(2).getS() == lsCard.get(3).getS() &&
            lsCard.get(3).getN() == lsCard.get(4).getN() - 1 &&
            lsCard.get(3).getS() == lsCard.get(4).getS())
        {
            return 9;
        }
        return 0;
    }
    //endregion

    //region tu quy
    public static int CheckFourOfAKind(List<Card> lsCard)
    {
        if (lsCard.get(0).getN() == lsCard.get(1).getN() &&
            lsCard.get(1).getN() == lsCard.get(2).getN() &&
            lsCard.get(2).getN() == lsCard.get(3).getN())
        {
            return 8;
        }
        else if (lsCard.get(1).getN() == lsCard.get(2).getN() &&
            lsCard.get(2).getN() == lsCard.get(3).getN() &&
            lsCard.get(3).getN() == lsCard.get(4).getN())
        {
            return 8;
        }
        return 0;
    }
    //endregion

    //region cu lu
    public static int CheckFullHouse(List<Card> lsCard)
    {
        if (lsCard.get(0).getN() == lsCard.get(1).getN())
        {
            if (lsCard.get(1).getN() == lsCard.get(2).getN())
            {
                if (lsCard.get(3).getN() == lsCard.get(4).getN())
                {
                    return 7;
                }
            }
            else
            {
                if (lsCard.get(2).getN() == lsCard.get(3).getN() &&
                    lsCard.get(3).getN() == lsCard.get(4).getN())
                {
                    return 7;
                }
            }
        }
        return 0;
    }
    //endregion

    //region thung
    public static int CheckFlush(List<Card> lsCard)
    {
        //lsCard = lsCard.OrderBy(c => c.getN()).ThenBy(c => c.getS()).ToList();
    	Collections.sort(lsCard,new SortCardASC());
        if (lsCard.size() == 3)
        {
            if (lsCard.get(0).getS() == lsCard.get(1).getS() &&
                lsCard.get(1).getS() == lsCard.get(2).getS())
            {
                return 6;
            }
        }
        else
        {
            if (lsCard.get(0).getS() == lsCard.get(1).getS() &&
            lsCard.get(1).getS() == lsCard.get(2).getS() &&
            lsCard.get(2).getS() == lsCard.get(3).getS() &&
            lsCard.get(3).getS() == lsCard.get(4).getS())
            {
                return 6;
            }
        }
        return 0;
    }
    //endregion

    //region sanh
    public static int CheckStraight(List<Card> lsCard)
    {
        //lsCard = lsCard.OrderBy(c => c.getN()).ThenBy(c=>c.getS()).ToList();
    	Collections.sort(lsCard,new SortCardASC());
        if (lsCard.size() == 3)
        {
            if (lsCard.get(2).getN() == 14)
            {
                if (lsCard.get(2).getN() - 13 == lsCard.get(0).getN() - 1 &&
                    lsCard.get(0).getN() == lsCard.get(1).getN() - 1)
                {
                    return 5;
                }
            }
            if (lsCard.get(0).getN() == lsCard.get(1).getN() - 1 &&
                lsCard.get(1).getN() == lsCard.get(2).getN() - 1)
            {
                return 5;
            }
        }
        else
        {
            if (lsCard.get(4).getN() == 14)
            {
                if (lsCard.get(4).getN() - 13 == lsCard.get(0).getN() - 1 &&
                lsCard.get(0).getN() == lsCard.get(1).getN() - 1 &&
                lsCard.get(1).getN() == lsCard.get(2).getN() - 1 &&
                lsCard.get(2).getN() == lsCard.get(3).getN() - 1)
                {
                    return 5;
                }
            }
            if (lsCard.get(0).getN() == lsCard.get(1).getN() - 1 &&
                lsCard.get(1).getN() == lsCard.get(2).getN() - 1 &&
                lsCard.get(2).getN() == lsCard.get(3).getN() - 1 &&
                lsCard.get(3).getN() == lsCard.get(4).getN() - 1)
            {
                return 5;
            }
        }
        
        return 0;
    }
    //endregion

    //region xam
    public static int CheckThreeOfAKind(List<Card> lsCard)
    {
        if (lsCard.size() == 3)
        {
            if (lsCard.get(0).getN() == lsCard.get(1).getN() &&
            lsCard.get(1).getN() == lsCard.get(2).getN())
            {
                return 4;
            }
        }
        else
        {
            if (lsCard.get(0).getN() == lsCard.get(1).getN() &&
            lsCard.get(1).getN() == lsCard.get(2).getN())
            {
                return 4;
            }
            if (lsCard.get(1).getN() == lsCard.get(2).getN() &&
                lsCard.get(2).getN() == lsCard.get(3).getN())
            {
                return 4;
            }
            if (lsCard.get(2).getN() == lsCard.get(3).getN() &&
                lsCard.get(3).getN() == lsCard.get(4).getN())
            {
                return 4;
            }
        }
        
        return 0;
    }
    //endregion

    //region thu
    public static int CheckTwoPair(List<Card> lsCard)
    {
        if (lsCard.get(0).getN() == lsCard.get(1).getN())
        {
            if (lsCard.get(2).getN() == lsCard.get(3).getN())
            {
                return 3;
            }
            if (lsCard.get(3).getN() == lsCard.get(4).getN())
            {
                return 3;
            }
        }
        if (lsCard.get(1).getN() == lsCard.get(2).getN())
        {
            if (lsCard.get(3).getN() == lsCard.get(4).getN())
            {
                return 3;
            }
        }
        return 0;
    }
    //endregion

    //region doi
    public static int CheckOnePair(List<Card> lsCard)
    {
        if (lsCard.size() == 3)
        {
            if (lsCard.get(0).getN() == lsCard.get(1).getN())
            {
                return 2;
            }
            if (lsCard.get(1).getN() == lsCard.get(2).getN())
            {
                return 2;
            }
        }
        else
        {
            if (lsCard.get(0).getN() == lsCard.get(1).getN())
            {
                return 2;
            }
            if (lsCard.get(1).getN() == lsCard.get(2).getN())
            {
                return 2;
            }
            if (lsCard.get(2).getN() == lsCard.get(3).getN())
            {
                return 2;
            }
            if (lsCard.get(3).getN() == lsCard.get(4).getN())
            {
                return 2;
            }
        }
        return 0;
    }
    //endregion
    //0 - Bonus
    //1 - Scatter
    //2 - Wild
    //Slot Function
    public static int CheckSlotRow(int[][] arrCheck, int H1, int V1, int H2, int V2, int H3, int V3, int H4, int V4, int H5, int V5) {
    	int value = arrCheck[H1][V1] ;
    	if (value == 2) value = arrCheck[H2][V2] ;
    	if (value == 2) value = arrCheck[H3][V3] ;
    	if (value == 2) value = arrCheck[H4][V4] ;
    	if (value == 2) value = arrCheck[H5][V5] ;
    	if (value == 0) value = 2 ;
    	if (((arrCheck[H1][V1] == value) || (arrCheck[H1][V1] == 2)) && ((arrCheck[H2][V2] == value) || (arrCheck[H2][V2] == 2))
    	  && ((arrCheck[H3][V3] == value) || (arrCheck[H3][V3] == 2)) && ((arrCheck[H4][V4] == value) || (arrCheck[H4][V4] == 2))
    	  && ((arrCheck[H5][V5] == value) || (arrCheck[H5][V5] == 2))) {
    		//Trung 5 duong
    		if (value == 2) return 10000 ;
    		else if (value == 3) return 2000 ;
    		else if (value == 4) return 1000 ;
    		else if (value == 5) return 500 ;
    		else if (value == 6) return 300 ;
    		else if (value == 7) return 200 ;
    		else if (value == 8) return 100 ;
    		else if (value == 9) return 80 ;
//    		else if (value == 10) return 80 ;
//    		else if (value == 11) return 50 ;
    	}
    	else if (((arrCheck[H1][V1] == value) || (arrCheck[H1][V1] == 2)) && ((arrCheck[H2][V2] == value) || (arrCheck[H2][V2] == 2))
    	    	  && ((arrCheck[H3][V3] == value) || (arrCheck[H3][V3] == 2)) 
    	    	  && ((arrCheck[H4][V4] == value) || (arrCheck[H4][V4] == 2))) {
    		//Trung 4 duong
    		if (value == 2) return 2000 ;
    		else if (value == 3) return 200 ;
    		else if (value == 4) return 100 ;
    		else if (value == 5) return 70 ;
    		else if (value == 6) return 50 ;
    		else if (value == 7) return 40 ;
    		else if (value == 8) return 30 ;
    		else if (value == 9) return 20 ;
//    		else if (value == 10) return 20 ;
//    		else if (value == 11) return 15 ;
    	}
    	else if (((arrCheck[H1][V1] == value) || (arrCheck[H1][V1] == 2)) && ((arrCheck[H2][V2] == value) || (arrCheck[H2][V2] == 2))
  	    	  && ((arrCheck[H3][V3] == value) || (arrCheck[H3][V3] == 2))) {
    		//Trung 3 duong
    		if (value == 2) return 1000 ;
    		else if (value == 3) return 50 ;
    		else if (value == 4) return 30 ;
    		else if (value == 5) return 20 ;
    		else if (value == 6) return 15 ;
    		else if (value == 7) return 10 ;
    		else if (value == 8) return 8 ;
    		else if (value == 9) return 6 ;
//    		else if (value == 10) return 5 ;
//    		else if (value == 11) return 4 ;
    	}
    	else if (((arrCheck[H1][V1] == value) || (arrCheck[H1][V1] == 2)) 
    			&& ((arrCheck[H2][V2] == value) || (arrCheck[H2][V2] == 2))) {
    		if (value == 2) return 100 ;
    		else if (value == 3) return 4 ;
    		else if (value == 4) return 3 ;
//    		else if (value == 5) return 3 ;
    	}
    	return 0;
    }
    
    public static int GetNumberWinPerRow(int[][] arrCheck, int H1, int V1, int H2, int V2, int H3, int V3, int H4, int V4, int H5, int V5) {
    	int value = arrCheck[H1][V1] ;
    	if (value == 2) value = arrCheck[H2][V2] ;
    	if (value == 2) value = arrCheck[H3][V3] ;
    	if (value == 2) value = arrCheck[H4][V4] ;
    	if (value == 2) value = arrCheck[H5][V5] ;
    	if (value == 0) value = 2 ;
    	if (((arrCheck[H1][V1] == value) || (arrCheck[H1][V1] == 2)) && ((arrCheck[H2][V2] == value) || (arrCheck[H2][V2] == 2))
    	  && ((arrCheck[H3][V3] == value) || (arrCheck[H3][V3] == 2)) && ((arrCheck[H4][V4] == value) || (arrCheck[H4][V4] == 2))
    	  && ((arrCheck[H5][V5] == value) || (arrCheck[H5][V5] == 2))) {
    		return 5 ;
    	}
    	else if (((arrCheck[H1][V1] == value) || (arrCheck[H1][V1] == 2)) && ((arrCheck[H2][V2] == value) || (arrCheck[H2][V2] == 2))
    	    	  && ((arrCheck[H3][V3] == value) || (arrCheck[H3][V3] == 2)) 
    	    	  && ((arrCheck[H4][V4] == value) || (arrCheck[H4][V4] == 2))) {
    		return 4 ;
    	}
    	else if (((arrCheck[H1][V1] == value) || (arrCheck[H1][V1] == 2)) && ((arrCheck[H2][V2] == value) || (arrCheck[H2][V2] == 2))
  	    	  && ((arrCheck[H3][V3] == value) || (arrCheck[H3][V3] == 2))) {
    		return 3 ;
    	}
    	else if (((arrCheck[H1][V1] == value) || (arrCheck[H1][V1] == 2)) 
    			&& ((arrCheck[H2][V2] == value) || (arrCheck[H2][V2] == 2))) {
    		return 2 ;
    	}
    	return 0;
    }
    public static int CheckScatter(int[][] arrCheck) {
    	int scatter = 0 ;
    	for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 5; j++) {
				if (arrCheck[i][j] == 1) scatter++ ;
			}
		}
    	return scatter ;
    }
    
    public static int CheckBonus(int[][] arrCheck, int H1, int V1, int H2, int V2, int H3, int V3, int H4, int V4, int H5, int V5) {
    	if (((arrCheck[H1][V1] == 0) && (arrCheck[H2][V2] == 0) && (arrCheck[H3][V3] == 0)) ||
    		((arrCheck[H2][V2] == 0) && (arrCheck[H3][V3] == 0) && (arrCheck[H4][V4] == 0)) ||
    		((arrCheck[H3][V3] == 0) && (arrCheck[H4][V4] == 0) && (arrCheck[H5][V5] == 0))) return 3 ;
    	if (((arrCheck[H1][V1] == 0) && (arrCheck[H2][V2] == 0) && (arrCheck[H3][V3] == 0) && (arrCheck[H4][V4] == 0)) ||
        		((arrCheck[H2][V2] == 0) && (arrCheck[H3][V3] == 0) && (arrCheck[H4][V4] == 0) && (arrCheck[H5][V5] == 0))) return 4 ;
    	if ((arrCheck[H1][V1] == 0) && (arrCheck[H2][V2] == 0) && (arrCheck[H3][V3] == 0)
    			&& (arrCheck[H4][V4] == 0) && (arrCheck[H5][V5] == 0)) return 5 ;
    	return 0 ;
    }
    
    
    
    public static int CheckJacksOrBetter(List<Card> lsCard)
    {
        return 1;
    }
}
