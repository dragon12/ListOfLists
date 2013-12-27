package com.android.gers.listoflists;

import java.util.HashMap;
import java.util.List;

import com.android.gers.listoflists.DB.DbTableListItems;
import com.android.gers.listoflists.DB.ListOfListsDb;
import com.android.gers.listoflists.model.ListArrayAdapter;
import com.android.gers.listoflists.model.ListDataSource;
import com.android.gers.listoflists.model.ListOfListsList;
import com.android.gers.listoflists.model.ListOfListsListItemStats;
import com.android.gers.utils.DialogValidator;
import com.android.gers.utils.SimpleInputDialog;
import com.android.gers.utils.SimpleInputDialog.DialogClickListener;

import android.os.Bundle;
import android.os.Environment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ListOfListsMainActivity extends ListActivity implements
		DialogClickListener {

	private static final int CONTEXT_MENU_DELETE_ID = Menu.FIRST + 1;
	private static final int CONTEXT_MENU_RENAME_ID = CONTEXT_MENU_DELETE_ID + 1;
	private static final int CONTEXT_MENU_CLONE_ID = CONTEXT_MENU_RENAME_ID + 1;

	private static final int DIALOG_ID_LIST_EDIT_NAME = 1;
	private static final int DIALOG_ID_EXPORT = 3;
	private static final int DIALOG_ID_IMPORT = 4;

	private static final String BACKUP_FILE_NAME = "listOfListsBackup.xml";

	private enum EditDialogState {
		NONE, ADD, RENAME;
	}

	private EditDialogState editDialogState = EditDialogState.NONE;

	private ListOfListsList listBeingModified = null;
	private int indexBeingModified = -1;

	private ListDataSource dataSource;
	private ListOfListsDb dbHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_of_lists_activity);

		this.getListView().setDividerHeight(2);

		registerForContextMenu(getListView());

		dbHelper = new ListOfListsDb(this);

		Log.d(ListOfLists.LOG_NAME, "about to create data source");
		dataSource = new ListDataSource(this, dbHelper);
		Log.d(ListOfLists.LOG_NAME, "Created data source");

		dataSource.open();

		reloadListDisplay();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		reloadListDisplay();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Log.d(ListOfLists.LOG_NAME, "onListItemClick position " + position
				+ ", id " + id);
		ListOfListsList listChosen = (ListOfListsList) l
				.getItemAtPosition(position);
		Log.d(ListOfLists.LOG_NAME,
				"corresponds to list: " + listChosen.toString()
						+ "(which has id " + listChosen.getId() + ")");
		Intent i = new Intent(this, ListOfListsSingleListActivity.class);
		i.putExtra(DbTableListItems.COL_LIST_ID, listChosen.getId());

		startActivityForResult(i, 0);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, CONTEXT_MENU_RENAME_ID, 0, R.string.rename_list);
		menu.add(0, CONTEXT_MENU_DELETE_ID, 1, R.string.delete_list);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		ListOfListsList listChosen = (ListOfListsList) getListView()
				.getItemAtPosition(info.position);
		indexBeingModified = info.position;

		switch (item.getItemId()) {
		case CONTEXT_MENU_DELETE_ID:
			dataSource.deleteList(listChosen);
			ListArrayAdapter adapter = (ListArrayAdapter) getListAdapter();
			adapter.remove(listChosen);

			return true;
		case CONTEXT_MENU_RENAME_ID:
			displayRenameDialog(listChosen);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	private void updateStats(ListArrayAdapter adapter) {
		HashMap<Long, ListOfListsListItemStats> stats = dataSource
				.getListOfListsListItemStats();
		adapter.setStats(stats);
	}

	private void reloadListDisplay() {
		List<ListOfListsList> lists = dataSource.getLists();
		ListArrayAdapter adapter = new ListArrayAdapter(this,
				R.layout.list_row, lists);
		updateStats(adapter);
		setListAdapter(adapter);
	}

	// update the list display with a new list or edited list
	// if positionToModify == -1 we need to add it to the end of the items
	private void updateListDisplay(ListOfListsList listOfInterest,
			int indexToModify) {
		ListArrayAdapter adapter = (ListArrayAdapter) getListAdapter();
		if (indexToModify == -1) {
			adapter.add(listOfInterest);
		} else {
			ListOfListsList listToDelete = adapter.getItem(indexToModify);
			adapter.remove(listToDelete);
			adapter.insert(listOfInterest, indexToModify);
		}
		adapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.list_of_lists_activity, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add_list:
			displayAddListDialog();
			return true;

		case R.id.menu_export:
			displayExportDialog();
			return true;

		case R.id.menu_import:
			displayImportDialog();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}

	}

	private AlertDialog showListNameEditDialog(String title) {
		LayoutInflater inflater = getLayoutInflater();
		View dialogLayout = inflater.inflate(R.layout.dialog_edit_list_name,
				null);

		AlertDialog dialog = SimpleInputDialog.SimpleInputDialogBuilder(this,
				this, DIALOG_ID_LIST_EDIT_NAME, title, null, dialogLayout);

		DialogValidator validator = new DialogValidator(
				dialog.getButton(Dialog.BUTTON_POSITIVE));
		((EditText) dialogLayout.findViewById(R.id.rename_list_edit_name))
				.addTextChangedListener(validator);

		return dialog;
	}

	private void displayAddListDialog() {
		Log.d(ListOfLists.LOG_NAME, "displayAddListDialog");
		editDialogState = EditDialogState.ADD;
		showListNameEditDialog("Add New List");
	}

	private void displayExportDialog() {
		Log.d(ListOfLists.LOG_NAME, "displayExportDialog");
		SimpleInputDialog
				.SimpleInputDialogBuilder(
						this,
						this,
						DIALOG_ID_EXPORT,
						"Export data",
						"Exporting data will overwrite previously-exported data!",
						null);
	}

	private void displayImportDialog() {
		Log.d(ListOfLists.LOG_NAME, "displayExportDialog");
		SimpleInputDialog.SimpleInputDialogBuilder(this, this,
				DIALOG_ID_IMPORT, "Import data",
				"Importing data will overwrite all current data!", null);
	}

	private void displayRenameDialog(ListOfListsList listChosen) {
		editDialogState = EditDialogState.RENAME;
		listBeingModified = listChosen;

		AlertDialog dialog = showListNameEditDialog("Rename List '"
				+ listBeingModified.getName() + "'");

		// populate the text box with the current name of the list
		EditText editText = (EditText) dialog
				.findViewById(R.id.rename_list_edit_name);
		editText.setText(listChosen.getName());
	}

	public void buttonClicked(int id, DialogInterface dialog, int whichButton) {
		switch (whichButton) {
		case DialogInterface.BUTTON_POSITIVE:
			AlertDialog alertView = (AlertDialog) dialog;
			switch (id) {
			case DIALOG_ID_LIST_EDIT_NAME:
				EditText inputText = (EditText) alertView
						.findViewById(R.id.rename_list_edit_name);
				String valueEntered = inputText.getText().toString();
				Log.d(ListOfLists.LOG_NAME, "User entered " + valueEntered
						+ "!");

				if (editDialogState == EditDialogState.ADD) {
					ListOfListsList listToAdd = new ListOfListsList(
							valueEntered);

					// add the value to our db
					try {
						ListArrayAdapter adapter = (ListArrayAdapter) getListAdapter();

						adapter.add(dataSource.createList(listToAdd));
						Toast.makeText(this, "New list created",
								Toast.LENGTH_SHORT).show();
					} catch (Exception e) {
						Log.e(ListOfLists.LOG_NAME,
								"Failed to create list with exception: "
										+ e.toString());
						Toast.makeText(this, "Failed to create new list",
								Toast.LENGTH_SHORT).show();
					}
				} else if (editDialogState == EditDialogState.RENAME) {
					// update the item that was selected for editing
					if (!listBeingModified.getName().equals(valueEntered)) {
						ListOfListsList renamedList = new ListOfListsList(
								listBeingModified);
						renamedList.setName(valueEntered);

						try {
							if (dataSource.updateList(renamedList)) {
								updateListDisplay(renamedList,
										indexBeingModified);
							} else {
								Log.e(ListOfLists.LOG_NAME,
										"Failed to do rename!");
								Toast.makeText(this, "Rename failed",
										Toast.LENGTH_SHORT).show();
							}
						} catch (Exception e) {
							Log.e(ListOfLists.LOG_NAME,
									"Threw exception from updateList: "
											+ e.toString());
							Toast.makeText(this, "Exception thrown",
									Toast.LENGTH_SHORT).show();
						}

					}
					break;
				} else {
					Log.e(ListOfLists.LOG_NAME, "Invalid state: "
							+ editDialogState.toString());
				}

				break;

			case DIALOG_ID_EXPORT:
				Log.i(ListOfLists.LOG_NAME, "User wanted to export");
				String outputFile = Environment.getExternalStorageDirectory()
						+ "/" + BACKUP_FILE_NAME;
				if (dbHelper.exportDbAsXml(outputFile)) {
					Toast.makeText(this, "Data exported to " + outputFile,
							Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(this,
							"Export failed to " + outputFile + "!",
							Toast.LENGTH_LONG).show();
				}
				break;

			case DIALOG_ID_IMPORT:
				Log.i(ListOfLists.LOG_NAME, "User wanted to import");
				String inputFile = Environment.getExternalStorageDirectory()
						+ "/" + BACKUP_FILE_NAME;
				try {
					dbHelper.importDbAsXml(inputFile);
					Toast.makeText(this, "Data imported from " + inputFile,
							Toast.LENGTH_LONG).show();
					reloadListDisplay();
				} catch (Exception e) {
					Log.e(ListOfLists.LOG_NAME, "Import failed with error: "
							+ e);
					Toast.makeText(this,
							"Import failed from " + inputFile + ": " + e + "!",
							Toast.LENGTH_LONG).show();
				}
				break;

			default:
				Log.e(ListOfLists.LOG_NAME, "Saw bad id in ButtonClicked: "
						+ id);
				break;
			}
			break;

		default:
			break;
		}

		// reset state
		editDialogState = EditDialogState.NONE;
	}

}
