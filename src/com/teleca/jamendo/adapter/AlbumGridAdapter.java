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

package com.teleca.jamendo.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teleca.jamendo.widget.RemoteImageView;

import fr.music.overallbrothers.R;

public class AlbumGridAdapter extends AlbumAdapter {
	
	int mAlbumSize;

	public AlbumGridAdapter(Activity context) {
		super(context);
		mAlbumSize = (int)context.getResources().getDimension(R.dimen.album_size); 
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row=convertView;

		ViewHolder holder;

		if (row==null) {
			LayoutInflater inflater = mContext.getLayoutInflater();
			row=inflater.inflate(R.layout.album_cell, parent, false);

			holder = new ViewHolder();
			holder.image = (RemoteImageView)row.findViewById(R.id.AlbumRowImageView);
			holder.albumText = (TextView)row.findViewById(R.id.AlbumRowAlbumTextView);
			
			row.setTag(holder);
		}
		else{
			holder = (ViewHolder) row.getTag();
		}
		
		holder.image.setDefaultImage(R.drawable.no_cd);
		holder.image.setImageUrl(mList.get(position).getImage(),position, getListView());
		holder.albumText.setText(mList.get(position).getName());
		
		return row;
	}
	
	/**
	 * Class implementing holder pattern,
	 * performance boost
	 * 
	 * @author Lukasz Wisniewski
	 */
	static class ViewHolder {
		RemoteImageView image;
		TextView albumText;
	}
}
