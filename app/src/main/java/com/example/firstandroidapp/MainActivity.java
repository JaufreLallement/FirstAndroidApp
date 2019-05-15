package com.example.firstandroidapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Main activity which displays the contact list and provides interface elements to change activity
 * @author Lallement Jaufré
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Name of the class
     */
    private static final String TAG = "MainActivity";

    /**
     * Database helper for database interactions
     */
    private DatabaseHelper dbHelper;

    /**
     * ListView containing the user list
     */
    private ListView userListView;

    /**
     * Fills the user list with the data from the database
     */
    private void populateContactList() {
        Cursor data = dbHelper.getContacts(); // Retreiving the contacts from the db


        if (data.getCount() == 0) return; // If there is o users in database, do nothing

        ArrayList<String> userList = new ArrayList<>(); // List of user

        // Filling the user list
        while (data.moveToNext()) {
            String currentName = data.getString(0); // Name of the user
            String currentFirstname = data.getString(1); // Firstname of the user
            String currentPhone = data.getString(2); // Phone number of the user
            String currentEmail = data.getString(3); // Email address of the user
            String currentUser = currentName + " " + currentFirstname + "\n" + currentPhone + " | " + currentEmail; // Converting data to string
            userList.add(currentUser); // Adding the user to the user list
        }

        // Adapting the user list to the interface
        ListAdapter userListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userList);
        this.userListView.setAdapter(userListAdapter);
    }

    /**
     * Retreive the email address from the text of a ListView item
     * @param text : retreived text
     * @return : email address of the selected user
     */
    private String emailFromListItem(String text) {
        String [] splitedText = text.split(" ");
        return splitedText[3];
    }

    /**
     *
     * @param code
     * @param action
     * @param userMail
     */
    private void contactActivityResult(int code, ContactAction action, @Nullable String userMail) {
        Intent intent = new Intent(MainActivity.this, ContactActivity.class);
        intent.putExtra("action", action.name()); // Specifying the contact action
        if (userMail != null) intent.putExtra("selectedContact", userMail); // Pass selected contact if there is one
        startActivityForResult(intent, code); // Starting ContactActivity
    }

    /**
     * Configures the fab button for adding contact
     */
    private void configureAddContactButton() {
        FloatingActionButton addContact = findViewById(R.id.add_contact_fab);
        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactActivityResult(1, ContactAction.CREATE, null); // Starts contact activity for result
            }
        });
    }

    /**
     *
     */
    private void configureUserList() {
        this.userListView = findViewById(R.id.user_list); // List of the users in the database

        this.userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String userText = parent.getItemAtPosition(position).toString();
                String email = emailFromListItem(userText);
                contactActivityResult(1, ContactAction.EDIT, email); // Starts contact activity for result
            }
        });
    }

    /**
     * On create method
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.dbHelper = new DatabaseHelper(this); // Initializing the database helper

        this.configureUserList(); // Configuration of the user list

        this.configureAddContactButton(); // Configuration of the fab button
        this.populateContactList(); // Filling the contact list
    }

    /**
     * Executes instructions when the result of children activities is set
     * @param requestCode : code of the request
     * @param resultCode : result code
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                this.populateContactList(); // Refresh the user list
            }
        }
    }

    /**
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
