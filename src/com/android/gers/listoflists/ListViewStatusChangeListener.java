package com.android.gers.listoflists;

import android.view.View;

public interface ListViewStatusChangeListener {
	public void statusChanged(int listPosition, View triggeredView, boolean isOn);
}
