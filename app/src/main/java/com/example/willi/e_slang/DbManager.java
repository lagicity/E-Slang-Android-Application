package com.example.willi.e_slang;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by USER on 2017-05-24.
 */
public final class DbManager {
    public static final String INDEX = "_index";
    public static final String WORD = "word";
    public static final String SHORT = "short";
    public static final String LONG = "long";
    public static final String CHARACTERISTIC = "characteristic";
    public static final String EX = "example";
    public static final String VIURL = "video_url";
    public static final String COUNTRY = "country";
    public static final String TAG = "tag";
    public static final String _TABLENAME = "WORD";
    private final static DbManager ourInstance = new DbManager();
    private static final String DATABASE_CREATE = "create table WORD (_index integer primary key autoincrement, "
            + "word text not null, short text not null, long text not null, characteristic text, example text, video_url text, country text not null,tag text not null);";
    private static final String DATABASE_NAME = "data";
    private static final int DATABASE_VERSION = 2;
    public Context mCtx = null;
    //word, short term, long term, country, tag cannot be null values
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private DbManager(Context ctx) {
        this.mCtx = ctx;
    }


    private DbManager() {
    }

    public static DbManager getInstance() {
        return ourInstance;
    }

    public static ArrayList<String> stringToArrayList(String str) {
        ArrayList<String> arrayList = new ArrayList<String>();
        String[] temp = str.split(";");

        for (int i = 0; i < temp.length; i++) {
            arrayList.add(temp[i]);
        }
        return arrayList;
    }

    public static ArrayList<String> stringToArrayListTag(String str) {
        ArrayList<String> arrayList = new ArrayList<String>();
        String[] temp = str.split(";");

        for (int i = 0; i < temp.length; i++) {
            arrayList.add(temp[i]);
        }
        return arrayList;
    }

    public static String elaborateDesc(String str) {
        ArrayList<String> temp = stringToArrayList(str);
        String result = new String();
        for (int i = 0; i < temp.size(); i++) {
            result = result.concat(i + 1 + ". ");
            result = result.concat(temp.get(i));
            result = result.concat("\n\n");
        }

        return result;
    }

    public static String getTags(String str) {
        ArrayList<String> temp = stringToArrayList(str);

        String result = new String();
        for (int i = 0; i < temp.size(); i++) {
            result = result.concat(temp.get(i));
        }
        return result;
    }

