package com.teleca.jamendo.gestures;

import android.util.Log;

import com.teleca.jamendo.media.PlayerEngine;

import fr.music.overallbrothers.JamendoApplication;

public class PlayerGesturePrevCommand implements GestureCommand {

	PlayerEngine mPlayerEngine;
	
	public PlayerGesturePrevCommand( PlayerEngine engine ){
		mPlayerEngine = engine;
	}

	@Override
	public void execute() {
		Log.v(JamendoApplication.TAG, "PlayerGesturePrevCommand");
		mPlayerEngine.prev();
	}
	
}
