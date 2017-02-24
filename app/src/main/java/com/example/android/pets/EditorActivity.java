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
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.UserDictionary;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDbHelper;

import static android.R.id.input;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

/**
 * Allows user to create a new p et or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    // unique loader id
    private static final int PET_LOADER = 1;

    // EditText field to enter the pet's name
    private EditText mNameEditText;

    // EditText field to enter the pet's breed
    private EditText mBreedEditText;

    // EditText field to enter the pet's weight
    private EditText mWeightEditText;

    // EditText field to enter the pet's gender
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    // Content URI for the existing pet (null if it's a new pet)
    private Uri mCurrentPetUri;

    // field for switching between "edit_pet" and "add_pet" mode
    // add_mode = true, edit_mode = false
    private static boolean ADD_MODE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        setupSpinner();

        // examine the intent that was used to launch this activiy, on order to figure out
        // if we're creating a new pet or editing an existing one
        Intent intent = getIntent();
        mCurrentPetUri = intent.getData();

        // if the intent does not contain a pet content URI, then we know that we are
        // creating a new pet.
        if(mCurrentPetUri == null) {
            setTitle(R.string.editor_activity_title_new_pet);
            ADD_MODE = true;
        } else {
            // otherwise this is an existing pet - set tittle "Edit pet"
            setTitle(R.string.editor_activity_title_edit_pet);
            ADD_MODE = false;

            // initialize a loader
            getSupportLoaderManager().initLoader(PET_LOADER, null, this);
        }
        Log.d("ADD_MODE", String.valueOf(ADD_MODE));
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE;    // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE;  // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    /**
     *  Get the user input from editor and save new pet data into database.
     */
    private void savePet() {
        // inserted row id
        long newRowId = 0;
        // number of updated rows
        int updatedRows = 0;

        Log.d("insertPet", "start -----");
        String nameString = mNameEditText.getText().toString().trim();
        Log.d("nameString", nameString);
        String breedString = mBreedEditText.getText().toString().trim();
        Log.d("breedString", breedString);
        String weightString = mWeightEditText.getText().toString().trim();
        Log.d("weightString", weightString);
        int weight = Integer.parseInt(weightString);
        Log.d("insertPet", "finish -----");

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, nameString);
        values.put(PetEntry.COLUMN_PET_BREED, breedString);
        values.put(PetEntry.COLUMN_PET_GENDER, mGender);
        values.put(PetEntry.COLUMN_PET_WEIGHT, weight);

        // if it's add_mode -> insert pet
        if(ADD_MODE) {
            Log.d(LOG_TAG, "in ADD_MODE");
            Uri mNewUri = getContentResolver().insert(
                    PetEntry.CONTENT_URI,
                    values
            );

            newRowId = ContentUris.parseId(mNewUri);

            Log.d("new_inserted_row_id", String.valueOf(newRowId));
        } else {
            // otherwise it's edit_mode -> update pet
            Log.d(LOG_TAG, "in EDIT_MODE");
            // Defines selection criteria for the rows you want to update
            String mSelectionClause = PetEntry._ID + "=?";
            int pet_id = (int) ContentUris.parseId(mCurrentPetUri);
            String[] mSelectionArgs = {String.valueOf(pet_id)};

            updatedRows = getContentResolver().update(
                    PetEntry.CONTENT_URI,
                    values,
                    mSelectionClause,
                    mSelectionArgs
            );

            Log.d("updated_rows_number", String.valueOf(updatedRows));
        }

        // Show a toast message depending on whether or not the insertion was successful
        if(newRowId == -1 || updatedRows == -1) {
            // If the row ID is -1, then there was an error with insertion.
            Toast.makeText(this, R.string.toast_error_saving_pet, Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast with the row ID.
            Toast.makeText(
                    this,
                    R.string.toast_pet_saved,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to database
                savePet();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // These are the pets rows that we will retrieve
        String[] projection = new String[] {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT
        };

        // create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed
        return new CursorLoader(
                this,             // Parent activity context
                mCurrentPetUri,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,             // No selection clause
                null,             // No selection arguments
                null              // Default sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if(data.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = data.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breedColumnIndex = data.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int genderColumnIndex = data.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weightColumnIndex = data.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

            // Extract out the value from the Cursor for the given column index
            String petName = data.getString(nameColumnIndex);
            Log.d("petName", petName);

            String petBreed = data.getString(breedColumnIndex);
            Log.d("petBreed", petBreed);

            int petGender = data.getInt(genderColumnIndex);
            Log.d("petGender", String.valueOf(petGender));

            int petWeight = data.getInt(weightColumnIndex);
            Log.d("petWeight", String.valueOf(petWeight));

            // Update the views on the screen with the values from the database
            // Set values from the cursor to corresponding EditText fields
            mNameEditText.setText(petName);
            mBreedEditText.setText(petBreed);
            mWeightEditText.setText(String.valueOf(petWeight));

            // set the dropdown spinner to display the correct gender.
            switch(petGender) {
                case PetEntry.GENDER_MALE:
                    mGenderSpinner.setSelection(1);
                    break;
                case PetEntry.GENDER_FEMALE:
                    mGenderSpinner.setSelection(2);
                    break;
                default:
                    mGenderSpinner.setSelection(0);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0);
    }
}