
#define LOG_TAG "BTC"

#undef NDEBUG

#include <sys/types.h>  
  
#include <sys/stat.h>  
#include <fcntl.h>  
#include <termios.h>  
#include <stdio.h>  
#include <pthread.h>
#include <utils/Log.h>


#define BAUDRATE        115200  
#define UART_DEVICE     "/dev/ttyS4"  
  
#define FALSE  -1  
#define TRUE   0  

#define STR_MAX_LEN 64
#define PB_MAX_RECORDS 5

typedef enum PAIR_STATUS{
	NOT_PAIR = 0,
	IN_PAIR,
	PAIRRED
}PAIR_STATUS;

PAIR_STATUS g_pair_status = NOT_PAIR;

typedef enum BFP_STATUS {
	BFP_DISCONNECT = 0,
	BFP_CONNECTED,
}BFP_STATUS;

BFP_STATUS g_bfp_status = BFP_DISCONNECT;

typedef enum CALL_STATUS {
	NO_CALL = 0,
	CALL_OUT,
	CALL_IN,
	IN_CALL,
}CALL_STATUS;

CALL_STATUS g_call_status = NO_CALL;

char g_call_number[STR_MAX_LEN];

typedef enum A2DP_STATUS {
	A2DP_DISCONNECT = 0,
	A2DP_CONNECTED,
	A2DP_PLAYING,
}A2DP_STATUS;

A2DP_STATUS g_a2dp_status = A2DP_DISCONNECT;



char g_phone_name[STR_MAX_LEN];
char g_phone_manufacturer[STR_MAX_LEN];
char g_phone_type[STR_MAX_LEN];

typedef enum PB_TYPE {
	PB_SIM = 0,
	PB_PHONE,
	PB_OUT,
	PB_MISS,
	PB_IN,
}PB_TYPE;

#define PB_TYPE_NUM 5
#define PB_MAX_RECORDS 1000

typedef struct PB_RECORD{
	char name[32];
	char number[32];
}PB_RECORD;

PB_RECORD g_phone_book[PB_TYPE_NUM][PB_MAX_RECORDS];
int g_phone_book_index[PB_TYPE_NUM];

typedef enum SYNC_STATUS {
	NOT_SYNC = 0,
	IN_SYNC,
	NEW_SYNC,
}SYNC_STATUS;

SYNC_STATUS g_pb_sync_status = NOT_SYNC;



int fd;

char REPLYS[][8] =
{
	"II",
	"IV",
	"IA",
	"IB",
	"IC",
	"ID",
	"IF",
	"IR",
	"IM",
	"IN",
	"IO",
	"MA",
	"MB",
	"MM",
	"MN",
	"IS",
	"PB",
	"PC",
};

#define REPLYS_NUM  (sizeof(REPLYS)/8)

typedef enum CMDS_INDEX
{
	BTC_ENTER_PAIR = 0,		//CA
	BTC_EXIT_PAIR,			//CB
	BTC_CONNECT_BFP,		//CC
	BTC_DISCONNECT_BFP,		//CD

	BTC_ANSWER_CALL,		//CE
	BTC_DENY_CALL,			//CF
	BTC_HANGUP_CALL,		//CG
	BTC_REDIAL_CALL,		//CH
	BTC_MUTE_CALL,			//CM
	BTC_DIAL_CALL,			//CW
	BTC_DTMF_CALL,			//CX

	BTC_PLAY_A2DP,			//MA
	BTC_PAUSE_A2DP,			//MB
	BTC_NEXT_A2DP,			//MD
	BTC_LAST_A2DP,			//ME

	BTC_START_PB,			//PA
	BTC_STOP_PB,			//PW
}CMDS_INDEX;
	

char CMDS[][8] =
{
	//pair
	"AT#CA",
	"AT#CB",
	"AT#CC",
	"AT#CD",

	//call
	"AT#CE",
	"AT#CF",
	"AT#CG",
	"AT#CH",
	"AT#CM",
	"AT#CW",
	"AT#CX",
	
	//a2dp
	"AT#MA",
	"AT#MB",
	"AT#MD",
	"AT#ME",

	//phonebook
	"AT#PA",
	"AT#PW",
};

#define CMDS_NUM  (sizeof(CMDS)/8)

int get_pair_status ()
{
	return g_pair_status;
}

int get_bfp_status ()
{
	return g_bfp_status;
}

int get_call_status ()
{
	return g_call_status;
}

int get_a2dp_status ()
{
	return g_a2dp_status;
}



