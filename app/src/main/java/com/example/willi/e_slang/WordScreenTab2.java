package com.example.willi.e_slang;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * WordScreenTab2 shows the long definition, exmple and videos
 */

public class WordScreenTab2 extends Fragment {
    DbManager dbm;
    Cursor cursor;

    //store the inputs
    String word;
    String id;
    String flag;
    String exampleString;
    String videoUrlString;

    //button to go to VideoScreen
    Button videoScreenButton;
    //display the data
    TextView longDefTv;
    TextView exampleTv;
    TextView videoTv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.word_screen_tab_2, container, false);

        //initialise the database
        dbm = DbManager.getInstance();
        dbm.mCtx = getContext();
        dbm.open();

        //retrieve data from Dictionary or TagScreen
        Bundle b = getActivity().getIntent().getExtras();
        flag = b.getString("flag");
        word = b.getString("word");

        //initialise the views and button
        longDefTv = (TextView) view.findViewById(R.id.long_def);
        exampleTv = (TextView) view.findViewById(R.id.eg);
        videoTv = (TextView) view.findViewById(R.id.video_url);
        videoScreenButton = (Button) view.findViewById(R.id.go_to_video_activity);

        //gets the respective data from the database
        cursor = dbm.getOneWord(flag, word);
        if (cursor.moveToFirst()) {
            do {
                longDefTv.setText(DbManager.elaborateDesc(cursor.getString(cursor.getColumnIndex("long"))));
                exampleString = DbManager.elaborateDesc(cursor.getString(cursor.getColumnIndex("example")));
                videoUrlString = DbManager.elaborateDesc(cursor.getString(cursor.getColumnIndex("video_url")).toString());
            } while (cursor.moveToNext());
        }
        cursor.close();

        //set text if there is no video found
        //if not video is found, the button will be unclickable
        if (TextUtils.isEmpty(videoUrlString)) {
            videoTv.setText("No videos found!");
            videoScreenButton.setEnabled(false);
            videoScreenButton.setText("No Videos found");
        } else
            videoTv.setText(videoUrlString);

        //if videoScreenButton is clicked
        videoScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToVideoScreen();
            }
        });

        return view;
    }

    //go to VideoScreen
    private void goToVideoScreen() {
        Intent myIntent = new Intent(getActivity(), VideoScreen.class);
        myIntent.putExtra("word", word); // string you want to pass, variable to receive
        myIntent.putExtra("flag", flag); // string you want to pass, variable to receive
        startActivity(myIntent);
    }
}
