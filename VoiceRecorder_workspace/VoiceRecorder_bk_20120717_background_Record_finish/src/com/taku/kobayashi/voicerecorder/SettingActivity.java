package com.taku.kobayashi.voicerecorder;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;

public class SettingActivity extends PreferenceActivity{

	private static final String TAG = "VoiceRecorder_SettingActivity";

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setPreferenceScreen(createPreferenceHierarchy());
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private PreferenceScreen createPreferenceHierarchy() {

		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);

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

		CheckBoxPreference VolumeAutoSetPref = new CheckBoxPreference(this);
		VolumeAutoSetPref.setKey(getResources().getString(R.string.VolumeAutoSetKey));
		VolumeAutoSetPref.setTitle(R.string.SettingVolumeAutoSetTitle);
		VolumeAutoSetPref.setSummary(R.string.SettingVolumeAutoSetSummary);
		SaveAudioFileCat.addPreference(VolumeAutoSetPref);

		CheckBoxPreference BackgroundRecordPref = new CheckBoxPreference(this);
		BackgroundRecordPref.setKey(getResources().getString(R.string.BackgroundRecordKey));
		BackgroundRecordPref.setTitle(R.string.SettingBackGroundRecordTitle);
		BackgroundRecordPref.setSummary(R.string.SettingBackGroundRecordSummary);
		SaveAudioFileCat.addPreference(BackgroundRecordPref);

		CheckBoxPreference PhoneRecordPref = new CheckBoxPreference(this);
		PhoneRecordPref.setKey(getResources().getString(R.string.SettingPhoneRecordKey));
		PhoneRecordPref.setTitle(R.string.SettingPhoneRecordTitle);
		PhoneRecordPref.setSummary(R.string.SettingPhoneRecordSummary);
		SaveAudioFileCat.addPreference(PhoneRecordPref);

		return root;
	}


	/*
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {
		Log.d(TAG, key);
		if(key.equals(getResources().getString(R.string.SettingFormatKey))){
			Log.d(TAG, key+":"+sharedPreferences.getString(key,"1"));
		}
	}
	*/

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

}