//phone book

int get_sync_status ()
{
	int ret = g_pb_sync_status;
	g_pb_sync_status = NOT_SYNC;
	return ret;
}


int get_phone_book_record_num (int type)
{
	if(type >= 0 && type < PB_TYPE_NUM) {
		return g_phone_book_index[type];
	}
	
	ALOGE("invalid phone book type %d\n", type);
	return -1;
}

char * get_phone_book_record_name_by_index (int type, int index)
{
	if(g_pb_sync_status == IN_SYNC) {
		ALOGE("pb not allowed to read in sync status\n");
		return NULL;
	}

	if(type < 0 || type >= PB_TYPE_NUM) {
		ALOGE("invalid pb type %d\n", type);
		return NULL;
	}

	if(index < 0 || index > g_phone_book_index[type]) {
		ALOGE("pb index %d out of range\n", index);
		return NULL;
	}

	return g_phone_book[type][g_phone_book_index[type]].name;
}

char * get_phone_book_record_number_by_index (int type, int index)
{
	if(g_pb_sync_status == IN_SYNC) {
		ALOGE("pb not allowed to read in sync status\n");
		return NULL;
	}

	if(type < 0 || type >= PB_TYPE_NUM) {
		ALOGE("invalid pb type %d\n", type);
		return NULL;
	}

	if(index < 0 || index > g_phone_book_index[type]) {
		ALOGE("pb index %d out of range\n", index);
		return NULL;
	}

	return g_phone_book[type][g_phone_book_index[type]].number;
}

int write_btc_commands (int cmd_index, const char * param)
{
	if(cmd_index >= 0 && cmd_index < CMDS_NUM) {
		ALOGD("cmd[%d]:%s\n", cmd_index, CMDS[cmd_index]);
		write(fd, CMDS[cmd_index], strlen(CMDS[cmd_index]));
		if(param) {
			ALOGD("param[%s]\n", param);
			write(fd, param, strlen(param));
		}
		write(fd, "\r\n", 2);
	}
	else {
		ALOGE("invalid cmd index %d\n", cmd_index);
		return -1;
	}
	return 0;

}


int start_sync_phone_book (int type)
{
	char param[STR_MAX_LEN];

	if(g_pb_sync_status == IN_SYNC) {
		ALOGE("in sync status, not allowd to start sync again\n");
		return -1;
	}

	if(type >= 0 &&  type < PB_TYPE_NUM) {
		//reset phone book index
		g_phone_book_index[type] = 0;		 
		g_pb_sync_status = IN_SYNC;
		
		sprintf(param, "%d,0,1000", type);
		write_btc_commands(BTC_START_PB, param);	
	}
	else {
		ALOGE("invalid pb type %d\n", type);
		return -2;
	}

	return 0;
}

//a2dp

int play_music ()
{
	return write_btc_commands(BTC_PLAY_A2DP, NULL);
}

int pause_music ()
{
	return write_btc_commands(BTC_PAUSE_A2DP, NULL);
}

int next_song ()
{
	return write_btc_commands(BTC_NEXT_A2DP, NULL);
}

int last_song ()
{
	return write_btc_commands(BTC_LAST_A2DP, NULL);
}


//dial
int disconnect_phone()
{
	return write_btc_commands(BTC_DISCONNECT_BFP, NULL);
}

int answer_call()
{
	return write_btc_commands(BTC_ANSWER_CALL, NULL);
}

int deny_call()
{
	return write_btc_commands(BTC_DENY_CALL, NULL);
}

int hangup_call()
{
	return write_btc_commands(BTC_HANGUP_CALL, NULL);
}

int mute_call()
{
	return write_btc_commands(BTC_MUTE_CALL, NULL);
}

int redial_call()
{
	return write_btc_commands(BTC_REDIAL_CALL, NULL);
}

int dial_call(const char* number)
{
	if(number == NULL) {
		ALOGE("invalid dial number\n");
		return -1;
	}
	return write_btc_commands(BTC_DIAL_CALL, number);
}

int dtmf_call(int dtmf)
{
	char buf[8];
	sprintf(buf, "%d", dtmf);
	return write_btc_commands(BTC_DTMF_CALL, buf);
}


////////////////////////////////////////////////////////////////////////////////  
/** 
*@brief  设置串口通信速率 
*@param  fd     类型 int  打开串口的文件句柄 
*@param  speed  类型 int  串口速度 
*@return  void 
*/  
int speed_arr[] = {B115200, B38400, B19200, B9600, B4800, B2400, B1200, B300,  
                   B115200, B38400, B19200, B9600, B4800, B2400, B1200, B300, };  
