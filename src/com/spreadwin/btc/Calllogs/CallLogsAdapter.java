package com.spreadwin.btc.Calllogs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.spreadwin.btc.R;
import com.spreadwin.btc.utils.PhoneBookInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CallLogsAdapter extends BaseAdapter {
	Context mContext;
	ArrayList<String> mTelName;
	ArrayList<String> mTelNumber;
	ArrayList<String> mTelTime;
	PhoneBookInfo mCallLogsInfo;
	LayoutInflater mInflater;
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
	private String mCurData;
	
	

	public CallLogsAdapter(Context context, ArrayList<String> _mTelName, ArrayList<String> _mTelNumber,
			ArrayList<String> _mTelTime) {
		mContext = context;
		mTelName = _mTelName;
		mTelNumber = _mTelNumber;
		mTelTime = _mTelTime;
		mInflater = LayoutInflater.from(context);		

	}

	public CallLogsAdapter(Context context, LayoutInflater _Inflater, PhoneBookInfo callLogsInfo) {
		mContext = context;
		mCallLogsInfo = callLogsInfo;
		mInflater = _Inflater;
		notifyDataSetChanged();
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        mCurData = simpleDateFormat.format(date);
        CallLogsFragment.mLog("mCurData =="+mCurData);
	}

	@Override
	public int getCount() {
//		CallLogsFragment.mLog(
//				"CallLogsAdapter getType ==" + mCallLogsInfo.getType() + ";getCount ==" + mCallLogsInfo.getSize());
		return mCallLogsInfo.getSize();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public void clearCallLogsList() {
		mCallLogsInfo.clear();
		notifyDataSetChanged();
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	private class ViewHolder {
		private TextView mTelName;
		private TextView mTelNumber;
		private TextView mTelTime;
		private ImageView mIcon;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.listview_item, null);
			CallLogsFragment.mLog("getView convertView111 ==" + convertView.getId() + "; position ==" + position
					+ ";mCallLogsInfo type ==" + mCallLogsInfo.getType());
			holder.mTelName = (TextView) convertView.findViewById(R.id.tel_name);
			holder.mTelNumber = (TextView) convertView.findViewById(R.id.tel_number);
			holder.mTelTime = (TextView) convertView.findViewById(R.id.tel_time);
			holder.mIcon = (ImageView) convertView.findViewById(R.id.icon);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (position >= mCallLogsInfo.getSize()) {
			return convertView;
		}
		try {
		    holder.mTelName.setVisibility(View.VISIBLE);
			if (mCallLogsInfo.getTelName(position).length() == 0) {
				onMatchingName(holder.mTelName, position);
			} else {
				holder.mTelName.setText(mCallLogsInfo.getTelName(position));
			}
		} catch (Exception e) {
			e.printStackTrace();
			holder.mTelName.setVisibility(View.GONE);
//			holder.mTelName.setText("未知");
		}
		try {		
		    if (holder.mTelName.getVisibility() == View.VISIBLE) {
		        holder.mTelNumber.setVisibility(View.GONE);
            }else{
                holder.mTelNumber.setVisibility(View.VISIBLE);
                holder.mTelNumber.setText(mCallLogsInfo.getTelNumber(position));                
            }
		} catch (Exception e) {
		    e.printStackTrace();
		    holder.mTelNumber.setText("未知");
		}
		
        try {
            if (mCallLogsInfo.getType() == CallLogsFragment.CALL_OUT_TYPE) {
                holder.mIcon.setImageResource(R.drawable.placed_);
            } else if (mCallLogsInfo.getType() == CallLogsFragment.CALL_MISS_TYPE) {
                holder.mIcon.setImageResource(R.drawable.missed);
            } else if (mCallLogsInfo.getType() == CallLogsFragment.CALL_IN_TYPE) {
                holder.mIcon.setImageResource(R.drawable.picked_up);
            }
            
            String[] str = mCallLogsInfo.getTelTime(position).split("T");
//            CallLogsFragment.mLog("getView getTelTime ==" + mCallLogsInfo.getTelTime(position) + "; position ==" + position
//                    + ";mCallLogsInfo type ==" + mCallLogsInfo.getType());
            String time = "";
            if (str.length >= 2) {
                if (str[0].equals(mCurData)) {
                    time = str[1].substring(0, 2) + ":"
                            + str[1].substring(2, 4);
                } else {
                    time =  str[0].substring(4, 6) + "/"
                            + str[0].substring(6, 8) + "\r\n"
                            +str[0].substring(0, 4);
                }
            }else{
                time =  str[0].substring(4, 6) + "/"
                        + str[0].substring(6, 8) + "\r\n"
                        +str[0].substring(0, 4);
            }
            holder.mTelTime.setText(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		return convertView;
	}

	private void onMatchingName(TextView mTextView, int position) {
//		CallLogsFragment.mLog("position ==" + position);
		String temp = CallLogsFragment.mNumberParams.get(mCallLogsInfo.getTelNumber(position));
//		CallLogsFragment.mLog("temp ==" + temp);
		if (temp != null) {
			mTextView.setText(temp);
		} else {
		    mTextView.setVisibility(View.GONE);
//			mTextView.setText("未知");
		}
	}

	public void updateListView(PhoneBookInfo phoneBookInfo) {
		CallLogsFragment.mLog("updateListView mCallLogsInfo size111 ==" + mCallLogsInfo.getSize());
		mCallLogsInfo = phoneBookInfo;
		CallLogsFragment.mLog("updateListView mCallLogsInfo size222 ==" + mCallLogsInfo.getSize());
		notifyDataSetChanged();
	}

}
