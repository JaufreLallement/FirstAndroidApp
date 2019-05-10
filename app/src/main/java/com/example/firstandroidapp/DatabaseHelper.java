package com.example.firstandroidapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Map;

/**
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    /**
     * Name of the class
     */
    private static final String TAG = "DatabaseHelper";

    /**
     * Name of the database table
     */
    private static final String TABLE_NAME = "contact";

    /**
     * Id column of the contact table in database
     */
    private static final String COL1 = "ID";

    /**
     * Name column of the contact table in database
     */
    private static final String COL2 = "name";

    /**
     * Firstname column of the contact table in database
     */
    private static final String COL3 = "firstname";

    /**
     * Birthdate column of the contact table in database
     */
    private static final String COL4 = "birthdate";

    /**
     * Phone column of the contact table in database
     */
    private static final String COL5 = "phone";

    /**
     * Email column of the contact table in database
     */
    private static final String COL6 = "email";

    /**
     * Gender column of the contact table in database
     */
    private static final String COL7 = "gender";


    /**
     * Constructor for DatabaseHelper
     * @param context : context in which the db helper is created
     */
    public DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    /**
     * On create method
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + COL2 + " TEXT, " + COL3 + " TEXT, " + COL4 + " TEXT, " + COL5 + " TEXT, " + COL6 + " TEXT UNIQUE, " + COL7 + " TEXT)";
        db.execSQL(createTable);
    }

    /**
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Gets all the contacts from the database
     * @return : cursor of the table
     */
    public Cursor getContacts() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT name, firstname, phone, email FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    /**
     * Retreive informations about the contact corresponding to the given email
     * @param email : email of the sought contact
     * @return : cursor containing sought informations
     */
    public Cursor getContactByEmail(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT name, firstname, birthdate, phone, email, gender FROM " + TABLE_NAME + " WHERE email = ?";
        Cursor data = db.rawQuery(query, new String[]{ email });
        return data;
    }

    /**
     * Checks if the given email is already used
     * @param email : email to check
     * @return : contacts using the given email
     */
    public Cursor checkUniqueMail(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT email FROM " + TABLE_NAME + " WHERE email = ?";
        Cursor data = db.rawQuery(query, new String[]{ email });
        return data;
    }

    /**
     * Creates an instance of ContentValues based on the given Map
     * @param contact : Map on which base the ContentValues instance
     * @return : whether or not the entry was updated
     */
    private ContentValues contactMapToContentValues(Map<String, String> contact) {
        ContentValues contentValues = new ContentValues(); // Creating the instance

        // Putting the data
        contentValues.put(COL2, contact.get("name"));
        contentValues.put(COL3, contact.get("firstname"));
        contentValues.put(COL4, contact.get("birthdate"));
        contentValues.put(COL5, contact.get("phone"));
        contentValues.put(COL6, contact.get("email"));
        contentValues.put(COL7, contact.get("gender"));

        return contentValues;
    }

    /**
     * Adds a contact to the database based on its informations
     * @param contact : Map containing informations about the contact
     * @return : whether of not the entry was inserted into database
     */
    public boolean addContact(Map<String, String> contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = this.contactMapToContentValues(contact); // Generating ContentValues based on the contact Map
        long res = db.insert(TABLE_NAME, null, contentValues); // Inserting the data

        return res != -1;
    }

    /**
     * Deletes a contact based on the given email address
     * @param email : email of the contact to delete
     * @return : whether or not the contact was deleted
     */
    public boolean deleteContact(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "email = ?", new String[]{ email }) > 0;
    }

    /**
     * Updates a contact based on its given informations
     * @param contact : Map containing contact informations
     * @return
     */
    public boolean updateContact(String email, Map<String, String> contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = this.contactMapToContentValues(contact); // Generating ContentValues based on the contact Map

        long res = db.update(TABLE_NAME, contentValues, "email = ?", new String[]{ email }); // Updating the entry

        return res != -1;
    }
}
