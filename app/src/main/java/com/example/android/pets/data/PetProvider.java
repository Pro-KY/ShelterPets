package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import com.example.android.pets.data.PetContract.PetEntry;

import static android.R.attr.data;
import static android.R.attr.name;


/**
 *  Content provider for shelter app
 */

public class PetProvider extends ContentProvider {

    /** URI matcher code for the content URI for the pets table */
    private static final int PETS = 100;

    /** URI matcher code for the content URI for a single pet in the pets table */
    private static final int PET_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // TODO: Add 2 content URIs to URI matcher
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    /** Tag for the log messages */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();
    // Database helper object
    private PetDbHelper mPetDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // TODO: Create and initialize a PetDbHelper object to gain access to the pets database.
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mPetDbHelper = new PetDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments,
     * and sort order.
     */
    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {

        SQLiteDatabase database = mPetDbHelper.getReadableDatabase();

        Cursor cursor;
        int match = sUriMatcher.match(uri);

        // Choose the table to query and a sort order based on the code returned for the incoming URI.
        switch (match) {
            // If the incoming URI was for all of "pets" table
            case PETS:
                // Perform database query on pets table
                cursor = database.query(
                        PetEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            // If the incoming URI was for the single row
            case PET_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                // Perform a query on the pets table where the specified _id to return a
                // Cursor containing that row of the table.
                cursor = database.query(
                        PetEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                // If the URI is not recognized, you should do some error handling here.
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set the notification URI on the cursor. If the data at this URI changes, then we need to
        // update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mPetDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        int match = sUriMatcher.match(uri);

        switch(match) {
            case PETS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PET_ID:
                // Delete a single row given by the ID in the URI
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                rowsDeleted = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if(rowsDeleted != 0) {
            // Notify all listeners that the data has changed for the pet content URI.
            // uri: content://com.example.android.pets/pets
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of deleted rows
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        int match = sUriMatcher.match(uri);

        switch(match) {
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPet(Uri uri, ContentValues values) {

        checkDataValidation(values);

        // Get writable database
        SQLiteDatabase database = mPetDbHelper.getWritableDatabase();

        // Insert the new pet with the given values
        long newRowId = database.insert(
                PetEntry.TABLE_NAME,
                null,
                values
        );

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (newRowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI.
        // uri: content://com.example.android.pets/pets
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, newRowId);
    }

    // Update pets in the database with the given content values.
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mPetDbHelper.getWritableDatabase();

        if(values.size() > 0) {
            // check data validation
            checkDataValidation(values);

            // Perform the update on the database and get the number of rows affected
            int rowsUpdated = database.update(
                    PetEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs
            );

            // If 1 or more rows were updated, then notify all listeners that the data at the
            // given URI has changed
            if(rowsUpdated != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }

            // Return the number of rows updated
            return rowsUpdated;
        } else {
            // TODO: Return the number of rows that were affected
            return 0;
        }
    }

    // helper method for data validation
    private void checkDataValidation(ContentValues values) {

        // Check that the NAME is not null
        if(values.containsKey(PetEntry.COLUMN_PET_NAME)) {

            String name = values.getAsString(PetEntry.COLUMN_PET_NAME);

            if (name == null || name.length() == 0) {
                throw new IllegalArgumentException("Pet requires a name");
            } else {
                Log.d(LOG_TAG, "pet name is not null");
            }
        }

        // No need to check the breed, any value is valid (including null).

        // Check that the GENDER is not null
        if(values.containsKey(PetEntry.COLUMN_PET_GENDER)) {

            Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);

            if (gender != null && PetContract.isGenderValueValid(gender)) {
                Log.d(LOG_TAG, "pet gender is valid");
            } else {
                throw new IllegalArgumentException("Invalid gender value");
            }
        }

        // Check that the BREED is not null
        if(values.containsKey(PetEntry.COLUMN_PET_WEIGHT)) {

            Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);

            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Invalid weight value");
            } else {
                Log.d(LOG_TAG, "pet weight is valid value");
            }
        }


    }
}
