package com.taku.kobayashi.voicerecorder;


import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class SettingDialogPreferences extends DialogPreference {

	private final static String TAG = "VoiceRecorder_SettingDialogPreferences";
	private Context m_Context;
	private EditText m_EditText;
	private RelativeLayout m_EditTime;
	private TextView m_TextView;
	private SharedPreferences m_Settings;

	// ---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public SettingDialogPreferences(Context context) {
		super(context, null);
		m_Context = context;
	}

	// ---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected View onCreateDialogView() {
		m_Settings = PreferenceManager.getDefaultSharedPreferences(m_Context);
		int nMaxDuration = m_Settings.getInt(m_Context.getResources().getString(R.string.SettingMaxDurationKey), Config.DEFAULT_MAXDURATION);
		int nTime = m_Settings.getInt(m_Context.getResources().getString(R.string.SettingTimeKey), 0);

		LayoutInflater factory = LayoutInflater.from(m_Context);
		View textEntryView = factory.inflate(R.layout.settingmaxdurationdialog, null);
		m_EditTime = (RelativeLayout) textEntryView.findViewById(R.id.EditTime);
		m_EditText = (EditText) textEntryView.findViewById(R.id.MaxDuration);
		m_TextView = (TextView) textEntryView.findViewById(R.id.UnitText);
		setUnitText(nTime);
		if(nMaxDuration > 0){
			m_EditText.setText(String.valueOf(nMaxDuration));
		}
		Spinner MaxDurationSpinner = (Spinner) textEntryView.findViewById(R.id.MaxDurationSpinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(m_Context, R.array.SettingMaxDurationSpinner, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		MaxDurationSpinner.setAdapter(adapter);
		MaxDurationSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if(position == 0){
					m_EditTime.setVisibility(View.INVISIBLE);
				}else{
					setUnitText(position);
					m_EditTime.setVisibility(View.VISIBLE);
				}
				SharedPreferences.Editor editor = m_Settings.edit();
				editor.putInt(m_Context.getResources().getString(R.string.SettingTimeKey), position);
				editor.commit();
			}

			public void onNothingSelected(AdapterView<?> parent) {
				m_EditTime.setVisibility(View.INVISIBLE);
			}
		});
		MaxDurationSpinner.setSelection(nTime, true);
		return textEntryView;

	}

	// ---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if(positiveResult == true){
			if(m_EditTime.getVisibility() == View.INVISIBLE){
				persistInt(Config.DEFAULT_MAXDURATION);
			}else{
				String strUpdate = m_EditText.getText().toString();
				if(strUpdate.length() == 0){
					//エラーがある場合はもう一度選択
					Tools.showToast(m_Context,m_Context.getResources().getString(R.string.EditTextEmptyMessage));
				}else{
					try{
						int num = Integer.parseInt(strUpdate);
						if(num > 0){
							persistInt(num);
						}else{
							Tools.showToast(m_Context,m_Context.getResources().getString(R.string.EditTextUnder0Message));
						}
					} catch (NumberFormatException e) {
						//エラーがある場合はもう一度選択
						Tools.showToast(m_Context,m_Context.getResources().getString(R.string.SettingNonNumberMessage));
					}
				}
			}
		}
		super.onDialogClosed(positiveResult);
	}

	// ---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private void setUnitText(int position){
		if(position == 1){
			m_TextView.setText(R.string.DialogSecondText);
		}else if(position == 2){
			m_TextView.setText(R.string.DialogMiniuteText);
		}else if(position == 3){
			m_TextView.setText(R.string.DialogHourText);
		}
	}

	// ---------------------------------------------------------------------------------------------------------------------------------------------------------------
}