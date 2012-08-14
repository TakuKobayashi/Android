package com.taku.kobayashi.voicerecorder;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class VoiceRecorderOnBackGroundService extends IntentService {

	private final static String TAG = "VoiceRecorder_VoiceRecorderOnBackGroundService";
	private VoiceRecorder m_VoiceRecorder;
	private boolean m_bRecording;

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public VoiceRecorderOnBackGroundService(String name) {
		super(name);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public VoiceRecorderOnBackGroundService() {
		super("VoiceRecorderOnBackGroundService");
		//ファイル保存容量の限界、ファイル保存時間の限界等が訪れた時呼ばれる。
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/*
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO 自動生成されたメソッド・スタブ
		return super.onStartCommand(intent, flags, startId);
	}
	*/

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void onCreate() {
		super.onCreate();
		m_VoiceRecorder = new VoiceRecorder(this);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onHandleIntent(Intent intent) {
		MediaRecorder mr = new MediaRecorder();
		m_VoiceRecorder.setRecord(mr,MediaRecorder.AudioSource.MIC,30000);
		mr.setOnInfoListener(m_MediaRecorderInfoListener);

		Log.d(TAG, "start");
		Log.d(TAG, "intent:"+intent);
		m_bRecording = 	m_VoiceRecorder.StartRecord();
		boolean bRecord = m_bRecording;
		//バックグラウンド処理で録音中、無限ループさせて録音させ続ける
		while(bRecord == false){
			Log.d(TAG, ""+bRecord);
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			bRecord = sp.getBoolean(getResources().getString(R.string.RecordingKey), true);
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "ServiceDestroy");
		if(m_bRecording == true){
			Log.d(TAG, "stop");
			//m_bRecording = m_VoiceRecorder.StopRecord(true);
			//m_VoiceRecorder.release();
		}
	}

	private OnInfoListener m_MediaRecorderInfoListener = new OnInfoListener() {
		@Override
		public void onInfo(MediaRecorder mr, int what, int extra) {
			Log.d(TAG,"what"+what+" extra:"+extra);
			if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED){
				m_bRecording = m_VoiceRecorder.StopRecord();
				Tools.showToast(VoiceRecorderOnBackGroundService.this, VoiceRecorderOnBackGroundService.this.getResources().getString(R.string.NoAvailableSpaceMessage));
			}else if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){
				m_bRecording = m_VoiceRecorder.StopRecord();
				Tools.showToast(VoiceRecorderOnBackGroundService.this, VoiceRecorderOnBackGroundService.this.getResources().getString(R.string.TimeoutMessage));
			}
		}
	};

}