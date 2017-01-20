package com.spreadwin.btc.Calllogs;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.spreadwin.btc.BtcNative;
import com.spreadwin.btc.MainActivity;
import com.spreadwin.btc.R;
import com.spreadwin.btc.Calllogs.MyListView.OnRefreshListener;
import com.spreadwin.btc.utils.PhoneBookInfo;
import com.spreadwin.btc.utils.PhoneBookInfo_new;
import com.spreadwin.btc.view.SlidingTabLayout;
import com.spreadwin.btc.utils.BtcGlobalData;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler; //import android.support.v4.app.FragmentManager;
//import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class CallLogsFragment extends Fragment {
	// public static final String TAG = MainActivity.TAG;
	public static final String TAG = "CallLogsFragment";
	public static final boolean DEBUG = MainActivity.DEBUG;

	public static final int CALL_OUT_TYPE = BtcGlobalData.PB_OUT;
	public static final int CALL_MISS_TYPE = BtcGlobalData.PB_MISS;
	public static final int CALL_IN_TYPE = BtcGlobalData.PB_IN;

	public static Map<String, String> mNumberParams = new HashMap<String, String>();

	TabInfo mCurTab = null;
	private ViewPager viewPager;
	public List<Fragment> fragments = new ArrayList<Fragment>();
	private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
	// private final ArrayList<PhoneBookInfo> mCallLogsInfo = new
	// ArrayList<PhoneBookInfo>();
	private List<PhoneBookInfo> mPhoneBookInfo = Collections.synchronizedList(new ArrayList<PhoneBookInfo>());
	PhoneBookInfo mCalloutInfo, mCallinInfo, mCallmissInfo;
	MyAdapter mAdapter;
	private LayoutInflater mInflater;
	private ViewGroup mContentContainer;
	private View mRootView;
	int tabType = 1;
	private SlidingTabLayout slidingTabLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (mTabs.size() == 0) {
			TabInfo tab1 = new TabInfo(CALL_OUT_TYPE);
			mTabs.add(tab1);
			TabInfo tab2 = new TabInfo(CALL_IN_TYPE);
			mTabs.add(tab2);
			TabInfo tab3 = new TabInfo(CALL_MISS_TYPE);
			mTabs.add(tab3);
		}
		if (MainActivity.binder != null) {
			if (MainActivity.binder.getPhoneBookInfo() != null) {
				mPhoneBookInfo = MainActivity.binder.getPhoneBookInfo();
			}
		}
		// mLog("CallLogsFragment onCreate 11111111111==="
		// + mPhoneBookInfo.get(CALL_IN_TYPE).getSize());

		initNumber();
	}

	private void initNumber() {
		try {
			InputStreamReader inputReader = new InputStreamReader(getResources().getAssets().open("numbers.txt"));
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line = "";
			String Result = "";
			while ((line = bufReader.readLine()) != null) {
				// Log.d("test", "line =="+line);
				String[] temp = line.split(",");
				if (temp.length == 2) {
					mNumberParams.put(temp[0], temp[1]);
				}
			}
			bufReader.close();
			inputReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mInflater = inflater;
		mLog("onCreateView 222222222");
		View rootView = inflater.inflate(R.layout.fragment_call_logs, container, false);
		mRootView = rootView;
		slidingTabLayout = (SlidingTabLayout) rootView.findViewById(R.id.tabs);
		viewPager = (ViewPager) rootView.findViewById(R.id.viewPager);
		mAdapter = new MyAdapter();
		viewPager.setAdapter(mAdapter);
		viewPager.setOnPageChangeListener(mAdapter);

		slidingTabLayout.setViewPager(viewPager);
		slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
			@Override
			public int getIndicatorColor(int position) {
				return getActivity().getResources().getColor(R.color.blue_00);
			}
		});
		return rootView;
	}

	public class MyAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {
		String[] mTitle = { "呼出电话", "呼入电话", "未接电话" };
		int mCurPos = 0;

		@Override
		public int getCount() {
			return mTitle.length;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			mLog("instantiateItem ==" + position);
			TabInfo tab = mTabs.get(position);
			View root = tab.build(mInflater, mContentContainer, mRootView);
			container.addView(root);
			return root;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			mLog("destroyItem ==" + position);
			container.removeView((View) object);
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			// TODO Auto-generated method stub
			mLog("onPageScrollStateChanged ==" + state);
			if (state == ViewPager.SCROLL_STATE_IDLE) {
				updateCurrentTab(mCurPos);
			}
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mTitle[position];
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onPageSelected(int position) {
			mCurPos = position;
		}

		public void updateCurrentTab(int position) {
			mLog("updateCurrentTab ==" + position);
			mCurPos = position;
		}
	}

	public class TabInfo implements OnItemClickListener, OnItemLongClickListener, OnCreateContextMenuListener {
		public int mTabTpye;
		public LayoutInflater mInflater;
		public View mCallView = null;
		private ListView mListView;
		CallLogsAdapter mCalllogAdapter = null;
		int index;
		TextView emptyView;
		LinearLayout mLoading;

		public TabInfo(int tabTpye) {
			mTabTpye = tabTpye;
		}

		public View build(LayoutInflater inflater, ViewGroup contentParent, View contentChild) {
//			if (mCallView != null) {
//				mLog("mTabTpye =" + mTabTpye + ";mCalllogAdapter ==" + mCalllogAdapter.getCount()
//						+ "build mRootView != null ==" + mPhoneBookInfo.get(mTabTpye).getSize());
//				mLog("build getmUpdateStatus ==" + MainActivity.binder.getmUpdateStatus() + "; mTabTpye==" + mTabTpye);
//				// 有单个更新状态，打开loading
//				if (MainActivity.binder.getmUpdateStatus() != BtcGlobalData.NO_CALL) {
//					showLoading();
//					// 不是更新状态或有数据就隐藏loading
//				} else if (MainActivity.binder.getSyncStatus() == BtcGlobalData.NEW_SYNC
//						|| mPhoneBookInfo.get(mTabTpye).getSize() > 0) {
//					hideLoading();
//				}
//				return mCallView;WW
//			}
			mInflater = inflater;
			mCallView = mInflater.inflate(R.layout.calllogs_list, null);
			mPhoneBookInfo = MainActivity.binder.getPhoneBookInfo();
			mListView = (ListView) mCallView.findViewById(R.id.call_logs_list);
			mListView.setOnItemClickListener(this);
			mListView.setSaveEnabled(true);
			mListView.setItemsCanFocus(true);
			mListView.setTextFilterEnabled(true);

			mLoading = (LinearLayout) mCallView.findViewById(R.id.loading);
			mLog("mTabTpye ==" + mTabTpye);
			mCalllogAdapter = new CallLogsAdapter(getActivity(), mInflater, mPhoneBookInfo.get(mTabTpye));

			mListView.setAdapter(mCalllogAdapter);
			mListView.setOnItemClickListener(this);
			mListView.setOnItemLongClickListener(this);
			mListView.setOnCreateContextMenuListener(this);
			// 设置通话记录为空的界面
			emptyView = new TextView(getActivity());
			emptyView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			emptyView.setText(getResources().getString(R.string.no_call_log));
			emptyView.setTextSize(18);
			emptyView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
			emptyView.setVisibility(View.GONE);
			((ViewGroup) mListView.getParent()).addView(emptyView, 1);
			mLog("build getSyncStatus ==" + MainActivity.binder.getSyncStatus() + "; getSize =="
					+ mPhoneBookInfo.get(mTabTpye).getSize());
			if (MainActivity.binder != null) {
				if (MainActivity.binder.getSyncStatus() == BtcGlobalData.NEW_SYNC
						|| mPhoneBookInfo.get(mTabTpye).getSize() > 0) {
					hideLoading();
				} else if (MainActivity.binder.getSyncStatus() == BtcGlobalData.BFP_CONNECTED) {
					showLoading();
				}
			}
			mListView.setEmptyView(emptyView);
			return mCallView;
		}

		private void showLoading() {
			mLog("showLoading getmUpdateStatus ==" + MainActivity.binder.getmUpdateStatus() + "; mTabTpye=="
					+ mTabTpye);
			if (MainActivity.binder.getmUpdateStatus() == mTabTpye
					|| MainActivity.binder.getmUpdateStatus() == BtcGlobalData.NO_CALL) {
				emptyView.setText("");
				mLoading.setVisibility(View.VISIBLE);
			}
		}

		private void hideLoading() {
			mLog("hideLoading isAdded() ==" + isAdded());
			if (isAdded()) {
				emptyView.setText(getResources().getString(R.string.no_call_log));
				if (mLoading.getVisibility() == View.VISIBLE) {
					mLoading.setVisibility(View.GONE);
				}
			}
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			mLog("onItemClick arg0 ==" + arg0.getId() + "arg1 ==" + arg1.getId() + "; arg2 ==" + arg2 + "; arg3 =="
					+ arg3);
			arg1.showContextMenu();
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			mLog("onItemLongClick arg0 ==" + arg0.getId() + "arg1 ==" + arg1.getId() + "; arg2 ==" + arg2 + "; arg3 =="
					+ arg3);
			index = arg2;
			return false;
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			mLog("onCreateContextMenu arg0 ==" + index);
			// if (mPhoneBookInfo.get(mTabTpye).getTelName(index).length() > 0)
			// {
			// menu.setHeaderTitle(mPhoneBookInfo.get(mTabTpye).getTelName(
			// index));
			// } else {
			menu.setHeaderTitle(mPhoneBookInfo.get(mTabTpye).getTelNumber(index));
			String number = mPhoneBookInfo.get(mTabTpye).getTelNumber(index);
			BtcNative.dialCall(number);
			// }
			// menu.add(mTabTpye, 1, 0, "拨打");
			// menu.add(0, 2, 0, "test2");
			// menu.add(0, 3, 0, "test3");
		}
	}

	// 长按菜单响应函数
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		int position = (int) info.id;// 这里的info.id对应的就是数据库中_id的值
		mLog("onContextItemSelected arg0 ==" + position + "; item.getItemId() ==" + item.getItemId()
				+ ";item.getGroupId() ==" + item.getGroupId());
		switch (item.getItemId()) {
		case 1:
			if (MainActivity.mBluetoothFragment != null) {
				MainActivity.mBluetoothFragment.dialCall(mPhoneBookInfo.get(item.getGroupId()).getTelNumber(position));
			}
			break;
		default:
			break;
		}

		return true;
	}

	public void notifyDataSetChanged() {
		if (mAdapter == null) {
			return;
		}
		mPhoneBookInfo = MainActivity.binder.getPhoneBookInfo();
		for (int i = 0; i < mTabs.size(); i++) {
			if (mTabs.get(i).mCalllogAdapter == null) {
				mTabs.get(i).mCallView = null;
				continue;
			}
			mLog("notifyDataSetChanged mCurPos ==" + mAdapter.mCurPos + "; mCurPos-i==" + Math.abs(mAdapter.mCurPos - i)
					+ "; isAdded()==" + isAdded());
			if (Math.abs(mAdapter.mCurPos - i) <= 1 && isAdded()) {
				mLog("notifyDataSetChanged mTabs.get(i).mTabTpye [" + mTabs.get(i).mTabTpye + "] =="
						+ mPhoneBookInfo.get(mTabs.get(i).mTabTpye).getSize());
				// 判断是否是NOT_SYNC
				if (MainActivity.binder.getSyncStatus() == BtcGlobalData.NEW_SYNC
						|| BtcNative.getBfpStatus() == BtcGlobalData.BFP_DISCONNECT) {
					mLog("notifyDataSetChanged hideLoading ");
					mTabs.get(i).hideLoading();
				}
				mTabs.get(i).mCalllogAdapter.updateListView(mPhoneBookInfo.get(mTabs.get(i).mTabTpye));
			} else {
				mTabs.get(i).mCallView = null;
			}
		}
	}

	public static void mLog(String string) {
		if (DEBUG) {
			Log.d(TAG, string);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		mLog("onStop 22222");
	}

	@Override
	public void onStart() {
		super.onStart();
		mLog("onStart 22222");
	}

	public void showLoading() {
		for (int i = 0; i < mTabs.size(); i++) {
			if (mTabs.get(i).mCalllogAdapter == null) {
				mTabs.get(i).mCallView = null;
				continue;
			}
			mLog("showUpdateing111111111");
			mTabs.get(i).showLoading();
		}
	}

}
