package com.example.willi.e_slang;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Last activity (3 of 3) when adding a new word to the database.
 * Displays a preview of all the things the user has inputted for final checking
 * Adds the word into database
 */


public class AddWord3 extends AppCompatActivity {
    DbManager dbm;

    //fields for adding the input data into database
    ArrayList<String> addShortDef = new ArrayList<>();
    ArrayList<String> addLongDef = new ArrayList<>();
    ArrayList<String> addCharacter = new ArrayList<>();
    ArrayList<String> addExample = new ArrayList<>();
    ArrayList<String> addVideoUrl = new ArrayList<>();
    ArrayList<String> tag = new ArrayList<>();

    //displays the preview of what the user has inputted
    TextView textViewWord;
    TextView textViewShortDef;
    TextView textViewLongDef;
    TextView textViewCharacteristic;
    TextView textViewCountry;
    TextView textViewExample;
    TextView textViewVideoUrl;
    TextView textViewTag;

    //stores the inputs
    String flag;
    String word;
    String shortDef;
    String longDef;
    String character;
    String example;
    String videoUrl;

    //for the country picture
    ImageView img;

    //for dynamically selecting the picture that is shown
    String flagId;
    int flagIdInt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_word_3);

        //initialises database
        dbm = DbManager.getInstance();
        dbm.mCtx = this;
        dbm.open();
    }

    protected void onStart() {
        super.onStart();

        //initialises database
        dbm = DbManager.getInstance();
        dbm.mCtx = this;
        dbm.open();

        //gets information from AddWord2
        Intent intent = getIntent();
        word = intent.getStringExtra("word");
        flag = intent.getStringExtra("flag");
        shortDef = intent.getStringExtra("shortDef");
        longDef = intent.getStringExtra("longDef");
        character = intent.getStringExtra("character");
        example = intent.getStringExtra("example");
        videoUrl = intent.getStringExtra("videoUrl");
        tag = intent.getStringArrayListExtra("tag");

        //for dynamically displaying the flag depending on country selected
        //all flags are passed as England, Korea etc. However, xml only accepts lower case titles (england, korea)
        //setFirstCharToLower changes makes the capital letter a lower case letter (England -> england)
        flagId = setFirstCharToLower(flag);
        img = (ImageView) findViewById(R.id.country_image);
        flagIdInt = getResources().getIdentifier(flagId, "drawable", this.getPackageName());
        img.setImageResource(flagIdInt);

        //displays action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //sets action bar title
        getSupportActionBar().setTitle("Preview (3/3)");

        initialiseEditTextPreview();
    }

    //displays all the TextViews for preview
    private void initialiseEditTextPreview() {
        String previewTagString = new String();

        textViewWord = (TextView) findViewById(R.id.preview_word);
        textViewShortDef = (TextView) findViewById(R.id.preview_short_def);
        textViewLongDef = (TextView) findViewById(R.id.preview_long_def);
        textViewCharacteristic = (TextView) findViewById(R.id.preview_characteristic);
        textViewExample = (TextView) findViewById(R.id.preview_example);
        textViewCountry = (TextView) findViewById(R.id.preview_country);
        textViewVideoUrl = (TextView) findViewById(R.id.preview_video_url);
        textViewTag = (TextView) findViewById(R.id.preview_tag);

        for (int i = 0; i < tag.size(); i++)
            previewTagString = previewTagString.concat(tag.get(i).toString() + " ");

        textViewWord.setText(word);
        textViewShortDef.setText(shortDef);
        textViewLongDef.setText(longDef);
        textViewCharacteristic.setText(character);
        textViewExample.setText(example);
        textViewCountry.setText(flag);

        //error checking if there is no input
        if (TextUtils.isEmpty(videoUrl)) {
            textViewVideoUrl.setText("No URL entered");
        } else {
            textViewVideoUrl.setText(videoUrl);
        }

        if (tag.size() == 0) {
            textViewTag.setText("No tags entered");
        } else {
            textViewTag.setText(previewTagString);
        }
    }

    //adds the word into db when the add button is pressed
    private void addWord() {
        addShortDef.add(shortDef);
        addLongDef.add(longDef);
        addCharacter.add(character);
        addExample.add(example);
        addVideoUrl.add(videoUrl);

        dbm.insert(word, addShortDef, addLongDef, addCharacter, addExample, addVideoUrl, flag, tag);
        successPopUp();
    }

    //sets the first char of "flag" from a capital case to a lower case for displaying the flag icon
    private String setFirstCharToLower(String str) {
        str = str.substring(0, 1).toLowerCase() + str.substring(1).toLowerCase();
        return str;
    }

    //what to do it "Next" and back buttons are pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add_word_button) {
            addWord();
        } else
            onBackPressed();
        return true;
    }

    //creates add button in actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_word_button, menu);
        return true;
    }

    //popup when user successfully adds word
    private void successPopUp() {
        AlertDialog.Builder messageBox = new AlertDialog.Builder(this);

        messageBox.setTitle("Word Added!");
        messageBox.setMessage("You have successfully added " + word + " to " + flag + "!");
        AlertDialog.Builder builder = messageBox.setNeutralButton("Main Menu", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent myIntent = new Intent(AddWord3.this, MainActivity.class); // add word -> main activity
                startActivity(myIntent);
            }
        });
        messageBox.setCancelable(false);
        messageBox.show();
    }
}