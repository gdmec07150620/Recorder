package com.zsl.view;

import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by zsl on 2017/7/9.
 */
public class AudioManager {
	private MediaRecorder mediaRecorder;
	private String mDirString;
	private  String mCurrentFilePathString;
	private boolean isPrepared;// 是否准备好了

	//利用单例化
	private static AudioManager mInstance;

	private AudioManager(String dir){
		mDirString = dir;
	}
	public static AudioManager getInstance(String dir){
		if(mInstance ==null){
			synchronized (AudioManager.class){
				if (mInstance == null) {
					mInstance = new AudioManager(dir);

				}
			}
		}
		return mInstance;
	}

	//设置一个接口，准备完毕后，button才会开始显示录音框
	public interface AudioStageListener {
		void wellPrepared();
	}

	public AudioStageListener mListener;

	public void setOnAudioStageListener(AudioStageListener listener) {
		mListener = listener;
	}

	public void prepareAudio() throws IOException {
		isPrepared = false;
		File dir = new File(mDirString);
		if(!dir.exists()){
			dir.mkdirs();
		}
		String fileNameString = generalFileName();
		File file = new File(dir,fileNameString);

		mCurrentFilePathString = file.getAbsolutePath();

		mediaRecorder = new MediaRecorder();
		// 设置输出文件
		mediaRecorder.setOutputFile(file.getAbsolutePath());
		// 设置meidaRecorder的音频源是麦克风
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		// 设置文件音频的输出格式为amr
		mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
		// 设置音频的编码格式为amr
		mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		// 严格遵守google官方api给出的mediaRecorder的状态流程图
		mediaRecorder.prepare();

		mediaRecorder.start();
		// 准备结束
		isPrepared = true;
		if(mListener!=null){
			mListener.wellPrepared();
		}
	}
	//随机生成文件的名称
	private String generalFileName() {
		return UUID.randomUUID().toString() + ".amr";
	}
	// 获得声音的level
	public int getVoiceLevel(int maxLevel) {
		// mRecorder.getMaxAmplitude()这个是音频的振幅范围，值域是1-32767
		if (isPrepared) {
			try {
				// 取证+1，否则去不到7
				return maxLevel * mediaRecorder.getMaxAmplitude() / 32768 + 1;
			} catch (Exception e) {

			}
		}

		return 1;
	}
	// 释放资源
	public void release() {
		// 严格按照api流程进行
		mediaRecorder.stop();
		mediaRecorder.release();
		mediaRecorder = null;

	}
	// 取消,因为prepare时产生了一个文件，所以cancel方法应该要删除这个文件，
	// 这是与release的方法的区别
	public void cancel() {
		release();
		if (mCurrentFilePathString != null) {
			File file = new File(mCurrentFilePathString);
			file.delete();
			mCurrentFilePathString = null;
		}
	}
	public String getCurrentFilePath() {
		return mCurrentFilePathString;
	}
}
