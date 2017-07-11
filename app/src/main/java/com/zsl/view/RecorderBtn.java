package com.zsl.view;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.zsl.recorder.R;

import java.io.IOException;

public class RecorderBtn extends Button implements AudioManager.AudioStageListener {
    private static final int STATE_NORMAL = 1;
    private static final int STATE_RECORDING = 2;
    private static final int STATE_WANT_TO_CANCEL = 3;

    private static final int DISTANCE_Y_CANCEL = 50;

    private int mCurrentState = STATE_NORMAL;
    // 已经开始录音
    private boolean isRecording = false;

    private DialogManager mDialogManager;

    private AudioManager mAudioManager;

    private float mTime = 0;
    // 是否触发了onlongclick，准备好了
    private boolean mReady;

    /**
     * 先实现两个参数的构造方法，布局会默认引用这个构造方法， 用一个 构造参数的构造方法来引用这个方法 * @param context
     */

    public RecorderBtn(Context context) {
        this(context, null);
    }

    public RecorderBtn(Context context, AttributeSet attrs) {
        super(context, attrs);

        mDialogManager = new DialogManager(getContext());

        // 这里没有判断储存卡是否存在，有空要判断
        String dir = Environment.getExternalStorageDirectory() + "/recorder_audios";
        mAudioManager = AudioManager.getInstance(dir);
        mAudioManager.setOnAudioStageListener(this);

        setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                    mReady = true;
                try {
                    mAudioManager.prepareAudio();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }

    // 录音完成后的回调，回调给activiy，可以获得mtime和文件的路径
    public interface AudioFinishRecorderListener{
        void onFinished(float seconds,String filePath);
    }

    private AudioFinishRecorderListener mListener;

    public void setAudioFinishRecorderListener(AudioFinishRecorderListener listener)
    {
        mListener=listener;
    }

    // 获取音量大小的runnable
    private Runnable mGetVoiceLevelRunnable = new Runnable() {

        @Override
        public void run() {
            while (isRecording) {
                try {
                    Thread.sleep(100);
                    mTime += 0.1f;
                    mhandler.sendEmptyMessage(MSG_VOICE_CHANGE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    // 准备三个常量,handler处理的三个事件
    private static final int MSG_AUDIO_PREPARED = 0X110;
    private static final int MSG_VOICE_CHANGE = 0X111;
    private static final int MSG_DIALOG_DIMISS = 0X112;

    private Handler mhandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_AUDIO_PREPARED:
                    // 显示应该是在audio end prepare之后回调
                    mDialogManager.showRecordingDialog();
                    isRecording = true;
                    new Thread(mGetVoiceLevelRunnable).start();
                    // 需要开启一个线程来变换音量
                    break;
                case MSG_VOICE_CHANGE:
                    mDialogManager.updateVoiceLevel(mAudioManager.getVoiceLevel(7));
                    break;
                case MSG_DIALOG_DIMISS:
                    mDialogManager.dimissDialog();
                    break;

            }
        };
    };

    // 在这里面发送一个handler的消息
    @Override
    public void wellPrepared() {
        mhandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
    }

    /**
     * 直接复写这个监听函数
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                changeState(STATE_RECORDING);
                break;
            case MotionEvent.ACTION_MOVE:

                if (isRecording) {

                    // 根据x，y来判断用户是否想要取消
                    if (wantToCancel(x, y)) {
                        changeState(STATE_WANT_TO_CANCEL);
                    } else {
                        changeState(STATE_RECORDING);
                    }

                }

                break;
            case MotionEvent.ACTION_UP:
                // 首先判断是否有触发onlongclick事件，没有的话直接返回reset
                if (!mReady) {
                    reset();
                    return super.onTouchEvent(event);
                }
                // 如果按的时间太短，还没准备好或者时间录制太短，就离开了，则显示这个dialog
                if (!isRecording || mTime < 0.5f) {
                    mDialogManager.tooShort();
                    mAudioManager.cancel();
                    mhandler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS, 1300);// 持续1.3s
                } else if (mCurrentState == STATE_RECORDING) {//正常录制结束

                    mDialogManager.dimissDialog();

                    mAudioManager.release();// release释放一个mediarecorder

                    if (mListener!=null) {// 并且callbackActivity，保存录音

                        mListener.onFinished(mTime, mAudioManager.getCurrentFilePath());
                    }
                } else if (mCurrentState == STATE_WANT_TO_CANCEL) {
                    // cancel
                    mAudioManager.cancel();
                    mDialogManager.dimissDialog();
                }
                reset();// 恢复标志位

                break;

        }

        return super.onTouchEvent(event);
    }
    //回复标志位以及状态
    private void reset() {
        isRecording = false;
        changeState(STATE_NORMAL);
        mReady = false;
        mTime = 0;
    }

    private boolean wantToCancel(int x, int y) {

        if (x < 0 || x > getWidth()) {// 判断是否在左边，右边，上边，下边
            return true;
        }
        if (y < -DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL) {
            return true;
        }

        return false;
    }

    private void changeState(int state) {
        if (mCurrentState != state) {
            mCurrentState = state;
            switch (mCurrentState) {
                case STATE_NORMAL:
                    setBackgroundResource(R.drawable.btn_recorder);
                    setText(R.string.recorder_normal);

                    break;
                case STATE_RECORDING:
                    setBackgroundResource(R.drawable.btn_recorder_down);
                    setText(R.string.recorder_recording);
                    if (isRecording) {
                        mDialogManager.recording();
                        // 复写dialog.recording();
                    }
                    break;

                case STATE_WANT_TO_CANCEL:
                    setBackgroundResource(R.drawable.btn_recorder_down);
                    setText(R.string.want_to_cancle);
                    // dialog want to cancel
                    mDialogManager.wantToCancel();
                    break;
            }
        }

    }

    @Override
    public boolean onPreDraw() {
        return false;
    }

}