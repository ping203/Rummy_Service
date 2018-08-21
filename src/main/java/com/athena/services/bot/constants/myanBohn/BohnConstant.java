package com.athena.services.bot.constants.myanBohn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BohnConstant {
    public static final List<MarkCreateTable> LIST_MARK_CREATE_TABLES = new ArrayList<>(
            Arrays.asList(
                    new MarkCreateTable(100     , 100,       100),	
                    new MarkCreateTable(500     , 2500,        5000),
                    new MarkCreateTable(1000     , 5000,        20000),
                    new MarkCreateTable(5000    , 25000,       100000),
                    new MarkCreateTable(10000   , 100000,      200000),
                    new MarkCreateTable(50000   , 500000,     1000000),
                    new MarkCreateTable(100000   , 2000000,      5000000),
                    new MarkCreateTable(500000   , 10000000,     25000000),
                    new MarkCreateTable(1000000   , 20000000,      50000000),
                    new MarkCreateTable(5000000   , 100000000,     250000000),
                    new MarkCreateTable(10000000   , 200000000,    500000000),
                    new MarkCreateTable(50000000   , 1000000000,   2500000000l)
            )
    );
    
    public static int getBoundGold(int mark) {
        
        for (MarkCreateTable markCreateTable : LIST_MARK_CREATE_TABLES) {
            if(mark == markCreateTable.getMark()){
                return markCreateTable.getAg();
            }
        }

        return LIST_MARK_CREATE_TABLES.get(LIST_MARK_CREATE_TABLES.size() - 1).getAg();
    }
}
