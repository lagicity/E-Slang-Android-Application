package com.example.willi.e_slang;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * MainActivity is the first screen the user sees when they open the app.
 * It contains all the countries for the user to explore.
 */

public class MainActivity extends AppCompatActivity {
    DbManager dbm;

    String countryName;

    //each country is assigned a code (ie. Korea = 1, England = 2)
    int countryCode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbm = DbManager.getInstance();
        dbm.mCtx = this;
        dbm.open();
    }

    // assigns the countryCode depending on the country clicked to pass onto goToDictionary
    public void goToDictionaryKorea(View v) {
        countryCode = 1;
        countryName = getCountry(countryCode);
        goToDictionary(countryName);
    }
    public void goToDictionaryEngland(View v) {
        countryCode = 2;
        countryName = getCountry(countryCode);
        goToDictionary(countryName);
    }
    public void goToDictionaryFrance(View v) {
        countryCode = 3;
        countryName = getCountry(countryCode);
        goToDictionary(countryName);
    }

    //makes it neater to collate all countries and countryCodes together
    private String getCountry(int countryCode) {
        switch (countryCode) {
            case 1:
                return countryName = "Korea";
            case 2:
                return countryName = "England";
            case 3:
                return countryName = "France";
            default:
                return "Error";
        }
    }

    //takes the countryCode and passes it to the dictionary screen
    private void goToDictionary(String country) {
        Intent myIntent = new Intent(this, Dictionary.class);
        myIntent.putExtra("flag", country);

        Bundle bundle2 = new Bundle();
        bundle2.putString("flag", country);

        DictionaryTab1 send2 = new DictionaryTab1();
        send2.setArguments(bundle2);

        startActivity(myIntent);
    }

    // go to AddWord1 screen (AddWord button)
    public void goToAddWord1(View v) {
        Intent myIntent = new Intent(this, AddWord1.class);
        startActivity(myIntent);
    }
}