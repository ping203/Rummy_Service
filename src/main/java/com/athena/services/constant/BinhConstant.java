package com.athena.services.constant;

import com.athena.services.vo.MarkCreateTable;

import java.util.ArrayList;
import java.util.List;

public class BinhConstant {
    public static final boolean DEBUG = true;

    public static final int Mark0 = 100;
    public static final int Mark1 = 500;
    public static final int Mark2 = 1000;
    public static final int Mark3 = 5000;
    public static final int Mark4 = 10000;
    public static final int Mark5 = 50000;
    public static final int Mark6 = 100000;
    public static final int Mark7 = 500000;
    public static final int Mark8 = 1000000;
    public static final int Mark9 = 5000000;

    public static List<MarkCreateTable> lsMarkCreateTable;

    static {
        lsMarkCreateTable = new ArrayList<MarkCreateTable>();
        lsMarkCreateTable.add(new MarkCreateTable(Mark0, 500, 0, 500)) ;
        lsMarkCreateTable.add(new MarkCreateTable(Mark1, 10000, 0, 25000)) ;
        lsMarkCreateTable.add(new MarkCreateTable(Mark2, 20000, 0, 50000)) ;
        lsMarkCreateTable.add(new MarkCreateTable(Mark3, 100000, 0, 250000)) ;
        lsMarkCreateTable.add(new MarkCreateTable(Mark4, 200000, 0, 500000)) ;
        lsMarkCreateTable.add(new MarkCreateTable(Mark5, 2500000, 0, 5000000)) ;
        lsMarkCreateTable.add(new MarkCreateTable(Mark6, 5000000, 0, 10000000)) ;
        lsMarkCreateTable.add(new MarkCreateTable(Mark7, 25000000, 0, 50000000)) ;
        lsMarkCreateTable.add(new MarkCreateTable(Mark8, 50000000, 0, 100000000)) ;
        lsMarkCreateTable.add(new MarkCreateTable(Mark9, 250000000, 0, 500000000)) ;
    }

    public static MarkCreateTable getMarkCreateTableByMark(int mark){
        for(MarkCreateTable markCreateTable: lsMarkCreateTable){
            if(markCreateTable.getMark() == mark) return markCreateTable;
        }
        return null;
    }

    public static int getRoomIdByMark(int mark){
        if(mark == BinhConstant.Mark1 || mark == BinhConstant.Mark2 || mark == BinhConstant.Mark0)
            return 1;
        else if(mark == BinhConstant.Mark4 || mark == BinhConstant.Mark5 || mark == BinhConstant.Mark3)
            return 2;
        else if(mark == BinhConstant.Mark7 || mark == BinhConstant.Mark6)
            return 3;
        else if(mark == BinhConstant.Mark9 || mark == BinhConstant.Mark8)
            return 4;

        return 1;

    }
}
