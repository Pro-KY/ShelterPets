package com.example.android.pets;

/**
 * PetCursorAdapter is an adapter for a list or grid view
 * that uses a Cursor of pet data as its data source. This adapter knows
 * how to create list items for each row of pet data in the Cursor.
 */

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.pets.data.PetContract.PetEntry;

public class PetCursorAdapter extends CursorAdapter {

    public PetCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    //  The newView method is used to inflate a new view and return it,
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView summaryTextView = (TextView) view.findViewById(R.id.summary);

        // Find the columns of pet attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
        int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);

        // Read the pet attributes from the Cursor for the current pet
        String name = cursor.getString(nameColumnIndex);
        String breed = cursor.getString(breedColumnIndex);

        // Populate fields with extracted properties
        nameTextView.setText(name);

        // if pet breed is unknown, set 'Unknown breed' title instead of empty string
        if(TextUtils.isEmpty(breed)) {
            summaryTextView.setText(R.string.unknown_listview_breed_title_text);
        } else {
            // Otherwise, set user-entered breed
            summaryTextView.setText(breed);
        }
}
}
