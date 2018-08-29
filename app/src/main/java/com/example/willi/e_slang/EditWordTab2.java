package com.example.willi.e_slang;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.util.Log;
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
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * EditWordTab1 allows the adding of additional ldata to the word
 */

public class EditWordTab2 extends Fragment {
    Context ctx;
    DbManager dbm;

    //stores the inputs
    String flag;
    String word;
    String inputData;

    //field for input
    EditText addTextData;
    //spinner
    Spinner spinnerTypeDef;

    //type selected (0 == word, 1 == short def, 2 == long def etc)
    int typedef;
    //number of elements that exist for that typedef
    int numberOfTerms;

    //determines whether to go back or not
    boolean goBack = false;
    //character count
    boolean characterCountExceeded;

    //what to do if character counter is greater than or less than the number of characters entered
    TextInputLayout characterCount;
    public final TextWatcher inputDataTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 0)
                characterCount.setError("*Input required!");

            if (s.length() > characterCount.getCounterMaxLength()) {
                characterCountExceeded = true;
                characterCount.setError("Character count exceeded!");
            } else {
                characterCountExceeded = false;
                characterCount.setErrorEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.edit_word_tab_2, container, false);

        //allows the Add button to be fixed to this fragment only (does not appear in EditWordTab1)
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
        addTextData = (EditText) view.findViewById(R.id.input_data);
        inputData = addTextData.getText().toString();
        //initialise the typedef spinner
        spinnerTypeDef = (Spinner) view.findViewById(R.id.options);
        intialiseSpinner(spinnerTypeDef);

        //initialise variables of text input layout (for character count)
        characterCount = (TextInputLayout) view.findViewById(R.id.character_count);
        addTextData.addTextChangedListener(inputDataTextWatcher);
        //listener to see what the user selects (typedef)
        spinnerTypeDef.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                typedef = position + 1;
                setCharacterCount();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
        return view;
    }

    //adds input into db
    private void addInput(int typedef) {
        //gets what is inputted
        inputData = addTextData.getText().toString();
        //number of terms for that typedef in db
        //ie. if there are 4 short def, numberOfTerms = 4
        numberOfTerms = dbm.getHowManyDesc(word, typedef);

        //check if character count is exceeded
        if (characterCountExceeded) {
            Toast.makeText(ctx, "Error: Character count exceeded! Please edit your entry again.", Toast.LENGTH_SHORT).show();

        }
        //check if there is an empty entry
        else if (checkEmptyEntry()) {
            addTextData.setHint("Error: Empty field!");
            addTextData.setHintTextColor(Color.RED);
        } else {
            //removes any spacing at the end
            inputData = removeSpacesAtEnd(inputData);

            //adds the data into db
            switch (typedef) {
                case 1: //short def
                    if (numberOfTerms < 10)
                        if (checkInputDataExists(inputData))
                            inputDataExistsPopUp();
                        else {
                            dbm.insertDataIntoDb(word, typedef, inputData);
                            successPopUp();
                        }
                    else
                        maxTermsReachedPopUp();
                    break;
                case 2: //long def
                    if (numberOfTerms < 10)
                        if (checkInputDataExists(inputData))
                            inputDataExistsPopUp();
                        else {
                            dbm.insertDataIntoDb(word, typedef, inputData);
                            successPopUp();
                        }
                    else
                        maxTermsReachedPopUp();
                    break;
                case 3: //characteristic
                    if (numberOfTerms < 10)
                        if (checkInputDataExists(inputData))
                            inputDataExistsPopUp();
                        else {
                            dbm.insertDataIntoDb(word, typedef, inputData);
                            successPopUp();
                        }
                    else
                        maxTermsReachedPopUp();
                    break;
                case 4: //example
                    if (numberOfTerms < 10)
                        if (checkInputDataExists(inputData))
                            inputDataExistsPopUp();
                        else {
                            dbm.insertDataIntoDb(word, typedef, inputData);
                            successPopUp();
                        }
                    else
                        maxTermsReachedPopUp();
                    break;
                case 5: //videoURL
                    if (numberOfTerms < 10) {
                        if (checkInputDataExists(inputData)) {
                            inputDataExistsPopUp();
                        } else {
                            if (checkUrl(inputData)) {
                                invalidVideoUrlPopUp();
                            } else {
                                dbm.insertDataIntoDb(word, typedef, inputData);
                                successPopUp();
                            }
                        }
                    } else
                        maxTermsReachedPopUp();
                    break;
                case 6: //tag
                    if (numberOfTerms < 10) {
                        if (checkInputDataExists(inputData))
                            inputDataExistsPopUp();
                        else {
                            dbm.insertDataIntoDb(word, typedef, inputData);
                            successPopUp();
                        }
                    } else
                        maxTermsReachedPopUp();
                    break;
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

    //gets the list of all entries to check if they exist
    //returns true if it exists
    //returns false if it does not
    private boolean checkInputDataExists(String inputData) {
        ArrayList<String> tmp = dbm.getAllType(word, typedef);

        for (int i = 0; i < tmp.size(); i++) {
            if (inputData.contentEquals(tmp.get(i).toString()))
                return true;
        }
        return false;
    }

    //checks if any input field is empty
    //returns true if there is
    //returns false if there isn't
    private boolean checkEmptyEntry() {
        if (TextUtils.isEmpty(addTextData.getText().toString())) {
            Toast.makeText(getActivity(), "Error: Empty field!", Toast.LENGTH_SHORT).show();
            return true;
        } else
            return false;
    }

    //returns the type that the user added (short def, long def, char etc)
    private String getAddedType() {
        switch (typedef) {
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

    //initialise the spinner
    private void intialiseSpinner(Spinner spinner) {
        ArrayList<String> options = new ArrayList<>();
        options.add("1. Short definition");
        options.add("2. Long definition");
        options.add("3. Characteristic");
        options.add("4. Example");
        options.add("5. Video URL");
        options.add("6. Tag");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
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

    // displays the add button logo
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_word_button, menu);
    }

    //what to do it "add" and back buttons are pressed
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add_word_button) {
            addInput(typedef);
        } else
            getActivity().onBackPressed();
        return true;
    }

    //what to do when back button is pressed
    public void onBackPressed() {
        if (!TextUtils.isEmpty(addTextData.getText().toString())) {
            goBack = goBackPopUp();
        } else {
            onBackPressed();
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

    //popup when the the max number of entries are in db
    private void maxTermsReachedPopUp() {
        AlertDialog.Builder messageBox = new AlertDialog.Builder(getActivity());

        messageBox.setTitle("Error!");
        messageBox.setMessage("The maximum number of " + getAddedType() + "s " + "allowed (10) has been reached!\nPlease consider editing instead.");
        messageBox.setNeutralButton("Continue editing", null);
        messageBox.setCancelable(false);
        messageBox.show();
    }

    //popup when user edits or adds word
    private void successPopUp() {
        final AlertDialog.Builder messageBox = new AlertDialog.Builder(getActivity());

        messageBox.setTitle("Success!");
        messageBox.setMessage("You have successfully added your " + getAddedType() + "!");

        messageBox.setNegativeButton("Continue adding", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addTextData.setText("");
            }
        });

        messageBox.setPositiveButton("Dictionary", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent myIntent = new Intent(getActivity(), Dictionary.class);
                myIntent.putExtra("flag", flag);

                Bundle bundle = new Bundle();
                bundle.putString("flag", flag);
                DictionaryTab1 send2 = new DictionaryTab1();
                send2.setArguments(bundle);
                startActivity(myIntent);
            }
        });
        messageBox.setCancelable(false);
        messageBox.show();
    }

    //popup when the entry already exists
    private void inputDataExistsPopUp() {
        AlertDialog.Builder messageBox = new AlertDialog.Builder(getActivity());
        messageBox.setTitle("Invalid Entry!");
        messageBox.setMessage("The " + getAddedType() + " that you entered already exists!");
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("Continue editing", null);
        messageBox.show();
    }
}
