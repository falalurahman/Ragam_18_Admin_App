package com.falalurahman.ragamadmin;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PresentParticipantsWorkshop extends AppCompatActivity {

    // UI references.
    private ListView mListView;
    private ProgressDialog progressDialog;

    private ArrayList<JSONObject> presentParticipants = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_present_participants_workshop);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent intent = new Intent(PresentParticipantsWorkshop.this,AddWorkshopRegistration.class);
            startActivity(intent);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mListView = findViewById(R.id.listview);

    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferenceController.initializeSharedPreferences(this);
        String workshopId = SharedPreferenceController.getSharedPreference(SharedPreferenceController.EVENT_ID_CONSTANT);

        sendDataRequest(workshopId);
    }

    private void populateListView(ArrayList<JSONObject> dataArray){

        PresentParticipant arrayAdapter = new PresentParticipant(this, dataArray);
        mListView.setAdapter(arrayAdapter);

    }

    private void sendDataRequest(final String WorkshopId){

        StringRequest workshopRequest = new StringRequest(
                Request.Method.POST,
                DataProvider.GET_WORKSHOP_PRESENT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONArray outputArray = new JSONArray(response);
                            for (int i = 0; i < outputArray.length(); i++) {
                                presentParticipants.add(outputArray.getJSONObject(i));
                            }

                            populateListView(presentParticipants);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(PresentParticipantsWorkshop.this, "No Internet Connection",Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();

                params.put("WorkshopID",WorkshopId);

                return params;
            }
        };

        workshopRequest.setTag(DataProvider.GET_WORKSHOP_PRESENT_TAG);
        DataProvider.getInstance(this).addToRequestQueue(workshopRequest);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Downloading Data...");
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                DataProvider.getInstance(PresentParticipantsWorkshop.this).cancelRequest(DataProvider.GET_WORKSHOP_PRESENT_TAG);
            }
        });
        progressDialog.setCancelable(true);
        progressDialog.show();

    }

    public class PresentParticipant extends ArrayAdapter<JSONObject> {

        private Context mContext;
        private ColorGenerator colorGenerator;
        private TextDrawable.IBuilder textDrawableBuilder;

        private PresentParticipant(@NonNull Context context, ArrayList<JSONObject> list) {
            super(context, 0, list);
            mContext = context;

            colorGenerator = ColorGenerator.MATERIAL;

            textDrawableBuilder = TextDrawable.builder()
                    .beginConfig()
                    .withBorder(4)
                    .endConfig()
                    .rect();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if (listItem == null)
                listItem = LayoutInflater.from(mContext).inflate(R.layout.row_registered_participants_workshop, parent, false);

            //UI References
            ImageView nameImage = listItem.findViewById(R.id.name_image);
            TextView nameText = listItem.findViewById(R.id.name_text);
            TextView collegeText = listItem.findViewById(R.id.college_text);

            //List Data
            try {
                JSONObject currentJSONObject = getItem(position);
                String Name = currentJSONObject.getString("Name");
                String College = currentJSONObject.getString("College");
                final String Phonenumber = currentJSONObject.getString("Phonenumber");

                int imageColor = colorGenerator.getColor(Name);
                TextDrawable textDrawable = textDrawableBuilder.build(Name.toUpperCase().substring(0,1), imageColor);
                nameImage.setImageDrawable(textDrawable);

                nameText.setText(Name);
                collegeText.setText(College);

                listItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:"+Phonenumber));
                        startActivity(intent);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return listItem;
        }
    }

}
