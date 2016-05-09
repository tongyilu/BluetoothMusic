package com.spreadwin.btc.contacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.spreadwin.btc.BtcNative;
import com.spreadwin.btc.MainActivity;
import com.spreadwin.btc.R;
import com.spreadwin.btc.SyncService;
import com.spreadwin.btc.contacts.SideBar.OnTouchingLetterChangedListener;
import com.spreadwin.btc.utils.PhoneBookInfo;
import com.spreadwin.btc.utils.BtcGlobalData;
import com.spreadwin.btc.utils.PhoneBookInfo_new;

import android.app.Fragment;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class ContactsFragment extends Fragment implements OnCreateContextMenuListener, OnItemLongClickListener {
	public static final String TAG = "ContactsFragment";
	public static final boolean DEBUG = MainActivity.DEBUG;

	// private static final int SIM_CONTACTS_TYPE = BtcGlobalData.PB_SIM;
	// private static final int PHONE_CONTACTS_TYPE = BtcGlobalData.PB_PHONE;

	private ViewPager viewPager;

	private ListView sortListView;
	private SideBar sideBar;
	private TextView dialog;
	private TextView mContactsNumber;
	private ContactsAdapter adapter;
	private ClearEditText mClearEditText;

	TextView emptyView;
	LinearLayout mLoading;
	// public CustomDialog.Builder builder;
	// public CustomDialog mCustomDialog;

	/**
	 * 汉字转换成拼音的类
	 */
	private CharacterParser characterParser;
	// private List<PhoneBookInfo_new> SourceDateList;

	/**
	 * 根据拼音来排列ListView里面的数据类
	 */
	private PinyinComparator pinyinComparator;

	public List<Fragment> fragments = new ArrayList<Fragment>();
	private ArrayList<PhoneBookInfo_new> mContactsInfo = new ArrayList<PhoneBookInfo_new>();
	PhoneBookInfo mPhoneContactsInfo, mSIMContactsInfo;
	private LayoutInflater mInflater;
	private ViewGroup mContentContainer;
	private View mRootView;
	int tabType = 1;
	int index;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLog("onCreate 111111111");
		try {
			mContactsInfo = (ArrayList<PhoneBookInfo_new>) MainActivity.binder.getPhoneBookInfo_new();
			characterParser = CharacterParser.getInstance();
			pinyinComparator = new PinyinComparator();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		// 实例化汉字转拼音类
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mInflater = inflater;
		mLog("onCreateView 222222222");
		mRootView = inflater.inflate(R.layout.fragment_contacts_new, container, false);
		sideBar = (SideBar) mRootView.findViewById(R.id.sidrbar);
		dialog = (TextView) mRootView.findViewById(R.id.dialog);
		mContactsNumber = (TextView) mRootView.findViewById(R.id.mContactsNumber);
		mLoading = (LinearLayout) mRootView.findViewById(R.id.loading);
		sideBar.setTextView(dialog);
		// 设置右侧触摸监听
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
			@Override
			public void onTouchingLetterChanged(String s) {
				// 该字母首次出现的位置
				int position = adapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					sortListView.setSelection(position);
				}
			}
		});

		sortListView = (ListView) mRootView.findViewById(R.id.country_lvcountry);
		sortListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String str = "号码：";
				mLog("mContactsInfo onItemClick ==" + position + "; size =="
						+ ((PhoneBookInfo_new) adapter.getItem(position)).getNumber().size());
				for (int i = 0; i < ((PhoneBookInfo_new) adapter.getItem(position)).getNumber().size(); i++) {
					str += ((PhoneBookInfo_new) adapter.getItem(position)).getNumber().get(i);
				}
				// Toast.makeText(getActivity(), str,
				// Toast.LENGTH_SHORT).show();
				view.showContextMenu();
			}
		});
		sortListView.setOnItemLongClickListener(this);
		// builder = new CustomDialog.Builder(getActivity());
		//
		// builder.setMessage("已更新" + SyncService.mNum + "联系人");
		// mContactsInfo =
		// filledData(getResources().getStringArray(R.array.date));
		// 根据a-z进行排序源数据
		try {
			Collections.sort(mContactsInfo, pinyinComparator);
		} catch (Exception e) {
		}
		adapter = new ContactsAdapter(getActivity());
		adapter.clearPhoneBookInfoList();
		adapter.addPhoneBookInfoList(mContactsInfo);
		sortListView.setAdapter(adapter);
		sortListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub

			}
		});
		// 设置通话记录为空的界面
		emptyView = new TextView(getActivity());
		emptyView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		emptyView.setText(getResources().getString(R.string.no_conntacts));
		emptyView.setTextSize(30);
		emptyView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
		emptyView.setVisibility(View.GONE);
		((ViewGroup) sortListView.getParent()).addView(emptyView, 1);
		sortListView.setEmptyView(emptyView);
		// mLog("onCreateView getSyncStatus ==" + BtcNative.getSyncStatus(D));
		mLog("mContactsInfo.size() ==" + mContactsInfo.size());
		if (MainActivity.binder!=null) {
			if (MainActivity.binder.getSyncStatus() == BtcGlobalData.NEW_SYNC || mContactsInfo.size() > 0) {
				hideLoading();
			} else if (MainActivity.binder.getSyncStatus() == BtcGlobalData.BFP_CONNECTED ) {
				showLoading();
			}
		}
		sortListView.setOnCreateContextMenuListener(this);
		mClearEditText = (ClearEditText) mRootView.findViewById(R.id.filter_edit);
		// 根据输入框输入值的改变来过滤搜索
		mClearEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// 当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
				filterData(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		return mRootView;
	}

	public void showLoading() {
		if (emptyView == null) {
			return;
		}
		// if (mCustomDialog == null) {
		// mCustomDialog = builder.crater();
		// }
		// mCustomDialog.show();
		emptyView.setText("");
		mLoading.setVisibility(View.VISIBLE);
		mContactsNumber.setVisibility(View.GONE);

		// handler.post(mRunnable);
	}

	public void hideLoading() {
		if (!isAdded() || emptyView == null) {
			return;
		}
		if (mContactsInfo.size() > 0) {
			mContactsNumber.setVisibility(View.VISIBLE);
			mContactsNumber.setText("联系人数量：" + mContactsInfo.size());
		} else {
			mContactsInfo.clear();
			mContactsNumber.setVisibility(View.GONE);
		}
		emptyView.setText(getResources().getString(R.string.no_conntacts));
		// if (mCustomDialog != null) {
		// mCustomDialog.dismiss();
		// mCustomDialog = null;
		// }
		mLoading.setVisibility(View.GONE);

		// handler.removeCallbacks(mRunnable);
	}

	public void clearList() {
		if (adapter != null) {
			adapter.clearPhoneBookInfoList();
		}
	}

	/**
	 * 为ListView填充数据
	 * 
	 * @param date
	 * @return
	 */
	private List<PhoneBookInfo_new> filledData(String[] date) {
		List<PhoneBookInfo_new> mSortList = new ArrayList<PhoneBookInfo_new>();

		for (int i = 0; i < date.length; i++) {
			PhoneBookInfo_new sortModel = new PhoneBookInfo_new(date[i], "10010");
			// sortModel.setName(date[i]);
			// sortModel.setNumber("10010");
			// 汉字转换成拼音
			String pinyin = characterParser.getSelling(date[i]);
			String sortString = pinyin.substring(0, 1).toUpperCase();

			// 正则表达式，判断首字母是否是英文字母
			if (sortString.matches("[A-Z]")) {
				sortModel.setSortLetters(sortString.toUpperCase());
			} else {
				sortModel.setSortLetters("#");
			}

			mSortList.add(sortModel);
		}
		return mSortList;

	}

	/**
	 * 根据输入框中的值来过滤数据并更新ListView
	 * 
	 * @param filterStr
	 */
	private void filterData(String filterStr) {
		List<PhoneBookInfo_new> filterDateList = new ArrayList<PhoneBookInfo_new>();

		if (TextUtils.isEmpty(filterStr)) {
			filterDateList = mContactsInfo;
		} else {
			filterDateList.clear();
			List<PhoneBookInfo_new> mTempInfo = mContactsInfo;
			for (PhoneBookInfo_new sortModel : mTempInfo) {
				String name = sortModel.getName();
				if (name.indexOf(filterStr.toString()) != -1
						|| characterParser.getSelling(name).startsWith(filterStr.toString())) {
					filterDateList.add(sortModel);
				}
			}
		}

		// 根据a-z进行排序
		try {
			Collections.sort(filterDateList, pinyinComparator);
		} catch (Exception e) {
			// TODO: handle exception
		}
		adapter.updateListView(filterDateList);
	}

	@Override
	public void onResume() {
		super.onResume();
		// notifyAll();
		// setUpdateStatus();
	}

	public void notifyDataSetChanged() {
		if (adapter == null) {
			mLog("ContactsFragment 3333333 == null");
			return;
		}
		mClearEditText.setText(null);
		// mContactsInfo = (ArrayList<PhoneBookInfo_new>)
		// MainActivity.binder.getPhoneBookInfo_new();

		mContactsInfo = (ArrayList<PhoneBookInfo_new>) MainActivity.binder.getPhoneBookInfo_new();

		mLog("notifyDataSetChanged mContactsInfo size ==" + mContactsInfo.size());
		// 根据a-z进行排序
		try {
			Collections.sort(mContactsInfo, pinyinComparator);
		} catch (Exception e) {
			// TODO: handle exception
		}
		if (mContactsInfo.size() > 0 || BtcNative.getBfpStatus() == BtcGlobalData.BFP_DISCONNECT) {
			hideLoading();
		}
		adapter.updateListView(mContactsInfo);
	}

	Handler handler = new Handler();
	Runnable runnable = new Runnable() {

		@Override
		public void run() {
			// if (mContactsInfo) {
			mLog("runnable  333333333");
			adapter.updateListView(mContactsInfo);
			// }

		}
	};

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		mLog("onItemLongClick111111111 arg0 ==" + arg0.getId() + "arg1 ==" + arg1.getId() + "; arg2 ==" + arg2
				+ "; arg3 ==" + arg3);
		if (arg0 == sortListView) {
			mLog("onItemLongClick222222222 arg0 ==" + arg0.getId() + "arg1 ==" + arg1.getId() + "; arg2 ==" + arg2
					+ "; arg3 ==" + arg3);
			index = arg2;
		}
		hide();

		return false;
	}

	private void hide() {
		mLog("hide  111111111");
		if (getActivity().getWindow()
				.getAttributes().softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED) {
			mLog("hide  222222222222");
			// 关闭输入法
			InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(mClearEditText.getWindowToken(), 0);
			// inputMethodManager.toggleSoftInput(0,
			// InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.setHeaderTitle(((PhoneBookInfo_new) adapter.getItem(index)).getName());
		mLog("onCreateContextMenu index ==" + index);
		if (((PhoneBookInfo_new) adapter.getItem(index)).getNumber().size() > 1) {
			for (int i = 0; i < ((PhoneBookInfo_new) adapter.getItem(index)).getNumber().size(); i++) {
				menu.add(i, 1, 0, "拨打:" + ((PhoneBookInfo_new) adapter.getItem(index)).getNumber().get(i));
			}
		} else {
			BtcNative.dialCall(((PhoneBookInfo_new) adapter.getItem(index)).getNumber().get(0));
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

				MainActivity.mBluetoothFragment
						.dialCall(((PhoneBookInfo_new) adapter.getItem(position)).getNumber().get(item.getGroupId()));
				// MainActivity.mBluetoothFragment.dialCall(mContactsInfo.get(position).getNumber().get(item.getGroupId()));
			}
			break;
		default:
			break;
		}
		return true;
	}

	public static void mLog(String string) {
		if (DEBUG) {
			Log.d(TAG, string);
		}
	}

}