int name_arr[] = {115200, 38400, 19200, 9600, 4800, 2400, 1200,  300,   
                  115200, 38400, 19200, 9600, 4800, 2400, 1200,  300, };  
void set_speed(int fd, int speed){  
  int   i;   
  int   status;   
  struct termios   Opt;  
  tcgetattr(fd, &Opt);   
  for ( i= 0;  i < sizeof(speed_arr) / sizeof(int);  i++) {   
    if  (speed == name_arr[i]) {       
      tcflush(fd, TCIOFLUSH);       
      cfsetispeed(&Opt, speed_arr[i]);    
      cfsetospeed(&Opt, speed_arr[i]);     
      status = tcsetattr(fd, TCSANOW, &Opt);    
      if  (status != 0) {          
        perror("tcsetattr fd1");    
        return;       
      }      
      tcflush(fd,TCIOFLUSH);     
    }    
  }  
}  
////////////////////////////////////////////////////////////////////////////////  
/** 
*@brief   设置串口数据位，停止位和效验位 
*@param  fd     类型  int  打开的串口文件句柄 
*@param  databits 类型  int 数据位   取值 为 7 或者8 
*@param  stopbits 类型  int 停止位   取值为 1 或者2 
*@param  parity  类型  int  效验类型 取值为N,E,O,,S 
*/  
int set_Parity(int fd,int databits,int stopbits,int parity)  
{   
    struct termios options;   
    if  ( tcgetattr( fd,&options)  !=  0) {   
        ALOGE("SetupSerial 1");       
        return(FALSE);    
    }  
    options.c_cflag &= ~CSIZE;   
    switch (databits) /*设置数据位数*/  
    {     
    case 7:       
        options.c_cflag |= CS7;   
        break;  
    case 8:       
        options.c_cflag |= CS8;  
        break;     
    default:      
        ALOGE("Unsupported data size\n");
		return (FALSE);    
    }  
    switch (parity)   
    {     
        case 'n':  
        case 'N':      
            options.c_cflag &= ~PARENB;   /* Clear parity enable */  
            options.c_iflag &= ~INPCK;     /* Enable parity checking */   
            break;    
        case 'o':     
        case 'O':       
            options.c_cflag |= (PARODD | PARENB); /* 设置为奇效验*/    
            options.c_iflag |= INPCK;             /* Disnable parity checking */   
            break;    
        case 'e':    
        case 'E':     
            options.c_cflag |= PARENB;     /* Enable parity */      
            options.c_cflag &= ~PARODD;   /* 转换为偶效验*/       
            options.c_iflag |= INPCK;       /* Disnable parity checking */  
            break;  
        case 'S':   
        case 's':  /*as no parity*/     
            options.c_cflag &= ~PARENB;  
            options.c_cflag &= ~CSTOPB;break;    
        default:     
            ALOGE("Unsupported parity\n");      
            return (FALSE);    
        }    
    /* 设置停止位*/    
    switch (stopbits)  
    {     
        case 1:      
            options.c_cflag &= ~CSTOPB;    
            break;    
        case 2:      
            options.c_cflag |= CSTOPB;    
           break;  
        default:      
             ALOGE("Unsupported stop bits\n");    
             return (FALSE);   
    }   
    /* Set input parity option */   
    if (parity != 'n')     
        options.c_iflag |= INPCK;   
    tcflush(fd,TCIFLUSH);  
    options.c_cc[VTIME] = 5; /* 设置超时15 seconds*/     
    options.c_cc[VMIN] = 100; /* Update the options and do it NOW */  

    options.c_lflag  &= ~(ICANON | ECHO | ECHOE | ISIG);  /*Input*/  
    options.c_oflag  &= ~OPOST;   /*Output*/  

	options.c_cflag &= ~CRTSCTS;

    if (tcsetattr(fd,TCSANOW,&options) != 0)     
    {   
        ALOGE("SetupSerial 3");     
        return (FALSE);    
    }   
    return (TRUE);    
}     

int openPort()
{
  
  
    ALOGD("Start...\n");  
    fd = open(UART_DEVICE, O_RDWR /*| O_NDELAY*/ | O_NOCTTY);  
  
    if (fd < 0) {  
        ALOGE("open btc uart failed\n");  
        return 1;  
    }  

    ALOGD("Open...\n");  
    set_speed(fd,BAUDRATE);  
    if (set_Parity(fd,8,1,'N') == FALSE)  {  
        ALOGE("Set Parity Error\n");  
        return 2;  
    } 
	
	return 0;
}

