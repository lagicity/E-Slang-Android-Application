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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

/**
 * DictionaryTab1 displays all the words of a country and allows the user to search for them.
 */

public class DictionaryTab1 extends Fragment {
    DbManager dbm;
    Context ctx;

    //displays the country name
    TextView name_of_country;
    String flag;

    //field for searching
    EditText searchText;

    //ArrayList for storing all the words of a country from the database
    ArrayList<String> allWordsArrayList;

    //displays the flag icon
    String imageFlag;
    int flagIdInt;

    //initialise the cursor
    Cursor cursor;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dictionary_tab_1, container, false);

        //allows the "Surprise Me!" button to be fixed to this fragment only (does not appear in DictionaryTab2)
        setHasOptionsMenu(true);

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

        //sets and displays the input field for searching the word
        searchText = (EditText) view.findViewById(R.id.searchText);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.scroll_layout);

        //gets all the words from flag from the database
        allWordsArrayList = dbm.getAllWords(flag);
        displayWords(allWordsArrayList, linearLayout);

        //dynamically searches database for matching words every time new character is entered
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            //looks up the database every time a there is an input change
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.scroll_layout);
                ArrayList<String> tmp2 = new ArrayList<>();
                cursor = dbm.getTypeData(flag);

                if (cursor.moveToFirst()) {
                    do {
                        if (cursor.getString(1).contains(searchText.getText())) {
                            tmp2.add(cursor.getString(1));
                        }
                    } while (cursor.moveToNext());
                }
                displayWords(tmp2, linearLayout);
                cursor.close();
            }
        });
        return view;
    }

    //randomly selects any word out of all the words
    private void surpriseMe() {
        Random r = new Random();
        String luckyWord;
        int max;
        int luckyNumber;

        //luckyNumber ranges from 0 to the total number of words (max)
        max = allWordsArrayList.size();
        luckyNumber = r.nextInt(max - 0) + 0;

        //gets the luckyWord of which its index is luckyNumber
        luckyWord = allWordsArrayList.get(luckyNumber);

        //sends the data to WordScreenTab1
        goToWordScreen(luckyWord);
    }

    //displays the words in a LinearLayout format
    private void displayWords(ArrayList<String> tmp2, LinearLayout linearLayout) {
        //removes all the words so that it is refreshed everytime the user enters a new character
        linearLayout.removeAllViews();

        for (int i = 0; i < tmp2.size(); i++) {
            TextView htext = new TextView(ctx);

            htext.setText(tmp2.get(i));
            htext.setTextSize(18);

            htext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView b = (TextView) v;
                    goToWordScreen(b.getText().toString());
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

    //what to do when the user selects a word
    private void goToWordScreen(String word) {
        Intent myIntent = new Intent(getActivity(), WordScreen.class);

        //sends the data to WordScreenTab1
        myIntent.putExtra("word", word);
        myIntent.putExtra("flag", flag);

        Bundle bundle2 = new Bundle();
        bundle2.putString("word", word);
        bundle2.putString("flag", flag);

        WordScreenTab1 send2 = new WordScreenTab1();
        send2.setArguments(bundle2);

        startActivity(myIntent);
    }

    //displays the surprise me in actionbar
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.go_to_surprise_me, menu);
    }

    //what to do it "Surprise Me" and back buttons are pressed
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.surprise_me)
            surpriseMe();
        else
            getActivity().onBackPressed();
        return true;
    }
}
