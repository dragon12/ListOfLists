package com.android.gers.listoflists.model;

import java.util.HashMap;
import java.util.List;

import com.android.gers.listoflists.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ListArrayAdapter extends ArrayAdapter<ListOfListsList> {
	private List<ListOfListsList> listOfLists;
	private HashMap<Long, ListOfListsListItemStats> stats = null;

	public ListArrayAdapter(Context context, int textViewResourceId, List<ListOfListsList> results) {
		super(context, textViewResourceId, results);

		listOfLists = results;
	}

	public void setStats(HashMap<Long, ListOfListsListItemStats> stats) {
		this.stats = stats;
	}
	
	public View getView(int position, View convertView, ViewGroup parent){

		// assign the view we are converting to a local variable
		View v = convertView;

		// first check to see if the view is null. if so, we have to inflate it.
		// to inflate it basically means to render, or show, the view.
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.list_row, parent, false);
		}

		/*
		 * Recall that the variable position is sent in as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 * 
		 * Therefore, i refers to the current Item object.
		 */
		ListOfListsList i = listOfLists.get(position);
		ListOfListsListItemStats stat = null;
		if (stats != null) {
			stat = stats.get(i.getId());
		}

		if (i != null) {

			// This is how you obtain a reference to the TextViews.
			// These TextViews are created in the XML files we defined.

			TextView listName = (TextView) v.findViewById(R.id.list_row_list_name);
			TextView listStats = (TextView) v.findViewById(R.id.list_row_list_stats);
			TextView listCreationDate = (TextView) v.findViewById(R.id.list_row_list_date);

			// check to see if each individual textview is null.
			// if not, assign some text!
			if (listName != null){
				listName.setText(i.getName());
			}
			if (listStats != null) {
				if (stat != null) {
					listStats.setText("" + stat.count);
				} else {
					listStats.setText("0");	
				}
			}
				
			if (listCreationDate != null){
				listCreationDate.setText(i.getDisplayDate());
			}
		}

		// the view must be returned to our activity
		return v;
	}

}
