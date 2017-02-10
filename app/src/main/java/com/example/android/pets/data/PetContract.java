package com.example.android.pets.data;

import android.provider.BaseColumns;


public final class PetContract {

    private PetContract() {}

    // Inner class that defines the table contents
    public static abstract class PetEntry implements BaseColumns {

        public static final String TABLE_NAME = "pets";

        // table columns
        static final String _ID = BaseColumns._ID;
        static final String COLUMN_PET_NAME = "name";
        static final String COLUMN_PET_BREED = "breed";
        static final String COLUMN_PET_GENDER = "gender";
        static final String COLUMN_PET_WEIGHT = "weight";

        // Possible values for the gender of animals.
        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;

    }
}
