package com.spreadwin.btc.view;

import com.spreadwin.btc.SyncService;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class CallLineraLayout extends LinearLayout {
	private View mView;
	private Context mContext;
	private DetachedFromWindow mDetachedFromWindow;

	public void init(Context context, View view,DetachedFromWindow mDetachedFromWindow) {
		this.mView = view;
		this.mContext = context;
		this.mDetachedFromWindow = mDetachedFromWindow;
	}

	public CallLineraLayout(Context context) {
		super(context);

	}

	public CallLineraLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CallLineraLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_BACK:
		case KeyEvent.KEYCODE_MENU:
			// 处理自己的逻辑break;
			WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
			((SyncService) mContext).finishMainActivity();
			wm.removeView(mView);
		default:
			break;
		}
		return super.dispatchKeyEvent(event);
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	public interface DetachedFromWindow{
		public void onDetachedFromWindow();
	}
}
