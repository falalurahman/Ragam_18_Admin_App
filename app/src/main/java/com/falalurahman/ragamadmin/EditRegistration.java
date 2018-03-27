package com.falalurahman.ragamadmin;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EditRegistration extends AppCompatActivity {

    private EditText ragamIdText;
    private EditText nameText;
    private EditText collegeText;
    private EditText emailText;
    private EditText phoneText;
    private Button NewRagamIDButton;
    private Button SaveProfileButton;
    private ProgressDialog progressDialog;

    private String oldRagamID;
    private String newRagamID;

    //TAGS
    private final int BARCODE_REQUEST = 1111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_registration);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ragamIdText = findViewById(R.id.ragam_id_text);
        nameText = findViewById(R.id.name_text);
        collegeText = findViewById(R.id.college_text);
        emailText = findViewById(R.id.email_text);
        phoneText = findViewById(R.id.phonenumber_text);
        NewRagamIDButton = findViewById(R.id.new_ragam_button);
        SaveProfileButton = findViewById(R.id.save_profile_button);

        String jsonString = getIntent().getStringExtra("UserDetails");
        try {
            JSONObject jsonObject = new JSONObject(jsonString);

            oldRagamID = jsonObject.getString("RagamID");
            newRagamID = oldRagamID;
            ragamIdText.setText(oldRagamID);
            nameText.setText(jsonObject.getString("Name"));
            collegeText.setText(jsonObject.getString("College"));
            emailText.setText(jsonObject.getString("Email"));
            phoneText.setText(jsonObject.getString("PhoneNumber"));

        }catch (JSONException exception){
            exception.printStackTrace();
            finish();
        }

        NewRagamIDButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditRegistration.this, ChangeRagamID.class);
                startActivityForResult(intent, BARCODE_REQUEST);
            }
        });

        SaveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BARCODE_REQUEST ){
            if(resultCode == RESULT_OK){
                newRagamID = data.getStringExtra("RagamID");
                ragamIdText.setText(newRagamID);
            }
        }
    }

    private void createDialog(){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Save User Profile")
                .setMessage("Do you want to save these user details?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        setUserDetails();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    private void setUserDetails() {

        StringRequest userDetaillsRequest = new StringRequest(
                Request.Method.POST,
                DataProvider.SET_USER_DETAILS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(progressDialog!=null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                        if(response.equals("SUCCESS")){
                            Toast.makeText(EditRegistration.this,"User Details Saved", Toast.LENGTH_LONG).show();
                            finish();
                        }else {
                            Toast.makeText(EditRegistration.this,"Error Saving User Details", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(progressDialog!=null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                        Toast.makeText(EditRegistration.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("OldRagamID", oldRagamID);
                params.put("NewRagamID", newRagamID);
                params.put("Name", nameText.getText().toString());
                params.put("College", collegeText.getText().toString());
                params.put("PhoneNumber", phoneText.getText().toString());

                return params;
            }
        };

        userDetaillsRequest.setTag(DataProvider.SET_USER_DETAILS_TAG);
        DataProvider.getInstance(this).addToRequestQueue(userDetaillsRequest);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving Profile...");
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                DataProvider.getInstance(EditRegistration.this).cancelRequest(DataProvider.SET_USER_DETAILS_TAG);
            }
        });
        progressDialog.setCancelable(true);
        progressDialog.show();

    }



}
