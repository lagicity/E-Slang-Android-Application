package com.example.willi.e_slang;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * First activity (1 of 3) when adding a new word to the database.
 * Consists of the word, country, short def and long def.
 */

public class AddWord1 extends AppCompatActivity {
    DbManager dbm;

    //fields for input
    EditText inputWord;
    EditText inputShortDef;
    EditText inputLongDef;

    //stores the inputs
    String flag;
    String word;
    String shortDef;
    String longDef;

    //character count (see if number of characters exceed)
    boolean wordCountExceeded = false;
    boolean shortDefCountExceeded = false;
    boolean longDefCountExceeded = false;

    //checks if there is an input entered and the user wants to go back to the main activity
    boolean goBack = false;

    //spinner to select country
    Spinner countrySpinner;

    //list of countries to display in the spinner
    String[] countryList;

    //what to do if character counter is greater than or less than the number of characters entered
    TextInputLayout characterCountWord;
    private final TextWatcher wordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() > characterCountWord.getCounterMaxLength()) {
                wordCountExceeded = true;
                characterCountWord.setError("Character count exceeded!");
            } else {
                wordCountExceeded = false;
                characterCountWord.setErrorEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
    TextInputLayout characterCountShortDef;
    private final TextWatcher shortDefTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() > characterCountShortDef.getCounterMaxLength()) {
                shortDefCountExceeded = true;
                characterCountShortDef.setError("Character count exceeded!");
            } else {
                shortDefCountExceeded = false;
                characterCountShortDef.setErrorEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
    TextInputLayout characterCountLongDef;
    private final TextWatcher longDefTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() > characterCountLongDef.getCounterMaxLength()) {
                longDefCountExceeded = true;
                characterCountLongDef.setError("Character count exceeded!");
            } else {
                longDefCountExceeded = false;
                characterCountLongDef.setErrorEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_word_1);

        //intialises database
        dbm = DbManager.getInstance();
        dbm.mCtx = this;
        dbm.open();
    }

    protected void onStart() {
        super.onStart();

        //intialises database
        dbm = DbManager.getInstance();
        dbm.mCtx = this;
        dbm.open();

        //initialise variables of edit text fields
        inputWord = (EditText) findViewById(R.id.wordName);
        inputShortDef = (EditText) findViewById(R.id.shortTerm);
        inputLongDef = (EditText) findViewById(R.id.longTerm);

        //initialise variables of text input layout (for character count)
        characterCountWord = (TextInputLayout) findViewById(R.id.char_word);
        characterCountShortDef = (TextInputLayout) findViewById(R.id.char_short_def);
        characterCountLongDef = (TextInputLayout) findViewById(R.id.char_long_def);

        //displays action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //sets action bar title
        getSupportActionBar().setTitle("Basic Info (1/3)");

        //initialise the number of characters for each character counter
        setCharacterCount();
        //initialises the country spinner
        initialiseCountrySpinner();
    }

    //upon clicking Next in the action bar, go to the next activity (AddWord2)
    private void goToAddWord2() {
        //gets the inputs from the input fields
        word = inputWord.getText().toString();
        shortDef = inputShortDef.getText().toString();
        longDef = inputLongDef.getText().toString();

        //error checking
        //check if there is any empty field
        if (checkEmptyEntry()) {
            Toast.makeText(this, "Error: Empty field!", Toast.LENGTH_SHORT).show();
        }
        //check if number of characters exceeded
        else if (wordCountExceeded || shortDefCountExceeded || longDefCountExceeded) {
            Toast.makeText(this, "Error: Character count exceeded! Please edit your entry.", Toast.LENGTH_SHORT).show();
        }
        //check if word is in db
        else if (checkWord(flag, word)) {
            inputDataExistsPopUp();
        }
        //all error checking passes, go to AddWord2
        else {
            Intent myIntent = new Intent(this, AddWord2.class);

            word = removeSpacesAtEnd(word);
            shortDef = removeSpacesAtEnd(shortDef);
            longDef = removeSpacesAtEnd(longDef);

            //sends information to AddWord2
            myIntent.putExtra("word", word);
            myIntent.putExtra("flag", flag);
            myIntent.putExtra("shortDef", shortDef);
            myIntent.putExtra("longDef", longDef);
            startActivity(myIntent);
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
        characterCountWord.setCounterMaxLength(20);
        characterCountShortDef.setCounterMaxLength(150);
        characterCountLongDef.setCounterMaxLength(300);

        inputWord.addTextChangedListener(wordTextWatcher);
        inputShortDef.addTextChangedListener(shortDefTextWatcher);
        inputLongDef.addTextChangedListener(longDefTextWatcher);
    }

    //spinner to display countries and determine country selected
    private String initialiseCountrySpinner() {
        Resources res = getResources();

        countrySpinner = (Spinner) findViewById(R.id.country_spinner);
        countryList = res.getStringArray(R.array.spinner_countries);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, countryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(adapter);

        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                flag = countryList[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        return flag;
    }

    //checks if word exists in db
    //returns true if it exists
    //returns false if it doesn't
    private boolean checkWord(String flag, String word) {
        ArrayList<String> wordsInCountry;

        //gets all the words from the flag selected in the spinner
        wordsInCountry = dbm.getAllWords(flag);

        //compares the input word (word) with all the elements in wordsInCountry
        for (int i = 0; i < wordsInCountry.size(); i++) {
            if (word.contentEquals(wordsInCountry.get(i).toString()))
                return true;
        }
        return false;
    }

    //checks if any input field is empty
    //returns true if there is
    //returns false if there isn't
    private boolean checkEmptyEntry() {
        boolean empty = false;
        if (TextUtils.isEmpty(inputWord.getText().toString())) {
            inputWord.setHint("Error: Empty field!");
            empty = true;
        }

        if (TextUtils.isEmpty(inputShortDef.getText().toString())) {
            inputShortDef.setHint("Error: Empty field!");
            empty = true;
        }

        if (TextUtils.isEmpty(inputLongDef.getText().toString())) {
            inputLongDef.setHint("Error: Empty field!");
            empty = true;
        }
        return empty;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.next_screen) {
            goToAddWord2();
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
    @Override
    public void onBackPressed() {
        //if there is an empty field
        if (TextUtils.isEmpty(inputWord.getText().toString()) ||
                TextUtils.isEmpty(inputShortDef.getText().toString()) ||
                TextUtils.isEmpty(inputLongDef.getText().toString())) {
            removeSharedPreferences();
            super.onBackPressed();
        } else {
            //goBack is default to false, so goPackPopUp will always be called on pressing back
            if (goBack) {
                super.onBackPressed();
                removeSharedPreferences();
            } else {
                goBackPopUp();
            }
        }
    }

    //When the user goes from AddWord2 -> AddWord1 -> AddWord2, AddWord2 is created again and therefore all input in AddWord2 will be lost.
    //SharedPreferences are used in AddWord2 to save (user goes from AddWord2 -> AddWord1) and load (user goes from AddWord1 -> AddWord2) any input that are already inputted there.
    //This portion deletes the data in the editor.
    //If it is not deleted, the input in AddWord2 will always remain (even if the user goes back to MainActivity)
    private void removeSharedPreferences() {
        SharedPreferences prefs;
        SharedPreferences.Editor editor;
        Context context = AddWord1.this;

        prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = prefs.edit();
        editor.remove("character");
        editor.remove("example");
        editor.remove("videoUrl");
        editor.commit();
    }

    //popup when word exists in db is true
    private void inputDataExistsPopUp() {
        AlertDialog.Builder messageBox = new AlertDialog.Builder(this);

        messageBox.setTitle("Word exists!");
        messageBox.setMessage("The word you entered already exists! Please enter a new word.");
        messageBox.setNeutralButton("Close", null);
        messageBox.setCancelable(false);
        messageBox.show();
    }

    //popup when there is any input and the user chooses to go back to MainActivity
    //returns goBack == true if user presses to go back
    //returns goBack == false if user presses cancel
    private boolean goBackPopUp() {
        AlertDialog.Builder messageBox = new AlertDialog.Builder(this);

        messageBox.setPositiveButton("Go back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                goBack = true;
                onBackPressed();
            }
        });
        messageBox.setTitle("Warning!");
        messageBox.setMessage("Are you sure you want to go back? You will lose all your data.");
        messageBox.setCancelable(false);
        messageBox.setNegativeButton("Cancel", null);
        messageBox.show();
        return goBack;
    }
}