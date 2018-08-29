package com.example.willi.e_slang;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

/**
 * EditWord allows the user to edit or add data to the word.
 * It contains of 2 fragments (EditWordTab1, EditWordTab2)
 * EditWordTab1 allows the editing of already inputted data
 * EditWordTab2 allows the adding of additional data to the world
 */

public class EditWord extends AppCompatActivity {
    DbManager dbm;

    //stores the inputs
    String word;
    String flag;

    private ViewPageAdapter mSectionsPageAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_word);

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
        //sets action bar title as the word that is passed in
        getSupportActionBar().setTitle(word);

    }

    //adds the fragments and displays their title
    private void setupViewPager(ViewPager viewPager) {
        ViewPageAdapter adapter = new ViewPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new EditWordTab1(), "Edit Word");
        adapter.addFragment(new EditWordTab2(), "Add to Word");
        viewPager.setAdapter(adapter);
    }
}