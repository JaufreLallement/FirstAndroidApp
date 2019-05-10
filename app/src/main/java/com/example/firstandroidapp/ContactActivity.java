package com.example.firstandroidapp;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class specifies the behavior of the application regarding saving and editing contact data
 * @author Lallement Jaufré
 * @version 1.0
 */
public class ContactActivity extends AppCompatActivity {

    /**
     * Name of the class
     */
    private static final String TAG = "ContactActivity";

    /**
     * Database helper for database interactions
     */
    private DatabaseHelper dbHelper;

    /**
     * Name field of the form
     */
    private EditText name;

    /**
     * Firstname field of the form
     */
    private EditText firstname;

    /**
     * Date of birth field of the form
     */
    private EditText birthdate;

    /**
     * Phone number field of the form
     */
    private EditText phone;

    /**
     * Email address field of the form
     */
    private EditText email;

    /**
     * Gender field of the form
     */
    private RadioGroup gender;

    /**
     * Action to execute
     */
    private Map<String, String> contact = null;

    /**
     * Encrypting a given text with SHA 256 algorithm
     * @param text : text to encrypt
     * @return : encrypted text
     */
    public static String sha256(String text) {
        MessageDigest digest;
        StringBuffer hexString = new StringBuffer();

        try {
            digest = MessageDigest.getInstance("SHA-256"); // SHA 256
            byte[] encodedHash = new byte[0]; // Array of bytes
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                encodedHash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            }

            // For each byte of the array : append to StringBuffer
            for (byte element : encodedHash) {
                String hex = Integer.toHexString(0xff & element);
                if (hex.length() == 1) hexString.append('0');
                else hexString.append(hex);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return hexString.toString();
    }

    /**
     * Checks if the given text matches the given regex
     * @param regex : regex to use
     * @param text : text to check
     * @return boolean : whether or not the text matches
     */
    public static boolean validateString(String regex, String text) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        return matcher.matches();
    }

    /**
     * Checks if all values in the given array are true
     * @param values : values to check
     * @return boolean : whether all the values are true or not
     */
    public static boolean allTrue(boolean[] values) {
        for (boolean value : values) if (!value) return false;
        return true;
    }

    /**
     * Execute various tests on a password to validate its format
     * @param password : password to check
     * @return boolean : whether the password passed the tests or not
     */
    private boolean checkPasswordFormat(String password) {
        // Password tests
        boolean[] tests = {
                ContactActivity.validateString("^(.*?[0-9]){1,}.*$", password), // The password contains at least one number
                ContactActivity.validateString("^(.*?[A-Z]){1,}.*$", password), // The password contains at least one upper case
                ContactActivity.validateString("^(.*?[a-z]){1,}.*$", password), // The password contains at least one lowercase
                password.length() >= 8 // The password is at least 8 characters long
        };

        return ContactActivity.allTrue(tests);
    }

    /**
     * CHecks if the given email address matches the format
     * @param email : email to check
     * @return whether or not it matches
     */
    private boolean checkEmail(String email) {
        String emailRegex = "\\A[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\z";
        return validateString(emailRegex, email);
    }

    /**
     * Checks if the given date matches the format
     * @param date : date to check
     * @return : whether or not it matches
     */
    private boolean checkDate(String date) {
        return validateString("^([0-2][0-9]|(3)[0-1])(\\/)(((0)[0-9])|((1)[0-2]))(\\/)\\d{4}$", date);
    }

    /**
     * Checks if the given name matches the format
     * @param name : name to check
     * @return : whether or not it matches
     */
    private boolean checkName(String name) {
        return validateString("^[A-Z][\\p{L}]*", name);
    }

    /**
     * Check the given values from the form
     * @param form : Map containing the values from the form
     */
    private String validateForm(Map<String, String> form) {
        // Retreiving informations
        String name = form.get("name");
        String firstname = form.get("firstname");
        String email = form.get("email");
        String birthdate = form.get("birthdate");

        String prevMail = this.contact.get("email"); // Email of the contact in database
        String finalMessage;

        boolean uniqueMail = dbHelper.checkUniqueMail(email).getCount() == 0;
        String nonUniqueMailError = "Error: this email address is already used!";

        if (!this.checkName(name)) return "Error: invalid last name!"; // Check if the last name has the right format
        if (!this.checkName(firstname)) return "Error: invalid first name!"; // Check if the first name has the right format
        if (!this.checkDate(birthdate)) return "Error: date of birth is not valid!"; // Check if birth date has the right format
        if (!this.checkEmail(email)) return "Error: the email address is not valid!"; // Check if the email address has the right format

        // Check if the contact is set: if it is, the action is EDITING
        if (this.contact == null) {
            if (!uniqueMail) return nonUniqueMailError; // Check if the email address is already used
            boolean addedContact = dbHelper.addContact(form); // Save data to the database

            if (!addedContact) return "Error: contact could not be saved in database"; // Check if there was a problem to save the contact
            finalMessage = "Success: contact " + form.get("email") + " was successfully saved in database!"; // Set the final message
        } else {
            boolean changedMail = prevMail != email;

            // Check if the email address was changed
            if (changedMail) {
                if (!uniqueMail) return nonUniqueMailError; // Check if the changed address is already used
            }
            boolean updatedContact = dbHelper.updateContact(this.contact.get("email"), form);

            if (!updatedContact) return "Error: contact could not be updated"; // Check if there was a problem to update the contact

            finalMessage = "Success: contact " + this.contact.get("email") + " was successfully updated!"; // Set the final message
        }

        return finalMessage;
    }

