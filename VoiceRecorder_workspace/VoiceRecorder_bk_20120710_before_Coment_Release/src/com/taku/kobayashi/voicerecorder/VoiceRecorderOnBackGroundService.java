package com.taku.kobayashi.voicerecorder;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class VoiceRecorderOnBackGroundService extends IntentService {

	private boolean m_bRecord;

	public VoiceRecorderOnBackGroundService(String name) {
		super(name);

	}

	@Override
	protected void onHandleIntent(Intent intent) {
		m_bRecord = true;
		while(m_bRecord == false){

		}
	}

	public void StopRecord(){
		m_bRecord = true;
	}


}