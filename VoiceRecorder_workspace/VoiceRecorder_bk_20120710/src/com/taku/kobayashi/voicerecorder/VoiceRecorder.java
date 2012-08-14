package com.taku.kobayashi.voicerecorder;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder.OnInfoListener;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class VoiceRecorder{

	private static final String TAG = "VoiceRecorder_VoiceRecorder";
	private Context m_Context;
	private MediaRecorder m_Recorder;
	private boolean m_bRecording;

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public VoiceRecorder(Context context){
		m_Context = context;
		m_Recorder = new MediaRecorder();
		m_bRecording = false;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public boolean startRecord(int AudioSource,int nDurationTime){
		if(m_bRecording == false){
			m_bRecording = true;
			SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(m_Context);
			String format = setting.getString(m_Context.getResources().getString(R.string.SettingFormatKey), ".wav");
			int nOutput = Tools.getOutputFormat(format);
			m_Recorder.setAudioSource(AudioSource);
			//オーディオファイルの出力フォーマット
			m_Recorder.setOutputFormat(nOutput);
			//高音質処理
			if(Build.VERSION.SDK_INT >= 10){
				String SamplingRate = setting.getString(m_Context.getResources().getString(R.string.SettingAudioSamplingRateKey), String.valueOf(Config.MIN_AUDIOSAMPLINGRATE));
				int nRate = Config.MIN_AUDIOSAMPLINGRATE;
				try{
					nRate = Integer.parseInt(SamplingRate);
				} catch (NumberFormatException e) {

				}
				m_Recorder.setAudioSamplingRate(nRate);
				if(nRate > Config.MIN_AUDIOSAMPLINGRATE){
					m_Recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
				}else{
					m_Recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
				}
			}else{
				m_Recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
			}

			int nMaxDuration = nDurationTime;
			if(nMaxDuration == Config.DEFAULT_MAXDURATION){
				nMaxDuration = setting.getInt(m_Context.getResources().getString(R.string.SettingMaxDurationKey), Config.DEFAULT_MAXDURATION);
			}
			//Timer == -1 で無限大
			if(nMaxDuration > 0 && AudioSource == MediaRecorder.AudioSource.MIC){
				m_Recorder.setMaxDuration(nMaxDuration);
			}
			//ファイル保存容量の限界、ファイル保存時間の限界等が訪れた時呼ばれる。
			m_Recorder.setOnInfoListener(m_MediaRecorderInfoListener);
			//オーディオファイルの出力先のパス
			String strSaveFilePath = Tools.getFilePath(format);
			m_Recorder.setOutputFile(strSaveFilePath);
			try {
				m_Recorder.prepare();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			m_Recorder.start();
		}
		return m_bRecording;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public boolean StopRecord(){
		if(m_bRecording == true){
			m_bRecording = false;
			m_Recorder.stop();
			m_Recorder.reset();
		}
		return m_bRecording;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private OnInfoListener m_MediaRecorderInfoListener = new OnInfoListener() {
		@Override
		public void onInfo(MediaRecorder mr, int what, int extra) {
			if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED){
				StopRecord();
				Tools.showToast(m_Context, m_Context.getResources().getString(R.string.NoAvailableSpaceMessage));
			}else if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){
				StopRecord();
				Tools.showToast(m_Context, m_Context.getResources().getString(R.string.TimeoutMessage));
			}
		}
	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/*
	private int getMaxDuration(int Time,int position){
		int nTimeMillisecond = -1;
		if(position == 0){
			nTimeMillisecond = -1;
		}else if(position == 1){
			//秒
			nTimeMillisecond = Time * 1000;
		}else if(position == 2){
			//分
			nTimeMillisecond = Time * 1000 * 60;
		}else if(position == 3){
			//時間
			nTimeMillisecond = Time * 1000 * 60 * 60;
		}
		return nTimeMillisecond;
	}
	*/

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void release(){
		if(m_bRecording == true){
			m_Recorder.stop();
			m_Recorder.reset();
		}
		m_Recorder.release();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

}