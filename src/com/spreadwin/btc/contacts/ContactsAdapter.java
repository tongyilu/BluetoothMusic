package com.spreadwin.btc.contacts;

import java.util.ArrayList;
import java.util.List;

import com.spreadwin.btc.R;
import com.spreadwin.btc.utils.PhoneBookInfo_new;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class ContactsAdapter extends BaseAdapter implements SectionIndexer {
	private List<PhoneBookInfo_new> list = null;
	private Context mContext;

	public ContactsAdapter(Context mContext) {
		this.mContext = mContext;
	}

	public void setPhoneBookInfoList(ArrayList<PhoneBookInfo_new> list) {
		if (list != null) {
			this.list = list;
			notifyDataSetChanged();
		}
	}

	public void clearPhoneBookInfoList() {
		if (list != null) {
			list.clear();
		}
		notifyDataSetChanged();
	}

	/**
	 * 当ListView数据发生变化时,调用此方法来更新ListView
	 * 
	 * @param list
	 */
	public void updateListView(List<PhoneBookInfo_new> list) {
		this.list = list;
	}

	public int getCount() {
		return this.list.size();
	}

	public List<PhoneBookInfo_new> getList() {
		return list;
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View view, ViewGroup arg2) {
		ViewHolder viewHolder = null;
		// final PhoneBookInfo_new mContent = list.get(position);
		if (view == null) {
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(mContext).inflate(R.layout.contact_item, null);
			viewHolder.name = (TextView) view.findViewById(R.id.name);
			viewHolder.sortKey = (TextView) view.findViewById(R.id.sort_key);
			viewHolder.sortKeyLayout = (LinearLayout) view.findViewById(R.id.sort_key_layout);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		// 根据position获取分类的首字母的Char ascii值
		int section = getSectionForPosition(position);
		// 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
		if (position == getPositionForSection(section)) {
			viewHolder.sortKey.setText(list.get(position).getSortLetters());
			viewHolder.sortKeyLayout.setVisibility(View.VISIBLE);
		} else {
			viewHolder.sortKeyLayout.setVisibility(View.GONE);
		}

		viewHolder.name.setText(this.list.get(position).getName());
		return view;
	}

	final static class ViewHolder {
		TextView name;
		TextView sortKey;
		LinearLayout sortKeyLayout;
	}

	/**
	 * 根据ListView的当前位置获取分类的首字母的Char ascii值
	 */
	public int getSectionForPosition(int position) {
		return list.get(position).getSortLetters().charAt(0);
	}

	/**
	 * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
	 */
	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = list.get(i).getSortLetters();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (firstChar == section) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * 提取英文的首字母，非英文字母用#代替。
	 * 
	 * @param str
	 * @return
	 */
	private String getAlpha(String str) {
		String sortStr = str.trim().substring(0, 1).toUpperCase();
		// 正则表达式，判断首字母是否是英文字母
		if (sortStr.matches("[A-Z]")) {
			return sortStr;
		} else {
			return "#";
		}
	}

	@Override
	public Object[] getSections() {
		return null;
	}
}