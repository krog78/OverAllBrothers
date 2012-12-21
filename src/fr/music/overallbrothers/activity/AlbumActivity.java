/*
 * Copyright (C) 2009 Teleca Poland Sp. z o.o. <android@teleca.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.music.overallbrothers.activity;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.teleca.jamendo.adapter.PlaylistAdapter;
import com.teleca.jamendo.adapter.ReviewAdapter;
import com.teleca.jamendo.api.Album;
import com.teleca.jamendo.api.Playlist;
import com.teleca.jamendo.api.PlaylistEntry;
import com.teleca.jamendo.api.Review;
import com.teleca.jamendo.api.Track;
import com.teleca.jamendo.dialog.AlbumLoadingDialog;
import com.teleca.jamendo.util.Helper;
import com.teleca.jamendo.util.download.DownloadManager;

import fr.music.overallbrothers.JamendoApplication;
import fr.music.overallbrothers.R;

// TODO context menu for tracks
/**
 * Activity representing album
 * 
 * @author Lukasz Wisniewski
 */
public class AlbumActivity extends Fragment implements
ActionBar.TabListener{

	private Album mAlbum;
	private ListView mReviewAlbumListView;
	private ListView mAlbumTrackListView;
	private ReviewAdapter mReviewAdapter;
	private Spinner mLanguageSpinner;
	
	TabSpec mAlbumTabSpec;
	TabSpec mReviewsTabSpec;
	
	private TabHost mTabHost;
	
	private String mBetterRes;

	private GestureOverlayView mGestureOverlayView;
	/**
	 * Launch this Activity from the outside
	 *
	 * @param c Activity from which AlbumActivity should be started
	 * @param album Album to be presented
	 */
	public static void launch(Activity c, Album album, FragmentManager fm){
		new AlbumLoadingDialog(c,R.string.album_loading, R.string.album_fail, fm).execute(album);
	}

	public static void launch(
			IntentDistributorActivity c, Album album,
			int reviewId, FragmentManager fm) {
		new AlbumLoadingDialog(c,R.string.album_loading, R.string.album_fail, fm).execute(album, reviewId);
	}

	/** Called when the activity is first created. */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//this.getActivity().requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		// Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.album, container, false);
				
		
		mBetterRes = getResources().getString(R.string.better_res);

		mAlbum = (Album) this.getArguments().getSerializable("album");

		mReviewAlbumListView = (ListView)view.findViewById(R.id.AlbumListView);
		mAlbumTrackListView = (ListView)view.findViewById(R.id.AlbumTrackListView);
		mLanguageSpinner = (Spinner)view.findViewById(R.id.LanguageSpinner);

		mReviewAdapter = new ReviewAdapter(this.getActivity());
		mReviewAdapter.setListView(mReviewAlbumListView);
		mReviewAlbumListView.setAdapter(mReviewAdapter);
		
		loadReviews();
		loadTracks();
				
		int selectedReviewId = this.getActivity().getIntent().getIntExtra("selectedReviewId", -1);
		if(selectedReviewId != -1){
			selectReview(selectedReviewId);
		}

		mGestureOverlayView = (GestureOverlayView) view.findViewById(R.id.gestures);
		mGestureOverlayView.addOnGesturePerformedListener(JamendoApplication.getInstance().getPlayerGestureHandler());
		
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		boolean gesturesEnabled = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getBoolean("gestures", true);
		mGestureOverlayView.setEnabled(gesturesEnabled);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.download_menu_item:
			downloadAlbum();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("unchecked")
	private void loadReviews() {
		ArrayList<Review> reviews = (ArrayList<Review>)this.getArguments().getSerializable("reviews");
		mReviewAdapter.setList(reviews);

		final ArrayList<String> langs = Helper.getLanguageCodes(reviews);
		langs.add(0, "all");

		ArrayAdapter<String> languageAdapter = new ArrayAdapter<String>(this.getActivity(),
				android.R.layout.simple_spinner_item, Helper.getLanguageNames(langs, AlbumActivity.this.getActivity()));
		languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mLanguageSpinner.setAdapter(languageAdapter);

		mLanguageSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				mReviewAdapter.setLang(langs.get(position));
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				mReviewAdapter.setLang("all");
			}
			
		});
	}
	
	private void loadTracks(){
		PlaylistAdapter playlistAdapter = new PlaylistAdapter(this.getActivity());
		Playlist playlist = new Playlist();
		playlist.addTracks(mAlbum);
		playlistAdapter.setPlaylist(playlist);
		mAlbumTrackListView.setAdapter(playlistAdapter);
		mAlbumTrackListView.setOnItemClickListener(mOnTracksItemClickListener);
	}
	
	/**
	 * Jump to the track (play it)
	 */
	private OnItemClickListener mOnTracksItemClickListener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int index,
				long time) {
			
			Playlist playlist = JamendoApplication.getInstance().getPlayerEngineInterface().getPlaylist();
			Track track = mAlbum.getTracks()[index];
			if (playlist == null) {
				// player's playlist is empty, create a new one with whole album and open it in the player
				playlist = new Playlist();
				playlist.addTracks(mAlbum);
				JamendoApplication.getInstance().getPlayerEngineInterface().openPlaylist(playlist);
			} 
			playlist.selectOrAdd(track, mAlbum);
			JamendoApplication.getInstance().getPlayerEngineInterface().play();
			PlayerActivity.launch(AlbumActivity.this.getActivity(), (Playlist)null);
		}

	};
	
	
	/**
	 * Add whole album to the download queue
	 */
	private void downloadAlbum(){
		
		AlertDialog alertDialog = new AlertDialog.Builder(AlbumActivity.this.getActivity())
		.setTitle(R.string.download_album_q)
		.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				
				DownloadManager downloadManager = JamendoApplication.getInstance().getDownloadManager();
				for(Track track : mAlbum.getTracks()) {
					PlaylistEntry entry = new PlaylistEntry();
					entry.setAlbum(mAlbum);
					entry.setTrack(track);
					downloadManager.download(entry);
				}
				
			}
		})
		.setNegativeButton(R.string.cancel, null)
		.create();
		
		alertDialog.show();
	}


	private void selectReview(int selectedReviewId) {
		mTabHost.setCurrentTab(1);
		for(int i = 0; i < mReviewAdapter.getCount(); i++){
			if(((Review)mReviewAdapter.getItem(i)).getId() == selectedReviewId){
				mReviewAlbumListView.setSelection(i);
				return;
			}
		}
	}
	
	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}
}
