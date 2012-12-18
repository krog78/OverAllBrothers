package fr.music.overallbrothers;

import android.app.Application;
import android.content.Intent;

import com.teleca.jamendo.api.JamendoGet2Api;
import com.teleca.jamendo.api.Playlist;
import com.teleca.jamendo.api.Playlist.PlaylistPlaybackMode;
import com.teleca.jamendo.gestures.GesturesHandler;
import com.teleca.jamendo.gestures.PlayerGestureCommandRegiser;
import com.teleca.jamendo.media.PlayerEngine;
import com.teleca.jamendo.media.PlayerEngineListener;
import com.teleca.jamendo.service.PlayerService;
import com.teleca.jamendo.util.ImageCache;
import com.teleca.jamendo.util.download.DownloadManager;
import com.teleca.jamendo.util.download.DownloadManagerImpl;

/**
 * Singleton with hooks to Player and Download Service
 * 
 * @author Lukasz Wisniewski
 */
public class JamendoApplication extends Application {

	/**
	 * Tag used for DDMS logging
	 */
	public static String TAG = "jamendo";
	
	/**
	 * Singleton pattern
	 */
	private static JamendoApplication instance;
	
	public static JamendoApplication getInstance() {
		return instance;
	}
	
	public String getStreamEncoding() {
		// http://groups.google.com/group/android-developers/msg/c546760177b22197
		// According to JBQ: ogg files are supported but not streamable
		return JamendoGet2Api.ENCODING_MP3;
	}
	
	public GesturesHandler getPlayerGestureHandler(){
		if(mPlayerGestureHandler == null){
			mPlayerGestureHandler = new GesturesHandler(this, new PlayerGestureCommandRegiser(getPlayerEngineInterface()));
		}
		return mPlayerGestureHandler;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		mImageCache = new ImageCache();
		mDownloadManager = new DownloadManagerImpl(this);
		mImageCache = new ImageCache();
	}
	
	
	/**
	 * Since 0.9.8.7 we embrace "bindless" PlayerService thus this adapter. No
	 * big need of code refactoring, we just wrap sending intents around defined
	 * interface
	 * 
	 * @author Lukasz Wisniewski
	 */
	private class IntentPlayerEngine implements PlayerEngine {

		@Override
		public Playlist getPlaylist() {
			return mPlaylist;
		}

		@Override
		public boolean isPlaying() {
			if (mServicePlayerEngine == null) {
				// service does not exist thus no playback possible
				return false;
			} else {
				return mServicePlayerEngine.isPlaying();
			}
		}

		@Override
		public void next() {
			if (mServicePlayerEngine != null) {
				playlistCheck();
				mServicePlayerEngine.next();
			} else {
				startAction(PlayerService.ACTION_NEXT);
			}
		}

		@Override
		public void openPlaylist(Playlist playlist) {
			mPlaylist = playlist;
			if(mServicePlayerEngine != null){
				mServicePlayerEngine.openPlaylist(playlist);
			}
		}

		@Override
		public void pause() {
			if (mServicePlayerEngine != null) {
				mServicePlayerEngine.pause();
			}
		}

		@Override
		public void play() {
			if (mServicePlayerEngine != null) {
				playlistCheck();
				mServicePlayerEngine.play();
			} else {
				startAction(PlayerService.ACTION_PLAY);
			}
		}

		@Override
		public void prev() {
			if (mServicePlayerEngine != null) {
				playlistCheck();
				mServicePlayerEngine.prev();
			} else {
				startAction(PlayerService.ACTION_PREV);
			}
		}

		@Override
		public void setListener(PlayerEngineListener playerEngineListener) {
			mPlayerEngineListener = playerEngineListener;
			// we do not want to set this listener if Service
			// is not up and a new listener is null
			if (mServicePlayerEngine != null || mPlayerEngineListener != null) {
				startAction(PlayerService.ACTION_BIND_LISTENER);
			}
		}

		@Override
		public void skipTo(int index) {
			if (mServicePlayerEngine != null) {
				mServicePlayerEngine.skipTo(index);
			}
		}

		@Override
		public void stop() {
			startAction(PlayerService.ACTION_STOP);
			// stopService(new Intent(JamendoApplication.this,
			// PlayerService.class));
		}

		private void startAction(String action) {
			Intent intent = new Intent(JamendoApplication.this,
					PlayerService.class);
			intent.setAction(action);
			startService(intent);
		}

		/**
		 * This is required if Player Service was binded but playlist was not
		 * passed from Application to Service and one of buttons: play, next,
		 * prev is pressed
		 */
		private void playlistCheck() {
			if (mServicePlayerEngine != null) {
				if (mServicePlayerEngine.getPlaylist() == null
						&& mPlaylist != null) {
					mServicePlayerEngine.openPlaylist(mPlaylist);
				}
			}
		}

		@Override
		public void setPlaybackMode(PlaylistPlaybackMode aMode) {
			mPlaylist.setPlaylistPlaybackMode(aMode);
		}

		@Override
		public PlaylistPlaybackMode getPlaybackMode() {
			return mPlaylist.getPlaylistPlaybackMode();
		}
		
		@Override
		public void forward(int time) {
			if(mServicePlayerEngine != null){				
				mServicePlayerEngine.forward( time );
			}
			
		}

		@Override
		public void rewind(int time) {
			if(mServicePlayerEngine != null){				
				mServicePlayerEngine.rewind( time );
			}
			
		}

		@Override
		public void prevList() {
			if(mServicePlayerEngine != null){
				mServicePlayerEngine.prevList();
			}
			
		}
		
	}
	
	/**
	 * This getter allows performing logical operations on the player engine's
	 * interface from UI space
	 * 
	 * @return
	 */
	public PlayerEngine getPlayerEngineInterface() {
		// request service bind
		if (mIntentPlayerEngine == null) {
			mIntentPlayerEngine = new IntentPlayerEngine();
		}
		return mIntentPlayerEngine;
	}
	
	/**
	 * This function allows to add listener to the concrete player engine
	 * 
	 * @param l
	 */
	public void setPlayerEngineListener(PlayerEngineListener l) {
		getPlayerEngineInterface().setListener(l);
	}
	
	public DownloadManager getDownloadManager() {
		return mDownloadManager;
	}
	
	/**
	 * Access to global image cache across Activity instances
	 * 
	 * @return
	 */
	public ImageCache getImageCache() {
		return mImageCache;
	}
	
	/**
	 * Intent player engine
	 */
	private PlayerEngine mIntentPlayerEngine;
	
	/**
	 * Player engine listener
	 */
	private PlayerEngineListener mPlayerEngineListener;
	
	/**
	 * Service player engine
	 */
	public PlayerEngine mServicePlayerEngine;
	
	/**
	 * Image cache, one for all activities and orientations
	 */
	private ImageCache mImageCache;
	
	/**
	 * Stored in Application instance in case we destroy Player service
	 */

	private Playlist mPlaylist;
	
	/**
	 * Handler for player related gestures.
	 */
	private GesturesHandler mPlayerGestureHandler;
	
	/**
	 * Provides interface for download related actions.
	 */
	private DownloadManager mDownloadManager;
}