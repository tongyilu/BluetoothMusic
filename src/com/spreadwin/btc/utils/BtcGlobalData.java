package com.spreadwin.btc.utils;

public class BtcGlobalData {
	//PhoneBookInfo Type
    public static final int PB_SIM = 0;
    public static final int PB_PHONE = 1;
    public static final int PB_OUT = 2;
    public static final int PB_MISS = 3;
    public static final int PB_IN = 4;
    
    //A2DP STATUS
    public static final int A2DP_DISCONNECT = 0;
    public static final int A2DP_CONNECTED = 1;
    public static final int A2DP_PLAYING = 2;
    
    //BFP STATUS
    public static final int BFP_DISCONNECT = 0;
    public static final int BFP_CONNECTED = 1;

    //CALL STATUS
    public static final int NO_CALL = 0;//挂断
    public static final int CALL_OUT = 1;//呼出
    public static final int CALL_IN = 2; //来电
    public static final int IN_CALL = 3;//接通
    
    //Pair STATUS
    public static final int NOT_PAIR = 0;
    public static final int IN_PAIR = 1;
    public static final int PAIRRED = 2;
    
    //Sync STATUS
    public static final int NOT_SYNC = 0;
    public static final int IN_SYNC = 1;
    public static final int NEW_SYNC = 2;
    
}
