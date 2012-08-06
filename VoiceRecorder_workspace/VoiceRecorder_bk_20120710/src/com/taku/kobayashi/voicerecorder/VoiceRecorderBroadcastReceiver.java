package com.taku.kobayashi.voicerecorder;

import android.app.Activity;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class VoiceRecorderBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG = "VoiceRecorder_VoiceRecorderBroadcastReceiver";
	private VoiceRecorder m_VoiceRecorder;
	private Context m_Context;
	private String m_TelephonyStatus;

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public VoiceRecorderBroadcastReceiver(){
		Log.d(TAG, "Initialize");
		m_VoiceRecorder = new VoiceRecorder(m_Context);
		//m_VoiceRecorder.startRecord(MediaRecorder.AudioSource.VOICE_CALL);
		/*
		if(m_bRecording == false){
			StartRecord(MediaRecorder.AudioSource.VOICE_CALL);
		}
		*/
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
			/*
			Log.d(TAG, strReceiveCall);
			//着信した受話器を取った時に録音開始
			if(strReceiveCall.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
				Tools.showToast(context, context.getResources().getString(R.string.PhoneRecordStartMessage));
			}else if(strReceiveCall.equals(TelephonyManager.CALL_STATE_IDLE)){
				Tools.showToast(context, context.getResources().getString(R.string.PhoneRecordFinishMessage));
				//ユーザーが電話をかけたとき受信
			}else if(intent.getAction() == intent.ACTION_NEW_OUTGOING_CALL){
				Tools.showToast(context, context.getResources().getString(R.string.PhoneRecordStartMessage));
				Log.d(TAG,"Call");
			}
			*/
		}
	}


	/*
	@Override
	public IBinder peekService(Context myContext, Intent service) {
		// TODO 自動生成されたメソッド・スタブ
		return super.peekService(myContext, service);
	}
	*/


	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private PhoneStateListener m_PhoneStateLister = new PhoneStateListener(){
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			Log.d(TAG,""+state+":"+m_TelephonyStatus);
			//TelephonyManager.CALL_STATE_OFFHOOKは通話開始時に呼ばれる
			//if(state == TelephonyManager.CALL_STATE_OFFHOOK && m_TelephonyStatus == TelephonyManager.EXTRA_STATE_RINGING){
			if(state == TelephonyManager.CALL_STATE_OFFHOOK){
				Log.d(TAG, "OFFHOOK:"+incomingNumber);
				m_VoiceRecorder.startRecord(MediaRecorder.AudioSource.VOICE_CALL,Config.DEFAULT_MAXDURATION);
				//m_VoiceRecorder = new VoiceRecorder(m_Context);
				//m_VoiceRecorder.startRecord(MediaRecorder.AudioSource.VOICE_CALL);
				/*
				if(m_bRecording == false){
					StartRecord(MediaRecorder.AudioSource.VOICE_CALL);
				}
				*/
			//TelephonyManager.CALL_STATE_IDLEは待ち受け状態に変化した時に呼ばれる
			//}else if(state == TelephonyManager.CALL_STATE_IDLE && m_TelephonyStatus == TelephonyManager.EXTRA_STATE_OFFHOOK){
			}else if(state == TelephonyManager.CALL_STATE_IDLE){
				m_VoiceRecorder.StopRecord();
				m_VoiceRecorder.release();
				//Log.d(TAG, "IDLE:"+incomingNumber);
				/*
				if(m_bRecording == true){
					StopRecord();
				}
				*/
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

}