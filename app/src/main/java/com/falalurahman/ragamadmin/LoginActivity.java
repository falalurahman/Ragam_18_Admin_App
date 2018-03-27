package com.falalurahman.ragamadmin;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LoginActivity extends AppCompatActivity{

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private Spinner mMainSelector;
    private Spinner mSubSelector;

    //Data needed
    private JSONObject loginJSON;

    //Shared Preferences

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        checkPermission();

    }

    private void startActivity(){

        SharedPreferenceController.initializeSharedPreferences(this);

        Intent intent;
        if(SharedPreferenceController.getSharedPreference(SharedPreferenceController.COMMITEE_NAME_CONSTANT).equals("Workshops")){
            intent = new Intent(this, WorkshopMain.class);
            startActivity(intent);
            finish();
        }else if(SharedPreferenceController.getSharedPreference(SharedPreferenceController.COMMITEE_NAME_CONSTANT).equals("Events")){
            intent = new Intent(this, EventMain.class);
            startActivity(intent);
            finish();
        }else if(SharedPreferenceController.getSharedPreference(SharedPreferenceController.COMMITEE_NAME_CONSTANT).equals("Registration")){
            intent = new Intent(this, RegistrationMain.class);
            startActivity(intent);
            finish();
        }

        loginJSON = loadJSONFromAsset(this);
        // Set up UI
        mEmailView = findViewById(R.id.username);
        mPasswordView = findViewById(R.id.password);
        mMainSelector = findViewById(R.id.main_selector);
        mSubSelector = findViewById(R.id.sub_selector);

        mMainSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                addItemsOnSubSpinner(mMainSelector.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

    }

    private void checkPermission(){
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA)
                .withListener(new BaseMultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            startActivity();
                        }else {
                            checkPermission();
                        }
                    }
                })
                .check();
    }

    private void attemptLogin(){
        String username = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        String committee = mMainSelector.getSelectedItem().toString();
        String eventName = mSubSelector.getSelectedItem().toString();

        if(committee.equals("Workshops") || committee.equals("Events")) {
            JSONObject loginObject;
            try {
                loginObject = loginJSON.getJSONObject(committee).getJSONObject(eventName);
                if (loginObject.getString("Name").toLowerCase().equals(username.toLowerCase()) && loginObject.getString("Password").equals(password)) {
                    Intent intent;
                    if (committee.equals("Workshops")) {
                        intent = new Intent(this, WorkshopMain.class);
                    } else {
                        intent = new Intent(this, EventMain.class);
                    }
                    SharedPreferenceController.putSharedPreference(SharedPreferenceController.COMMITEE_NAME_CONSTANT, committee);
                    SharedPreferenceController.putSharedPreference(SharedPreferenceController.EVENT_ID_CONSTANT, loginObject.getString("EventId"));
                    SharedPreferenceController.putSharedPreference(SharedPreferenceController.EVENT_NAME_CONSTANT, eventName);
                    startActivity(intent);
                    finish();

                } else {
                    mEmailView.setError("Invalid Username / Password");
                    mPasswordView.setError("Invalid Username / Password");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else if(committee.equals("Registration")){
            if (username.toLowerCase().equals("registration") && password.equals("ragam2k18")) {
                Intent intent = new Intent(this, RegistrationMain.class);
                SharedPreferenceController.putSharedPreference(SharedPreferenceController.COMMITEE_NAME_CONSTANT, committee);
                startActivity(intent);
                finish();
            }else {
                mEmailView.setError("Invalid Username / Password");
                mPasswordView.setError("Invalid Username / Password");
            }
        }
    }

    private void addItemsOnSubSpinner(String Committee) {
        List<String> list = new ArrayList<>();
        if(Committee.equals("None") || Committee.equals("Registration")){
            list.add("None");
        }else {
            try {
                JSONObject MainObject = loginJSON.getJSONObject(Committee);
                Iterator<String> SubName = MainObject.keys();
                while(SubName.hasNext()){
                    list.add(SubName.next());
                }

            } catch (JSONException e) {
                e.printStackTrace();
                list.add("None");
            }
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSubSelector.setAdapter(dataAdapter);
    }

    private JSONObject loadJSONFromAsset(Context context){
        JSONObject json;
        try{
            InputStream inputStream = context.getAssets().open("loginFile.json");

            int size  = inputStream.available();

            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            json = new JSONObject(new String(buffer, "UTF-8"));
        } catch (Exception exception){
            exception.printStackTrace();
            return null;
        }
        return json;
    }

}

