package com.example.willi.e_slang;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


/**
 * Second activity (2 of 3) when adding a new word to the database.
 * Consists of the characteristics, examples, video URL and tags.
 */

public class AddWord2 extends AppCompatActivity {
    DbManager dbm;

    //fields for input
    EditText inputCharacter;
    EditText inputExample;
    EditText inputVideoUrl;
    EditText inputTag;

    //store the inputs
    String shortDef;
    String longDef;
    String character;
    String example;
    String videoUrl;
    String flag;
    String word;

    //character count (see if number of characters exceed)
    boolean characterCountExceeded = false;
    boolean exampleCountExceeded = false;
    boolean videoUrlCountExceeded = false;
    boolean tagCountExceeded = false;

    //displays the tags
    TextView tagList;

    //keeps track of the number of tags added (refer to addTag method)
    int numberOfTagListElements;

    //stores all the tags added
    ArrayList<String> tag = new ArrayList<>();

    //Going from AddWord1 -> AddWord2 creates a new activity (all previous inputs are erased)
    //SharedPreferences stores the inputs in case user goes from AddWord2 -> AddWord1 -> AddWord2
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    Context context = AddWord2.this;

    //what to do if character counter is greater than or less than the number of characters entered
    TextInputLayout characterCountCharacteristic;
    public final TextWatcher characterTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() > characterCountCharacteristic.getCounterMaxLength()) {
                characterCountExceeded = true;
                characterCountCharacteristic.setError("Character count exceeded!");
            } else {
                characterCountExceeded = false;
                characterCountCharacteristic.setErrorEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
    TextInputLayout characterCountExample;
    public final TextWatcher exampleTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() > characterCountExample.getCounterMaxLength()) {
                exampleCountExceeded = true;
                characterCountExample.setError("Character count exceeded!");
            } else {
                exampleCountExceeded = false;
                characterCountExample.setErrorEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
    TextInputLayout characterCountVideoUrl;
    public final TextWatcher videoUrlTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() > characterCountVideoUrl.getCounterMaxLength()) {
                videoUrlCountExceeded = true;
                characterCountVideoUrl.setError("Character count exceeded!");
            } else {
                videoUrlCountExceeded = false;
                characterCountVideoUrl.setErrorEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
    TextInputLayout characterCountTag;
    public final TextWatcher tagTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() > characterCountTag.getCounterMaxLength()) {
                tagCountExceeded = true;
                characterCountTag.setError("Character count exceeded!");
            } else {
                tagCountExceeded = false;
                characterCountTag.setErrorEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_word_2);

        //intialises database
        dbm = DbManager.getInstance();
        dbm.mCtx = this;
        dbm.open();

        //initialise variables of edit text fields
        inputCharacter = (EditText) findViewById(R.id.character);
        inputExample = (EditText) findViewById(R.id.example);
        inputVideoUrl = (EditText) findViewById(R.id.videoUrl);
        inputTag = (EditText) findViewById(R.id.tag);
        tagList = (TextView) findViewById(R.id.tagListView);

        //loads saved SharedPreferences when user goes from AddWord1 -> AddWord2
        prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = prefs.edit();

        String restoredCharacter = prefs.getString("character", null);
        String restoredExample = prefs.getString("example", null);
        String restoredVideoUrl = prefs.getString("videoUrl", null);

        //sets the text
        inputCharacter.setText(restoredCharacter);
        inputExample.setText(restoredExample);
        inputVideoUrl.setText(restoredVideoUrl);
    }

    protected void onStart() {
        super.onStart();

        //for url checking - makes sure it does not run on main process
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //initialises the database
        dbm = DbManager.getInstance();
        dbm.mCtx = this;
        dbm.open();

        //gets information from AddWord1
        Intent intent = getIntent();
        word = intent.getStringExtra("word");
        shortDef = intent.getStringExtra("shortDef");
        longDef = intent.getStringExtra("longDef");
        flag = intent.getStringExtra("flag");

        //initialise variables of text input layout (for character count)
        characterCountCharacteristic = (TextInputLayout) findViewById(R.id.char_char);
        characterCountExample = (TextInputLayout) findViewById(R.id.char_example);
        characterCountVideoUrl = (TextInputLayout) findViewById(R.id.char_video_url);
        characterCountTag = (TextInputLayout) findViewById(R.id.char_tag);

        //displays action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //sets action bar title
        getSupportActionBar().setTitle("More Info (2/3)");

        //initialise the number of characters for each character counter
        setCharacterCount();
    }

    //upon clicking Next in the action bar, go to the next activity (AddWord3)
    private void goToAddWord3() {
        //gets the inputs from the input fields
        character = inputCharacter.getText().toString();
        example = inputExample.getText().toString();
        videoUrl = inputVideoUrl.getText().toString();

        //error checking
        //check if there is any empty field
        if (checkEmptyEntry()) {
            Toast.makeText(this, "Error: Empty field!", Toast.LENGTH_SHORT).show();
        }
        //check if number of characters exceeded
        else if (characterCountExceeded || exampleCountExceeded || videoUrlCountExceeded || tagCountExceeded) {
            Toast.makeText(this, "Error: Character count exceeded! Please edit your entry again.", Toast.LENGTH_SHORT).show();
        }
        //check if url is valid
        else if (checkUrl(inputVideoUrl.getText().toString())) {
            invalidVideoUrlPopUp();
        }
        //all error checking passes, go to AddWord3
        else {
            Intent myIntent = new Intent(this, AddWord3.class);

            character = removeSpacesAtEnd(character);
            example = removeSpacesAtEnd(example);
            videoUrl = removeSpacesAtEnd(videoUrl);

            //sends information to AddWord3
            myIntent.putExtra("word", word);
            myIntent.putExtra("flag", flag);
            myIntent.putExtra("shortDef", shortDef);
            myIntent.putExtra("longDef", longDef);
            myIntent.putExtra("character", character);
            myIntent.putExtra("example", example);
            myIntent.putExtra("videoUrl", videoUrl);
            myIntent.putStringArrayListExtra("tag", tag);

            startActivity(myIntent);
        }
    }

    //adds tags to tag (ArrayList) and displays tags in tagList (TextView)
    public void addTag(View v) {
        //get tag that is entered in input field
        String inputString = inputTag.getText().toString();

        //gets rid of any spaces at the end
        inputString = removeSpacesAtEnd(inputString);

        //check if tag starts with #
        if (inputString.toString().contains("#")) {
            inputTag.setText("");
            inputTag.setHint("Please do not start your tag with '#'!");
        }
        //check if max number of tags (10) reached
        else if (tag.size() == 10) {
            inputTag.setText("");
            inputTag.setHint("Maximum number of tags added!");
        }
        // check for empty input
        else if (inputTag.getText().toString().isEmpty()) {
            inputTag.setHint("Empty tag!");
        }
        //check if added tag is already added
        else {
            //for comparison
            String temp;

            //all error checking passes, tag added to ArrayList tag
            tag.add("#" + inputString);

            //tag added to temp String for comparison to see if it has already been added
            temp = "#" + inputString;

            //see if tag has been added or not
            boolean added = false;

            //cycles through all the tags that have been already added in ArrayList tag
            //the tag that was just added (temp) is compared with all cycled elements
            for (int i = 0; i < numberOfTagListElements; i++) {
                if (temp.contentEquals(tag.get(i).toString())) {
                    inputTag.setText("");
                    inputTag.setHint("Tag already added!");
                    tag.remove(i);
                    numberOfTagListElements--;
                    added = true;
                }
            }
            //tag is not entered, display on tagList (EditText)
            //if tag is already entered (aded == true), tag is not displayed
            if (!added) {
                tagList = (TextView) findViewById(R.id.tagListView);
                inputTag.setHint("");
                tagList.setText(" " + temp + tagList.getText().toString() + " ");
                inputTag.setText("");
            }
            //increments the number of tags
            numberOfTagListElements++;
        }
    }

    // removes spaces at the end of any input. This is to prevent any errors that may arise.
    // if string == "apple banana coconut    "
    // returns "apple banana coconut"
    private String removeSpacesAtEnd(String string) {
        ArrayList<String> tempArrayList = new ArrayList<>();
        String modifiedInputString = new String();

        //splits the input string using the regex of a space (" ")
        //if string == "apple banana coconut"
        //tempInputString == "[apple, banana, coconut]"
        String[] tempInputString = string.split(" ");

        //adds each element in the tempInputString into the tempArrayList
        //using an ArrayList makes it easier to manipulate the data
        //tempArrayList == "[apple, banana, coconut]"
        for (int i = 0; i < tempInputString.length; i++) {
            tempArrayList.add(tempInputString[i]);
        }

        //adds all the elements in tempArrayList with spaces in between to form back the original string without spaces at the end
        //modifiedInputString = "[ apple banana coconut]"
        for (int i = 0; i < tempArrayList.size(); i++) {
            modifiedInputString = modifiedInputString.concat(" " + tempArrayList.get(i));
        }

        //removes the first " " in modifiedInputString
        //returns "[apple banana coconut]
        return modifiedInputString.replaceFirst(" ", "");
    }

    //set the number of characters for each character counter
    private void setCharacterCount() {
        characterCountCharacteristic.setCounterMaxLength(150);
        characterCountExample.setCounterMaxLength(300);
        characterCountVideoUrl.setCounterMaxLength(50);
        characterCountTag.setCounterMaxLength(20);

        inputCharacter.addTextChangedListener(characterTextWatcher);
        inputExample.addTextChangedListener(exampleTextWatcher);
        inputVideoUrl.addTextChangedListener(videoUrlTextWatcher);
        inputTag.addTextChangedListener(tagTextWatcher);
    }

    //checks if inputted URL is valid
    //returns true if invalid
    //returns false if valid
    private boolean checkUrl(String url) {
        //checks if url input is empty
        if (TextUtils.isEmpty(url)) {
            return false;
        }

        //checks if url contains "youtu"
        else if (videoUrlStringChecker(url)) {
            //checks if URL is valid
            if ((!urlChecker(url))) {
                return true;
            } else {
                return false;
            }
        } else
            return true;
    }

    //checks if URL is valid (can be accessed)
    private boolean urlChecker(String url) {
        HttpURLConnection connection = null;

        try {
            URL u = new URL(url);
            connection = (HttpURLConnection) u.openConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    //checks if URL starts with youtu
    //returns true if contains
    //returns false if doesn't contain
    private boolean videoUrlStringChecker(String url) {
        if (url.contains("youtu")) {
            return true;
        } else {
            return false;
        }
    }

    //checks if any input field is empty
    //returns true if there is
    //returns false if there isn't
    private boolean checkEmptyEntry() {
        boolean empty = false;

        if (TextUtils.isEmpty(inputCharacter.getText().toString())) {
            inputCharacter.setHint("Error: Empty Field!");
            empty = true;
        }

        if (TextUtils.isEmpty(inputExample.getText().toString())) {
            inputExample.setHint("Error: Empty Field!");
            empty = true;
        }
        return empty;
    }

    //what to do it "Next" and back buttons are pressed
    //as user goes from AddWord2 -> AddWord3, SharedPreferences are deleted
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.next_screen) {
            SharedPreferences prefs;
            SharedPreferences.Editor editor;
            Context context = AddWord2.this;

            prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            editor = prefs.edit();
            editor.remove("character");
            editor.remove("example");
            editor.remove("videoUrl");
            editor.commit();

            goToAddWord3();
        } else
            onBackPressed();
        return true;
    }

    //creates next button in actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.go_to_next_screen, menu);
        return true;
    }

    //overrides the default onBackPressed method when the back button is pressed
    //stores inputs in SharedPreferences
    @Override
    public void onBackPressed() {
        editor.putString("character", inputCharacter.getText().toString());
        editor.putString("example", inputExample.getText().toString());
        editor.putString("videoUrl", inputVideoUrl.getText().toString());
        editor.commit();
        super.onBackPressed();
    }

    //popup when video URL is invalid
    public void invalidVideoUrlPopUp() {
        AlertDialog.Builder messageBox = new AlertDialog.Builder(this);

        messageBox.setTitle("Invalid Entry!");
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("Continue editing", null);
        messageBox.setMessage("Your video URL is invalid!\n\nPlease check it again.");
        messageBox.show();
    }
}