package com.spreadwin.btc.Calllogs;

import java.util.ArrayList;
import java.util.zip.Inflater;

import com.spreadwin.btc.R;
import com.spreadwin.btc.utils.PhoneBookInfo;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class CallLogsAdapter extends BaseAdapter {
	Context mContext;
	ArrayList<String> mTelName;
	ArrayList<String> mTelNumber;
	ArrayList<String> mTelTime;
	PhoneBookInfo mCallLogsInfo;
	LayoutInflater mInflater;
	public CallLogsAdapter(Context context, ArrayList<String> _mTelName, ArrayList<String> _mTelNumber, ArrayList<String> _mTelTime) {
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
		 CallLogsFragment.mLog("CallLogsAdapter getCount =="+mCallLogsInfo.getSize());	     
		return mCallLogsInfo.getSize();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	 private   class ViewHolder {
	        private  TextView mTelName;
	        private  TextView mTelNumber;
	        private  TextView mTelTime;
	    }
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null)
        {
               holder = new ViewHolder();
               convertView = mInflater.inflate(R.layout.listview_item, null);
               CallLogsFragment.mLog("getView convertView111 =="+convertView.getId()+"; position =="+position+";mCallLogsInfo type =="+mCallLogsInfo.getType());
               holder.mTelName = (TextView) convertView.findViewById(R.id.tel_name);
               holder.mTelNumber = (TextView) convertView.findViewById(R.id.tel_number);
               holder.mTelTime = (TextView) convertView.findViewById(R.id.tel_time);
               convertView.setTag(holder);
        }
        else
        {
               holder=(ViewHolder)convertView.getTag();
        }
        
        CallLogsFragment.mLog("getView convertView33 =="+convertView.getId()+"; position =="
        		+position+";mCallLogsInfo type =="+mCallLogsInfo.getType() + "; mCallLogsInfo.getTelName(position)"+mCallLogsInfo.getTelName(position));
        if (mCallLogsInfo.getTelName(position).length() == 0) {
        	holder.mTelName.setVisibility(View.GONE);
		}else{
			holder.mTelName.setText(mCallLogsInfo.getTelName(position));	
		}
        holder.mTelNumber.setText(mCallLogsInfo.getTelNumber(position));
        holder.mTelTime.setText(mCallLogsInfo.getTelTime(position));
		return convertView;
	}

	public void setPhoneBookInfo(PhoneBookInfo phoneBookInfo) {
		mCallLogsInfo = phoneBookInfo;		
	}
	

}