void * writeTh(void *p)
{
	char cmd[128];
	
	write(fd, "AT#MMV1\r\n", 9);
	write(fd, "AT#MN0000\r\n", 11);

	while(1) {
		printf("pls input commands:\n");
		scanf("%s", cmd);
		printf("cmd[%d]:%s\n", strlen(cmd), cmd);
		if(strstr(cmd, "exit") == cmd)
			return NULL;
		write(fd, cmd, strlen(cmd));
		write(fd, "\r\n", 2);
	}

}


int getCallNumber(char * buf)
{
	//syntax IC123456\r\n
	char * end = strstr(buf, "\n\n");
	if(end && (end - buf) > 2 && (end - buf) < 30) {
		int number_len = end - buf - 2;
		memcpy(g_call_number, buf+2, number_len);
		g_call_number[number_len] = 0;
		ALOGD("getCallNumber %d %s\n", number_len, g_call_number);
		return number_len;
	}
	return 0;

}
	
int getPhoneInfo(char * buf, int type)
{
	char * dst;
	if(type == 1)
		dst = &g_phone_manufacturer[0];
	else if(type == 2)
		dst = &g_phone_type[0];
	else
		dst = &g_phone_name[0];

	//syntax IC123456\r\n
	char * end = strstr(buf, "\n\n");
	if(end && (end - buf) > 2 && (end - buf) < 30) {
		int number_len = end - buf - 2;
		memcpy(dst, buf+2, number_len);
		dst[number_len] = 0;
		ALOGD("getPhone info %d %d %s\n", type, number_len, dst);
		return number_len;
	}
	return 0;

}

int getPhoneBookRecord(char * buf)
{
	int type, name_len, number_len;
	int count = 0;
	char * end = strstr(buf, "\n\n");
	//ALOGI("phonebook info %d %s\n", strlen(buf), buf);
	//for(int k = 0; k < 30; k ++) {
	//	ALOGI("%02X", buf[k]);
	//}

	if(end && (end - buf) > 2 ) {
		count = end - buf - 2;
		buf += 2;
		sscanf(buf, "%01d%02d%02d", &type, &name_len, &number_len);
		if(g_phone_book_index[type] < PB_MAX_RECORDS) {
			buf += 5;
			memcpy(g_phone_book[type][g_phone_book_index[type]].name, buf, name_len);
			g_phone_book[type][g_phone_book_index[type]].name[name_len] = 0;
			buf += name_len;
			memcpy(g_phone_book[type][g_phone_book_index[type]].number, buf, number_len);
			g_phone_book[type][g_phone_book_index[type]].number[number_len] = 0;
			ALOGD("get phone book record type %d index %d name %s number %s\n", type, g_phone_book_index[type],
							g_phone_book[type][g_phone_book_index[type]].name,
							g_phone_book[type][g_phone_book_index[type]].number);
			g_phone_book_index[type] ++;
		}
		else
			ALOGE("too many pb records type %d\n", type);
	}
	else
		ALOGE("invalid phone book record start %x end %x\n", buf, end);

	return count;
}	

