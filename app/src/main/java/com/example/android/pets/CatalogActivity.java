/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.example.android.pets;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;


import com.example.android.pets.data.PetContract.PetEntry;


/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = CatalogActivity.class.getSimpleName();

    // This is the Adapter being used to display the list's data.
    SimpleCursorAdapter mAdapter;

    // These are the pets rows that we will retrieve
    static final String[] PROJECTION = new String[] {
            PetEntry._ID,
            PetEntry.COLUMN_PET_NAME,
            PetEntry.COLUMN_PET_BREED
    };

    // This is the select criteria
    static final String SELECTION = "(" + PetEntry.COLUMN_PET_NAME + "NOTNULL" + ")";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the pet data
        ListView petListView = (ListView) findViewById(R.id.pets_list_view);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        // Set the view to show if the adapter is empty
        petListView.setEmptyView(emptyView);

        // For the cursor adapter, specify which columns go into which views
        String[] fromColumns = {PetEntry.COLUMN_PET_NAME, PetEntry.COLUMN_PET_BREED};
        int[] toViews = {R.id.name, R.id.summary}; // The TextView in list_item

        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        mAdapter = new SimpleCursorAdapter(
                this,
                R.layout.list_item,
                null,
                fromColumns,
                toViews,
                0
        );

        petListView.setAdapter(mAdapter);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    // Insert dummy data when user clicks on the menu item
    private void insertPet() {

        Uri mNewUri;

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Toto");
        values.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 7);

        // Insert the new row, returning the primary key value of the new row
        mNewUri = getContentResolver().insert(
                PetEntry.CONTENT_URI,
                values
        );

        long newRowId = ContentUris.parseId(mNewUri);

        Log.d(LOG_TAG, "New row ID " + newRowId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                // Do nothing for now
                insertPet();
                displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ----- HELPER METHOD FOR DEBUGGING PURPOSE ------
    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    private void displayDatabaseInfo() {

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
            PetEntry._ID,
            PetEntry.COLUMN_PET_NAME,
            PetEntry.COLUMN_PET_BREED,
            PetEntry.COLUMN_PET_GENDER,
            PetEntry.COLUMN_PET_WEIGHT
        };

        // Perform a query on the provider using the ContentResolver.
        Cursor cursor = getContentResolver().query(
                PetEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        // Find the ListView which will be populated with the pet data
        ListView lvItems = (ListView) findViewById(R.id.pets_list_view);

        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
        PetCursorAdapter petAdapter = new PetCursorAdapter(this, cursor);

        // Attach the adapter to the ListView
        lvItems.setAdapter(petAdapter);
    }

    // Called when a new Loader needs to be created
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed
        return new CursorLoader(
                this,
                PetEntry.CONTENT_URI,
                PROJECTION,
                SELECTION,
                null,
                null
        );
    }

    // Called when a previously created loader has finished loading
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
    }

    // Called when a previously created loader is reset, making the data unavailable
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }
}
