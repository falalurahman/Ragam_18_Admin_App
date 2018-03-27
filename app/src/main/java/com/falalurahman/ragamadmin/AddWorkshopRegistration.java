package com.falalurahman.ragamadmin;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class AddWorkshopRegistration extends AppCompatActivity implements ZBarScannerView.ResultHandler{

    private ZBarScannerView mScannerView;
    private ProgressDialog progressDialog;

    //Global Data
    private String WorkshopId;
    private String RagamId;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZBarScannerView(this);   // Programmatically initialize the scanner view
        mScannerView.setAspectTolerance(0.5f);
        mScannerView.setFormats(Collections.singletonList(BarcodeFormat.CODE128));
        setContentView(mScannerView);                // Set the scanner view as the content view
        setTitle("Add Participants");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferenceController.initializeSharedPreferences(this);
        WorkshopId = SharedPreferenceController.getSharedPreference(SharedPreferenceController.EVENT_ID_CONSTANT);
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
            sendDataRequest(RagamId, WorkshopId);
        }else {
            Toast.makeText(AddWorkshopRegistration.this, "Invalid Ragam ID", Toast.LENGTH_LONG).show();
        }

    }

    private void sendDataRequest(final String RagamId, final String WorkshopId) {

        StringRequest userDetailsRequest = new StringRequest(
                Request.Method.POST,
                DataProvider.GET_WORKSHOP_USER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        progressDialog = null;
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
                        Toast.makeText(AddWorkshopRegistration.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                        mScannerView.resumeCameraPreview(AddWorkshopRegistration.this);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("RagamID", RagamId);
                params.put("WorkshopID", WorkshopId);

                return params;
            }
        };

        userDetailsRequest.setTag(DataProvider.GET_WORKSHOP_USER_TAG);
        DataProvider.getInstance(this).addToRequestQueue(userDetailsRequest);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Downloading Data...");
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                DataProvider.getInstance(AddWorkshopRegistration.this).cancelRequest(DataProvider.GET_WORKSHOP_USER_TAG);
                mScannerView.resumeCameraPreview(AddWorkshopRegistration.this);
            }
        });
        progressDialog.setCancelable(true);
        progressDialog.show();

    }

    private void setParticipantParticipating(final String RagamId, final String WorkshopId) {

        StringRequest userDetailsRequest = new StringRequest(
                Request.Method.POST,
                DataProvider.SET_WORKSHOP_ADD,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        progressDialog = null;
                        if(response.equals("SUCCESS")){
                            Toast.makeText(AddWorkshopRegistration.this,"Participant Added To Workshop", Toast.LENGTH_LONG).show();
                            finish();
                        }else {
                            Toast.makeText(AddWorkshopRegistration.this,"Error Adding Participant", Toast.LENGTH_LONG).show();
                            mScannerView.resumeCameraPreview(AddWorkshopRegistration.this);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        progressDialog = null;
                        Toast.makeText(AddWorkshopRegistration.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                        mScannerView.resumeCameraPreview(AddWorkshopRegistration.this);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("RagamID", RagamId);
                params.put("WorkshopID", WorkshopId);

                return params;
            }
        };

        userDetailsRequest.setTag(DataProvider.SET_WORKSHOP_ADD_TAG);
        DataProvider.getInstance(this).addToRequestQueue(userDetailsRequest);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Setting Participating...");
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                DataProvider.getInstance(AddWorkshopRegistration.this).cancelRequest(DataProvider.SET_WORKSHOP_ADD_TAG);
                mScannerView.resumeCameraPreview(AddWorkshopRegistration.this);
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
                        setParticipantParticipating(RagamId, WorkshopId);
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
                mScannerView.resumeCameraPreview(AddWorkshopRegistration.this);
            }
        });

        dialog.show();
    }
}