void parseReplys(char * buf, int len)
{
	int i;
	int used = 0;
	for (i = 0; i < len;) {
		int j;
		for(j = 0; j < REPLYS_NUM; j ++) {
			if(strstr(buf, REPLYS[j]) == buf) {
				ALOGD("match reply[%s]\n", REPLYS[j]);
				break;
			}
		}
		if(j < REPLYS_NUM) {
			used = 4;

			if(! strcmp(REPLYS[j], "II")) {
				//enter paring
				g_pair_status = IN_PAIR;
			}
			else if(! strcmp(REPLYS[j], "IJ")) {
				g_pair_status = NOT_PAIR;
			}
			else if(! strcmp(REPLYS[j], "IB")) {
				g_bfp_status = BFP_CONNECTED;
				g_pair_status = PAIRRED;
			}
			else if(! strcmp(REPLYS[j], "IA")) {
				g_bfp_status = BFP_DISCONNECT;
			}
			else if(! strcmp(REPLYS[j], "IC")) {
				g_call_status = CALL_OUT;
				used += getCallNumber(buf);
			}
			else if(! strcmp(REPLYS[j], "ID")) {
				g_call_status = CALL_IN;
				used += getCallNumber(buf);
			}
			else if(! strcmp(REPLYS[j], "IF")) {
				g_call_status = NO_CALL;
			}
			else if(! strcmp(REPLYS[j], "IR")) {
				g_call_status = IN_CALL;
				used += getCallNumber(buf);
			}
			else if(! strcmp(REPLYS[j], "IN")) {
				g_call_status = IN_CALL;
				used += getPhoneInfo(buf, 0);
			}
			else if(! strcmp(REPLYS[j], "IM")) {
				g_call_status = IN_CALL;
				used += getPhoneInfo(buf, 1);
			}
			else if(! strcmp(REPLYS[j], "IO")) {
				g_call_status = IN_CALL;
				used += getPhoneInfo(buf, 2);
			}
			else if(! strcmp(REPLYS[j], "MA")) {
				g_a2dp_status = A2DP_CONNECTED;
				g_pair_status = PAIRRED;
			}
			else if(! strcmp(REPLYS[j], "MB")) {
				g_a2dp_status = A2DP_PLAYING;
			}
			else if(! strcmp(REPLYS[j], "MA")) {
				g_a2dp_status = A2DP_DISCONNECT;
			}
			else if(! strcmp(REPLYS[j], "PB")) {
				g_pb_sync_status = IN_SYNC;
				used += getPhoneBookRecord(buf);
			}
			else if(! strcmp(REPLYS[j], "PC")) {
				g_pb_sync_status = NEW_SYNC;
			}
			else {
				ALOGE("unknown replys %s\n", REPLYS[j]);
			}
			
		}
		else {
			used = 1;
		}
		buf += used;
		i += used;
		//ALOGD("i %d used %d\n", i ,used);
	}


}


void * readTh(void * p)
{  
  
	int    c=0, res;  
    char  buf[512];  
  
  
    ALOGD("Reading...\n");  
    while(1) {  
		int j;
        res = read(fd, buf, 500);  
		ALOGD("read return[%d]\n", res);
  
		if(res < 0) {
			ALOGE("read uart failed %d\n", res);
			return NULL;
		}
        if(res==0)  
            continue;  
        buf[res]=0;  
  
        ALOGD("%s", buf);  

		parseReplys(buf, res);
          
          
        //if (buf[0] == '@') break;  
    }  

  
    ALOGD("Close...\n");  
    close(fd);  
  
    return NULL;  
}

void* dumpTh(void *p)
{
	while(1) {
		sleep(5);
		ALOGD("pair status %d\n", g_pair_status);
		ALOGD("bfp status %d\n", g_bfp_status);
		ALOGD("call status %d\n", g_call_status);
		ALOGD("a2dp status %d\n", g_a2dp_status);
		ALOGD("sync status %d\n", g_pb_sync_status);
		
		if(g_pb_sync_status != 1) {
			for(int i = 0; i < PB_TYPE_NUM; i ++) {
				ALOGD("pb type %d num %d\n", i, g_phone_book_index[i]);
#if 0
				for(int j = 0; j < g_phone_book_index[i]; j ++) {
					ALOGD("pb %d index %d name %s number %s\n", i, j, g_phone_book[i][j].name, g_phone_book[i][j].number);
				}
#endif
			}
		}
		
	}
	return NULL;
}

void* syncTh(void *p)
{
	for (int i = 0; i < PB_TYPE_NUM; i ++) {
		while(g_pb_sync_status == IN_SYNC)
			sleep (1);
		start_sync_phone_book(i);
	}
	
	return NULL;
}


#if 0
int main(int argc, char *argv[])
#else
void btc_init()
#endif
{
	void *a;
	pthread_t tidp;
	pthread_t tidp2;
	pthread_t tidp3;
	pthread_t tidp4;

	//system("tinymix 19 1");
	//system("tinymix 29 1");

	openPort();

	pthread_create(&tidp, NULL, readTh, NULL);
	//pthread_create(&tidp2, NULL, writeTh, NULL);
	pthread_create(&tidp3, NULL, dumpTh, NULL);
	pthread_create(&tidp4, NULL, syncTh, NULL);

	//start_sync_phone_book(PB_SIM);
	//start_sync_phone_book(PB_PHONE);
	//start_sync_phone_book(PB_OUT);
	//start_sync_phone_book(PB_MISS);
	//start_sync_phone_book(PB_IN);

	pthread_join(tidp2, &a);

	//system("tinymix 19 0");
	//system("tinymix 29 0");
	ALOGD("bluetooth test exit\n");

	return;
}

