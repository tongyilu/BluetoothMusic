
void btc_init ();

int get_pair_status ();

int get_bfp_status ();

int get_call_status ();

int get_a2dp_status ();

int get_sync_status ();

int start_sync_phone_book (int type);

int get_phone_book_record_num (int type);

char * get_phone_book_record_name_by_index (int type, int index);

char * get_phone_book_record_number_by_index (int type, int index);

int write_btc_commands (int cmd_index, const char * param);

int play_music ();

int pause_music ();

int next_song ();

int last_song ();


int disconnect_phone();

int answer_call();

int deny_call();

int hangup_call();

int redial_call();

int mute_call();

int dial_call(const char* number);

int dtmf_call(int dtmf);
