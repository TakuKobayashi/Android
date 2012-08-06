package com.taku.kobayashi.voicerecorder;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.AdapterView.OnItemSelectedListener;

public class VoiceRecorderActivity extends Activity {

	private static final String TAG ="VoiceRecorder_VoiceRecoderActivity";
	private static final int TIMER_MENU_ID = Menu.FIRST;
	private static final int SEARCH_MENU_ID = Menu.FIRST + 1;
	private static final int SETTING_MENU_ID = Menu.FIRST + 2;
	private static final int DELETEFILES_MENU_ID = Menu.FIRST + 3;
	//private static final int DIALOG_DISMISS_TIME = 1000; // Millisecond

	//private MediaRecorder m_Recorder;
	private VoiceRecorder m_VoiceRecorder;

	private Button m_RecordButton;
	private boolean m_bRecording;
//	private AudioManager m_AudioManager;
	//private ProgressDialog m_ProgressDialog = null;
//	private int m_nOtherVolume;
//	private int m_nRecordVolume;
	private int m_nFileNum = 0;
	private AudioFileListAdapter m_Adapter;
	//private MediaPlayer m_MediaPlayer;
	private VoiceRecordCounterView m_VoiceRecordCounterView;
	//private String m_strSaveFilePath = null;
	private EditText m_TimerText;
	private int m_nPosition;

