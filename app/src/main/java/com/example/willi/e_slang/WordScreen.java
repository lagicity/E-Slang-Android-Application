package com.example.willi.e_slang;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

/**
 * WordScreen shows the words and all its attributes.
 * It contains of 2 fragments (WordScreenTab1, WordScreenTab2)
 * WordScreenTab1 shows the word, country, short definition and characteristic and tags.
 * WordScreenTab2 shows the long definition, example and videos.
 */

public class WordScreen extends AppCompatActivity {
    DbManager dbm;

    //stores the inputs
    String word;
    String id;
    String flag;

    private ViewPageAdapter mSectionsPageAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_screen);

        mSectionsPageAdapter = new ViewPageAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    protected void onStart() {
        super.onStart();

        //intialises database
        dbm = DbManager.getInstance();
        dbm.mCtx = this;
        dbm.open();

        //gets the data from DictionaryScreen or TagScreen
        Intent intent = getIntent();
        flag = intent.getStringExtra("flag");
        word = intent.getStringExtra("word");

        //displays action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //sets action bar title as word
        getSupportActionBar().setTitle(word);
    }

    //adds the fragments and displays their title
    private void setupViewPager(ViewPager viewPager) {
        ViewPageAdapter adapter = new ViewPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new WordScreenTab1(), "Basic Info");
        adapter.addFragment(new WordScreenTab2(), "More Info & Video");
        viewPager.setAdapter(adapter);
        viewPager.destroyDrawingCache();
    }

    //initialises floating action button
    public void goToEditWord(View v) {
        Intent myIntent = new Intent(this, EditWord.class);
        myIntent.putExtra("word", word); // string you want to pass, variable to receive
        myIntent.putExtra("flag", flag); // string you want to pass, variable to receive

        Bundle bundle = new Bundle();
        bundle.putString("flag", flag);
        bundle.putString("word", word);

        EditWordTab1 send2 = new EditWordTab1();
        send2.setArguments(bundle);

        startActivity(myIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }
}