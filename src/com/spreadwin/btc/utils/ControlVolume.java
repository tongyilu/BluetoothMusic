package com.spreadwin.btc.utils;

import com.spreadwin.btc.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ControlVolume extends RelativeLayout {
	private ImageView img_volume;
	private ImageView img_v1;
	private ImageView img_v2;
	private ImageView img_v3;
	private ImageView img_v4;
	private ImageView img_v5;
	private ImageView img_v6;
	private ImageView img_v7;
	private ImageView img_v8;
	private ImageView img_v9;
	private ImageView img_v10;
	private ImageView img_v11;
	private ImageView img_v12;
	private ImageView img_v13;
	private ImageView img_v14;
	private ImageView img_v15;
	private ImageView img_v16;
	private ImageView[] mImgVol ={img_v1,img_v2,img_v3,img_v4,img_v5,img_v6,img_v7,img_v8,img_v9,img_v10,
								  img_v11,img_v12,img_v13,img_v14,img_v15,img_v16};
	private int[] mImgId ={R.id.img_v1,R.id.img_v2,R.id.img_v3,R.id.img_v4,R.id.img_v5,R.id.img_v6,R.id.img_v7,R.id.img_v8,
			           R.id.img_v9,R.id.img_v10,R.id.img_v11,R.id.img_v12,R.id.img_v13,R.id.img_v14,R.id.img_v15,R.id.img_v16};
	
	private int mAudioMax = 16;
	public ControlVolume(Context ctx) {
		super(ctx);
		this.init(ctx);
	}

	public ControlVolume(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		this.init(ctx);
	}

	
	private void init(Context ctx) {
		LayoutInflater inflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.controlvolume, this);
		img_volume = (ImageView) findViewById(R.id.img_volume);	
		for (int i = 0; i < mImgId.length; i++) {
			mImgVol[i] = (ImageView) findViewById(mImgId[i]);
			if (i > mAudioMax-1) {
				mImgVol[i].setVisibility(View.GONE);
			}
		}
		
	}

	
	public int setImgBg(int current, boolean toggle) {
		Log.d("MainActivity", "current1111 =="+current);
		int cur = current;
		if (toggle) {			
			if (cur < mAudioMax)
				cur += 1;
			if (cur == 1)
				img_volume.setImageResource(R.drawable.volumn);
		}else {
			if (cur > 0)
				cur -= 1;
			if (cur == 0)
				img_volume.setImageResource(R.drawable.mute);			
		}
		Log.d("MainActivity", "current22222 =="+cur);
		for (int i = 0; i < mImgId.length; i++) {
			if (i <= cur -1 ) {
				mImgVol[i].setImageResource(R.drawable.seekbar_bg2);
			}else{
				mImgVol[i].setImageResource(R.drawable.seekbar_bg);				
			}
		}		
		return cur;
	}
}
