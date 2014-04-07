package com.hamaksoftware.eztvdroid.fragments;

import android.view.View;

public interface IFragmentListener {
	/* add more methods here if needed  to monitor fragment activities 
	 * and pass data to listening object
	 */
	public void onFragmentViewCreated();
	public void onViewClicked(View v);
}
