package com.falalurahman.ragamadmin;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

public class RegistrationMain extends AppCompatActivity implements ZBarScannerView.ResultHandler{

    private ZBarScannerView mScannerView;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZBarScannerView(this);   // Programmatically initialize the scanner view
        mScannerView.setAspectTolerance(0.5f);
        mScannerView.setFormats(Collections.singletonList(BarcodeFormat.CODE128));
        setContentView(mScannerView);                // Set the scanner view as the content view
        setTitle("Scan Barcode");
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
        String RagamId = rawResult.getContents();
        if(RagamId.substring(0,3).equals("RID")){
            sendDataRequest(RagamId);
        }else {
            Toast.makeText(RegistrationMain.this, "Invalid Ragam ID", Toast.LENGTH_LONG).show();
        }

    }

    private void sendDataRequest(final String RagamId) {

        StringRequest userDetailsRequest = new StringRequest(
                Request.Method.POST,
                DataProvider.GET_USER_DETAILS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        progressDialog = null;
                        if(response.equals("FAILURE")){
                            Toast.makeText(RegistrationMain.this, "Invalid Ragam ID", Toast.LENGTH_SHORT).show();
                        }else {
                            Intent intent = new Intent(RegistrationMain.this, EditRegistration.class);
                            intent.putExtra("UserDetails", response);
                            startActivity(intent);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        progressDialog = null;
                        Toast.makeText(RegistrationMain.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                        mScannerView.resumeCameraPreview(RegistrationMain.this);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("RagamID", RagamId);

                return params;
            }
        };

        userDetailsRequest.setTag(DataProvider.GET_USER_DETAILS_TAG);
        DataProvider.getInstance(this).addToRequestQueue(userDetailsRequest);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Downloading Data...");
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                DataProvider.getInstance(RegistrationMain.this).cancelRequest(DataProvider.GET_USER_DETAILS_TAG);
                mScannerView.resumeCameraPreview(RegistrationMain.this);
            }
        });
        progressDialog.setCancelable(true);
        progressDialog.show();

    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.workshop_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.logout){
            SharedPreferenceController.initializeSharedPreferences(this);
            SharedPreferenceController.putSharedPreference(SharedPreferenceController.COMMITEE_NAME_CONSTANT,"");
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
