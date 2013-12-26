package com.android.gers.listoflists;

import java.util.List;

import com.android.gers.listoflists.DB.DbTableListItems;
import com.android.gers.listoflists.DB.ListOfListsDb;
import com.android.gers.listoflists.model.ItemArrayAdapter;
import com.android.gers.listoflists.model.ListDataSource;
import com.android.gers.listoflists.model.ListOfListsList;
import com.android.gers.listoflists.model.ListOfListsListItem;
import com.android.gers.utils.DialogValidator;
import com.android.gers.utils.SimpleInputDialog;
import com.android.gers.utils.SimpleInputDialog.DialogClickListener;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class ListOfListsSingleListActivity
	extends ListActivity
	implements 
			DialogClickListener,
			OnClickListener,
			ListViewStatusChangeListener
{
	
	private static final int CONTEXT_MENU_DELETE_ID = Menu.FIRST + 1;

	protected ListDataSource dataSource;
	protected ListOfListsDb dbHelper;

	protected long originatingListId;
	
	private int viewResourceId;
	private int menuResourceId;
	
	private enum DialogState {
		NONE,
		ADD,
		EDIT
	};
	
	
	DialogState currentState = DialogState.NONE;
	ListOfListsListItem itemToEdit = null;
	int indexBeingEdited = -1;

	public ListOfListsSingleListActivity() {
		this.viewResourceId = R.layout.activity_single_list_items;
		this.menuResourceId = R.menu.activity_single_list_items;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(ListOfLists.LOG_NAME, "onCreate called");
		super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(viewResourceId);

		if (savedInstanceState != null) {
			Log.d(ListOfLists.LOG_NAME, "saved state non-null");
		}

		this.getListView().setDividerHeight(1);

		registerForContextMenu(getListView());

		dbHelper = new ListOfListsDb(this);

		dataSource = new ListDataSource(this, dbHelper);
		dataSource.open();
		
        Button addButton = (Button) findViewById(R.id.button_add);
        addButton.setOnClickListener(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.d(ListOfLists.LOG_NAME, "onPause called");
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		
		editor.putLong("originatingListId", originatingListId);
		
		editor.commit();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(ListOfLists.LOG_NAME, "onResume called");
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (extras.containsKey(DbTableListItems.COL_LIST_ID)) {
				Log.d(ListOfLists.LOG_NAME, "extras does contain our key");
			}

			originatingListId = extras.getLong(DbTableListItems.COL_LIST_ID);
			Log.d(ListOfLists.LOG_NAME, "Originating was " + originatingListId);

		} else {
			SharedPreferences prefs = getPreferences(MODE_PRIVATE);
			long retVal = prefs.getLong("originatingListId", -1);
			if (retVal != -1) {
				originatingListId = retVal;
			}
			else {
				Log.e(ListOfLists.LOG_NAME, "Had no extras in activity!");
				this.finish();
			}
		}
		
		//get the list details
		ListOfListsList list = dataSource.getList(originatingListId);
		EditText title = (EditText)findViewById(R.id.list_name);
		title.setText(list.getName());

		reloadListDisplay();
	}
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(ListOfLists.LOG_NAME, "onSaveInstanceState called");
		outState.putLong("originatingListId", originatingListId);
		super.onSaveInstanceState(outState);
	}
	
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	    // Always call the superclass so it can restore the view hierarchy
	    super.onRestoreInstanceState(savedInstanceState);
	   
	    Log.d(ListOfLists.LOG_NAME, "onRestoreInstanceState called");
	}
	
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	
    	Log.d(ListOfLists.LOG_NAME, "onListItemClick position " + position + ", id " + id);
    	ListOfListsListItem itemClicked = (ListOfListsListItem) l.getItemAtPosition(position);
    	
    	listItemClicked(itemClicked, position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(menuResourceId, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case android.R.id.home:
    		NavUtils.navigateUpFromSameTask(this);
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    

    public void statusChanged(int listPosition, View triggeredView, boolean isOn) {
	
	}
	
    protected void reloadListDisplay() {
    	Log.d(ListOfLists.LOG_NAME, "reloadListDisplay");
        List<ListOfListsListItem> items = dataSource.getListOfListsListItems(originatingListId);
        
        ItemArrayAdapter adapter = createAdapter(items);
        setListAdapter(adapter);
    }
    
	protected Boolean updateItemInDb(ListOfListsListItem itemToUpdate) {
		Boolean retVal = true;
		try {
			dataSource.updateItem(itemToUpdate);
		} catch (Exception e) {
			Log.e(ListOfLists.LOG_NAME, "Failed to edit item with exception: " + e.toString());
			Toast.makeText(this, "Failed to edit item", Toast.LENGTH_SHORT).show();
			retVal = false;
		}
		return retVal;
	}
   
    
    private ItemArrayAdapter createAdapter(List<ListOfListsListItem> items) {
    	return new ItemArrayAdapter(this, this, R.layout.item_row, items);
    }
    

    private AlertDialog showEditDialog(String title) {
    	
		LayoutInflater inflater = getLayoutInflater();
		View dialogLayout = inflater.inflate(R.layout.dialog_edit_item, null);

		AlertDialog dialog = SimpleInputDialog.SimpleInputDialogBuilder(this, this, 0, title, null, dialogLayout);
				
		DialogValidator validator = new DialogValidator(dialog.getButton(Dialog.BUTTON_POSITIVE));
		((EditText)dialogLayout.findViewById(R.id.edit_item_edit_name)).addTextChangedListener(validator);
		((EditText)dialogLayout.findViewById(R.id.edit_item_edit_rating)).addTextChangedListener(validator);

		return dialog;
    }
    
    private void itemAddButtonClicked() {
		Log.d(ListOfLists.LOG_NAME, "itemAddButtonClicked");
		
		AlertDialog dialog = showEditDialog("Add New Item");
		dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
		
		currentState = DialogState.ADD;
	}
    
    private void listItemClicked(ListOfListsListItem itemClicked, int position) {
    	itemToEdit = itemClicked;
    	
    	AlertDialog dialog = showEditDialog("Edit Item");
    	
    	EditText nameBox = (EditText)dialog.findViewById(R.id.edit_item_edit_name);
    	EditText ratingBox = (EditText)dialog.findViewById(R.id.edit_item_edit_rating);
    	
    	nameBox.setText(itemToEdit.getName());
    	ratingBox.setText(itemToEdit.getRating().toString());
    	
    	currentState = DialogState.EDIT;
    	indexBeingEdited = position;
    }
    
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	menu.add(0, CONTEXT_MENU_DELETE_ID, 0, R.string.delete_item);  
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case CONTEXT_MENU_DELETE_ID:
    		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
    		ListOfListsListItem itemChosen = (ListOfListsListItem)getListView().getItemAtPosition(info.position);
    		
    		dataSource.deleteItem(itemChosen);
    		
    		ItemArrayAdapter adapter = (ItemArrayAdapter)getListAdapter();
    		adapter.remove(itemChosen);
    		
    		return true;
    	}
    	return super.onContextItemSelected(item);
    }
    
    //update the list display with a new item or edited item
    //if positionToModify == -1 we need to add it to the end of the items
    private void updateListDisplay(ListOfListsListItem itemOfInterest, int indexToModify) {
		ItemArrayAdapter adapter = (ItemArrayAdapter)getListAdapter();
		if (indexToModify == -1) {
			adapter.add(itemOfInterest);
		} else {
			ListOfListsListItem itemToDelete = adapter.getItem(indexToModify);
			adapter.remove(itemToDelete);
			adapter.insert(itemOfInterest, indexToModify);
		}
		adapter.notifyDataSetChanged();
    }


	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.button_add:
			itemAddButtonClicked();
			break;
		
		default:
			//nothing
			break;
		}
	}
	
	public void buttonClicked(int id, DialogInterface dialog, int which) {
		switch(which) {
        case DialogInterface.BUTTON_POSITIVE:
        {
        	AlertDialog editAlertView = (AlertDialog)dialog;
        	
        	EditText nameBox = (EditText)editAlertView.findViewById(R.id.edit_item_edit_name);
        	EditText ratingBox = (EditText)editAlertView.findViewById(R.id.edit_item_edit_rating);
        	
        	String name = nameBox.getText().toString();
        	double rating = Double.parseDouble(ratingBox.getText().toString());
 
        	Log.d(ListOfLists.LOG_NAME, "User entered " + name + "!");

        	switch(currentState)
        	{
        	case ADD:
        		ListOfListsListItem itemToAdd = new ListOfListsListItem(originatingListId, name, rating);

        		//add the value to our db
        		try {
        			ListOfListsListItem itemAdded = dataSource.createItem(itemToAdd);
        			updateListDisplay(itemAdded, -1);
        			
        			Toast.makeText(this, "New item created", Toast.LENGTH_SHORT).show();
        		} catch (Exception e) {
        			Log.e(ListOfLists.LOG_NAME, "Failed to create item with exception: " + e.toString());
        			Toast.makeText(this, "Failed to create new item", Toast.LENGTH_SHORT).show();
        		}
        		break;
        		
        	case EDIT:
        		//update the item that was selected for editing
        		ListOfListsListItem editedItem = new ListOfListsListItem(itemToEdit.getId(), itemToEdit.getListId(), name, rating);
        		if (editedItem.equals(itemToEdit)) {
        			//no change, do nothing
        		} else {
        			if (updateItemInDb(editedItem)) {
        				updateListDisplay(editedItem, indexBeingEdited);
        			}
        		}
        		break;
        		
        	case NONE:
        		break;
        	}
        	break;
        }
		default:
			Log.d(ListOfLists.LOG_NAME, "non-positive button pressed");
			break;
		}
    	currentState = DialogState.NONE;
	}
}
