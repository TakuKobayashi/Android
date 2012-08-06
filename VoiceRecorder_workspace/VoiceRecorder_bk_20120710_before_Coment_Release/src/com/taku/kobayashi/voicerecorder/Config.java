package com.taku.kobayashi.voicerecorder;

public class Config {
	public static final String DIRECTORY_NAME_TO_SAVE = "VoiceRecoder/";
	//アプリを動作することが可能なSDカード内の最低空き容量(1MB)
	public static final long LIMIT_MINIMAM_SPACE = 1 * 1024;
	public static final int MIN_AUDIOSAMPLINGRATE = 8000;
	public static final int DEFAULT_MAXDURATION = -1;
}
