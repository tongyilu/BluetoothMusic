package com.spreadwin.btc.contacts;

import java.util.ArrayList;
import java.util.List;

import com.spreadwin.btc.MainActivity;
import com.spreadwin.btc.R;
import com.spreadwin.btc.utils.PhoneBookInfo;
import com.spreadwin.btc.utils.BtcGlobalData;

import android.app.Fragment;
import android.app.ActionBar.LayoutParams;
import android.os.Bundle;
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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class ContactsFragment extends Fragment {
		public  static final String TAG = MainActivity.TAG;
//		public  static final String TAG = "ContactsFragment";
		public  static final boolean DEBUG = true;

		private static final int SIM_CONTACTS_TYPE = BtcGlobalData.PB_SIM;
		private static final int PHONE_CONTACTS_TYPE = BtcGlobalData.PB_PHONE;
		
		private ViewPager viewPager;
		public List<Fragment> fragments = new ArrayList<Fragment>();
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();	
//		private ArrayList<PhoneBookInfo> mContactsInfo = new ArrayList<PhoneBookInfo>();	
		private ArrayList<PhoneBookInfo> mPhoneBookInfo = null;	
		TabInfo mCurTab = null;
		PhoneBookInfo mPhoneContactsInfo,mSIMContactsInfo;
		MyAdapter mAdapter;
		private LayoutInflater mInflater;
		private ViewGroup mContentContainer;
		private View mRootView;
		String[] mTitle={"手机号码","SIM卡号码"};
		int tabType=1;
//		int testNumber =10;
		@Override
		public void onCreate(Bundle savedInstanceState) {		
			super.onCreate(savedInstanceState);
			if (mTabs.size() == 0) {
				TabInfo tab1 = new TabInfo(PHONE_CONTACTS_TYPE);
				mTabs.add(tab1);
				TabInfo tab2 = new TabInfo(SIM_CONTACTS_TYPE);
				mTabs.add(tab2);				
			}
			mLog("onCreate 111111111");
			mPhoneBookInfo = MainActivity.binder.getPhoneBookInfo();				

		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			mInflater = inflater;
			mLog("onCreateView 222222222");
			View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
			mRootView = rootView;		
			viewPager = (ViewPager) rootView.findViewById(R.id.viewPager);
			mAdapter = new MyAdapter();
			viewPager.setAdapter(mAdapter);
			viewPager.setOnPageChangeListener(mAdapter);
			return mRootView;
		}
		
		@Override
		public void onResume() {		
			super.onResume();
//			notifyAll();
//			setUpdateStatus();
		}
		
		public void notifyDataSetChanged() {
			if (mPhoneBookInfo == null ||  mAdapter == null) {
				mLog("ContactsFragment 3333333 == null");
				return;
			}
			ArrayList<PhoneBookInfo> tempPhoneBookInfo = MainActivity.binder.getPhoneBookInfo();
			mLog("getSyncStatus 3333333 =="+MainActivity.binder.getSyncStatus());
			mLog("mPhoneBookInfo.get(SIM_CONTACTS_TYPE).getSize() =="+mPhoneBookInfo.get(SIM_CONTACTS_TYPE).getSize()
					+"; tempPhoneBookInfo.get(SIM_CONTACTS_TYPE).getSize() =="+tempPhoneBookInfo.get(SIM_CONTACTS_TYPE).getSize());
			mLog("mPhoneBookInfo.get(PHONE_CONTACTS_TYPE).getSize() =="+mPhoneBookInfo.get(PHONE_CONTACTS_TYPE).getSize()
					+"; tempPhoneBookInfo.get(PHONE_CONTACTS_TYPE).getSize() =="+tempPhoneBookInfo.get(PHONE_CONTACTS_TYPE).getSize());

			mLog("ContactsFragment 444444444");
			mPhoneBookInfo = MainActivity.binder.getPhoneBookInfo();
			for (int i = 0; i < mTabs.size(); i++) {
				if (mTabs.get(i).mContactsAdapter == null) {
					return;
				}
				mTabs.get(i).mContactsAdapter.setPhoneBookInfo(mPhoneBookInfo.get(mTabs.get(i).mTabTpye));					
				mTabs.get(i).mContactsAdapter.notifyDataSetChanged();
			}
			mAdapter.notifyDataSetChanged();
			
		}

		public  class MyAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {
			
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
//				TabInfo tab = mTabs.get(position);
//				mCurTab = tab;
//				
//				// Put things in the correct paused/resumed state.
//				if (mActivityResumed) {
//					mCurTab.build(mInflater, mContentContainer, mRootView);
//					mCurTab.resume(mSortOrder);
//				} else {
//					mCurTab.pause();
//				}
//				for (int i=0; i<mTabs.size(); i++) {
//					TabInfo t = mTabs.get(i);
//					if (t != mCurTab) {
//						t.pause();
//					}
//				}
//				
//				mCurTab.updateStorageUsage();
//				updateOptionsMenu();
//				final Activity host = getActivity();
//				if (host != null) {
//					host.invalidateOptionsMenu();
//				}
			}
		}
		
		public  class TabInfo implements OnItemClickListener, OnCreateContextMenuListener, OnItemLongClickListener {
			public int mTabTpye;
			public LayoutInflater mInflater;
	        public View mRootView;
	        public ListView mListView;
	        ContactsAdapter mContactsAdapter;
	        int index;
	        
			public TabInfo(int tabTpye) {
				mTabTpye = tabTpye;
			}

			public View build(LayoutInflater inflater, ViewGroup contentParent, View contentChild) {
				if (mRootView != null) {
	                return mRootView;
	            }
				mInflater = inflater;				
				mRootView = mInflater.inflate(R.layout.contacts_list, null);		
				ListView lv = (ListView) mRootView.findViewById(R.id.contacts_list);	
                lv.setOnItemClickListener(this);
                lv.setSaveEnabled(true);
                lv.setItemsCanFocus(true);
                lv.setTextFilterEnabled(true);
                mListView = lv;
				for (int i = 0; i < mPhoneBookInfo.get(mTabTpye).getSize(); i++) {
				mLog("mCallLogsInfo.get(mTabTpye-1)["+i+"] =="+mPhoneBookInfo.get(mTabTpye).getTelName(i));
				}
				mLog("mTabTpye =="+mTabTpye);
                mContactsAdapter = new ContactsAdapter(getActivity(),mInflater,mPhoneBookInfo.get(mTabTpye));
                mListView.setAdapter(mContactsAdapter);
                mListView.setOnItemClickListener(this);
                mListView.setOnItemLongClickListener(this);
                mListView.setOnCreateContextMenuListener(this);
                //设置联系人为空的界面
                TextView emptyView = new TextView(getActivity());  
                emptyView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));  
                emptyView.setText(getResources().getString(R.string.no_conntacts));  
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
				mLog("arg0 =="+arg0.getId()+"arg1 =="+arg1.getId()+"; arg2 =="+arg2+"; arg3 =="+arg3);
				arg1.showContextMenu();
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
				
			}

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				mLog("onItemLongClick arg0 =="+arg0.getId()+"arg1 =="+arg1.getId()+"; arg2 =="+arg2+"; arg3 =="+arg3);
				index = arg2;
				return false;
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
		

}
