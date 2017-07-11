package com.zsl.view;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zsl.recorder.R;

/**
 * Created by zsl on 2017/7/8.
 */
public class DialogManager {

	private Dialog dialog;
	private ImageView mIcon;
	private ImageView mVoice;
	private TextView mLable;
	private Context mcontext;

	public DialogManager(Context context) {
		mcontext = context;
	}

	public void showRecordingDialog() {
		if(dialog==null){
			dialog = new Dialog(mcontext, R.style.Theme_audioDialog);
			// 用layoutinflater来引用布局
			LayoutInflater layoutInflater = LayoutInflater.from(mcontext);
			View view = layoutInflater.inflate(R.layout.dialog_manager,null);
			dialog.setContentView(view);

			mIcon = (ImageView) dialog.findViewById(R.id.dialog_icon);
			mVoice = (ImageView) dialog.findViewById(R.id.dialog_voice);
			mLable = (TextView) dialog.findViewById(R.id.recorder_dialogtext);
			dialog.show();
		}
	}
	//正在录音的dialog布局
	public void recording() {
		if(dialog!=null&&dialog.isShowing()){
			mIcon.setVisibility(View.VISIBLE);//显示
			mVoice.setVisibility(View.VISIBLE);
			mLable.setVisibility(View.VISIBLE);

			mIcon.setImageResource(R.mipmap.recorder);
			mLable.setText(R.string.shouzhishanghua);
		}

	}
	//取消录音的dialog布局
	public void wantToCancel() {
		if (dialog != null && dialog.isShowing()) {
			mIcon.setVisibility(View.VISIBLE);
			mVoice.setVisibility(View.GONE);//不可见，但这个View在ViewGroup中不保留位置，会重新layout，不再占用空间，那后面的view
			mLable.setVisibility(View.VISIBLE);

			mIcon.setImageResource(R.mipmap.cancel);
			mLable.setText(R.string.want_to_cancle);
		}
	}
	//时间过短
	public void tooShort() {
		if (dialog != null && dialog.isShowing()) {
			mIcon.setVisibility(View.VISIBLE);
			mVoice.setVisibility(View.GONE);
			mLable.setVisibility(View.VISIBLE);

			mIcon.setImageResource(R.mipmap.voice_to_short);
			mLable.setText(R.string.tooshort);
		}
	}
	//隐藏dialog
	public void dimissDialog() {
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
			dialog = null;
		}
	}
	//更新显示音量大小的图标
	public void updateVoiceLevel(int level) {
		if (dialog != null && dialog.isShowing()) {

			//先不改变它的默认状态
//			mIcon.setVisibility(View.VISIBLE);
//			mVoice.setVisibility(View.VISIBLE);
//			mLable.setVisibility(View.VISIBLE);

			//通过level来找到图片的id，也可以用switch来寻址，但是代码可能会比较长
			int resId = mcontext.getResources().getIdentifier("v" + level, "mipmap", mcontext.getPackageName());

			mVoice.setImageResource(resId);
		}
	}
}