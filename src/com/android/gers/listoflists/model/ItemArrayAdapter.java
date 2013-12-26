package com.android.gers.listoflists.model;

import java.util.List;

import com.android.gers.listoflists.ListViewStatusChangeListener;
import com.android.gers.listoflists.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class ItemArrayAdapter 
		extends ArrayAdapter<ListOfListsListItem> {
	private List<ListOfListsListItem> shoppingListItems;
	private int textViewResourceId;
	
	protected ListViewStatusChangeListener listenerCb;
	
	public ItemArrayAdapter(Context context, ListViewStatusChangeListener listenerCb, int textViewResourceId, List<ListOfListsListItem> results) {
		super(context, textViewResourceId, results);
		this.textViewResourceId = textViewResourceId;

		this.shoppingListItems = results;
		this.listenerCb = listenerCb;
	}

	public View getView(int position, View convertView, ViewGroup parent){

		// assign the view we are converting to a local variable
		View v = convertView;

		// first check to see if the view is null. if so, we have to inflate it.
		// to inflate it basically means to render, or show, the view.
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(textViewResourceId, null);
		}

		/*
		 * Recall that the variable position is sent in as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 * 
		 * Therefore, i refers to the current Item object.
		 */
		ListOfListsListItem i = shoppingListItems.get(position);
		if (i != null) {
			populateRow(v, i, position);
		}
		
		// the view must be returned to our activity
		return v;
	}

	private void populateRow(View v, ListOfListsListItem item, int position) {
		
		if (item != null) {
			TextView itemRating = (TextView) v.findViewById(R.id.item_row_item_rating);
			TextView itemName = (TextView) v.findViewById(R.id.item_row_item_name);
			
			if (itemRating != null) {
				StringBuilder displayRating = new StringBuilder(item.getRating().toString());
				if (displayRating.substring(displayRating.length() - 2).equals(".0")) {
					displayRating = displayRating.delete(displayRating.length() - 2, displayRating.length());
				}
				itemRating.setText(displayRating.toString());
			}
			
			if (itemName != null){
				itemName.setText(item.getDisplayName());
			}
		}
	}
}
