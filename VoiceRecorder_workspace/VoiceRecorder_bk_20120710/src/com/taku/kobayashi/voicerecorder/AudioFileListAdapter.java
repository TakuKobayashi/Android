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
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AudioFileListAdapter extends BaseAdapter {

	private static final String TAG = "VoiceRecorder_Adapter";
	private Context m_Context;
	private ArrayList<File> m_AudioFiles = null;
	private MediaPlayer[] m_MediaPlayers = null;
	//private MediaPlayer m_SelectMedia = null;

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public AudioFileListAdapter(Context context) {
		m_Context = context;
		//SharedPreferences sp = m_Context.getSharedPreferences(m_Context.getResources().getString(R.string.PreferenceName), m_Context.MODE_PRIVATE);
		m_AudioFiles = new ArrayList<File>();
		this.setFilter(null);

		/*
		m_AudioFiles = Tools.FileStringFilter(SDCardCtrl.AudioFileDir, null , null);
		Collections.sort(m_AudioFiles);
		Collections.reverse(m_AudioFiles);
		setAudio();
		*/
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/*
	private void setAudio(){
		m_MediaPlayers = new MediaPlayer[m_AudioFiles.size()];
		int nLength = m_AudioFiles.size();
		for(int i = 0;i < nLength;i++){
			m_MediaPlayers[i] = MediaPlayer.create(m_Context, Uri.fromFile(m_AudioFiles.get(i)));
		}
	}
	*/

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void setFilter(String strSearch){
		m_AudioFiles.clear();
		release();
		m_AudioFiles = Tools.FileStringFilter(SDCardCtrl.AudioFileDir, strSearch);
		Collections.sort(m_AudioFiles);
		Collections.reverse(m_AudioFiles);
		m_MediaPlayers = new MediaPlayer[m_AudioFiles.size()];
		Log.d(TAG, ""+m_AudioFiles);
		int nLength = m_AudioFiles.size();
		for(int i = 0;i < nLength;i++){
			m_MediaPlayers[i] = MediaPlayer.create(m_Context, Uri.fromFile(m_AudioFiles.get(i)));
		}
		this.notifyDataSetChanged();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public int getCount() {
		return m_AudioFiles.size();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public Object getItem(int position) {
		return position;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public long getItemId(int position) {
		return position;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if(convertView == null){
			convertView = ((Activity) m_Context).getLayoutInflater().inflate(R.layout.audiofilelistcellview, null);
		}
		TextView FileName = (TextView) convertView.findViewById(R.id.VoiceRecordFileName);
		FileName.setText(m_AudioFiles.get(position).getName());

		TextView FileDate = (TextView) convertView.findViewById(R.id.VoiceRecordFileDate);
		FileDate.setText(DateFormat.format("yyyy-MM-dd kk:mm:ss",m_AudioFiles.get(position).lastModified()));

		TextView FileLength = (TextView) convertView.findViewById(R.id.VoiceRecordFileLength);
		if(m_MediaPlayers[position] != null){
			FileLength.setText(Tools.ConversionTime(m_MediaPlayers[position].getDuration()));
		}

		return convertView;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public File getFile(int nFileNum){
		return m_AudioFiles.get(nFileNum);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void deleteFile(int nFileNum){
		m_AudioFiles.get(nFileNum).delete();
		setFilter(null);
		//m_AudioFiles.remove(nFileNum);
		//this.notifyDataSetChanged();
	}

	public int SearchFiles(File file){
		return m_AudioFiles.indexOf(file);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void release(){
		int nLength = m_AudioFiles.size();
		for(int i = 0;i < nLength;i++){
			if(m_MediaPlayers[i] != null){
				m_MediaPlayers[i].release();
				m_MediaPlayers[i] = null;
			}
		}
	}


	/*
	public void AudioPlay(int nFileNum){
		m_SelectMedia = new MediaPlayer();
		m_SelectMedia = m_MediaPlayers[nFileNum];
		//m_MediaPlayer = MediaPlayer.create(this, Uri.fromFile(m_AudioFile));
		try {
			m_SelectMedia.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		m_SelectMedia.setOnCompletionListener(new OnCompletionListener(){
			@Override
			public void onCompletion(MediaPlayer mp) {
				mp.stop();
			}
		});
		m_SelectMedia.start();
	}

	public void AudioPauseORestart(){
		if(m_SelectMedia.isPlaying() == true){
			m_SelectMedia.pause();
		}else{
			m_SelectMedia.start();
		}
	}

	public void AudioStop(){
		if(m_SelectMedia.isPlaying() == true){
			m_SelectMedia.stop();
		}
	}
	*/

}