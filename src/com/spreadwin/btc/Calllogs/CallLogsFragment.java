package com.spreadwin.btc.Calllogs;

import java.util.ArrayList;
import java.util.List;




import com.spreadwin.btc.MainActivity;
import com.spreadwin.btc.R;
import com.spreadwin.btc.Calllogs.MyListView.OnRefreshListener;
import com.spreadwin.btc.utils.PhoneBookInfo;
import com.spreadwin.btc.utils.BtcGlobalData;



import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class CallLogsFragment extends Fragment {
		public  static final String TAG = MainActivity.TAG;
//		public  static final String TAG = "CallLogsFragment";
		public  static final boolean DEBUG = true;
	
		private static final int CALL_OUT_TYPE = BtcGlobalData.PB_OUT;
		private static final int CALL_MISS_TYPE = BtcGlobalData.PB_MISS;
		private static final int CALL_IN_TYPE = BtcGlobalData.PB_IN;
		
		TabInfo mCurTab = null;
		private ViewPager viewPager;
		public List<Fragment> fragments = new ArrayList<Fragment>();
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();	
//		private final ArrayList<PhoneBookInfo> mCallLogsInfo = new ArrayList<PhoneBookInfo>();	
		private ArrayList<PhoneBookInfo> mPhoneBookInfo = null;	
		PhoneBookInfo mCalloutInfo,mCallinInfo,mCallmissInfo;
		MyAdapter mAdapter;
		private LayoutInflater mInflater;
		private ViewGroup mContentContainer;
		private View mRootView;
		int tabType=1;
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
			mPhoneBookInfo = MainActivity.binder.getPhoneBookInfo();
			mLog("CallLogsFragment onCreate 11111111111==="+mPhoneBookInfo.get(CALL_IN_TYPE).getSize());
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			mInflater = inflater;
			mLog("onCreateView 222222222");
			View rootView = inflater.inflate(R.layout.fragment_call_logs, container, false);
			mRootView = rootView;		
			viewPager = (ViewPager) rootView.findViewById(R.id.viewPager);
			mAdapter = new MyAdapter();
			viewPager.setAdapter(mAdapter);
			viewPager.setOnPageChangeListener(mAdapter);
			return rootView;
		}	
		        
		public  class MyAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {
			String[] mTitle={"呼出电话","呼入电话","未接电话"};
			int mCurPos = 0;
			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return mTitle.length;
			}

			@Override
			public boolean isViewFromObject(View view, Object object) {				
				return view == object;
			}
			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				mLog("instantiateItem =="+position);
				TabInfo tab = mTabs.get(position);
		        View root = tab.build(mInflater, mContentContainer, mRootView);
				container.addView(root);
				return root;
			}
			
			@Override
			public void destroyItem(ViewGroup container, int position,
					Object object) {	
				mLog("destroyItem =="+position);
				container.removeView((View)object);
			}
			
			@Override
			public void onPageScrollStateChanged(int position) {
				// TODO Auto-generated method stub
				mLog("onPageScrollStateChanged =="+position);
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

			}
		}

		
		public  class TabInfo implements OnItemClickListener, OnItemLongClickListener, OnCreateContextMenuListener {
			public int mTabTpye;
			public LayoutInflater mInflater;
	        public View mRootView;
	        private ListView mListView;  
	        CallLogsAdapter mCalllogAdapter;
	        int index;
	        
			public TabInfo(int tabTpye) {
				mTabTpye = tabTpye;
			}

			public View build(LayoutInflater inflater, ViewGroup contentParent, View contentChild) {
				if (mRootView != null) {
	                return mRootView;
	            }
				mInflater = inflater;				
				mRootView = mInflater.inflate(R.layout.calllogs_list, null);
				ListView lv = (ListView) mRootView.findViewById(R.id.call_logs_list);	
                lv.setOnItemClickListener(this);
                lv.setSaveEnabled(true);
                lv.setItemsCanFocus(true);
                lv.setTextFilterEnabled(true);
                mListView = lv;
				for (int i = 0; i < mPhoneBookInfo.get(mTabTpye).getSize(); i++) {
					mLog("mCallLogsInfo.get(mTabTpye-1)["+i+"] =="+mPhoneBookInfo.get(mTabTpye).getTelName(i));
				}
				mLog("mTabTpye =="+mTabTpye);
                mCalllogAdapter = new CallLogsAdapter(getActivity(),mInflater,mPhoneBookInfo.get(mTabTpye));
                mListView.setAdapter(mCalllogAdapter);
                mListView.setOnItemClickListener(this);
                mListView.setOnItemLongClickListener(this);
                mListView.setOnCreateContextMenuListener(this);
                //设置通话记录为空的界面
                TextView emptyView = new TextView(getActivity());  
                emptyView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));  
                emptyView.setText(getResources().getString(R.string.no_call_log));  
                emptyView.setTextSize(30);
                emptyView.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
                emptyView.setVisibility(View.GONE);  
                ((ViewGroup)mListView.getParent()).addView(emptyView); 
                mListView.setEmptyView(emptyView);
				return mRootView;
			}

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				mLog("onItemClick arg0 =="+arg0.getId()+"arg1 =="+arg1.getId()+"; arg2 =="+arg2+"; arg3 =="+arg3);
				arg1.showContextMenu();
			}

			
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				mLog("onItemLongClick arg0 =="+arg0.getId()+"arg1 =="+arg1.getId()+"; arg2 =="+arg2+"; arg3 =="+arg3);
				index = arg2;
				return false;
			}

			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				mLog("onCreateContextMenu arg0 =="+index);
				if (mPhoneBookInfo.get(mTabTpye).getTelName(index).length() > 0) {
					menu.setHeaderTitle(mPhoneBookInfo.get(mTabTpye).getTelName(index));
				}else{
					menu.setHeaderTitle(mPhoneBookInfo.get(mTabTpye).getTelNumber(index));
				}				
				menu.add(mTabTpye, 1, 0, "拨打");
//				menu.add(0, 2, 0, "test2");
//				menu.add(0, 3, 0, "test3");				
			}
			 
		}
		  // 长按菜单响应函数 
        public boolean onContextItemSelected(MenuItem item) { 
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item 
                                .getMenuInfo(); 
                int position = (int) info.id;// 这里的info.id对应的就是数据库中_id的值 
                mLog("onContextItemSelected arg0 =="+position+"; item.getItemId() =="+item.getItemId()+";item.getGroupId() =="+item.getGroupId());
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

		public static  void mLog(String string) {
			if (DEBUG) {
				Log.d(TAG, string);				
			}
		}

		public void notifyDataSetChanged() {			
			if (mPhoneBookInfo == null ||  mAdapter == null ) {
				mLog("CallLogsFragment 3333333 == null");
				return;
			}
				mLog("CallLogsFragment 444444444");
				mPhoneBookInfo = MainActivity.binder.getPhoneBookInfo();
				for (int i = 0; i < mTabs.size(); i++) {
					if (mTabs.get(i).mCalllogAdapter == null) {
						mLog("mCalllogAdapter 444444 == null");
						return;
					}
					mLog("CallLogsFragment mTabs.get(i).mTabTpye ["+mTabs.get(i).mTabTpye+"] =="+mPhoneBookInfo.get(mTabs.get(i).mTabTpye).getSize());
					mTabs.get(i).mCalllogAdapter.setPhoneBookInfo(mPhoneBookInfo.get(mTabs.get(i).mTabTpye));					
					mTabs.get(i).mCalllogAdapter.notifyDataSetChanged();
				}
				mAdapter.notifyDataSetChanged();
//			}
		}

}
