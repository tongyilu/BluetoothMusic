package com.spreadwin.btc.contacts;

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

public class ContactsAdapter extends BaseAdapter {
	Context mContext;
	ArrayList<String> mTelName;
	ArrayList<String> mTelNumber;
	ArrayList<String> mTelTime;
	PhoneBookInfo mContactsInfo;
	LayoutInflater mInflater;
	public ContactsAdapter(Context context, ArrayList<String> _mTelName, ArrayList<String> _mTelNumber, ArrayList<String> _mTelTime) {
		mContext = context;
		mTelName = _mTelName;
		mTelNumber = _mTelNumber;
		mTelTime = _mTelTime;
		mInflater = LayoutInflater.from(context);
	}

	public ContactsAdapter(Context context, LayoutInflater _Inflater, PhoneBookInfo callLogsInfo) {
		mContext = context;
		mContactsInfo = callLogsInfo;
		mInflater = _Inflater;
	}

	@Override
	public int getCount() {
		 ContactsFragment.mLog("ContactsAdapter getCount =="+mContactsInfo.getSize());
		return mContactsInfo.getSize();
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
               ContactsFragment.mLog("getView convertView111 =="+convertView.getId()+"; position =="+position+";mCallLogsInfo type =="+mContactsInfo.getType());
               holder.mTelName = (TextView) convertView.findViewById(R.id.tel_name);
               holder.mTelNumber = (TextView) convertView.findViewById(R.id.tel_number);
               holder.mTelTime = (TextView) convertView.findViewById(R.id.tel_time);
               convertView.setTag(holder);
        }
        else
        {
               holder=(ViewHolder)convertView.getTag();
        }
        
        ContactsFragment.mLog("getView convertView33 =="+convertView.getId()+"; position =="
        		+position+";mCallLogsInfo type =="+mContactsInfo.getType() + "; mCallLogsInfo.getTelName(position)"+mContactsInfo.getTelName(position));
        holder.mTelName.setText(mContactsInfo.getTelName(position));
        holder.mTelNumber.setText(mContactsInfo.getTelNumber(position));
        holder.mTelTime.setText(mContactsInfo.getTelTime(position));
		return convertView;
	}

	public void setPhoneBookInfo(PhoneBookInfo phoneBookInfo) {
		mContactsInfo = phoneBookInfo;		
	}
	

}
