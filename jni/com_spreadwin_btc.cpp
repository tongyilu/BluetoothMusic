/* //device/libs/android_runtime/android_server_AlarmManagerService.cpp
**
** Copyright 2006, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

#define LOG_TAG "BTC"

#include "JNIHelp.h"
#include "jni.h"
#include <utils/Log.h>
#include <utils/misc.h>

#include <fcntl.h>
#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <stdlib.h>
#include <errno.h>
#include <unistd.h>
#include <linux/ioctl.h>

#include "btchal.h"

namespace android {


static jint spreadwin_btc_initBtc(JNIEnv* env, jobject obj)
{
	btc_init ();
	return 0;
}

static jstring spreadwin_btc_getPhoneName (JNIEnv* env, jobject obj)
{
	return env->NewStringUTF(get_phone_name());
}

static jint spreadwin_btc_getPairStatus(JNIEnv* env, jobject obj)
{
	return get_pair_status ();
}

static jint spreadwin_btc_getBfpStatus(JNIEnv* env, jobject obj)
{
	return get_bfp_status ();
}

static jint spreadwin_btc_getCallStatus(JNIEnv* env, jobject obj)
{
	return get_call_status ();
}

static jint spreadwin_btc_getA2dpStatus(JNIEnv* env, jobject obj)
{
	return get_a2dp_status ();
}

static jint spreadwin_btc_getSyncStatus (JNIEnv* env, jobject obj)
{
	return get_sync_status ();
}

static jint spreadwin_btc_startSyncPhoneBook (JNIEnv* env, jobject obj, jint type)
{
	return start_sync_phone_book (type);
}

static jint spreadwin_btc_getPhoneBookRecordNum (JNIEnv* env, jobject obj, jint type)
{
	return get_phone_book_record_num (type);
}

static jstring spreadwin_btc_getPhoneBookRecordNameByIndex(JNIEnv* env, jobject obj, jint type, jint index)
{
	return env->NewStringUTF(get_phone_book_record_name_by_index(type, index));
}

static jstring spreadwin_btc_getPhoneBookRecordNumberByIndex(JNIEnv* env, jobject obj, jint type, jint index)
{
	return env->NewStringUTF(get_phone_book_record_number_by_index(type, index));
}


static jint spreadwin_btd_writeCommands(JNIEnv* env, jobject obj, jint cmd_index, jstring jParam)
{
	const char *param = jParam ? env->GetStringUTFChars(jParam, NULL) : NULL;
	return write_btc_commands (cmd_index, param);
}

static jint spreadwin_btc_playMusic (JNIEnv* env, jobject obj)
{
	return play_music ();
}

static jint spreadwin_btc_pauseMusic (JNIEnv* env, jobject obj)
{
	return pause_music ();
}

static jint spreadwin_btc_lastSong (JNIEnv* env, jobject obj)
{
	return last_song ();
}

static jint spreadwin_btc_nextSong (JNIEnv* env, jobject obj)
{
	return next_song ();
}

static jint spreadwin_btc_enterPair (JNIEnv* env, jobject obj)
{
	return enter_pair ();
}

static jint spreadwin_btc_disconnectPhone (JNIEnv* env, jobject obj)
{
	return disconnect_phone ();
}

static jint spreadwin_btc_answerCall (JNIEnv* env, jobject obj)
{
	return answer_call ();
}

static jint spreadwin_btc_denyCall (JNIEnv* env, jobject obj)
{
	return deny_call ();
}

static jint spreadwin_btc_hangupCall (JNIEnv* env, jobject obj)
{
	return hangup_call ();
}

static jint spreadwin_btc_redialCall (JNIEnv* env, jobject obj)
{
	return redial_call ();
}

static jint spreadwin_btc_muteCall (JNIEnv* env, jobject obj)
{
	return mute_call ();
}

static jint spreadwin_btc_dialCall (JNIEnv* env, jobject obj, jstring number)
{
	const char *param = number ? env->GetStringUTFChars(number, NULL) : NULL;
	return dial_call (param);
}

static jint spreadwin_btc_dtmfCall (JNIEnv* env, jobject obj, jstring dtmf)
{
	const char *param = dtmf ? env->GetStringUTFChars(dtmf, NULL) : NULL;
	return dtmf_call (param);
}

static jstring spreadwin_btc_getCallNumber (JNIEnv* env, jobject obj)
{
	return env->NewStringUTF(get_call_number());
}

static jstring spreadwin_btc_getPairDeviceName (JNIEnv* env, jobject obj, jint index)
{
	return env->NewStringUTF(get_pair_device_name(index));
}

static jint spreadwin_btc_setVolume (JNIEnv* env, jobject obj, jint vol)
{
	return set_volume(vol);
}

static jint spreadwin_btc_getVolume (JNIEnv* env, jobject obj)
{
	return get_volume();
}

static jint spreadwin_btc_getPowerStatus (JNIEnv* env, jobject obj)
{
	return get_power_status();
}

static jstring spreadwin_btc_getDeviceName (JNIEnv* env, jobject obj)
{
	return env->NewStringUTF(get_device_name());
}


static JNINativeMethod sMethods[] = {
     /* name, signature, funcPtr */
	{"initBtc", "()I", (void*)spreadwin_btc_initBtc},
	{"getPhoneName", "()Ljava/lang/String;", (void*)spreadwin_btc_getPhoneName},
	{"getPairStatus", "()I", (void*)spreadwin_btc_getPairStatus},
	{"getBfpStatus", "()I", (void*)spreadwin_btc_getBfpStatus},
	{"getCallStatus", "()I", (void*)spreadwin_btc_getCallStatus},
	{"getA2dpStatus", "()I", (void*)spreadwin_btc_getA2dpStatus},
	{"getSyncStatus", "()I", (void*)spreadwin_btc_getSyncStatus},
	{"startSyncPhoneBook", "(I)I", (void*)spreadwin_btc_startSyncPhoneBook},
	{"getPhoneBookRecordNum", "(I)I", (void*)spreadwin_btc_getPhoneBookRecordNum},
	{"getPhoneBookRecordNameByIndex", "(II)Ljava/lang/String;", (void*)spreadwin_btc_getPhoneBookRecordNameByIndex},
	{"getPhoneBookRecordNumberByIndex", "(II)Ljava/lang/String;", (void*)spreadwin_btc_getPhoneBookRecordNumberByIndex},
	{"writeCommands", "(ILjava/lang/String;)I", (void*)spreadwin_btd_writeCommands},
	{"playMusic", "()I", (void*)spreadwin_btc_playMusic},
	{"pauseMusic", "()I", (void*)spreadwin_btc_pauseMusic},
	{"lastSong", "()I", (void*)spreadwin_btc_lastSong},
	{"enterPair", "()I", (void*)spreadwin_btc_enterPair},
	{"nextSong", "()I", (void*)spreadwin_btc_nextSong},
	{"disconnectPhone", "()I", (void*)spreadwin_btc_disconnectPhone},
	{"answerCall", "()I", (void*)spreadwin_btc_answerCall},
	{"denyCall", "()I", (void*)spreadwin_btc_denyCall},
	{"hangupCall", "()I", (void*)spreadwin_btc_hangupCall},
	{"redialCall", "()I", (void*)spreadwin_btc_redialCall},
	{"muteCall", "()I", (void*)spreadwin_btc_muteCall},
	{"dialCall", "(Ljava/lang/String;)I", (void*)spreadwin_btc_dialCall},
	{"dtmfCall", "(Ljava/lang/String;)I", (void*)spreadwin_btc_dtmfCall},
	{"getCallNumber", "()Ljava/lang/String;", (void*)spreadwin_btc_getCallNumber},
	{"getPairDeviceName", "(I)Ljava/lang/String;", (void*)spreadwin_btc_getPairDeviceName},
	{"getVolume", "()I", (void*)spreadwin_btc_getVolume},
	{"setVolume", "(I)I", (void*)spreadwin_btc_setVolume},
	{"getPowerStatus", "()I", (void*)spreadwin_btc_getPowerStatus},
	{"getDeviceName", "()Ljava/lang/String;", (void*)spreadwin_btc_getDeviceName},
};

int register_spreadwin_btc(JNIEnv* env)
{
    return jniRegisterNativeMethods(env, "com/spreadwin/btc/BtcNative",
                                    sMethods, NELEM(sMethods));
}

} /* namespace android */
