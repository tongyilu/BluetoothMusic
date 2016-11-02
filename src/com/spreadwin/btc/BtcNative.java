
package com.spreadwin.btc;



import android.util.Log;

public class BtcNative {
	static String TAG = "BTC";
	
	static {
		Log.d(TAG, "load jni library");
		try {
			System.loadLibrary("btc_spreadwin");  			
		} catch (Exception e) {
			Log.d(TAG, "load jni library is error");
		}
	} 
	/**
	 * @return 初始化蓝牙模块
	 */
	static native int initBtc();
	
	public static native int getPairStatus(); 
	/**
	 * @return 更新蓝牙连接状态
	 * @param getBfpStatus() = 1 : "连接" ? "断开"
	 */
	public static native int getBfpStatus();
	/**
	 * @return 来电状态 
	 * @param BtcGlobalData.CALL_IN  来电
	 * @param BtcGlobalData.IN_CALL  接通
	 * @param BtcGlobalData.CALL_OUT 呼出
	 * @param BtcGlobalData.NO_CALL  挂断
	 */
	public static native int getCallStatus();
	/**
	 * @return 获取接通状态
	 */
	public static native int changeAudioPath();
	/**
	 * @return 判断接通状态
	 * @return getAudioPath() = 0 : "车机" ? "手机"
	 */
	public static native int getAudioPath();
	/**
	 * @return 播放状态
	 */
	public static native int getA2dpStatus();
	/**
	 * @return 获取同步联系人
	 * @param type PB_OUT   = 2;  呼出
	 * @param type PB_MISS  = 3;  未接
	 * @param type PB_IN    = 4;  呼入
	 */
	public static native int getSyncStatus(int type);
	/**
	 * @return 需要同步联系人
	 * @param type PB_OUT   = 2;  呼出
	 * @param type PB_MISS  = 3;  未接
	 * @param type PB_IN    = 4;  呼入
	 * @return
	 */
	static native int startSyncPhoneBook(int type);
	/**
	 * @return 查询手机联系人个数
	 * @param type PB_PHONE = 1;  手机
	 * @param type PB_SIM =   0;  sim卡
	 */
	public static native int getPhoneBookRecordNum(int type);
	/**
	 * @param type PB_PHONE = 1;  手机
	 * @param type PB_SIM   = 0;  sim卡
	 * @param type PB_OUT   = 2;  呼出
	 * @param type PB_MISS  = 3;  未接
	 * @param type PB_IN    = 4;  呼入
	 * @param index 下标
	 * @return 获取联系人名称
	 */
	public static native String getPhoneBookRecordNameByIndex(int type, int index);
	/**
	 * @param type PB_PHONE = 1;  手机
	 * @param type PB_SIM   = 0;  sim卡
	 * @param type PB_OUT   = 2;  呼出
	 * @param type PB_MISS  = 3;  未接
	 * @param type PB_IN    = 4;  呼入
	 * @param index 下标
	 * @return 获取联系人电话号码
	 */
	public static native String getPhoneBookRecordNumberByIndex(int type, int index);
	/**
	 * @param type PB_PHONE = 1;  手机
	 * @param type PB_SIM   = 0;  sim卡
	 * @param type PB_OUT   = 2;  呼出
	 * @param type PB_MISS  = 3;  未接
	 * @param type PB_IN    = 4;  呼入
	 * @param index 下标
	 * @return 获取联系人呼出呼入未接时间
	 */
	public static native String getPhoneBookRecordTimeByIndex (int type, int index);
    static native int writeCommands(int index, String param);
	/**
	 * @return 播放蓝牙音乐
 	 */
    public static native int playMusic();
    /**
     * @return 播放蓝牙音乐
     */
	public static native int pauseMusic();
	/**
	 * @return 上一首
	 */
	public static native int lastSong();
	/**
	 * @return 下一首
	 */
	public static native int nextSong();
	public static native int enterPair();
	/**
	 * @return 手动断开连接
	 */
	public static native int disconnectPhone();
	/**
	 * 接听
	 * @return
	 */		
	public static native int answerCall();
	/**
	 * 拒听
	 * @return
	 */		
	public static native int denyCall();
	/**
	 * 挂断
	 * @return
	 */			
	public static native int hangupCall();
	/**
	 * 重拨
	 * @return
	 */
	public static native int redialCall();
	/**
	 * @param i == 1 "静音"
	 * @param i == 0 "不静音"
	 * @return 设置静音
	 */
	public static native int muteCall(int i);
	/**
	 * @return 获取来电和呼出名称
	 */
	public static native String getPhoneName();
	/**
	 * @return 获取来电和呼出电话号码
	 */
	public static native String getCallNumber();
	/**
	 * 获取手机设备名称
	 * @param index 
	 * @return
	 */
	public static native String getPairDeviceName(int index);
	/**
	 * 获取手机设备mac地址
	 * @param index 
	 * @return
	 */
	public static native String getPairDeviceMac(int index);
	/**
	 * 拨打
	 * @param number 
	 * @return
	 */
	public static native int dialCall(String number);
	/**
	 * 键盘键值
	 * @param dtmf 1，2，3，4，5，*，#，0；
	 * @return
	 */
	public static native int dtmfCall(String dtmf);
	/**
	 * 设置静音
	 * @param vol
	 * @return
	 */
	public static native int setVolume(int vol);
	/**
	 * @return  获取蓝牙声音
	 */
	public static native int getVolume();
	/**
	 * 获取电源状态
	 * @return
	 */
	public static native int getPowerStatus();
	/**
	 * 获取蓝牙型号名称
	 * @return
	 */
	public static native String getDeviceName();
	/**
	 * 设置蓝牙型号名称
	 * @param name "名称"
	 * @return
	 */
	public static native int setDeviceName(String name);
	/**
	 * 获取播放歌曲名称
	 * @return
	 */
	public static native String getPlayTitle();
	/**
	 * 获取播放歌曲歌手名称
	 * @return
	 */
	public static native String getPlayArtist();
	/**
	 * 获取播放歌曲专辑名称
	 * @return
	 */
	public static native String getPlayAlbum();
	/**
	 * occ_off 设置进入睡眠
	 * @return
	 */
	public static native int enterSleep();
	/**
	 * occ_on 设置退出睡眠
	 * @return
	 */
	public static native int leaveSleep();
}