    public DbManager open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    //insert new word
    public long insert(String word, ArrayList<String> shortTerm, ArrayList<String> longTerm, ArrayList<String> character, ArrayList<String> example, ArrayList<String> videoUrl, String country, ArrayList<String> tag) {
        ArrayList<String> wordList = new ArrayList<String>();
        wordList = getAllWords(country);

        String shortT = arrayListToString(shortTerm);
        String longT = arrayListToString(longTerm);
        String charact = arrayListToString(character);
        String ex = arrayListToString(example);
        String viUrl = arrayListToString(videoUrl);
        String tags = arrayListToString(tag);

        int temp = 0;
        for (int i = 0; i < wordList.size(); i++) {
            if (wordList.get(i).equals(word)) temp++;
        }
        if (temp == 0) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(WORD, word);
            initialValues.put(SHORT, shortT);
            initialValues.put(LONG, longT);
            initialValues.put(CHARACTERISTIC, charact);
            initialValues.put(EX, ex);
            initialValues.put(VIURL, viUrl);
            initialValues.put(COUNTRY, country);
            initialValues.put(TAG, tags);
            return mDb.insert(_TABLENAME, null, initialValues);
        } else return 0;
    }

    public boolean delete(int index) {
        return mDb.delete(_TABLENAME, INDEX + "=" + index, null) > 0;
    }

    public void drop() {
        mDb.execSQL("delete from " + _TABLENAME);
    }

    public void deleteWord(String word) {
        mDb.execSQL("delete from WORD where word=" + "'" + word + "'" + ";");
    }

    public Cursor fetchAll() {
        return mDb.query(_TABLENAME, new String[]{INDEX, WORD, SHORT, LONG, CHARACTERISTIC, EX, VIURL, COUNTRY, TAG}, null, null, null, null, null);
    }

    public Cursor fetch(long index) throws SQLException {

        Cursor mCursor = mDb.query(true, _TABLENAME, new String[]{WORD, SHORT, LONG, CHARACTERISTIC, EX, VIURL, COUNTRY, TAG}, INDEX
                + "=" + index, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public ArrayList<String> getAllCountries() {
        int temp = 0;
        ArrayList<String> countryList = new ArrayList<String>();
        Cursor cursor = mDb.query(_TABLENAME, new String[]{INDEX, WORD, SHORT, LONG, CHARACTERISTIC, EX, VIURL, COUNTRY, TAG}, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            for (int i = 0; i < countryList.size(); i++) {
                if (countryList.get(i).equals(cursor.getString(1))) temp++;
            }
            if (temp == 0) countryList.add(cursor.getString(8));
            cursor.moveToNext();
        }
        return countryList;
    }

    public ArrayList<String> getAllWords(String country) {
        if (country == null)
            return null;
        ArrayList<String> wordList = new ArrayList<>();
        String query = "SELECT * FROM " + _TABLENAME + " where country='" + country + "' ORDER BY word COLLATE NOCASE;";
        Cursor mCursor = mDb.rawQuery(query, null);

        mCursor.moveToFirst();

        if (mCursor.moveToFirst()) {
            do {
                if (country.equals(mCursor.getString(7))) {
                    wordList.add(mCursor.getString(1));
                }
            } while (mCursor.moveToNext());
        }
        mCursor.close();
        return wordList;
    }

    public Cursor getAllWordsCursor(String country) {
        if (country == null)
            return null;
        String query = "SELECT * FROM " + _TABLENAME + " where country='" + country + "' ORDER BY word COLLATE NOCASE;";
        Cursor mCursor = mDb.rawQuery(query, null);

        mCursor.moveToFirst();
        return mCursor;
    }

    public Cursor getTypeData(String country) {
        if (country == null)
            return null;
        ArrayList<String> wordList = new ArrayList<String>();
        String query = new String();
        query = "SELECT * FROM " + _TABLENAME + " where country='" + country + "' ORDER BY word COLLATE NOCASE;";
        //  Cursor mCursor = mDb.query(_TABLENAME, null, "country="+"'"+country+"'",  null, null,null,"word",null);

        Cursor mCursor = mDb.rawQuery(query, null);

        mCursor.moveToFirst();

        return mCursor;
    }

    public Cursor getOneWord(String country, String word) { // removed String id parameter
        if (country == null)
            return null;
        ArrayList<String> wordList = new ArrayList<String>();
        String query = new String();
        query = "SELECT * FROM " + _TABLENAME + " where country='" + country + "' AND word='" + word + "';";
        //query = "SELECT * FROM " + _TABLENAME + " where country='" + country + "' AND word='" + word + "' AND _index='" + id + "';";
        Cursor mCursor = mDb.rawQuery(query, null);
        //Cursor mCursor = mDb.query(_TABLENAME, new String[] { INDEX, WORD, SHORT, LONG, CHARACTERISTIC,EX,AUURL,VIURL,COUNTRY,TAG }, null, null, null, null, null);

        mCursor.moveToFirst();

        return mCursor;
    }

    public int getHowManyDesc(String word, int typedef) {
        String query = new String();
        Cursor mcursor;
        String data = new String();
        ArrayList<String> desc = new ArrayList<String>();

        switch (typedef) {
            case 1: //shortDef
                query = "SELECT * FROM WORD WHERE word='" + word + "'";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(2);
                desc = stringToArrayList(data);
                break;
            case 2: //longDef
                query = "SELECT * FROM WORD WHERE word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(3);
                desc = stringToArrayList(data);
                break;
            case 3: //character
                query = "SELECT * FROM WORD WHERE word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(4);
                desc = stringToArrayList(data);
                break;
            case 4: //example
                query = "SELECT * FROM WORD WHERE word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(5);
                desc = stringToArrayList(data);
                break;
            case 5: // videoURL
                query = "SELECT * FROM WORD WHERE word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(6);
                desc = stringToArrayList(data);
                break;
            case 6: //tag
                query = "SELECT * FROM WORD WHERE word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(8);
                desc = stringToArrayListTag(data);
                break;
        }
        return desc.size();
    }

    public String getExactString(String word, int typedef, int index) {

        String query = new String();
        String data = new String();
        Cursor mcursor;
        ArrayList<String> result = new ArrayList<String>();

        switch (typedef) {
            case 1: //shortDef
                query = "SELECT * FROM WORD where word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(2);
                result = stringToArrayList(data);
                break;
            case 2: //longDef
                query = "SELECT * FROM WORD WHERE word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(3);
                result = stringToArrayList(data);
                break;
            case 3: //character
                query = "SELECT * FROM WORD WHERE word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(4);
                result = stringToArrayList(data);
                break;
            case 4: //example
                query = "SELECT * FROM WORD WHERE word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(5);
                result = stringToArrayList(data);
                break;
            case 5: //videoURL
                query = "SELECT * FROM WORD WHERE word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(6);
                result = stringToArrayList(data);
                break;
            case 6: //tag
                query = "SELECT * FROM WORD WHERE word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(8);
                result = stringToArrayListTag(data);
                break;
        }
        return result.get(index);
    }

    public ArrayList<String> getAllType(String word, int typedef) {

        String query = new String();
        String data = new String();
        Cursor mcursor;
        ArrayList<String> result = new ArrayList<String>();

        switch (typedef) {
            case 1: //shortDef
                query = "SELECT * FROM WORD where word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(2);
                result = stringToArrayList(data);
                break;
            case 2: //longDef
                query = "SELECT * FROM WORD WHERE word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(3);
                result = stringToArrayList(data);
                break;
            case 3: //character
                query = "SELECT * FROM WORD WHERE word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(4);
                result = stringToArrayList(data);
                break;
            case 4: //example
                query = "SELECT * FROM WORD WHERE word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(5);
                result = stringToArrayList(data);
                break;
            case 5: //videoURL
                query = "SELECT * FROM WORD WHERE word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(6);
                result = stringToArrayList(data);
                break;
            case 6: //tag
                query = "SELECT * FROM WORD WHERE word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(8);
                result = stringToArrayList(data);
                break;
        }
        return result;
    }

    public void update(String word, ArrayList<String> shortTerm, ArrayList<String> longTerm, ArrayList<String> character, ArrayList<String> example, ArrayList<String> videoUrl, String country, ArrayList<String> tag) {
        String handledStr = new String();
        String query = new String();
        if (shortTerm.size() != 0) {
            handledStr = arrayListToString(shortTerm);
            query = "UPDATE WORD SET short=" + "'" + handledStr + "'" + "WHERE word=" + "'" + word + "'";
            mDb.execSQL(query);
        }
        if (longTerm.size() != 0) {
            handledStr = arrayListToString(longTerm);
            query = "UPDATE WORD SET long=" + "'" + handledStr + "'" + "WHERE word=" + "'" + word + "'";
            mDb.execSQL(query);
        }
        if (character.size() != 0) {
            handledStr = arrayListToString(character);
            query = "UPDATE WORD SET characteristic=" + "'" + handledStr + "'" + "WHERE word=" + "'" + word + "'";
            mDb.execSQL(query);
        }
        if (example.size() != 0) {
            handledStr = arrayListToString(example);
            query = "UPDATE WORD SET example=" + "'" + handledStr + "'" + "WHERE word=" + "'" + word + "'";
            mDb.execSQL(query);
        }
        if (videoUrl.size() != 0) {
            handledStr = arrayListToString(videoUrl);
            query = "UPDATE WORD SET video_url=" + "'" + handledStr + "'" + "WHERE word=" + "'" + word + "'";
            mDb.execSQL(query);
        }
        if (tag.size() != 0) {
            handledStr = arrayListToString(tag);
            query = "UPDATE WORD SET tag=" + "'" + handledStr + "'" + "WHERE word=" + "'" + word + "'";
            mDb.execSQL(query);
        }
    }

    public void insertDataIntoDb(String word, int typedef, String editedStr) {
        String query = new String();
        String data = new String();
        Cursor mcursor;
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<String> empty = new ArrayList<String>();

        Log.d(DbManager.class.getName(), "my url =" + editedStr);

        switch (typedef) {
            case 1: //shortDef
                query = "SELECT * FROM WORD WHERE word='" + word + "'";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(2).concat(editedStr);
                result = stringToArrayList(data);
                break;
            case 2: //longDef
                query = "SELECT * FROM WORD WHERE word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(3).concat(editedStr);
                result = stringToArrayList(data);
                break;
            case 3: //character
                query = "SELECT * FROM WORD WHERE word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(4).concat(editedStr);
                result = stringToArrayList(data);
                break;
            case 4: //example
                query = "SELECT * FROM WORD WHERE word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(5).concat(editedStr);
                result = stringToArrayList(data);
                break;
            case 5: //videourl
                query = "SELECT * FROM WORD WHERE word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(6).concat(editedStr);
                Log.d(DbManager.class.getName(), "my new url string =" + data);
                result = stringToArrayList(data);
                break;
            case 6: //tag
                query = "SELECT * FROM WORD WHERE word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                if (editedStr.toString().contains("#"))
                    data = mcursor.getString(8).concat(editedStr);
                else
                    data = mcursor.getString(8).concat("#" + editedStr);
                result = stringToArrayListTag(data);
                break;
        }

        switch (typedef) {
            case 1:
                update(word, result, empty, empty, empty, empty, null, empty);
                break;
            case 2:
                update(word, empty, result, empty, empty, empty, null, empty);
                break;
            case 3:
                update(word, empty, empty, result, empty, empty, null, empty);
                break;
            case 4:
                update(word, empty, empty, empty, result, empty, null, empty);
                break;
            case 5:
                update(word, empty, empty, empty, empty, result, null, empty);
                break;
            case 6:
                update(word, empty, empty, empty, empty, empty, null, result);
                break;
        }
    }

    public void insertWord(String word, String newWord) {
        String query;

        query = "UPDATE WORD SET word=" + "'" + newWord + "'" + "WHERE word=" + "'" + word + "'";
        mDb.execSQL(query);
    }

    public void updateExactString(String word, int typedef, int index, String editedStr) {
        String query = new String();
        String data = new String();
        Cursor mcursor;
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<String> empty = new ArrayList<String>();
        ArrayList<String> temp = new ArrayList<String>();
        switch (typedef) {
            case 1: //shortDef
                query = "SELECT * FROM WORD WHERE word='" + word + "'";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(2);
                result = stringToArrayList(data);
                break;
            case 2: //longDef
                query = "SELECT * FROM WORD WHERE word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(3);
                result = stringToArrayList(data);
                break;
            case 3: //character
                query = "SELECT * FROM WORD WHERE word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(4);
                result = stringToArrayList(data);
                break;
            case 4: //example
                query = "SELECT * FROM WORD WHERE word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(5);
                result = stringToArrayList(data);
                break;
            case 5: //videourl
                query = "SELECT * FROM WORD WHERE word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(6);
                result = stringToArrayList(data);
                break;
            case 6: //tag
                query = "SELECT * FROM WORD WHERE word='" + word + "';";
                mcursor = mDb.rawQuery(query, null);
                mcursor.moveToFirst();
                data = mcursor.getString(8);

                result = stringToArrayListTag(data);

                for (int i = 0; i < result.size(); i++)
                    temp.add(result.get(i));

                temp.remove(index);
                temp.add(index, editedStr);

                for (int i = 0; i < temp.size(); i++) {
                    update(word, empty, empty, empty, empty, empty, null, temp);
                }
                break;
        }

        if (typedef != 6) {
            result.remove(index);
            result.add(index, editedStr);
        }

        switch (typedef) {
            case 1:
                update(word, result, empty, empty, empty, empty, null, empty);
                break;
            case 2:
                update(word, empty, result, empty, empty, empty, null, empty);
                break;
            case 3:
                update(word, empty, empty, result, empty, empty, null, empty);
                break;
            case 4:
                update(word, empty, empty, empty, result, empty, null, empty);
                break;
            case 5:
                update(word, empty, empty, empty, empty, result, null, empty);
                break;
        }
    }

    //change array list to string. string separates descriptions by ';'
    public String arrayListToString(ArrayList<String> description) {
        String handledString = new String();

        for (int i = 0; i < description.size(); i++) {
            handledString = handledString + description.get(i);
            handledString = handledString + ";";
        }

        return handledString;
    }

    public String arrayListToStringTag(ArrayList<String> tag) {
        String handledString = new String();

        for (int i = 0; i < tag.size(); i++)
            handledString = handledString + tag.get(i);

        return handledString;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
           /* Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
                    + ", which will destroy all old data");*/
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }
}