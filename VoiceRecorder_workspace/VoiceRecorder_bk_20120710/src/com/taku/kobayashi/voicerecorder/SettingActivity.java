package com.taku.kobayashi.voicerecorder;

import java.util.prefs.Preferences;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.DialogPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class SettingActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setPreferenceScreen(createPreferenceHierarchy());
	}

	private PreferenceScreen createPreferenceHierarchy() {
		// Root
		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);

		// Inline preferences
		PreferenceCategory SaveAudioFileCat = new PreferenceCategory(this);
		SaveAudioFileCat.setTitle(getResources().getString(R.string.SettingSaveAudioFileTitle));
		root.addPreference(SaveAudioFileCat);

		ListPreference OutputFormatPref = new ListPreference(this);
		OutputFormatPref.setEntries(R.array.SettingOutputFormat);
		OutputFormatPref.setEntryValues(R.array.SettingOutputFormatValues);
		OutputFormatPref.setDialogTitle(R.string.SettingOutputFormatDialogTitle);
		OutputFormatPref.setKey(getResources().getString(R.string.SettingFormatKey));
		OutputFormatPref.setTitle(R.string.SettingOutputFormatTitle);
		OutputFormatPref.setSummary(R.string.SettingOutputFormatSummary);
		SaveAudioFileCat.addPreference(OutputFormatPref);

		if(Build.VERSION.SDK_INT >= 10){
			ListPreference AudioSamplingRatePref = new ListPreference(this);
			AudioSamplingRatePref.setEntries(R.array.SettingAudioSamplingRate);
			AudioSamplingRatePref.setEntryValues(R.array.SettingAudioSamplingRate);
			AudioSamplingRatePref.setDialogTitle(R.string.SettingAudioSamplingRateDialogTitle);
			AudioSamplingRatePref.setKey(getResources().getString(R.string.SettingAudioSamplingRateKey));
			AudioSamplingRatePref.setTitle(R.string.SettingAudioSamplingRateTitle);
			AudioSamplingRatePref.setSummary(R.string.SettingAudioSamplingRateSummary);
			SaveAudioFileCat.addPreference(AudioSamplingRatePref);
		}

		SettingDialogPreferences MaxDurationPreference = new SettingDialogPreferences(this);
		MaxDurationPreference.setKey(getResources().getString(R.string.SettingMaxDurationKey));
		MaxDurationPreference.setDialogTitle(R.string.SettingMaxDurationDialogTitle);
		MaxDurationPreference.setTitle(R.string.SettingMaxDurationTitle);
		MaxDurationPreference.setSummary(R.string.SettingMaxDurationSummary);
		SaveAudioFileCat.addPreference(MaxDurationPreference);

		CheckBoxPreference PhoneRecordPref = new CheckBoxPreference(this);
		PhoneRecordPref.setKey(getResources().getString(R.string.SettingPhoneRecordKey));
		PhoneRecordPref.setTitle(R.string.SettingPhoneRecordTitle);
		PhoneRecordPref.setSummary(R.string.SettingPhoneRecordSummary);
		SaveAudioFileCat.addPreference(PhoneRecordPref);

		return root;
	}

}
