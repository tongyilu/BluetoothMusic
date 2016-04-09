package com.spreadwin.btc.Calllogs;

import java.util.ArrayList;

import com.spreadwin.btc.R;
import com.spreadwin.btc.utils.PhoneBookInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CallLogsAdapter extends BaseAdapter {
	Context mContext;
	ArrayList<String> mTelName;
	ArrayList<String> mTelNumber;
	ArrayList<String> mTelTime;
	PhoneBookInfo mCallLogsInfo;
	LayoutInflater mInflater;

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
	}

	@Override
	public int getCount() {
		CallLogsFragment.mLog(
				"CallLogsAdapter getType ==" + mCallLogsInfo.getType() + ";getCount ==" + mCallLogsInfo.getSize());
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
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (position >= mCallLogsInfo.getSize()) {
			return convertView;
		}
		try {
			// CallLogsFragment.mLog("getView convertView33
			// =="+convertView.getId()+"; position =="
			// +position+";mCallLogsInfo type =="+mCallLogsInfo.getType() +
			// "; getTelName(position) =="+mCallLogsInfo.getTelName(position)+
			// "; getTelNumber(position)
			// =="+mCallLogsInfo.getTelNumber(position)+
			// "; mCallLogsInfo.getSize() =="+mCallLogsInfo.getSize());
			// 号码没有配备到名字的
			if (mCallLogsInfo.getTelName(position).length() == 0) {
				onMatchingName(holder.mTelName, position);
			} else {
				holder.mTelName.setText(mCallLogsInfo.getTelName(position));
			}
			// holder.mTelTime.setText(mCallLogsInfo.getTelTime(position));
		} catch (Exception e) {
			CallLogsFragment.mLog("getView111 e==" + e);
			holder.mTelName.setText("未知");
		}
		try {
			holder.mTelNumber.setText(mCallLogsInfo.getTelNumber(position));
		} catch (Exception e) {
			CallLogsFragment.mLog("getView222 e==" + e);
			holder.mTelNumber.setText("未知");
		}
		return convertView;
	}

	private void onMatchingName(TextView mTextView, int position) {
		CallLogsFragment.mLog("position ==" + position);
		String temp = CallLogsFragment.mNumberParams.get(mCallLogsInfo.getTelNumber(position));
		CallLogsFragment.mLog("temp ==" + temp);
		if (temp != null) {
			mTextView.setText(temp);
		} else {
			mTextView.setText("未知");
		}
	}

	public void updateListView(PhoneBookInfo phoneBookInfo) {
		CallLogsFragment.mLog("updateListView mCallLogsInfo size111 ==" + mCallLogsInfo.getSize());
		mCallLogsInfo = phoneBookInfo;
		CallLogsFragment.mLog("updateListView mCallLogsInfo size222 ==" + mCallLogsInfo.getSize());
		notifyDataSetChanged();
	}

}