	private Handler m_Handler;

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.voicerecorderview);

		m_RecordButton = (Button) findViewById(R.id.VoiceRecordButton);
		m_RecordButton.setText(R.string.RecordStartButtonText);
		m_RecordButton.setOnClickListener(m_RecodeListener);

		TextView RecordingTimeText = (TextView) findViewById(R.id.RecordingTimeText);
		m_Adapter = new AudioFileListAdapter(this);
		ListView AudioFileList = (ListView) findViewById(R.id.AudioFileList);
		AudioFileList.setAdapter(m_Adapter);
		AudioFileList.setOnItemClickListener(m_AudioListListener);

		m_VoiceRecordCounterView = (VoiceRecordCounterView) findViewById(R.id.RecordTimeCounter);
		m_VoiceRecordCounterView.Init(RecordingTimeText);
		//m_Recorder = new MediaRecorder();
		m_VoiceRecorder = new VoiceRecorder(this);
		//SharedPreferences sp = getSharedPreferences(getResources().getString(R.string.PreferenceName), MODE_PRIVATE);
		//m_AudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		//m_nRecordVolume = sp.getInt(getResources().getString(R.string.RecordVolumeKey), m_AudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		//m_nOtherVolume = m_AudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		m_bRecording = false;
		m_nPosition = 1;

	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private OnClickListener m_RecodeListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(SDCardCtrl.checkSDCard(VoiceRecorderActivity.this) == true){
				if(m_bRecording == false){
					StartRecord(Config.DEFAULT_MAXDURATION);
				}else{
					StopRecord();
				}
			}
		}
	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/*
	private OnInfoListener m_MediaRecorderInfoListener = new OnInfoListener() {
		@Override
		public void onInfo(MediaRecorder mr, int what, int extra) {
			if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED){
				StopRecord();
				Tools.showToast(VoiceRecorderActivity.this, VoiceRecorderActivity.this.getResources().getString(R.string.NoAvailableSpaceMessage));
			}else if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){
				StopRecord();
				Tools.showToast(VoiceRecorderActivity.this, VoiceRecorderActivity.this.getResources().getString(R.string.TimeoutMessage));
			}
		}
	};
	*/

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/*
	private int getTimer(int Time,int position){
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

	private void StopRecord(){
		//m_bRecording = false;
		m_bRecording = m_VoiceRecorder.StopRecord();
		if(isFinishing() == false){
			m_RecordButton.setText(R.string.RecordStartButtonText);
			//m_Recorder.stop();
			//m_Recorder.reset();
			m_Adapter.setFilter(null);
			m_VoiceRecordCounterView.CounterControll(true);
			ChangeFileNameDialog(VoiceRecorderActivity.this.getResources().getString(R.string.SaveFileNameTitle),VoiceRecorderActivity.this.getResources().getString(R.string.DialogSaveButton),VoiceRecorderActivity.this.getResources().getString(R.string.DialogDefaultSaveButton));
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private void StartRecord(int nDuration){
		//m_bRecording = true;
		MediaRecorder mr = new MediaRecorder();
		Log.d(TAG, "Start:"+String.valueOf(nDuration));
		m_VoiceRecorder.setRecord(mr,MediaRecorder.AudioSource.MIC,nDuration);
		//ファイル保存容量の限界、ファイル保存時間の限界等が訪れた時呼ばれる。
		mr.setOnInfoListener(m_MediaRecorderInfoListener);
		m_bRecording = m_VoiceRecorder.startRecord();
		//m_bRecording = m_VoiceRecorder.startRecord(MediaRecorder.AudioSource.MIC,nDuration);
		if(isFinishing() == false){
			m_RecordButton.setText(R.string.RecordStopButtonText);
			m_VoiceRecordCounterView.CounterControll(false);
		}
		/*
		SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(VoiceRecorderActivity.this);
		String format = setting.getString(VoiceRecorderActivity.this.getResources().getString(R.string.SettingFormatKey), ".wav");
		int nOutput = Tools.getOutputFormat(format);
		m_Recorder.setAudioSource(AudioSource);
		//オーディオファイルの出力フォーマット
		m_Recorder.setOutputFormat(nOutput);
		//高音質処理
		if(Build.VERSION.SDK_INT >= 10){
			String SamplingRate = setting.getString(VoiceRecorderActivity.this.getResources().getString(R.string.SettingAudioSamplingRateKey), String.valueOf(Config.MIN_AUDIOSAMPLINGRATE));
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

		int nMaxDuration = setting.getInt(VoiceRecorderActivity.this.getResources().getString(R.string.SettingMaxDurationKey), Config.DEFAULT_MAXDURATION);
		int nTime = setting.getInt(VoiceRecorderActivity.this.getResources().getString(R.string.SettingTimeKey), 0);
		nMaxDuration = getTimer(nMaxDuration, nTime);
		//Timer == -1 で無限大
		if(nMaxDuration > 0 && AudioSource == MediaRecorder.AudioSource.MIC){
			m_Recorder.setMaxDuration(nMaxDuration);
		}
		//ファイル保存容量の限界、ファイル保存時間の限界等が訪れた時呼ばれる。
		m_Recorder.setOnInfoListener(m_MediaRecorderInfoListener);
		//オーディオファイルの出力先のパス
		m_strSaveFilePath = Tools.getFilePath(format);
		m_Recorder.setOutputFile(m_strSaveFilePath);
		try {
			m_Recorder.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		m_Recorder.start();
		*/
		//m_VoiceRecordCounterView.CounterControll(false);
	}

	private OnInfoListener m_MediaRecorderInfoListener = new OnInfoListener() {
		@Override
		public void onInfo(MediaRecorder mr, int what, int extra) {
			if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED){
				StopRecord();
				Tools.showToast(VoiceRecorderActivity.this, VoiceRecorderActivity.this.getResources().getString(R.string.NoAvailableSpaceMessage));
			}else if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){
				StopRecord();
				Tools.showToast(VoiceRecorderActivity.this, VoiceRecorderActivity.this.getResources().getString(R.string.TimeoutMessage));
			}
		}
	};

	/*
	private void RecordPhone(){
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		//PhoneStateListener.LISTEN_CALL_STATEで通話状態を取得する
		tm.listen(m_PhoneStateLister, PhoneStateListener.LISTEN_CALL_STATE);
	}

	private PhoneStateListener m_PhoneStateLister = new PhoneStateListener(){
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			//TelephonyManager.CALL_STATE_OFFHOOKは通話開始時に呼ばれる
			if(state == TelephonyManager.CALL_STATE_OFFHOOK){
				if(m_bRecording == false){
					StartRecord(MediaRecorder.AudioSource.VOICE_CALL);
				}
			//TelephonyManager.CALL_STATE_IDLEは待ち受け状態に変化した時に呼ばれる
			}else if(state == TelephonyManager.CALL_STATE_IDLE){
				if(m_bRecording == true){
					StopRecord();
				}
			}
		};
	};
	*/

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private void ReservationRecordDialog(){
		LayoutInflater factory = LayoutInflater.from(this);
		View textEntryView = factory.inflate(R.layout.settingtimerdialog, null);
		Date now = new Date(System.currentTimeMillis());
		final DatePicker datePicker = (DatePicker) textEntryView.findViewById(R.id.datePicker);
		datePicker.updateDate(now.getYear(), now.getMonth(), now.getDate());
		final TimePicker timePicker = (TimePicker) textEntryView.findViewById(R.id.timePicker);
		timePicker.setIs24HourView(true);
		timePicker.setCurrentHour(now.getHours());
		timePicker.setCurrentMinute(now.getMinutes());

		m_TimerText = (EditText) textEntryView.findViewById(R.id.TimerDuration);
		final TextView UnitText = (TextView) textEntryView.findViewById(R.id.Unit);
		UnitText.setText(R.string.DialogSecondText);
		Spinner MaxDurationSpinner = (Spinner) textEntryView.findViewById(R.id.TimerSpinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.SettingTimerSpinner, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		MaxDurationSpinner.setAdapter(adapter);
		MaxDurationSpinner.setSelection(0,true);
		MaxDurationSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if(position == 0){
					UnitText.setText(R.string.DialogSecondText);
				}else if(position == 1){
					UnitText.setText(R.string.DialogMiniuteText);
				}else if(position == 2){
					UnitText.setText(R.string.DialogHourText);
				}
				m_nPosition = position + 1;
			}

			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(getResources().getString(R.string.TimerRecordTitle));
		alertDialogBuilder.setView(textEntryView);
		alertDialogBuilder.setPositiveButton(R.string.DialogSettingButton, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Date da = new Date(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), timePicker.getCurrentHour(), timePicker.getCurrentMinute(), 0);
				long SettingTimer = da.getTime();
				//long SettingTimer = Date.UTC(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), timePicker.getCurrentHour(), timePicker.getCurrentMinute(), 0);
				Log.d(TAG,SettingTimer +":"+System.currentTimeMillis());
				Log.d(TAG,datePicker.getYear()+"-"+datePicker.getMonth()+"-"+datePicker.getDayOfMonth()+" "+timePicker.getCurrentHour()+":"+timePicker.getCurrentMinute()+":"+"0");
				if(System.currentTimeMillis() < SettingTimer){
					RecordTimer((SettingTimer - System.currentTimeMillis()));
				}else{
					String strSetTimer = m_TimerText.getText().toString();
					if(strSetTimer.length() == 0){
						Tools.showToast(VoiceRecorderActivity.this,VoiceRecorderActivity.this.getResources().getString(R.string.EditTextEmptyMessage));
					}else{
						Tools.showToast(VoiceRecorderActivity.this, VoiceRecorderActivity.this.getResources().getString(R.string.SettingTimerErrorMessage));
					}
				}
			}
		});
		alertDialogBuilder.setNegativeButton(R.string.DialogCancelButton, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});
		alertDialogBuilder.setCancelable(true);
		alertDialogBuilder.create().show();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private void RecordTimer(long time){
		m_Handler = new Handler();
		Timer timer = new Timer(false);
		TimerTask ts = new TimerTask() {
			@Override
			public void run() {
				m_Handler.post(new Runnable() {
					@Override
					public void run() {
						//ここに時間が来たときに実行する処理を記入する
						String strSetTimer = m_TimerText.getText().toString();
						Log.d(TAG, "strSetTimer:"+strSetTimer);
						if(strSetTimer.length() == 0){
							Tools.showToast(VoiceRecorderActivity.this,VoiceRecorderActivity.this.getResources().getString(R.string.EditTextEmptyMessage));
						}else{
							try{
								int num = Integer.parseInt(strSetTimer);
								if(num > 0){
									//ここにタイマーによる録音開始処理
									num = Tools.getMaxDuration(num, m_nPosition);
									Log.d(TAG, "Click"+String.valueOf(num));
									StartRecord(num);
									//m_VoiceRecorder.startRecord(MediaRecorder.AudioSource.MIC, num);
								}else{
									Tools.showToast(VoiceRecorderActivity.this,VoiceRecorderActivity.this.getResources().getString(R.string.EditTextUnder0Message));
								}
							} catch (NumberFormatException e) {
								Tools.showToast(VoiceRecorderActivity.this,VoiceRecorderActivity.this.getResources().getString(R.string.SettingNonNumberMessage));
							}
						}
					}
				});
			}
		};
		Log.d(TAG, ""+time);
		//一回だけ
		timer.schedule(ts,time);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private AdapterView.OnItemClickListener m_AudioListListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> l, View v, int position,long id) {
			if(m_bRecording == false){
				m_nFileNum = position;
				ChoiceAudioFileDialog();
			}
		}
	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//バックボタンを押したときの処理
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(m_bRecording == true){
				StopRecord();
			}else{
				finish();
			}
		}
		/*
		else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
			m_nRecordVolume++;
			int nMaxVolume = m_AudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			if(nMaxVolume < m_nRecordVolume){
				m_nRecordVolume = nMaxVolume;
			}
			Tools.setVolume(this, m_AudioManager, m_nRecordVolume);
			//AudioVolumeDialog();
			//true:他のKeyEventを取得できないようにする
			return true;
		}else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
			m_nRecordVolume--;
			if(0 > m_nRecordVolume){
				m_nRecordVolume = 0;
			}
			Tools.setVolume(this, m_AudioManager, m_nRecordVolume);
			//AudioVolumeDialog();
			//true:他のKeyEventを取得できないようにする
			return true;
		}
		*/
		return false;
		//boolean bEvent = Tools.VolumeKeyEvent(this, keyCode, m_AudioManager, m_nRecordVolume);
		//return bEvent;


	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, TIMER_MENU_ID, 0, getResources().getString(getResources().getIdentifier("MenuText"+TIMER_MENU_ID, "string", getPackageName())));
		menu.add(0, SEARCH_MENU_ID, 0, getResources().getString(getResources().getIdentifier("MenuText"+SEARCH_MENU_ID, "string", getPackageName())));
		menu.add(0, SETTING_MENU_ID, 0, getResources().getString(getResources().getIdentifier("MenuText"+SETTING_MENU_ID, "string", getPackageName())));
		menu.add(0, DELETEFILES_MENU_ID, 0, getResources().getString(getResources().getIdentifier("MenuText"+DELETEFILES_MENU_ID, "string", getPackageName())));
		return true;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		return true;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case TIMER_MENU_ID:
				ReservationRecordDialog();
				return true;

			case SEARCH_MENU_ID:
				SearchFileDialog();
				return true;

			case SETTING_MENU_ID:
				Intent intent = new Intent(this,SettingActivity.class);
				startActivity(intent);
				return true;

			case DELETEFILES_MENU_ID:

				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/*
	private void setVolume(){
		m_AudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, m_nRecordVolume, AudioManager.FLAG_SHOW_UI);

		SharedPreferences settings = getSharedPreferences(getResources().getString(R.string.PreferenceName), 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(getResources().getString(R.string.SettingRecordVolumeKey), m_nRecordVolume);
		editor.commit();
	}
	*/

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private void ChoiceAudioFileDialog(){
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(getResources().getString(R.string.ChoiceRecordFileListTitle));
		alertDialogBuilder.setItems(R.array.ChioceAudioFile, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(SDCardCtrl.checkSDCard(VoiceRecorderActivity.this) == true){
					if(which == 0){
						Intent intent = new Intent(VoiceRecorderActivity.this,MeidaPlayerActivity.class);
						intent.putExtra(VoiceRecorderActivity.this.getResources().getString(R.string.IntentAudioFilePathKey), m_Adapter.getFile(m_nFileNum).getPath());
						startActivity(intent);
					}else if(which == 1){
						Intent audioIntent = new Intent(Intent.ACTION_SEND);
						//音声を添付するために必要
						audioIntent.setType("audio/*");
						//音声へのパス
						audioIntent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(m_Adapter.getFile(m_nFileNum)));
						startActivity(audioIntent);
					}else if(which == 2){
						ChangeFileNameDialog(VoiceRecorderActivity.this.getResources().getString(R.string.ChangeFileNameTitle),VoiceRecorderActivity.this.getResources().getString(R.string.DialogChangeButton),VoiceRecorderActivity.this.getResources().getString(R.string.DialogCancelButton));
					}else if(which == 3){
						CheckDeleteFileDialog();
					}
				}
			}
		});
		alertDialogBuilder.setCancelable(true);
		alertDialogBuilder.create().show();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private void ChangeFileNameDialog(String Title,String PositiveButton,String NegativeButton){
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.edittextdialog, null);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		//alertDialogBuilder.setTitle(getResources().getString(R.string.ChoiceRecordFileListTitle));
		alertDialogBuilder.setTitle(Title);
		alertDialogBuilder.setView(textEntryView);
		alertDialogBuilder.setPositiveButton(PositiveButton, new DialogInterface.OnClickListener() {
		//alertDialogBuilder.setPositiveButton(R.string.DialogChangeButton, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				if(SDCardCtrl.checkSDCard(VoiceRecorderActivity.this) == true){
					EditText text = (EditText) textEntryView.findViewById(R.id.InputText);
					String str = text.getText().toString();
					boolean sucess = Tools.RenameFile(m_Adapter.getFile(m_nFileNum),str);
					if(sucess == false){
						Tools.showToast(VoiceRecorderActivity.this, VoiceRecorderActivity.this.getResources().getString(R.string.AudioFileRenameFailedMessage));
					}
				}
			}
		});
		alertDialogBuilder.setNegativeButton(NegativeButton, new DialogInterface.OnClickListener() {
		//alertDialogBuilder.setNegativeButton(R.string.DialogCancelButton, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});
		alertDialogBuilder.setCancelable(true);
		alertDialogBuilder.create().show();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private void CheckDeleteFileDialog(){
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setMessage(R.string.CheckFileDeleteMessage);
		alertDialogBuilder.setPositiveButton(R.string.DialogOKButton, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				m_Adapter.deleteFile(m_nFileNum);
			}
		});
		alertDialogBuilder.setNegativeButton(R.string.DialogCancelButton, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});
		alertDialogBuilder.setCancelable(true);
		alertDialogBuilder.create().show();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private void SearchFileDialog(){
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.edittextdialog, null);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(getResources().getString(R.string.DialogSearchButton));
		alertDialogBuilder.setView(textEntryView);
		alertDialogBuilder.setPositiveButton(R.string.DialogSearchButton, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				if(SDCardCtrl.checkSDCard(VoiceRecorderActivity.this) == true){
					EditText text = (EditText) textEntryView.findViewById(R.id.InputText);
					String str = text.getText().toString();
					m_Adapter.setFilter(str);
				}
			}
		});
		alertDialogBuilder.setNegativeButton(R.string.DialogCancelButton, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});
		alertDialogBuilder.setCancelable(true);
		alertDialogBuilder.create().show();
	}



	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG,"onStart");
		//Tools.setVolume(this,m_AudioManager,m_nRecordVolume);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onResume() {
		super.onResume();
		SDCardCtrl.checkSDCard(this);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG,"onStop");
		//m_AudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, m_nOtherVolume, AudioManager.FLAG_SHOW_UI);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG,"onDestroy");
		/*
		if(m_bRecording == true){
			m_Recorder.stop();
			m_Recorder.reset();
		}
		m_Recorder.release();
		*/
		m_VoiceRecorder.release();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

}