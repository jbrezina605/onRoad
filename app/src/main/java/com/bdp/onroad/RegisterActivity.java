package com.bdp.onroad;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bdp.onroad.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class RegisterActivity extends AppCompatActivity
{

    // Constants
    public static final String CHAT_PREFS = "ChatPrefs";
    public static final String DISPLAY_NAME_KEY = "username";



    private AutoCompleteTextView mEmailView;
    private AutoCompleteTextView mUsernameView;
    private AutoCompleteTextView mUserContactNumberView;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;
    private DatabaseReference mDatabaseRefrence;

    // Firebase instance variables
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.register_email);
        mPasswordView = (EditText) findViewById(R.id.register_password);
        mConfirmPasswordView = (EditText) findViewById(R.id.register_confirm_password);
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.register_username);
        mUserContactNumberView=(AutoCompleteTextView)findViewById(R.id.register_userContactNumber);

        // Keyboard sign in action
        mConfirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.integer.register_form_finished || id == EditorInfo.IME_NULL) {
                    attemptRegistration();
                    return true;
                }
                return false;
            }
        });

        // Getting instance of FirebaseAuth

        mAuth= FirebaseAuth.getInstance();

    }

    // Executed when Sign Up button is pressed.
    public void signUp(View v)
    {
        attemptRegistration();
    }

    private void attemptRegistration() {

        // Reset errors displayed in the form.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {

            createFirebaseUser();

        }
    }

    private boolean isEmailValid(String email)
    {

        return email.contains("@") && (email.length()>1);
    }

    private boolean isPasswordValid(String password)
    {

        String confirmedPass=mConfirmPasswordView.getText().toString();


        return (confirmedPass.equals(password))&&(password.length()>6);
    }

    //  Create a Firebase user
    private void createFirebaseUser()
    {
        String email=mEmailView.getText().toString();
        String pass=mPasswordView.getText().toString();
        Log.d("NewUserHas:",email +pass);
        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                Log.d("hey","createUser onComplete:"+task.isSuccessful());
                if(!task.isSuccessful())
                {
                    Log.d("hey","Could'nt create user"+task.getException());
                    showDialogue("Registeration attempt failed.");
                }
                else
                {
                    //saveDisplayNameLocally();
                    saveUserInfoOnDataBase();
                    Intent intnt = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intnt);
                    finish();
                }
            }
        });
    }

    //  Save the display name to Shared Preferences
    private void saveDisplayNameLocally()
    {
        String DispName=mUsernameView.getText().toString();
        SharedPreferences pref=getSharedPreferences(CHAT_PREFS,0);
        pref.edit().putString(DISPLAY_NAME_KEY,DispName).apply();

    }
    private void saveUserInfoOnDataBase()
    {
        FirebaseUser user = mAuth.getCurrentUser();
        String name=mUsernameView.getText().toString();
        if(user!=null)
        {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();
            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("hey", "User name saved to database with authenticated ID.");
                            }
                        }
                    });
        }

        mDatabaseRefrence= FirebaseDatabase.getInstance().getReference();
        User newuser = new User(mUsernameView.getText().toString(),mEmailView.getText().toString(),mUserContactNumberView.getText().toString());
        mDatabaseRefrence.child("Users").child(alterToMakeFBPath(mEmailView.getText().toString())).setValue(newuser);


    }

    //  alert dialog to show in case registration failed

    private void showDialogue(String Diag)
    {
        new AlertDialog.Builder(this)
                .setTitle("Waoh")
                .setMessage(Diag)
                .setPositiveButton(android.R.string.ok,null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    private String alterToMakeFBPath(String str)
    {
        Log.d("hey","got here");
        String ret="";
        for(int i=0;i<str.length();i++)
        {
            if(str.charAt(i)=='.'||str.charAt(i)=='#'||str.charAt(i)=='$'||str.charAt(i)=='['||str.charAt(i)==']')
                ret+='_';

            else
                ret+=str.charAt(i);

        }
        return ret;
    }

    public void SignInUser(View v)
    {
        Intent intent = new Intent(this, com.bdp.onroad.LoginActivity.class);
        startActivity(intent);
        onBackPressed();
        finish();
    }
}
