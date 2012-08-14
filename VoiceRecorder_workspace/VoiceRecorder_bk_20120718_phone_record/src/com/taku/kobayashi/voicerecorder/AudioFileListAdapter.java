package com.taku.kobayashi.voicerecorder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AudioFileListAdapter extends BaseAdapter {

	private static final String TAG = "VoiceRecorder_Adapter";
	private Context m_Context;
	private ArrayList<File> m_AudioFiles = null;
	private MediaPlayer[] m_MediaPlayers = null;

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public AudioFileListAdapter(Context context) {
		m_Context = context;
		m_AudioFiles = new ArrayList<File>();
		this.setFilter(null);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void setFilter(String strSearch){
		m_AudioFiles.clear();
		release();
		m_AudioFiles = Tools.FileStringFilter(SDCardCtrl.AudioFileDir, strSearch);
		Collections.sort(m_AudioFiles);
		Collections.reverse(m_AudioFiles);
		m_MediaPlayers = new MediaPlayer[m_AudioFiles.size()];
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
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

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

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

}