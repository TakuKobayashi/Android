package com.taku.kobayashi.voicerecorder;

import android.content.Context;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class MediaPlayerCounterView extends View {

	private boolean m_CountStop;
	private MediaPlayer m_MediaPlayer;
	private SeekBar m_SeekBar;
	private TextView m_NowTimeText;

	// ---------------------------------------------------------------------------------------------------------------------------------------------------------------
	public MediaPlayerCounterView(Context context,AttributeSet attrs) {
		super(context,attrs);
	}

	// ---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void Init(MediaPlayer mp,SeekBar sb,TextView tv){
		m_MediaPlayer = mp;
		m_SeekBar = sb;
		m_NowTimeText = tv;
		m_CountStop = false;
	}

	// ---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void CounterControll(boolean bCountStop){
		m_CountStop = bCountStop;
		if(m_CountStop == false){
			this.invalidate();
		}
	}

	// ---------------------------------------------------------------------------------------------------------------------------------------------------------------
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		m_SeekBar.setProgress(m_MediaPlayer.getCurrentPosition());
		m_NowTimeText.setText(Tools.ConversionTime(m_MediaPlayer.getCurrentPosition()));
		if(m_CountStop == false){
			this.invalidate();
		}
	}

	// ---------------------------------------------------------------------------------------------------------------------------------------------------------------
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}

	// ---------------------------------------------------------------------------------------------------------------------------------------------------------------
}