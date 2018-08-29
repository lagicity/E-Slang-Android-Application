package com.example.willi.e_slang;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * DictionaryTab2 displays all the tags of a country and allows the user to search for them.
 */

public class DictionaryTab2 extends Fragment {
    DbManager dbm;
    Context ctx;

    //displays the country name
    TextView name_of_country;
    String flag;

    //field for searching
    EditText searchText;

    //displays the flag icon
    String imageFlag;
    int flagIdInt;

    //initialise the cursor
    Cursor cursor;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dictionary_tab_2, container, false);

        //intialises database
        ctx = getContext();
        dbm = DbManager.getInstance();
        dbm.mCtx = getContext();
        dbm.open();

        //gets data from MainActivity
        Bundle b = getActivity().getIntent().getExtras();
        flag = b.getString("flag");

        //for dynamically displaying the flag depending on country selected
        //all flags are passed as England, Korea etc. However, xml only accepts lower case titles (england, korea)
        //setFirstCharToLower changes makes the capital letter a lower case letter (England -> england)
        imageFlag = setFirstCharToLower(flag);
        ImageView img = (ImageView) view.findViewById(R.id.country_image);
        flagIdInt = getResources().getIdentifier(imageFlag, "drawable", getActivity().getPackageName());
        img.setImageResource(flagIdInt);

        //sets and displays the name of the country
        name_of_country = (TextView) view.findViewById(R.id.name_of_country);
        name_of_country.setText(flag);

        //sets and displays the input field for searching the tag
        searchText = (EditText) view.findViewById(R.id.searchText);
        final LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.scroll_layout);

        cursor = dbm.getTypeData(flag);

        //dynamically searches database for matching tags every time new character is entered
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ArrayList<String> tmp2 = new ArrayList<String>();
                ArrayList<String> temp = new ArrayList<String>();
                cursor = dbm.getAllWordsCursor(flag);
                if (cursor.moveToFirst()) {
                    do {
                        if (cursor.getString(8).contains(searchText.getText()))
                            tmp2.add(cursor.getString(8));
                    } while (cursor.moveToNext());
                }

                String string = dbm.arrayListToStringTag(tmp2);
                tmp2 = dbm.stringToArrayListTag(string);

                for (int i = 0; i < tmp2.size(); i++) {
                    if (tmp2.get(i).toString().contains(searchText.getText()))
                        temp.add(tmp2.get(i).toString());
                }

                //removes any duplicated tags and sorts them alphabetically
                temp = deleteDuplicates(temp);
                //displays the tags
                displayTags(temp, linearLayout);
                cursor.close();
            }
        });
        return view;

    }

    //deletes and duplicates that may occur due to multiple indentical tags
    //alphabetically sorts the tags
    private ArrayList<String> deleteDuplicates(ArrayList<String> array) {
        for (int i = 0; i < array.size(); i++) {
            for (int j = array.size() - 1; j > i; j--) {
                if (array.get(i).toString().contentEquals(array.get(j).toString())) {
                    array.remove(j);
                }
            }
        }
        // sorts arraylist alphabetically
        java.util.Collections.sort(array);
        return array;
    }

    //displays the tags in a LinearLayout format
    private void displayTags(ArrayList<String> list, LinearLayout linearLayout) {
        linearLayout.removeAllViews();

        for (int i = 0; i < list.size(); i++) {
            TextView htext = new TextView(ctx);
            htext.setText(list.get(i));
            htext.setTextSize(18);

            htext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView b = (TextView) v;
                    goToTagScreen(b.getText().toString());
                }
            });
            linearLayout.addView(htext);

        }
    }

    //sets the first char of "flag" from a capital case to a lower case for displaying the flag icon
    private String setFirstCharToLower(String str) {
        str = str.substring(0, 1).toLowerCase() + str.substring(1).toLowerCase();
        return str;
    }

    //what to do when user selects a tag
    private void goToTagScreen(String tag) {
        Intent myIntent = new Intent(getActivity(), TagScreen.class);

        //sends data to TagScreen
        myIntent.putExtra("tag", tag); // string you want to pass, variable to receive
        myIntent.putExtra("flag", flag); // string you want to pass, variable to receive

        startActivity(myIntent);
    }
}
