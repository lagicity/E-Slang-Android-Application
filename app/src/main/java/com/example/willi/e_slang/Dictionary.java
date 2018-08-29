package com.example.willi.e_slang;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

/**
 * Dictionary displays all the words or tags of a country and allows the user to search for them.
 * It contains of 2 fragments (DictionaryTab1, DictionaryTab2)
 * DictionaryTab1 shows the words
 * DictionaryTab2 shows the tags
 */

public class Dictionary extends AppCompatActivity {
    DbManager dbm;

    //stores the inputs
    String word;
    String flag;

    private ViewPageAdapter mSectionsPageAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        mSectionsPageAdapter = new ViewPageAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    protected void onStart() {
        super.onStart();

        //initialises the database
        dbm = DbManager.getInstance();
        dbm.mCtx = this;
        dbm.open();

        //gets the data from MainActivity
        Intent intent = getIntent();
        flag = intent.getStringExtra("flag");
        word = intent.getStringExtra("word");

        //displays action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //sets action bar title
        getSupportActionBar().setTitle("Dictionary");
    }

    //adds the fragments and displays their title
    private void setupViewPager(ViewPager viewPager) {
        ViewPageAdapter adapter = new ViewPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new DictionaryTab1(), "Search by Word");
        adapter.addFragment(new DictionaryTab2(), "Search by Tag");
        viewPager.setAdapter(adapter);
    }
}