    /**
     * Converts a Cursor instance of a contact to a Map instance
     * @param contact : Cursor instance to convert
     * @return : Map instance
     */
    private Map<String, String> contactCursorToMap(Cursor contact) {
        Map<String, String> contactMap = new HashMap<String, String>(); // Creating the Map instance

        // Filling the Map
        if (contact.moveToNext()) {
            contactMap.put("name", contact.getString(0));
            contactMap.put("firstname", contact.getString(1));
            contactMap.put("birthdate", contact.getString(2));
            contactMap.put("phone", contact.getString(3));
            contactMap.put("email", contact.getString(4));
            contactMap.put("gender", contact.getString(5));
        }

        return contactMap;
    }

    /**
     * Retreives the data from the form
     * @return Map<String, String> user : current user
     */
    private Map<String, String> getFieldsValue() {
        Map<String, String> contact = new HashMap<String, String>();

        // Saving data into Map
        contact.put("name", this.name.getText().toString());
        contact.put("firstname", this.firstname.getText().toString());
        contact.put("birthdate", this.birthdate.getText().toString());
        contact.put("phone", this.phone.getText().toString());
        contact.put("email", this.email.getText().toString());

        int checkedRadioId = this.gender.getCheckedRadioButtonId();
        RadioButton checkedGender = findViewById(checkedRadioId);
        contact.put("gender", checkedGender.getText().toString());

        return contact;
    }

    /**
     * Fill the fields of the form with data contained in map
     * @param contact
     */
    private void setFieldsValue(Map<String, String> contact) {
        // Set the values
        this.name.setText(contact.get("name"));
        this.firstname.setText(contact.get("firstname"));
        this.birthdate.setText(contact.get("birthdate"));
        this.phone.setText(contact.get("phone"));
        this.email.setText(contact.get("email"));

        // Handling gender check
        String genderValue = contact.get("gender");
        int id; // Id of the radio button to check
        switch (genderValue) {
            case "F":
                id = R.id.contact_gender_f;
                break;
            case "M":
                id = R.id.contact_gender_m;
                break;
            default:
                id = R.id.contact_gender_o;
        }

        this.gender.check(id);
    }

    /**
     * Displays the given message via a Toast
     * @param msg : message to display
     */
    public void displayMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Allows to save the given user to database
     * @param user : user to save
     */
    private void saveUserToDatabase(Map<String, String> user) {
        boolean insertedData = dbHelper.addContact(user);
        String message = (insertedData) ? "Success: user saved to database!" : "Error: user could not be saved to database";
        displayMessage(message); // Displaying return massage
    }

    /**
     * Configuration of the cancel button to end the activity
     * @return void
     */
    private void configureCancelButton() {
        Button cancelButton = findViewById(R.id.contact_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish(); // Finish the activity
            }
        });
    }

    /**
     * Configuration of the save button to start db saving processes etc
     */
    private void configureSaveButton() {
        Button saveButton = findViewById(R.id.contact_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> form = getFieldsValue(); // Retreiving the data from the fields

                String message = validateForm(form); // Validate the form
                displayMessage(message); // Display validation message

                if (message.startsWith("Success")) {
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
    }

    /**
     * Configuration of the delete button to delete a contact
     */
    private void configureDeleteButton() {
        Button deleteContact = findViewById(R.id.contact_delete);

        // If the selected contact is set, show the button
        if (this.contact == null) deleteContact.setVisibility(View.INVISIBLE);
        else deleteContact.setVisibility(View.VISIBLE);

        deleteContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contactEmail = contact.get("email");
                boolean deletedUser = dbHelper.deleteContact(contactEmail);

                // Finish the activity
                if (deletedUser) {
                    displayMessage("Success: the contact " + contactEmail + " was deleted!");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    displayMessage("Error: the contact " + contactEmail + " could not be deleted!");
                }
            }
        });
    }

    /**
     *  On create method
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_contact);

        // Initialization of the attributes
        this.name = findViewById(R.id.contact_name); // Name field
        this.firstname = findViewById(R.id.contact_firstname); // Firstname field
        this.birthdate = findViewById(R.id.contact_birthdate); // Birthdate field
        this.phone = findViewById(R.id.contact_phone); // Phone field
        this.email = findViewById(R.id.contact_email); // Email field
        this.gender = findViewById(R.id.contact_gender); // Gender radio group

        this.dbHelper = new DatabaseHelper(this); // Initializing the database helper

        // Handling edition or creation
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            if (extras.getString("action").equals("EDIT")) {
                String selectedContact = extras.getString("selectedContact");

                // Initializing the user
                Log.d(TAG, "selected contact email = " + selectedContact);
                Cursor contactCursor = dbHelper.getContactByEmail(selectedContact);
                this.contact = this.contactCursorToMap(contactCursor);

                this.setFieldsValue(this.contact); // Filling the fields

            }
        }

        this.configureSaveButton(); // Configuration of the save button
        this.configureCancelButton(); // Configuration of the cancel button to return to main activity
        this.configureDeleteButton(); // Configuration of the delete button
    }
}
