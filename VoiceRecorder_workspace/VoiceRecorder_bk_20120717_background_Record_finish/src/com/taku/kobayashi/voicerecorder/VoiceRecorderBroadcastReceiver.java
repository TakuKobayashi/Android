package com.taku.kobayashi.voicerecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class VoiceRecorderBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG = "VoiceRecorder_VoiceRecorderBroadcastReceiver";
	private VoiceRecorder m_VoiceRecorder;
	private Context m_Context;
	private String m_TelephonyStatus;

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public VoiceRecorderBroadcastReceiver(){
		Log.d(TAG, "Initialize");
		m_VoiceRecorder = new VoiceRecorder(m_Context);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void onReceive(Context context, Intent intent) {
		m_Context = context;
		SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(context);
		if(setting.getBoolean(context.getResources().getString(R.string.SettingPhoneRecordKey), false) == true && CheckRecord(context) == true){
			TelephonyManager tm = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
			tm.listen(m_PhoneStateLister, PhoneStateListener.LISTEN_CALL_STATE);
			m_TelephonyStatus = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private PhoneStateListener m_PhoneStateLister = new PhoneStateListener(){
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			Log.d(TAG,""+state+":"+m_TelephonyStatus);
			//TelephonyManager.CALL_STATE_OFFHOOKは通話開始時に呼ばれる
			//if(state == TelephonyManager.CALL_STATE_OFFHOOK && m_TelephonyStatus == TelephonyManager.EXTRA_STATE_RINGING){
			if(state == TelephonyManager.CALL_STATE_OFFHOOK){
				Log.d(TAG, "OFFHOOK:"+incomingNumber);
				MediaRecorder mr = new MediaRecorder();
				m_VoiceRecorder.setRecord(mr, MediaRecorder.AudioSource.VOICE_CALL,Config.DEFAULT_MAXDURATION);
				mr.setOnInfoListener(m_MediaRecorderInfoListener);
				m_VoiceRecorder.StartRecord();
			//TelephonyManager.CALL_STATE_IDLEは待ち受け状態に変化した時に呼ばれる
			}else if(state == TelephonyManager.CALL_STATE_IDLE){
				m_VoiceRecorder.StopRecord();
				m_VoiceRecorder.release();
			}
		};
	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//SDカードの状態をチェック
	private boolean CheckRecord(Context con){
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) == false){
			Tools.showToast(con, con.getResources().getString(R.string.SDCardMountRemovingMessage));
			return false;
		}else{
			String strSDcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
			StatFs statFs = new StatFs(strSDcardPath);
			double SDcardAvailableSpace = (double)statFs.getAvailableBlocks() * (double)statFs.getBlockSize() / 1024.0;
			if (SDcardAvailableSpace <= com.taku.kobayashi.voicerecorder.Config.LIMIT_MINIMAM_SPACE) {
				Tools.showToast(con, con.getResources().getString(R.string.SDCardNoAvailableSpaceMessage));
				return false;
			}
		}
		return true;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private OnInfoListener m_MediaRecorderInfoListener = new OnInfoListener() {
		@Override
		public void onInfo(MediaRecorder mr, int what, int extra) {
			if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED){
				m_VoiceRecorder.StopRecord();
				Tools.showToast(m_Context, m_Context.getResources().getString(R.string.NoAvailableSpaceMessage));
			}else if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){
				m_VoiceRecorder.StopRecord();
				Tools.showToast(m_Context, m_Context.getResources().getString(R.string.TimeoutMessage));
			}
		}
	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

}