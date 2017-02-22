package com.example.android.pets.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import static com.example.android.pets.data.PetContract.PetEntry.GENDER_FEMALE;
import static com.example.android.pets.data.PetContract.PetEntry.GENDER_MALE;
import static com.example.android.pets.data.PetContract.PetEntry.GENDER_UNKNOWN;


public final class PetContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.pets";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PETS = "pets";

    private PetContract() {}

    // Inner class that defines the table contents
    public static abstract class PetEntry implements BaseColumns {

        // The MIME type for a list of pets.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS;

        // The MIME type for a single pet.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS;


        /** The content URI to access the pet data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PETS);

        public static final String TABLE_NAME = "pets";

        // table columns
        public static final String _ID = BaseColumns._ID;

        public static final String COLUMN_PET_NAME = "name";      // String
        public static final String COLUMN_PET_BREED = "breed";    // String
        public static final String COLUMN_PET_GENDER = "gender";  // int
        public static final String COLUMN_PET_WEIGHT = "weight";  // int

        // Possible values for the gender of animals.
        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;
    }

    public static boolean isGenderValueValid(int genderValue) {
        switch (genderValue) {
            case GENDER_UNKNOWN:
                return true;
            case GENDER_MALE:
                return true;
            case GENDER_FEMALE:
                return true;
            default:
                return false;
        }
    }
}
