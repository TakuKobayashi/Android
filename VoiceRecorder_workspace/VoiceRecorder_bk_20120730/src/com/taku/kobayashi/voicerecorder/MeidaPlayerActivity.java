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
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
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
	private TextView m_NowTimeText;
	private ImageButton m_MediaPlayerButton;

	private Handler m_Handler;
	private Timer m_TimeCountTimer = null;
	private Timer m_ButtonClickHandleTimer = null;
	private boolean m_Playing = false;

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mediaplayerview);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		m_AudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		m_nOtherVolume = m_AudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		if(sp.getBoolean(getResources().getString(R.string.VolumeAutoSetKey), false) == true){
			m_nRecordVolume = sp.getInt(getResources().getString(R.string.RecordVolumeKey), m_AudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		}else{
			m_nRecordVolume = m_nOtherVolume;
		}

		m_MediaPlayer = new MediaPlayer();
		m_Handler = new Handler();
		m_TimeCountTimer = new Timer(true);

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
			m_SeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
					seekBar.setProgress(progress);
					m_MediaPlayer.seekTo(progress);
					m_NowTimeText.setText(Tools.ConversionTime(progress));
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					m_Playing = m_MediaPlayer.isPlaying();
					if(m_Playing == true){
						m_MediaPlayer.pause();
						//m_MediaPlayerButton.setText(R.string.AudioPlayerStartButtonText);
						m_MediaPlayerButton.setImageResource(R.drawable.audioplay_icon);
					}
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					if(m_Playing == true){
						m_MediaPlayer.start();
						m_Playing = false;
						m_MediaPlayerButton.setImageResource(R.drawable.audiopause_icon);
						//m_MediaPlayerButton.setText(R.string.AudioPlayerPauseButtonText);
					}
				}
			});
			m_SeekBar.setMax(m_MediaPlayer.getDuration());

			TextView EndTimeText = (TextView) findViewById(R.id.EndTimeText);
			EndTimeText.setText(Tools.ConversionTime(m_MediaPlayer.getDuration()));
		}

		ImageButton BackToStartButton = (ImageButton) findViewById(R.id.BackToStartButton);
		BackToStartButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_MediaPlayer.seekTo(0);
				m_SeekBar.setProgress(0);
				m_NowTimeText.setText(Tools.ConversionTime(0));
			}
		});

		ImageButton PlayerBackButton = (ImageButton) findViewById(R.id.PlayerBackButton);
		//ボタンの押した時とボタンを離した時のHandleを取得するためTouchListenerにする
		PlayerBackButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN){
					if(m_MediaPlayer.isPlaying() == true){
						m_MediaPlayer.pause();
						m_Playing = true;
					}
					setControlPlayerTimer(-(m_MediaPlayer.getDuration() / Config.PLAYER_FORWARD_BACK_TIMESPAN));
					return true;
				}else if(event.getAction() == MotionEvent.ACTION_UP){
					if(m_Playing == true){
						m_MediaPlayer.start();
						m_Playing = false;
					}
					if(m_ButtonClickHandleTimer != null){
						m_ButtonClickHandleTimer.cancel();
						m_ButtonClickHandleTimer.purge();
						m_ButtonClickHandleTimer = null;
					}
					return true;
				}
				return false;
			}
		});

		m_MediaPlayerButton = (ImageButton) findViewById(R.id.MediaPlayerButton);
		m_MediaPlayerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(m_MediaPlayer.isPlaying() == true){
					m_MediaPlayer.pause();
					m_MediaPlayerButton.setImageResource(R.drawable.audioplay_icon);
				}else{
					m_MediaPlayer.start();
					m_MediaPlayerButton.setImageResource(R.drawable.audiopause_icon);
				}
			}
		});
		m_MediaPlayerButton.setImageResource(R.drawable.audiopause_icon);
		//m_MediaPlayerButton.setText(R.string.AudioPlayerPauseButtonText);

		ImageButton FastForwardButton = (ImageButton) findViewById(R.id.FastForwardButton);
		//ボタンの押した時とボタンを離した時のHandleを取得するためTouchListenerにする
		FastForwardButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN){
					if(m_MediaPlayer.isPlaying() == true){
						m_MediaPlayer.pause();
						m_Playing = true;
					}
					setControlPlayerTimer(m_MediaPlayer.getDuration() / Config.PLAYER_FORWARD_BACK_TIMESPAN);
					return true;
				}else if(event.getAction() == MotionEvent.ACTION_UP){
					if(m_Playing == true){
						m_MediaPlayer.start();
						m_Playing = false;
					}
					if(m_ButtonClickHandleTimer != null){
						m_ButtonClickHandleTimer.cancel();
						m_ButtonClickHandleTimer.purge();
						m_ButtonClickHandleTimer = null;
					}
					return true;
				}
				return false;
			}
		});
		settingCountTimer();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/*
	private OnClickListener m_MediaPlayerClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(m_MediaPlayer.isPlaying() == true){
				m_MediaPlayer.pause();
				m_MediaPlayerButton.setImageResource(R.drawable.audioplay_icon);
			}else{
				m_MediaPlayer.start();
				m_MediaPlayerButton.setImageResource(R.drawable.audiopause_icon);
			}
		}
	};
	*/

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/*
	private OnSeekBarChangeListener m_SeekBarListener = new OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
			seekBar.setProgress(progress);
			m_MediaPlayer.seekTo(progress);
			m_NowTimeText.setText(Tools.ConversionTime(progress));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			m_Playing = m_MediaPlayer.isPlaying();
			if(m_Playing == true){
				m_MediaPlayer.pause();
				//m_MediaPlayerButton.setText(R.string.AudioPlayerStartButtonText);
				m_MediaPlayerButton.setImageResource(R.drawable.audioplay_icon);
			}
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			if(m_Playing == true){
				m_MediaPlayer.start();
				m_Playing = false;
				m_MediaPlayerButton.setImageResource(R.drawable.audiopause_icon);
				//m_MediaPlayerButton.setText(R.string.AudioPlayerPauseButtonText);
			}
		}
	};
	*/

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//ボタンを押しつつけた場合の処理
	private void setControlPlayerTimer(final int nMoveTime){
		if(m_ButtonClickHandleTimer == null){
			m_ButtonClickHandleTimer = new Timer(true);
		}
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				int nSetPosition = m_MediaPlayer.getCurrentPosition() + nMoveTime;
				if(nSetPosition > m_MediaPlayer.getDuration()){
					nSetPosition = m_MediaPlayer.getDuration();
				}else if(nSetPosition < 0){
					nSetPosition = 0;
				}
				m_MediaPlayer.seekTo(nSetPosition);
				m_SeekBar.setProgress(nSetPosition);
				final String text = Tools.ConversionTime(nSetPosition);
				m_Handler.post(new Runnable() {
					@Override
					public void run() {
						m_NowTimeText.setText(text);
					}
				});
			}
		};
		m_ButtonClickHandleTimer.schedule(task, 0, Config.PRESS_BUTTON_HANDLE_TIMESPAN);

	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

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
		m_TimeCountTimer.schedule(task, 0, 1);
	}

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
			if(0 >= m_nRecordVolume){
				m_nRecordVolume = 0;
				Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
				//500ミリ秒バイブレーション起動。
				vibrator.vibrate(Config.VIBRATING_TIME);
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
		//バックグラウンド再生はさせない
		if(m_MediaPlayer.isPlaying() == true){
			m_MediaPlayer.pause();
			m_MediaPlayerButton.setImageResource(R.drawable.audioplay_icon);
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
			if(m_ButtonClickHandleTimer != null){
				m_ButtonClickHandleTimer.cancel();
				m_ButtonClickHandleTimer.purge();
				m_ButtonClickHandleTimer = null;
			}
			if(m_TimeCountTimer != null){
				m_TimeCountTimer.cancel();
				m_TimeCountTimer.purge();
				m_TimeCountTimer = null;
			}
			if(m_MediaPlayer.isPlaying() == true){
				m_MediaPlayer.stop();
			}
			m_MediaPlayer.release();
			m_MediaPlayer = null;
		}
		Tools.releaseImageView(m_MediaPlayerButton);
		Tools.releaseImageView((ImageButton) findViewById(R.id.BackToStartButton));
		Tools.releaseImageView((ImageButton) findViewById(R.id.PlayerBackButton));
		Tools.releaseImageView((ImageButton) findViewById(R.id.FastForwardButton));
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

}