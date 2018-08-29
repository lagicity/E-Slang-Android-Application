package com.example.willi.e_slang;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * EditWordTab1 allows the editing of already inputted data
 */


public class EditWordTab1 extends Fragment {
    DbManager dbm;
    Context ctx;

    //field for input
    EditText editTextData;

    //determines whether to go back or not
    boolean goBack = false;
    //character count
    boolean characterCountExceeded = false;

    //displays the preview of the data from the typedef and option selected
    TextView preview;

    //stores the inputs
    String flag;
    String word;
    String id;

    //type selected (0 == word, 1 == short def, 2 == long def etc)
    int typedef;
    //option selected (0 == 1st short def, 1 == 2nd short def, 2 == 3rd short def etc)
    int option;

    //what to do if character counter is greater than or less than the number of characters entered
    TextInputLayout characterCount;
    public final TextWatcher inputDataTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 0)
                characterCount.setError("*Input required!");

            else if (s.length() > characterCount.getCounterMaxLength()) {
                characterCountExceeded = true;
                characterCount.setError("Character count exceeded!");
            } else {
                characterCountExceeded = false;
                characterCount.setErrorEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    Spinner spinnerTypeDef;
    Spinner spinnerOption;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.edit_word_tab_1, container, false);

        //allows the Edit button to be fixed to this fragment only (does not appear in EditWordTab2)
        setHasOptionsMenu(true);

        //for url checking. makes sure it does not run on main process
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //initialise database
        ctx = getContext();
        dbm = DbManager.getInstance();
        dbm.mCtx = getContext();
        dbm.open();

        //get data from WordScreen
        Bundle b = getActivity().getIntent().getExtras();
        flag = b.getString("flag");
        word = b.getString("word");

        //intialise the input field
        editTextData = (EditText) view.findViewById(R.id.input_data);
        //intialise the preview to be shown
        preview = (TextView) view.findViewById(R.id.edit_preview);

        //intialises the typedef and option spinners
        spinnerTypeDef = (Spinner) view.findViewById(R.id.type_of_def_spinner);
        spinnerOption = (Spinner) view.findViewById(R.id.choice_spinner);
        initialiseTypedefSpinner(spinnerTypeDef);
        initialiseOptionSpinner(spinnerOption, typedef);

        //initialise variables of text input layout (for character count)
        characterCount = (TextInputLayout) view.findViewById(R.id.character_count);
        editTextData.addTextChangedListener(inputDataTextWatcher);
        //listener to see what the user selects (typedef)
        spinnerTypeDef.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                //once typedef has been determined
                typedef = position;

                //calls setCharacterCount to show initialise the character counter
                setCharacterCount();

                //calls intialiaseOptionSpinner to show the number of options as according to how many exist in the database
                //ie. if there are 4 short defs in the database, it shows 1~4
                initialiseOptionSpinner(spinnerOption, typedef);

                //displays the preview of the option selected of the typedef
                //ie. if typedef == 1 (short def), option == 1 ("the second short definition")
                //getPreview previews "the second short definition"
                getPreview(typedef, option);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
        //listener to see what the user selects (option)
        spinnerOption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                option = position;

                //displays the preview of the option selected of the typedef
                //ie. if typedef == 1 (short def), option == 1 ("the second short definition")
                //getPreview previews "the second short definition"
                //getPreview has to be refreshed everytime another shortdef is chosen
                getPreview(typedef, option);

                //resets option back to 0
                option = 0;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
        return view;
    }

    //upon clicking edit word
    private void editWord() {
        //check for empty input
        if (checkEmptyEntry()) {
            Toast.makeText(getActivity(), "Error: Empty field!", Toast.LENGTH_SHORT).show(); // error check fails -> popup
            editTextData.setHintTextColor(Color.RED);
        } else {
            //gets the input data
            String inputData = editTextData.getText().toString();

            //index is the option selected (1~10)
            int index = spinnerOption.getSelectedItemPosition();

            //removes any spacing at the end
            inputData = removeSpacesAtEnd(inputData);

            //checks if character count is exceeded
            if (characterCountExceeded) {
                Toast.makeText(ctx, "Error: Character count exceeded! Please edit your entry again.", Toast.LENGTH_SHORT).show();
            } else if (typedef == 0) { //check specifically for word
                //check if word is in db
                if (checkWord(inputData)) {
                    Toast.makeText(ctx, "Error: You have not made any changes!", Toast.LENGTH_SHORT).show();
                } else {
                    dbm.insertWord(word, inputData);
                    successPopUp();
                }
            } else if (typedef == 5) { //check specifically for video URL
                //check if URL is valid
                if (checkUrl(inputData)) {
                    invalidVideoUrlPopUp();
                } else {
                    dbm.updateExactString(word, typedef, index, inputData);
                    successPopUp();
                }
            } else { //for all other types of data (short def, long def, characteristics etc)
                //check if data is in db
                if (checkData(inputData)) {
                    Toast.makeText(ctx, "Error: You have not made any changes!", Toast.LENGTH_SHORT).show();
                } else {
                    dbm.updateExactString(word, typedef, index, inputData);
                    successPopUp();
                }
            }
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

    //displays character count
    private void setCharacterCount() {
        switch (typedef) {
            case 0:
                characterCount.setCounterMaxLength(20);
                break;
            case 1:
                characterCount.setCounterMaxLength(150);
                break;
            case 2:
                characterCount.setCounterMaxLength(300);
                break;
            case 3:
                characterCount.setCounterMaxLength(150);
                break;
            case 4:
                characterCount.setCounterMaxLength(300);
                break;
            case 5:
                characterCount.setCounterMaxLength(50);
                break;
            case 6:
                characterCount.setCounterMaxLength(20);
                break;
        }
    }

    //returns the type that the user added (short def, long def, char etc)
    private String getAddedType() {
        switch (typedef) {
            case 0:
                return "word";
            case 1:
                return "short definition";
            case 2:
                return "long definition";
            case 3:
                return "characteristic";
            case 4:
                return "example";
            case 5:
                return "video URL";
            case 6:
                return "tag";
            default:
                return "error!";
        }
    }

    //displays the preview string
    private void getPreview(int type, int position) {
        //for word
        if (type == 0) {
            preview.setText(word);
            editTextData.setText(word);
        }

        //for videoURL
        else if (type == 5) {
            if (dbm.getHowManyDesc(word, 5) == 0) {
                preview.setText("No data found! You can add something in the Add to Word screen.");
                editTextData.setText("");
                editTextData.setHint("No data found!");
                editTextData.setHintTextColor(Color.RED);
            } else {
                preview.setText(dbm.getExactString(word, type, position));
                editTextData.setText(dbm.getExactString(word, type, position));
            }

        } else {
            preview.setText(dbm.getExactString(word, type, position));
            editTextData.setText(dbm.getExactString(word, type, position));
        }
    }

    //checks if any input field is empty
    //returns true if there is
    //returns false if there isn't
    private boolean checkEmptyEntry() {
        Boolean emptyWord = false;

        if (TextUtils.isEmpty(editTextData.getText().toString())) {
            editTextData.setHint("Error: Empty field!");
            emptyWord = true;
        }
        return emptyWord;
    }

    //initialise type of def spinner
    private void initialiseTypedefSpinner(Spinner v) {
        Resources res = getResources();

        String [] options = res.getStringArray(R.array.spinner_type_def);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        v.setAdapter(adapter);
    }

    //initialise option spinner
    private void initialiseOptionSpinner(Spinner spinner, int type) {
        ArrayList<String> numberOfSpinners;
        Resources res = getResources();
        String[] adder;

        int howManySpinners;
        adder = res.getStringArray(R.array.spinner_options);

        //dynamically displays the number of options selected
        //there is only 1 option if typedef == 0 (word)
        if (type == 0) {
            numberOfSpinners = new ArrayList<>();
            numberOfSpinners.add("1");
        } else {
            //gets how many elements of typedef the word has
            //if it has 4 short defs (typedef == 1), howManySpinners = 4
            howManySpinners = dbm.getHowManyDesc(word, type);
            numberOfSpinners = new ArrayList<>(howManySpinners);

            //adds from 1~10 the number of options necessary
            //howManySpinners == 4, loops 4 times (0~3)
            for (int i = 0; i < howManySpinners; i++)
                numberOfSpinners.add(i, adder[i]);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, numberOfSpinners);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    //for checking if edited data exists in db
    //returns true if it exists
    //returns false if it doesn't
    private Boolean checkData(String string) {
        //gets all the data depending on the word chosen and typedef
        ArrayList<String> data = dbm.getAllType(word, typedef);

        //checks if there is any change in input (compares preview and input)
        if (preview.getText().toString().contentEquals(string)) {
            Toast.makeText(ctx, "Error: You have not made any changes!", Toast.LENGTH_SHORT).show();
            return true;
        }

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).toString().contentEquals(string)) {
                return true;
            }
        }
        return false;
    }

    //checks if word exists in db
    //returns true if it exists
    //returns false if it doesn't
    private boolean checkWord(String inputWord) {
        //gets all the words from the flag selected in the spinner
        ArrayList<String> wordsInCountry = dbm.getAllWords(flag);

        //checks if there is any change in input (compares preview and input)
        if (preview.getText().toString().contentEquals(inputWord)) {
            return true;
        }

        //compares the input word (word) with all the elements in wordsInCountry
        for (int i = 0; i < wordsInCountry.size(); i++) {
            if (wordsInCountry.get(i).toString().contentEquals(inputWord)) {
                return true;
            }
        }
        return false;
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
        if (url.contains("youtu") || url.contains("youtu.be")) {
            return true;
        } else {
            return false;
        }
    }

    //displays the edit button logo
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.edit_word_button, menu);
    }

    //what to do it "edit" and back buttons are pressed
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.edit_word_button) {
            editWord();
        } else
            onBackPressed();
        return true;
    }

    //what to do when back button is pressed
    public void onBackPressed() {
        //empty input field, go back
        if (TextUtils.isEmpty(editTextData.getText().toString())) {
            getActivity().onBackPressed();
        } else {
            //goBack is default to false, so goPackPopUp will always be called on pressing back
            if (goBack) {
                getActivity().onBackPressed();
            } else {
                goBackPopUp();
            }
        }
    }

    //popup when there is any input and the user chooses to go back to MainActivity
    //returns goBack == true if user presses to go back
    //returns goBack == false if user presses cancel
    private boolean goBackPopUp() {
        AlertDialog.Builder messageBox = new AlertDialog.Builder(getActivity());

        messageBox.setTitle("Warning!");
        messageBox.setMessage("Are you sure you want to go back? You will lose all your data.");
        messageBox.setCancelable(false);
        messageBox.setPositiveButton("Go back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                goBack = true;
                onBackPressed();
            }
        });
        messageBox.setNegativeButton("Cancel", null);
        messageBox.show();
        return goBack;
    }

    //popup when video URL is invalid
    private void invalidVideoUrlPopUp() {
        AlertDialog.Builder messageBox = new AlertDialog.Builder(getActivity());

        messageBox.setTitle("Invalid Entry!");
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("Continue editing", null);
        messageBox.setMessage("Your video URL is invalid!\n\nPlease ensure it starts with http:// ");
        messageBox.show();
    }

    //popup when word is successfully added
    private void successPopUp() {
        AlertDialog.Builder messageBox = new AlertDialog.Builder(getActivity());

        messageBox.setTitle("Success!");
        messageBox.setMessage("You have successfully edited your " + getAddedType() + "!");
        messageBox.setNeutralButton("Dictionary", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent myIntent = new Intent(getActivity(), Dictionary.class);
                myIntent.putExtra("flag", flag);
                startActivity(myIntent);
            }
        });
        messageBox.setCancelable(false);
        messageBox.show();
    }
}