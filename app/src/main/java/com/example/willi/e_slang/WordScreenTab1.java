package com.example.willi.e_slang;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * WordScreenTab1 shows the word, country, short definition and characteristic and tags.
 */

public class WordScreenTab1 extends Fragment {
    DbManager dbm;
    Cursor cursor;

    //stores the inputs
    String word;
    String id;
    String flag;
    String characteristicString;
    String tagsString;

    //displays the respective information
    TextView shortDefTV;
    TextView characteristicTV;
    TextView countryTV;
    TextView tagsTV;
    //displays the flag icon and name
    ImageView img;
    String flagId;
    int flagIdInt;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.word_screen_tab_1, container, false);

        //initialise database
        dbm = DbManager.getInstance();
        dbm.mCtx = getContext();
        dbm.open();

        shortDefTV = (TextView) view.findViewById(R.id.short_def);
        characteristicTV = (TextView) view.findViewById(R.id.characteristics);
        countryTV = (TextView) view.findViewById(R.id.Country);
        tagsTV = (TextView) view.findViewById(R.id.tag);

        //get data from DictionaryScreen or TagScreen
        Bundle b = getActivity().getIntent().getExtras();
        flag = b.getString("flag");
        word = b.getString("word");

        Intent intent = getActivity().getIntent();
        flag = intent.getStringExtra("flag");
        word = intent.getStringExtra("word");

        //for dynamically displaying the flag depending on country selected
        flagId = setFirstCharToLower(flag);
        img = (ImageView) view.findViewById(R.id.country_image);
        flagIdInt = getResources().getIdentifier(flagId, "drawable", getActivity().getPackageName());
        img.setImageResource(flagIdInt);

        setText();
        return view;
    }

    //displays the data retrieved
    private void setText() {
        cursor = dbm.getOneWord(flag, word);

        //gets the short dev, characteristics, country and tags of the selected word from the db
        if (cursor.moveToFirst()) {
            do {
                shortDefTV.setText(dbm.elaborateDesc(cursor.getString(cursor.getColumnIndex("short"))));
                characteristicString = dbm.elaborateDesc(cursor.getString(cursor.getColumnIndex("characteristic")));
                countryTV.setText(cursor.getString(cursor.getColumnIndex("country")));
                tagsString = dbm.getTags(cursor.getString(cursor.getColumnIndex("tag"))).toString();
            } while (cursor.moveToNext());
        }
        cursor.close();

        //in case there are empty inputs
        if (TextUtils.isEmpty(tagsString)) {
            tagsTV.setText("No tag found!");
        } else {
            tagsString = tagsString.replaceAll("#", "\n#");
            tagsTV.setText(tagsString);
        }
    }

    //sets the first char of "flag" from a capital case to a lower case for displaying the flag icon
    public String setFirstCharToLower(String str) {
        str = str.substring(0, 1).toLowerCase() + str.substring(1).toLowerCase();
        return str;
    }
}
