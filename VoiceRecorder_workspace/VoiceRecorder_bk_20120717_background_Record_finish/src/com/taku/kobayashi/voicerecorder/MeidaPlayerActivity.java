package com.taku.kobayashi.voicerecorder;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MeidaPlayerActivity extends Activity {

	private static final String TAG ="VoiceRecoderActivity";

	private AudioManager m_AudioManager;
	private int m_nRecordVolume;
	private int m_nOtherVolume;
	private MediaPlayer m_MediaPlayer = null;
	private SeekBar m_SeekBar;
	//private MediaPlayerCounterView m_TimeCounterView;
	private TextView m_NowTimeText;
	private Button m_MediaPlayerButton;

	private Handler m_Handler;
	private Timer m_TimeCountTimer;

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mediaplayerview);

		m_AudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		m_nOtherVolume = m_AudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		if(sp.getBoolean(getResources().getString(R.string.VolumeAutoSetKey), false) == true){
			m_nRecordVolume = sp.getInt(getResources().getString(R.string.RecordVolumeKey), m_AudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		}else{
			m_nRecordVolume = m_nOtherVolume;
		}
		m_MediaPlayer = new MediaPlayer();
		if(SDCardCtrl.checkSDCard(this) == true){
			m_MediaPlayer = MediaPlayer.create(this, Uri.fromFile(new File(getIntent().getStringExtra(getResources().getString(R.string.IntentAudioFilePathKey)))));
			try {
				m_MediaPlayer.prepare();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			m_MediaPlayer.setOnCompletionListener(m_AudioLitener);
			m_MediaPlayer.start();
			m_NowTimeText = (TextView) findViewById(R.id.PlayingTimeText);

			m_SeekBar = (SeekBar) findViewById(R.id.MediaPlayerSeek);
			m_SeekBar.setOnSeekBarChangeListener(m_SeekBarListener);
			m_SeekBar.setMax(m_MediaPlayer.getDuration());

			TextView EndTimeText = (TextView) findViewById(R.id.EndTimeText);
			EndTimeText.setText(Tools.ConversionTime(m_MediaPlayer.getDuration()));

			//m_TimeCounterView = (MediaPlayerCounterView) findViewById(R.id.PlayingTimeCounter);
			//m_TimeCounterView.Init(m_MediaPlayer, m_SeekBar,m_NowTimeText);
		}
		m_MediaPlayerButton = (Button) findViewById(R.id.MediaPlayerButton);
		m_MediaPlayerButton.setOnClickListener(m_MediaPlayerClickListener);
		m_MediaPlayerButton.setText(R.string.AudioPlayerPauseButtonText);

		m_TimeCountTimer = new Timer(true);
		m_Handler = new Handler();
		settingCountTimer();
		//setMutableView(m_bRecording);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private OnClickListener m_MediaPlayerClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(m_MediaPlayer.isPlaying() == true){
				m_MediaPlayer.pause();
				m_MediaPlayerButton.setText(R.string.AudioPlayerStartButtonText);
			}else{
				m_MediaPlayer.start();
				m_MediaPlayerButton.setText(R.string.AudioPlayerPauseButtonText);
			}
		}
	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private OnSeekBarChangeListener m_SeekBarListener = new OnSeekBarChangeListener(){

		private boolean m_Playing;

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
			seekBar.setProgress(progress);
			m_MediaPlayer.seekTo(progress);
			m_NowTimeText.setText(Tools.ConversionTime(progress));
			Log.d(TAG,String.valueOf(progress));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			m_Playing = m_MediaPlayer.isPlaying();
			if(m_Playing == true){
				m_MediaPlayer.pause();
				m_MediaPlayerButton.setText(R.string.AudioPlayerStartButtonText);
			}
			//m_TimeCounterView.CounterControll(true);
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			if(m_Playing == true){
				m_MediaPlayer.start();
				m_Playing = false;
				m_MediaPlayerButton.setText(R.string.AudioPlayerPauseButtonText);
			}
			//sm_TimeCounterView.CounterControll(false);
		}
	};

	/*
	private void setMutableView(boolean bRecording){
		if(bRecording == true){
			m_RecordButton.setText(R.string.RecordStopButtonText);
			settingCountTimer(bRecording);
		}else{
			m_RecordButton.setText(R.string.RecordStartButtonText);
			m_Adapter.setFilter(null);
			settingCountTimer(bRecording);
		}
	}
	*/

	private void settingCountTimer(){
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				m_Handler.post(new Runnable() {
					@Override
					public void run() {
						if(m_MediaPlayer != null){
							if(m_MediaPlayer.isPlaying() == true){
								m_NowTimeText.setText(Tools.ConversionTime(m_MediaPlayer.getCurrentPosition()));
								m_SeekBar.setProgress(m_MediaPlayer.getCurrentPosition());
							}
						}
					}
				});
			}
		};
		m_TimeCountTimer.schedule(task, 0, 1);	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private OnCompletionListener m_AudioLitener = new OnCompletionListener(){
		@Override
		public void onCompletion(MediaPlayer mp) {
			finish();
		}
	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			finish();
		}else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
			m_nRecordVolume++;
			int nMaxVolume = m_AudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			if(nMaxVolume < m_nRecordVolume){
				m_nRecordVolume = nMaxVolume;
			}
			if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getResources().getString(R.string.VolumeAutoSetKey), false) == true){
				Tools.setVolume(this, m_AudioManager, m_nRecordVolume);
			}else{
				m_AudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, m_nRecordVolume, AudioManager.FLAG_SHOW_UI);
			}
			//true:他のKeyEventを取得できないようにする
			return true;
		}else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
			m_nRecordVolume--;
			if(0 > m_nRecordVolume){
				m_nRecordVolume = 0;
			}
			if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getResources().getString(R.string.VolumeAutoSetKey), false) == true){
				Tools.setVolume(this, m_AudioManager, m_nRecordVolume);
			}else{
				m_AudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, m_nRecordVolume, AudioManager.FLAG_SHOW_UI);
			}
			//true:他のKeyEventを取得できないようにする
			return true;
		}
		return false;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onStart() {
		super.onStart();
		if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getResources().getString(R.string.VolumeAutoSetKey), false) == true){
			Tools.setVolume(this,m_AudioManager,m_nRecordVolume);
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onPause() {
		super.onPause();
		if(m_MediaPlayer.isPlaying() == true){
			m_MediaPlayer.pause();
			m_MediaPlayerButton.setText(R.string.AudioPlayerStartButtonText);
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onStop() {
		super.onStop();
		if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getResources().getString(R.string.VolumeAutoSetKey), false) == true){
			m_AudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, m_nOtherVolume, AudioManager.FLAG_SHOW_UI);
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(m_MediaPlayer != null){
			//Timerスレッド内でMediaPlayerを使っているため、まずTimerスレッドを初期化するのを先にしないとエラーになる。
			m_TimeCountTimer.cancel();
			m_TimeCountTimer.purge();
			m_TimeCountTimer = null;
			if(m_MediaPlayer.isPlaying() == true){
				m_MediaPlayer.stop();
			}
			m_MediaPlayer.release();
			m_MediaPlayer = null;
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

}