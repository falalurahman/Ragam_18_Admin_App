package com.falalurahman.ragamadmin;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class AddEventTeamMember extends AppCompatActivity implements ZBarScannerView.ResultHandler{

    private ZBarScannerView mScannerView;
    private ProgressDialog progressDialog;

    //Global Data
    private String EventId;
    private String RagamId;
    private String CHECK_URL;
    private String ActivityResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZBarScannerView(this);   // Programmatically initialize the scanner view
        mScannerView.setAspectTolerance(0.5f);
        mScannerView.setFormats(Collections.singletonList(BarcodeFormat.CODE128));
        setContentView(mScannerView);                // Set the scanner view as the content view
        setTitle("Add Team Members");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferenceController.initializeSharedPreferences(this);
        EventId = SharedPreferenceController.getSharedPreference(SharedPreferenceController.EVENT_ID_CONSTANT);

        CHECK_URL = DataProvider.GET_EVENT_USER_1;
        if(EventId.equals("EID026")||EventId.equals("EID027")||EventId.equals("EID028")||EventId.equals("EID029")||EventId.equals("EID030")||EventId.equals("EID031")||EventId.equals("EID032")){
            CHECK_URL = DataProvider.GET_EVENT_USER_2;
        }
        if(EventId.equals("EID048")||EventId.equals("EID049")||EventId.equals("EID050")||EventId.equals("EID054")||EventId.equals("EID055")||EventId.equals("EID056")||EventId.equals("EID057")){
            CHECK_URL = DataProvider.GET_EVENT_USER_3;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        RagamId = rawResult.getContents();
        if(RagamId.substring(0,3).equals("RID")){
            sendDataRequest(RagamId, EventId);
        }else {
            Toast.makeText(AddEventTeamMember.this, "Invalid Ragam ID", Toast.LENGTH_LONG).show();
        }

    }

    private void sendDataRequest(final String RagamId, final String EventId) {

        StringRequest userDetailsRequest = new StringRequest(
                Request.Method.POST,
                CHECK_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("Check",response);
                        progressDialog.dismiss();
                        progressDialog = null;
                        ActivityResult = response;
                        try {
                            JSONObject outputObject = new JSONObject(response);
                            createDialog(outputObject);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        progressDialog = null;
                        Toast.makeText(AddEventTeamMember.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                        mScannerView.resumeCameraPreview(AddEventTeamMember.this);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("RagamID", RagamId);
                params.put("EventID", EventId);

                return params;
            }
        };

        userDetailsRequest.setTag(DataProvider.GET_EVENT_USER_TAG);
        DataProvider.getInstance(this).addToRequestQueue(userDetailsRequest);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Downloading Data...");
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                DataProvider.getInstance(AddEventTeamMember.this).cancelRequest(DataProvider.GET_EVENT_USER_TAG);
                mScannerView.resumeCameraPreview(AddEventTeamMember.this);
            }
        });
        progressDialog.setCancelable(true);
        progressDialog.show();

    }

    private void setParticipantParticipating(final String RagamId, final String EventId) {

        StringRequest eventsRequest = new StringRequest(
                Request.Method.POST,
                DataProvider.SET_EVENT_ADD_TEAM_MEMBER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        progressDialog = null;
                        if(response.equals("SUCCESS")){
                            Toast.makeText(AddEventTeamMember.this,"Participant Added To Team", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent();
                            intent.putExtra("UserProfile",ActivityResult);
                            setResult(RESULT_OK,intent);
                            finish();
                        }else {
                            Toast.makeText(AddEventTeamMember.this,"Error Adding Participant To Team", Toast.LENGTH_LONG).show();
                            mScannerView.resumeCameraPreview(AddEventTeamMember.this);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        progressDialog = null;
                        Toast.makeText(AddEventTeamMember.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                        mScannerView.resumeCameraPreview(AddEventTeamMember.this);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("RagamID", RagamId);
                params.put("EventID", EventId);

                return params;
            }
        };

        eventsRequest.setTag(DataProvider.SET_EVENT_ADD_TEAM_MEMBER_TAG);
        DataProvider.getInstance(this).addToRequestQueue(eventsRequest);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding Team Member...");
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                DataProvider.getInstance(AddEventTeamMember.this).cancelRequest(DataProvider.SET_EVENT_ADD_TEAM_MEMBER_TAG);
                mScannerView.resumeCameraPreview(AddEventTeamMember.this);
            }
        });
        progressDialog.setCancelable(true);
        progressDialog.show();

    }

    private void createDialog(JSONObject dataObject){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.user_details);

        TextView nameText = dialog.findViewById(R.id.name_text);
        TextView collegeText = dialog.findViewById(R.id.college_text);
        TextView statusText = dialog.findViewById(R.id.status_text);
        Button confirmButton = dialog.findViewById(R.id.confirmButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);

        try {
            nameText.setText(dataObject.getString("Name"));
            collegeText.setText(dataObject.getString("College"));
            if(dataObject.getInt("Registered") == 1){
                statusText.setTextColor(Color.parseColor("#23990f"));
                statusText.setText("Success");
                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setParticipantParticipating(RagamId, EventId);
                        dialog.dismiss();
                    }
                });
            }else {
                statusText.setTextColor(Color.parseColor("#b50522"));
                statusText.setText("Failure");
                confirmButton.setVisibility(View.GONE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                mScannerView.resumeCameraPreview(AddEventTeamMember.this);
            }
        });

        dialog.show();
    }


}
