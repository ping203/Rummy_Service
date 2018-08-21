package com.athena.services.constant;

import com.dst.bean.MarkBotConfig;
import com.athena.services.vo.MarkCreateTable;

import java.util.ArrayList;
import java.util.List;

public class SamGongConstant {
    public static final boolean DEBUG = true;
    //@Inject
    public static final int Mark100 = 100;
    public static final int Mark500 = 500;
    public static final int Mark1k = 1000;
    public static final int Mark5k = 5000;
    public static final int Mark10k = 10000;
    public static final int Mark50k = 50000;
    public static final int Mark100k = 100000;
    public static final int Mark500k = 500000;
    public static final int Mark1000k = 1000000;
    public static final int Mark5000k = 5000000;


    public static List<MarkCreateTable> lsMarkCreateTable;

    static {
        lsMarkCreateTable = new ArrayList<MarkCreateTable>();

        // Setup Danh sach muc cuoc ban
        lsMarkCreateTable.add(new MarkCreateTable(Mark100, 500, 500, 10000)); // 100
        lsMarkCreateTable.add(new MarkCreateTable(Mark500, 2500, 10000, 50000)); // 500
        lsMarkCreateTable.add(new MarkCreateTable(Mark1k, 5000, 50000, 200000)); // 1,000
        lsMarkCreateTable.add(new MarkCreateTable(Mark5k, 25000, 250000, 1000000)); // 5,000
        lsMarkCreateTable.add(new MarkCreateTable(Mark10k, 100000, 500000, 3000000)); // 10,000
        lsMarkCreateTable.add(new MarkCreateTable(Mark50k, 500000, 5000000, 15000000)); // 50,000
        lsMarkCreateTable.add(new MarkCreateTable(Mark100k, 2000000, 10000000, 50000000)); // 100,000
        lsMarkCreateTable.add(new MarkCreateTable(Mark500k, 10000000, 50000000, 250000000)); // 500,000
        lsMarkCreateTable.add(new MarkCreateTable(Mark1000k, 20000000, 100000000, 500000000)); // 1000,000
        lsMarkCreateTable.add(new MarkCreateTable(Mark5000k, 100000000, 500000000, 2500000000L)); // 5000000
    }

    public static List<MarkBotConfig> markBotConfigs = new ArrayList<>();

    public static final short Type100 = 11;
    public static final short Type500 = 12;
    public static final short Type1k = 13;
    public static final short Type5k = 14;
    public static final short Type10k = 15;
    public static final short Type50k = 16;
    public static final short Type100k = 17;
    public static final short Type500k = 18;
    public static final short Type1000k = 19;
    public static final short Type5000k = 20;

    static {
        markBotConfigs.add(new MarkBotConfig(Mark100, Type100, 20, 1, 3));
        markBotConfigs.add(new MarkBotConfig(Mark500, Type500, 15, 1, 3));
        markBotConfigs.add(new MarkBotConfig(Mark1k, Type1k, 15, 1, 3));
        markBotConfigs.add(new MarkBotConfig(Mark5k, Type5k, 10, 2, 4));
        markBotConfigs.add(new MarkBotConfig(Mark10k, Type10k, 12, 2,4));
        markBotConfigs.add(new MarkBotConfig(Mark50k, Type50k, 10, 2, 5));
        markBotConfigs.add(new MarkBotConfig(Mark100k, Type100k, 10, 3, 6));
        markBotConfigs.add(new MarkBotConfig(Mark500k, Type500k, 4, 5, 8));
        markBotConfigs.add(new MarkBotConfig(Mark1000k, Type1000k, 4, 5, 8));
    }


    public static MarkBotConfig getMarkBotConfigByMark(int mark){
        for(MarkBotConfig markBotConfig: markBotConfigs){
            if(markBotConfig.getMark() == mark){
                return markBotConfig;
            }
        }
        return markBotConfigs.get(0);
    }

    public static MarkBotConfig getMarkBotConfigByType(short type){
        for(MarkBotConfig markBotConfig: markBotConfigs){
            if(markBotConfig.getType() == type){
                return markBotConfig;
            }
        }
        return markBotConfigs.get(0);
    }


    public static short getUserTypeByCountBot(int sum, int count){
        int sumP = 0;
        for(MarkBotConfig botConfig: markBotConfigs){
            sumP += botConfig.getPercent();
            int sumB = sum * sumP / 100;
            if(count > sumB) continue;
            return botConfig.getType();
        }
        return markBotConfigs.get(0).getType();
    }

    public static MarkCreateTable getMarkCreateTableByMark(int mark){
        for(MarkCreateTable markCreateTable: lsMarkCreateTable){
            if(markCreateTable.getMark() == mark) return markCreateTable;
        }
        return null;
    }

    public static short getRoomIDbyMark(int mark) {
            if(mark == SamGongConstant.Mark500 || mark == SamGongConstant.Mark1k || mark == SamGongConstant.Mark100)
                return 1;
            else if(mark == SamGongConstant.Mark10k || mark == SamGongConstant.Mark50k || mark == SamGongConstant.Mark5k)
                return 2;
            else if(mark == SamGongConstant.Mark500k || mark == SamGongConstant.Mark100k)
                return 3;
            else if(mark == SamGongConstant.Mark5000k || mark == SamGongConstant.Mark1000k)
                return 4;
        return (short) 1;
    }
}
