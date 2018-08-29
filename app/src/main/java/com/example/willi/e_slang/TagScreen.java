package com.example.willi.e_slang;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * TagScreen (WordScreenTab2 -> TagScreen) contains all the words which are related to the chosen tag
 */

public class TagScreen extends AppCompatActivity {
    DbManager dbm;
    Context ctx;

    //stores the inputs
    String flag;
    String tag;
    String imageFlag;

    TextView name_of_country;
    TextView instruction;

    //for displaying the flag icon
    int flagIdInt;

    LinearLayout displayTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_screen);

        //intialises database
        ctx = this;
        dbm = DbManager.getInstance();
        dbm.mCtx = this;
        dbm.open();
    }

    protected void onStart() {
        super.onStart();

        //get data from WordScreenTab2
        Intent intent = getIntent();
        flag = intent.getStringExtra("flag");
        tag = intent.getStringExtra("tag");

        //dynamically displays the flag depending on country selected
        imageFlag = setFirstCharToLower(flag);
        ImageView img = (ImageView) findViewById(R.id.country_image);
        flagIdInt = getResources().getIdentifier(imageFlag, "drawable", getPackageName());
        img.setImageResource(flagIdInt);

        //displays name of the country
        name_of_country = (TextView) findViewById(R.id.name_of_country);
        name_of_country.setText(flag);
        //displays some text to guide the user
        instruction = (TextView) findViewById(R.id.explanation);
        instruction.setText("Words with the tag " + tag + " are shown below.");

        //displays the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //displays the action bar text
        getSupportActionBar().setTitle(tag);

        displayTags = (LinearLayout) findViewById(R.id.scroll_layout);

        //tag are stored in the database with #<tag name>.
        //this removes it for easier searching
        tag = tag.replace("#", "");

        //gets all the words from the databse which contain the tag selected
        ArrayList<String> tagList = new ArrayList<>();
        Cursor cursor = dbm.getAllWordsCursor(flag);
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(8).contains(tag)) {
                    tagList.add(cursor.getString(1));
                }
            } while (cursor.moveToNext());
        }
        displayTags.removeAllViews();
        cursor.close();

        //shows the tags in the TextView
        for (int i = 0; i < tagList.size(); i++) {
            TextView htext = new TextView(ctx);
            htext.setText(tagList.get(i));
            htext.setTextSize(18);

            htext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView b = (TextView) v;
                    goToWordScreen(b.getText().toString());
                }
            });
            displayTags.addView(htext);
        }
    }

    //called when the user selects the word
    private void goToWordScreen(String word) {
        Intent myIntent = new Intent(this, WordScreen.class);

        myIntent.putExtra("word", word);
        myIntent.putExtra("flag", flag);

        Bundle bundle = new Bundle();
        bundle.putString("word", word);
        bundle.putString("flag", flag);

        WordScreenTab1 send3 = new WordScreenTab1();
        send3.setArguments(bundle);

        startActivity(myIntent);
    }

    //when back button is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    //sets and returns the first char of "flag" from a capital case to a lower case for displaying the flag icon
    private String setFirstCharToLower(String str) {
        str = str.substring(0, 1).toLowerCase() + str.substring(1).toLowerCase();
        return str;
    }
}
