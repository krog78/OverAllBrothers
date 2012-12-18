package com.teleca.jamendo.gestures;

import android.util.Log;

import com.teleca.jamendo.media.PlayerEngine;

import fr.music.overallbrothers.JamendoApplication;

public class PlayerGesturePlayCommand implements GestureCommand {

	PlayerEngine mPlayerEngine;

	public PlayerGesturePlayCommand(PlayerEngine engine) {
		mPlayerEngine = engine;
	}

	@Override
	public void execute() {
		Log.v(JamendoApplication.TAG, "PlayerGesturePlayCommand");
		if (mPlayerEngine.isPlaying()) {
			mPlayerEngine.pause();
		} else {
			mPlayerEngine.play();
		}
	}

}
