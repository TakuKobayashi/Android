package com.taku.kobayashi.voicerecorder;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Timer;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

public class VoiceRecorder{

	private static final String TAG = "VoiceRecorder_VoiceRecorder";
	private Context m_Context;
	private MediaRecorder m_Recorder = null;
	private boolean m_bRecording;
	//private AudioRecord m_AudioRecord;
	private SharedPreferences m_SharedPreferences;
	private Thread m_Thread;
	//private Timer m_FinishTimer = null;
	private Handler m_Handler;

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public VoiceRecorder(Context context){
		m_Context = context;
		m_bRecording = false;
		m_SharedPreferences = PreferenceManager.getDefaultSharedPreferences(m_Context);
		m_Handler = new Handler();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void setRecord(MediaRecorder mr,int AudioSource,int nDurationTime){
		m_Recorder = mr;
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
		//m_Recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		Log.d(TAG,String.valueOf(nRate));
		m_Recorder.setAudioSamplingRate(nRate);

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

	public boolean StartRecord(){
		if(m_bRecording == false){
			m_bRecording = true;
			EditRecordingStatus(m_bRecording);
			try {
				m_Recorder.prepare();
			} catch (IllegalStateException e) {
				Log.d(TAG, "ERROR:IllegalStateException");
				e.printStackTrace();
			} catch (IOException e) {
				Log.d(TAG, "ERROR:IOException");
				e.printStackTrace();
			}
			m_Recorder.start();
			//Thread(Runnable)は処理部分のマルチスレッド,m_Handler.postは表示に反映させるために必要
			m_Handler.post(new Runnable() {
				@Override
				public void run() {
					Tools.showToast(m_Context, m_Context.getResources().getString(R.string.RecordStartMessage));
				}
			});
		}
		return true;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/*
	private void showToastMessage(final Integer nMessageStringID){
		boolean bBackGround = m_SharedPreferences.getBoolean(m_Context.getResources().getString(R.string.BackgroundRecordKey), false);
		if(bBackGround == true){
			m_Handler.post(new Runnable() {
				@Override
				public void run() {
					Tools.showToast(m_Context, m_Context.getResources().getString(nMessageStringID));
				}
			});
		}else{
			Tools.showToast(m_Context, m_Context.getResources().getString(nMessageStringID));
		}
	}
	*/

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public boolean StopRecord(){
		if(m_Recorder != null){
			m_bRecording = false;
			EditRecordingStatus(m_bRecording);
			m_Recorder.stop();
			m_Recorder.reset();
			Log.d(TAG,"RecordRelease");
		}else{
			m_bRecording = false;
			EditRecordingStatus(m_bRecording);
		}
		//バックグラウンドであってもToastを表示させるため
		m_Handler.post(new Runnable() {
			@Override
			public void run() {
				Tools.showToast(m_Context, m_Context.getResources().getString(R.string.RecordStopMessage));
			}
		});
		return false;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private void EditRecordingStatus(boolean Recording){
		SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(m_Context);
		SharedPreferences.Editor editor = setting.edit();
		//Booleanの変化のリスナーはLongの後に取得したいため、順番を変えてはだめ
		editor.putLong(m_Context.getResources().getString(R.string.RecordStartTimeKey), System.currentTimeMillis());
		editor.putBoolean(m_Context.getResources().getString(R.string.RecordingKey), Recording);
		editor.commit();
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

	//バックグラウンド録音はスタートボタンを押したときのみ実行される
	public boolean BackgroundStartRecord(){
		m_Handler = new Handler();
		m_Thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "Startback");
				StartRecord();
				while(m_bRecording == true) {
					//録音中のxmlを削除した場合falseになり処理を抜ける
					m_bRecording = m_SharedPreferences.getBoolean(m_Context.getResources().getString(R.string.RecordingKey),false);
				}
				StopRecord();
				Log.d(TAG, "Stoptback");
				//releaseしないと再度マルチスレッドで録音を行ったときにMediaRecorderが残っていて、例外処理が発生する
				release();
				//m_Thread.stop();
			}
		});
		//突然スレッドが終了した時のハンドラ
		m_Thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler(){
			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				Log.d(TAG, "UncaughtException!!!!");
				StopRecord();
			}
		});
		m_Thread.setDaemon(true);
		m_Thread.start();
		return true;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public boolean BackgroundStopRecord(){
		m_bRecording = false;
		EditRecordingStatus(m_bRecording);
		return false;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void release(){
		if(m_Recorder != null){
			Log.d(TAG,"RecordRelease");
			if(m_bRecording == true){
				m_Recorder.stop();
				m_Recorder.reset();
				EditRecordingStatus(false);
			}
			m_Recorder.release();
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/*
	//AudioRecordバージョン
	public boolean BackgroundStartRecord(){
		m_Handler = new Handler();
		int nMaxDuration = m_SharedPreferences.getInt(m_Context.getResources().getString(R.string.SettingMaxDurationKey), Config.DEFAULT_MAXDURATION);
		if(nMaxDuration == Config.DEFAULT_MAXDURATION){
			BackGroundRecordFinishTimer(nMaxDuration);
		}
		m_Thread = new Thread(new Runnable() {
			@Override
			public void run() {
				//Thread(Runnable)は処理部分のマルチスレッド,m_Handler.postは表示に反映させるために必要
				m_Handler.post(new Runnable() {
					@Override
					public void run() {
						Tools.showToast(m_Context, m_Context.getResources().getString(R.string.RecordStartMessage));
					}
				});
				StartAudioRecord();
			}
		});
		m_Thread.start();
		return true;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//WAVEに変換する処理が必要
	private void StartAudioRecord(){
		int nSamplingRate = getSamplingRate();
		int audioBuf = AudioRecord.getMinBufferSize(nSamplingRate,AudioFormat.CHANNEL_CONFIGURATION_MONO,AudioFormat.ENCODING_PCM_16BIT) * 2;
		m_AudioRecord =  new AudioRecord(MediaRecorder.AudioSource.MIC,getSamplingRate(),AudioFormat.CHANNEL_CONFIGURATION_MONO,AudioFormat.ENCODING_PCM_16BIT,audioBuf);
		// バッファ
		byte[] buffer = new byte[audioBuf];
		// 録音用ファイル取得  SDカード状態チェック略
		String path = Tools.getFilePath(m_SharedPreferences.getString(m_Context.getResources().getString(R.string.SettingFormatKey), ".wav"));
		File recFile = new File(Tools.getFilePath(m_SharedPreferences.getString(m_Context.getResources().getString(R.string.SettingFormatKey), ".raw")));
		m_bRecording = true;
		EditRecordingStatus(m_bRecording);
		int nMaxDuration = m_SharedPreferences.getInt(m_Context.getResources().getString(R.string.SettingMaxDurationKey), Config.DEFAULT_MAXDURATION);
		if(nMaxDuration == Config.DEFAULT_MAXDURATION){
			BackGroundRecordFinishTimer(nMaxDuration);
		}
		try {
			recFile.createNewFile();
			// ファイル書き込み  例外処理略
			FileOutputStream out =  new FileOutputStream(recFile);
			// 録音開始
			m_AudioRecord.startRecording();
			// フラグが落ちるまでループ  例外処理略
			//trueの間はループし続ける
			while(m_bRecording == true) {
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
			addWavHeader(recFile,path);
		} catch (IOException e) {
			Log.d(TAG, "error");
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
		Log.d(TAG, "StopAudio");
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

	// WAVヘッダをつけて保存
	// 各種例外処理略 チェック処理略
	public void addWavHeader(File recFile,String path) {
		// 録音したファイル
		//File recFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/rec.raw");
		// WAVファイル
		File wavFile = new File(path);
		// ストリーム
		FileInputStream in;
		FileOutputStream out;
		try {
			in = new FileInputStream(recFile);
			out = new FileOutputStream(wavFile);
			byte[] header = createHeader(getSamplingRate(), (int)recFile.length());
			out.write(header);
			// 録音したファイルのバイトデータ読み込み
			int n = 0,offset = 0;
			byte[] buffer = new byte[(int)recFile.length()];
			while (offset < buffer.length && (n = in.read(buffer, offset, buffer.length - offset)) >= 0) {
				offset += n;
			}
			// バイトデータ書き込み
			out.write(buffer);
			// 終了
			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	// Wavファイルのヘッダを作成する（PCM16ビット モノラル）
	// sampleRate  サンプルレート
	// datasize データサイズ
	// これなんかもっとキレイに書けると思うが 。。 Ringroidのソースなんかキレイかも
	public static byte[] createHeader(int sampleRate, int datasize) {
		byte[] byteRIFF = {'R', 'I', 'F', 'F'};
		byte[] byteFilesizeSub8 = intToBytes((datasize + 36));  // ファイルサイズ-8バイト数
		byte[] byteWAVE = {'W', 'A', 'V', 'E'};
		byte[] byteFMT_ = {'f', 'm', 't', ' '};
		byte[] byte16bit = intToBytes(16);                  // fmtチャンクのバイト数
		byte[] byteSamplerate = intToBytes(sampleRate);     // サンプルレート
		byte[] byteBytesPerSec = intToBytes(sampleRate * 2);    // バイト/秒 = サンプルレート x 1チャンネル x 2バイト
		byte[] bytePcmMono = {0x01, 0x00, 0x01, 0x00};      // フォーマットID 1 =リニアPCM  ,  チャンネル 1 = モノラル
		byte[] byteBlockBit = {0x02, 0x00, 0x10, 0x00};     // ブロックサイズ2バイト サンプルあたりのビット数16ビット
		byte[] byteDATA = {'d', 'a', 't', 'a'};
		byte[] byteDatasize = intToBytes(datasize);         // データサイズ

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			out.write(byteRIFF);
			out.write(byteFilesizeSub8);
			out.write(byteWAVE);
			out.write(byteFMT_);
			out.write(byte16bit);
			out.write(bytePcmMono);
			out.write(byteSamplerate);
			out.write(byteBytesPerSec);
			out.write(byteBlockBit);
			out.write(byteDATA);
			out.write(byteDatasize);
		} catch (IOException e) {
			return out.toByteArray();
		}

		return out.toByteArray();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	// int型32ビットデータをリトルエンディアンのバイト配列にする
	public static byte[] intToBytes(int value) {
		byte[] bt = new byte[4];
		bt[0] = (byte)(value & 0x000000ff);
		bt[1] = (byte)((value & 0x0000ff00) >> 8);
		bt[2] = (byte)((value & 0x00ff0000) >> 16);
		bt[3] = (byte)((value & 0xff000000) >> 24);
		return bt;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------
	*/

}