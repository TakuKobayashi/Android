package com.taku.kobayashi.voicerecorder;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

public class VoiceRecorder{

	private static final String TAG = "VoiceRecorder_VoiceRecorder";
	private Context m_Context;
	private MediaRecorder m_Recorder = null;
	private boolean m_bRecording;
	//private boolean m_bBackGround;
	private AudioRecord m_AudioRecord;
	private SharedPreferences m_SharedPreferences;
	private Thread m_Thread;
	private Timer m_FinishTimer;
	private Handler m_Handler;

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public VoiceRecorder(Context context){
		m_Context = context;
		m_bRecording = false;
		//m_bBackGround = PreferenceManager.getDefaultSharedPreferences(m_Context).getBoolean(m_Context.getResources().getString(R.string.RecordingKey), m_bRecording);
		m_SharedPreferences = PreferenceManager.getDefaultSharedPreferences(m_Context);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void setRecord(MediaRecorder mr,int AudioSource,int nDurationTime){
		m_Recorder = mr;
		//SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(m_Context);
		String format = m_SharedPreferences.getString(m_Context.getResources().getString(R.string.SettingFormatKey), ".wav");
		int nOutput = Tools.getOutputFormat(format);
		m_Recorder.setAudioSource(AudioSource);
		//オーディオファイルの出力フォーマット
		m_Recorder.setOutputFormat(nOutput);
		//高音質処理
		int nRate = getSamplingRate();
		if(nRate > Config.MIN_AUDIOSAMPLINGRATE){
			m_Recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		}else{
			m_Recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		}
		/*
		if(Build.VERSION.SDK_INT >= 10){
			String SamplingRate = m_SharedPreferences.getString(m_Context.getResources().getString(R.string.SettingAudioSamplingRateKey), String.valueOf(Config.MIN_AUDIOSAMPLINGRATE));
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
		*/
		int nMaxDuration = nDurationTime;
		if(nMaxDuration == Config.DEFAULT_MAXDURATION){
			nMaxDuration = m_SharedPreferences.getInt(m_Context.getResources().getString(R.string.SettingMaxDurationKey), Config.DEFAULT_MAXDURATION);
		}
		//Timer == -1 で無限大
		if(nMaxDuration > 0 && AudioSource == MediaRecorder.AudioSource.MIC){
			m_Recorder.setMaxDuration(nMaxDuration);
		}
		//オーディオファイルの出力先のパス
		String strSaveFilePath = Tools.getFilePath(format);
		m_Recorder.setOutputFile(strSaveFilePath);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//バックグラウンド録音はスタートボタンを押したときのみ実行
	public boolean BackgroundRecordStart(){
		m_Handler = new Handler();
		int nMaxDuration = m_SharedPreferences.getInt(m_Context.getResources().getString(R.string.SettingMaxDurationKey), Config.DEFAULT_MAXDURATION);
		if(nMaxDuration == Config.DEFAULT_MAXDURATION){
			BackGroundRecordFinishTimer(nMaxDuration);
		}
		m_Thread = new Thread(new Runnable() {
			@Override
			public void run() {
				m_Handler.post(new Runnable() {
					@Override
					public void run() {
						StartAudioRecord();
					}
				});
			}
		});
		m_Thread.start();
		return true;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private void StartAudioRecord(){
		int nSamplingRate = getSamplingRate();
		int audioBuf = AudioRecord.getMinBufferSize(nSamplingRate,AudioFormat.CHANNEL_CONFIGURATION_MONO,AudioFormat.ENCODING_PCM_16BIT) * 2;
		m_AudioRecord =  new AudioRecord(MediaRecorder.AudioSource.MIC,getSamplingRate(),AudioFormat.CHANNEL_CONFIGURATION_MONO,AudioFormat.ENCODING_PCM_16BIT,audioBuf);

		// バッファ
		byte[] buffer = new byte[audioBuf];
		// 録音用ファイル取得  SDカード状態チェック略
		File recFile = new File(Tools.getFilePath(m_SharedPreferences.getString(m_Context.getResources().getString(R.string.SettingFormatKey), ".wav")));
		m_bRecording = true;
		EditRecordingStatus(m_bRecording);
		/*
		int nMaxDuration = m_SharedPreferences.getInt(m_Context.getResources().getString(R.string.SettingMaxDurationKey), Config.DEFAULT_MAXDURATION);
		if(nMaxDuration == Config.DEFAULT_MAXDURATION){
			BackGroundRecordFinishTimer(nMaxDuration);
		}
		*/
		try {
			recFile.createNewFile();
			// ファイル書き込み  例外処理略
			FileOutputStream out =  new FileOutputStream(recFile);
			// 録音開始
			m_AudioRecord.startRecording();
			// フラグが落ちるまでループ  例外処理略
			while(m_bRecording == false) {
				// バッファを読み込んで書き込む
				m_bRecording = m_SharedPreferences.getBoolean(m_Context.getResources().getString(R.string.RecordingKey),true);
				m_AudioRecord.read(buffer, 0, audioBuf);
			}
			// 終了処理 例外処理略
			m_AudioRecord.stop();
			out.close();
			out = null;
			m_AudioRecord.release();
			m_AudioRecord = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		StopAudioRecord();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public boolean StopAudioRecord(){
		m_bRecording = false;
		EditRecordingStatus(m_bRecording);
		if(m_FinishTimer != null){
			m_FinishTimer.cancel();
			m_FinishTimer.purge();
			m_FinishTimer = null;
		}
		if(m_Thread.isAlive() == true){
			m_Thread.stop();
		}
		return false;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void BackGroundRecordFinishTimer(long MaxDuration){
		m_FinishTimer = new Timer(true);
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				m_Handler.post(new Runnable() {
					@Override
					public void run() {
						//ここに時間が来たときに実行する処理を記入する
						StopAudioRecord();
					}
				});
			}
		};
		m_FinishTimer.schedule(task, MaxDuration);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private int getSamplingRate(){
		int nRate = Config.MIN_AUDIOSAMPLINGRATE;
		if(Build.VERSION.SDK_INT >= 10){
			String SamplingRate = m_SharedPreferences.getString(m_Context.getResources().getString(R.string.SettingAudioSamplingRateKey), String.valueOf(Config.MIN_AUDIOSAMPLINGRATE));
			try{
				nRate = Integer.parseInt(SamplingRate);
			} catch (NumberFormatException e) {

			}
		}
		return nRate;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public boolean startRecord(){
		//SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(m_Context);
		//boolean bBackGround = m_SharedPreferences.getBoolean(m_Context.getResources().getString(R.string.RecordingKey), m_bRecording);
		if(m_bRecording == false){
			m_bRecording = true;
//			m_bBackGround = true;
			EditRecordingStatus(m_bRecording);
			try {
				m_Recorder.prepare();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			m_Recorder.start();
			Tools.showToast(m_Context, m_Context.getResources().getString(R.string.RecordStartMessage));
		}
		/*
		if(m_bRecording == bBackGround){

		}else{
			EditRecordingStatus(false);
			Tools.showToast(m_Context, m_Context.getResources().getString(R.string.RecordingStartErrorMessage));
			return false;
		}
		*/
		return true;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/*
	private OnSharedPreferenceChangeListener m_PrefernceListener = new OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {
			if(key.equals(m_Context.getResources().getString(R.string.RecordStartTimeKey))){
				if(m_bRecording == sharedPreferences.getBoolean(m_Context.getResources().getString(R.string.RecordingKey), false) && m_bRecording != m_bBackGround){
					startRecord();
				}
			}
		}
	};
	*/

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public boolean StopRecord(){
		if(m_Recorder != null){

			//if(PreferenceManager.getDefaultSharedPreferences(m_Context).getBoolean(m_Context.getResources().getString(R.string.RecordingKey), true) == true){
			//if(m_bRecording == true){
				//if(bBackground == false){
					m_bRecording = false;
					//m_bBackGround = false;
					EditRecordingStatus(m_bRecording);
					m_Recorder.stop();
					m_Recorder.reset();
				//}else{
					//m_bRecording = false;
					//EditRecordingStatus(m_bRecording);
				//}
			//}
		}else{
			m_bRecording = false;
			EditRecordingStatus(m_bRecording);
		}
		return false;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private void EditRecordingStatus(boolean Recording){
		SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(m_Context);
		SharedPreferences.Editor editor = setting.edit();
		//Booleanの変化のリスナーはLongの後に取得したいため、順番を変えてはだめ
		editor.putLong(m_Context.getResources().getString(R.string.RecordStartTimeKey), System.currentTimeMillis());
		editor.putBoolean(m_Context.getResources().getString(R.string.RecordingKey), Recording);
		/*
		if(Recording == false){
			editor.putBoolean(m_Context.getResources().getString(R.string.RecordingFinishKey), true);
		}
		*/
		editor.commit();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void release(){
		if(m_Recorder != null){
			if(m_bRecording == true){
				m_Recorder.stop();
				m_Recorder.reset();
				EditRecordingStatus(false);
			}
			m_Recorder.release();
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

}