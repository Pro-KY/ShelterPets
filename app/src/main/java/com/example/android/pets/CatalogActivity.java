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
import android.widget.AdapterView;
import android.widget.ListView;


import com.example.android.pets.data.PetContract.PetEntry;


/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = CatalogActivity.class.getSimpleName();

    // unique loader id
    private static final int PET_LOADER = 0;

    // This is the Adapter being used to display the list's data.
    PetCursorAdapter mCursorAdapter;

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

        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        mCursorAdapter = new PetCursorAdapter (this, null);
        petListView.setAdapter(mCursorAdapter);

        // setup the click listener
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                // passes the selected Pet's URI
                // The Uri would be content://com.example.android.pets/pets/2 if the user
                // clicked on the pet with ID = 2 (i.e. second pet in the list)
                Uri contentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);
                intent.setData(contentPetUri);

                Log.d("uri", contentPetUri.toString());

                // open up an EditorActivity
                startActivity(intent);
            }
        });

        // initialize loader
        getSupportLoaderManager().initLoader(PET_LOADER, null, this);
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
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // Called when a new Loader needs to be created
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // These are the pets rows that we will retrieve
        String[] projection = new String[] {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED
        };

        // create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed
        return new CursorLoader(
                this,                   // Parent activity context
                PetEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null                    // Default sort order
        );
    }

    // Called when a previously created loader has finished loading
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update the PetCursorAdapter with this new cursor containing updated pet data
        mCursorAdapter.swapCursor(data);
    }

    // Called when a previously created loader is reset, making the data unavailable
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
