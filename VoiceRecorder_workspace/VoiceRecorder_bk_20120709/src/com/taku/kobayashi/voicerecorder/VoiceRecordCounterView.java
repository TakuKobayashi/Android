package com.taku.kobayashi.voicerecorder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class VoiceRecordCounterView extends View {

	private static final String TAG = "VoiceRecorder_VoiceRecordCounterView";
	private boolean m_CountStop;
	private long m_StartTime = 0;
	private TextView m_RecordingTimeText;

	// ---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public VoiceRecordCounterView(Context context,AttributeSet attrs) {
		super(context, attrs);
		//m_Paint = new Paint();
		//m_Paint.setColor(Color.WHITE);
		//m_Paint.setTextSize(32);
	}

	// ---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void Init(TextView tv){
		m_RecordingTimeText = tv;
		m_StartTime = -1;
		m_CountStop = true;
	}

	// ---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void CounterControll(boolean bCountStop){
		m_CountStop = bCountStop;
		m_StartTime = System.currentTimeMillis();
		if(m_CountStop == false){
			this.invalidate();
		}else{
			m_StartTime = -1;
		}
	}

	// ---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(m_StartTime == -1){
			m_RecordingTimeText.setText(Tools.ConversionTime(0));
		}else{
			m_RecordingTimeText.setText(Tools.ConversionTime((int)(System.currentTimeMillis() - m_StartTime)));
		}
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