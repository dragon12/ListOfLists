package com.android.gers.listoflists;

import android.content.Context;
import android.widget.Toast;

public final class ListOfLists {

	private ListOfLists()
	{
		
	}
	
	public static final String LOG_NAME = "LIST_OF_LISTS";
	
	public static final class DB {
		public static final String NAME = "ListOfLists";
		public static final Integer DB_VERSION = 1;
	}
	
	
	public static void ToastNotImplemented(Context context) {
		Toast.makeText(context, "Not yet implemented", Toast.LENGTH_SHORT).show();
	}
}
