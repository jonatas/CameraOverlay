/*
 * Copyright 2011 Anders Kalør
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.kaloer.filepicker;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import me.ideia.cameraoverlay.R;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class FilePickerActivity extends ListActivity {

	/**
	 * The file path
	 */
	public final static String EXTRA_FILE_PATH = "file_path";

	/**
	 * Sets whether hidden files should be visible in the list or not
	 */
	public final static String EXTRA_SHOW_HIDDEN_FILES = "show_hidden_files";

	/**
	 * The allowed file extensions in an ArrayList of Strings
	 */
	public final static String EXTRA_ACCEPTED_FILE_EXTENSIONS = "accepted_file_extensions";

	/**
	 * The initial directory which will be used if no directory has been sent with the intent
	 */
	private final static String DEFAULT_INITIAL_DIRECTORY = "/";

	protected File mDirectory;
	protected ArrayList<File> mFiles;
	protected FilePickerListAdapter mAdapter;
	protected boolean mShowHiddenFiles = false;
	protected String[] acceptedFileExtensions;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set the view to be shown if the list is empty
		LayoutInflater inflator = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View emptyView = inflator.inflate(R.layout.file_picker_empty_view, null);
		((ViewGroup)getListView().getParent()).addView(emptyView);
		getListView().setEmptyView(emptyView);
		getListView().setFastScrollEnabled(true);

		// Set initial directory
		mDirectory = new File(DEFAULT_INITIAL_DIRECTORY);

		// Initialize the ArrayList
		mFiles = new ArrayList<File>();

		// Set the ListAdapter
		mAdapter = new FilePickerListAdapter(this, mFiles);
		setListAdapter(mAdapter);

		// Initialize the extensions array to allow any file extensions
		acceptedFileExtensions = new String[] {};

		// Get intent extras
		if (getIntent().hasExtra(EXTRA_FILE_PATH)) {
			mDirectory = new File(getIntent().getStringExtra(EXTRA_FILE_PATH));
		}
		if (getIntent().hasExtra(EXTRA_SHOW_HIDDEN_FILES)) {
			mShowHiddenFiles = getIntent().getBooleanExtra(EXTRA_SHOW_HIDDEN_FILES, false);
		}
		if (getIntent().hasExtra(EXTRA_ACCEPTED_FILE_EXTENSIONS)) {
			acceptedFileExtensions = getIntent().getStringArrayExtra(EXTRA_ACCEPTED_FILE_EXTENSIONS);
		}
	}

	@Override
	protected void onResume() {
		refreshFilesList();
		super.onResume();
	}

	/**
	 * Updates the list view to the current directory
	 */
	protected void refreshFilesList() {
		// Clear the files ArrayList
		mFiles.clear();

		// Set the extension file filter
		ExtensionFilenameFilter filter = new ExtensionFilenameFilter(acceptedFileExtensions);
		setTitle(mDirectory.getPath().toString());
		// Get the files in the directory
		File[] files = mDirectory.listFiles(filter);
		if (mDirectory.getParentFile() != null) {
			mFiles.add(mDirectory.getParentFile());
		}
		if (files != null && files.length > 0) {
			for (File f : files) {
				if (f.isHidden() && !mShowHiddenFiles) {
					// Don't add the file
					continue;
				}

				// Add the file the ArrayAdapter
				mFiles.add(f);
			}
		}
		mAdapter.groupFilesByType();
		mAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final File newFile = (File)l.getItemAtPosition(position);
		if (newFile.isFile()) {
			Intent extra = new Intent();
			extra.putExtra(EXTRA_FILE_PATH, newFile.getAbsolutePath());
			setResult(RESULT_OK, extra);

			finish();
		} else {
			mDirectory = newFile;
			refreshFilesList();
		}
		super.onListItemClick(l, v, position, id);
	}

	private class FilePickerListAdapter extends ArrayAdapter<File> implements SectionIndexer {

		private static final String IMAGE = "(.*).(jpeg|jpg|png|bmp|gif)";

		private List<File> mObjects;

		HashMap<String, ArrayList<File>> fileTypeIndexer;

		private String[] sections;

		private HashMap<Integer, Integer> indexForSections;

		public FilePickerListAdapter(Context context, List<File> objects) {
			super(context, R.layout.file_picker_list_item, R.string.choose_the_file, objects);
			mObjects = objects;
			setTitle(mDirectory.getPath().toString());
		}

		public void groupFilesByType() {
			fileTypeIndexer = new HashMap<String, ArrayList<File>>();
			for (File file : mObjects) {
				String indexName;
				if (file.isDirectory())
					indexName = "Dir";
				else
					indexName = "Others";
				if (fileTypeIndexer.get(indexName) == null)
					fileTypeIndexer.put(indexName, new ArrayList<File>());
				fileTypeIndexer.get(indexName).add(file);
			}

			ArrayList<String> sectionList = new ArrayList<String>(fileTypeIndexer.keySet());

			Collections.sort(sectionList);

			sections = new String[sectionList.size()];

			sectionList.toArray(sections);
			mObjects.clear();
			indexForSections = new HashMap<Integer, Integer>();
			for (String fileType : sectionList) {
				ArrayList<File> list = fileTypeIndexer.get(fileType);
				Collections.sort(list);
				if (list.size() > 0) {
					indexForSections.put(sectionList.indexOf(fileType), mObjects.size());
					mObjects.addAll(list);
				}

			}

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View row = null;

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.file_picker_list_item, parent, false);
			} else {
				row = convertView;
			}

			File object = mObjects.get(position);

			ImageView imageView = (ImageView)row.findViewById(R.id.file_picker_image);
			TextView textView = (TextView)row.findViewById(R.id.file_picker_text);
			// Set single line
			textView.setSingleLine(true);

			textView.setText(object.getName());
			if (object.isFile()) {
				imageView.setImageResource(R.drawable.file);
			} else {
				imageView.setImageResource(R.drawable.folder);
				if (object.equals(mDirectory.getParentFile())) {
					textView.setText("..");// (" + textView.getText() + ")");
					imageView.setImageResource(R.drawable.back);
				}
			}

			return row;
		}

		public int getPositionForSection(int section) {
			if (indexForSections.size() == 1)
				section = 0;
			return indexForSections.get(section);
		}

		public int getSectionForPosition(int position) {
			int sectionBefore = -1;
			for (Integer section : indexForSections.keySet()) {
				Integer currentPosition = indexForSections.get(section);
				if (currentPosition > position && sectionBefore > -1)
					return sectionBefore;
				sectionBefore = section.intValue();
			}
			return 0;
		}

		public Object[] getSections() {
			return sections;
		}

	}

	private class FileComparator implements Comparator<File> {
		@Override
		public int compare(File f1, File f2) {
			if (f1 == f2) {
				return 0;
			}
			if (f1.isDirectory() && f2.isFile()) {
				// Show directories above files
				return -1;
			}
			if (f1.isFile() && f2.isDirectory()) {
				// Show files below directories
				return 1;
			}
			// Sort the directories alphabetically
			return f1.getName().compareToIgnoreCase(f2.getName());
		}
	}

	private class ExtensionFilenameFilter implements FilenameFilter {
		private String[] mExtensions;

		public ExtensionFilenameFilter(String[] extensions) {
			super();
			mExtensions = extensions;
		}

		@Override
		public boolean accept(File dir, String filename) {
			if (new File(dir, filename).isDirectory()) {
				// Accept all directory names
				return true;
			}
			if (mExtensions != null && mExtensions.length > 0) {
				for (int i = 0; i < mExtensions.length; i++) {
					if (filename.endsWith(mExtensions[i])) {
						// The filename ends with the extension
						return true;
					}
				}
				// The filename did not match any of the extensions
				return false;
			}
			// No extensions has been set. Accept all file extensions.
			return true;
		}
	}

}
