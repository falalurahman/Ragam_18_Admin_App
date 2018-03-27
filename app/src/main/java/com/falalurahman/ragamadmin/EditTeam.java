package com.falalurahman.ragamadmin;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

public class EditTeam extends AppCompatActivity {

    // UI references.
    private ListView mListView;
    private ProgressDialog progressDialog;
    private Button mSaveButton;

    //Global Data
    private ArrayList<JSONObject> teamMembers = new ArrayList<>();
    private ArrayList<String> RagamIdList = new ArrayList<>();
    EditTeamListAdapter arrayAdapter;

    private int TeamNumber = -1;
    private String eventId;

    //TAGS
    private final int BARCODE_REQUEST = 1111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_team);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditTeam.this, AddEventTeamMember.class);
                startActivityForResult(intent, BARCODE_REQUEST);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mListView = findViewById(R.id.listview);
        mSaveButton = findViewById(R.id.save_button);

        SharedPreferenceController.initializeSharedPreferences(this);
        eventId = SharedPreferenceController.getSharedPreference(SharedPreferenceController.EVENT_ID_CONSTANT);

        TeamNumber = getIntent().getIntExtra("TeamNumber",-1);
        if(TeamNumber > 0){
            sendDataRequest(eventId, TeamNumber);
        }else {
            TeamNumber = SharedPreferenceController.getIntSharedPreference(eventId);
            if(TeamNumber == -1){
                TeamNumber = 1;
            }
            arrayAdapter = new EditTeamListAdapter(this, teamMembers);
            mListView.setAdapter(arrayAdapter);
        }

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSaveTeamRequest(eventId, TeamNumber, TextUtils.join(",", RagamIdList));
            }
        });

    }

    private void populateListView(ArrayList<JSONObject> dataArray){

        arrayAdapter = new EditTeamListAdapter(this, dataArray);
        mListView.setAdapter(arrayAdapter);

    }

    private void sendDataRequest(final String EventId, final int TeamNumber){

        StringRequest eventRequest = new StringRequest(
                Request.Method.POST,
                DataProvider.GET_EVENT_TEAM_MEMBERS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONArray outputArray = new JSONArray(response);
                            for (int i = 0; i < outputArray.length(); i++) {
                                teamMembers.add(outputArray.getJSONObject(i));
                                RagamIdList.add("'"+outputArray.getJSONObject(i).getString("RagamID")+"'");
                            }
                            populateListView(teamMembers);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(EditTeam.this, "No Internet Connection",Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();

                params.put("EventID",EventId);
                params.put("TeamNumber",Integer.toString(TeamNumber));

                return params;
            }
        };

        eventRequest.setTag(DataProvider.GET_EVENT_TEAM_MEMBERS_TAG);
        DataProvider.getInstance(this).addToRequestQueue(eventRequest);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Downloading Data...");
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                DataProvider.getInstance(EditTeam.this).cancelRequest(DataProvider.GET_EVENT_TEAM_MEMBERS_TAG);
                finish();
            }
        });
        progressDialog.setCancelable(true);
        progressDialog.show();

    }

    private void sendSaveTeamRequest(final String EventId, final int TeamNumber, final String RagamIdList){

        StringRequest eventRequest = new StringRequest(
                Request.Method.POST,
                DataProvider.SET_EVENT_SAVE_TEAM_MEMBERS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Toast.makeText(EditTeam.this,"Team Saved", Toast.LENGTH_LONG).show();
                        if(SharedPreferenceController.getIntSharedPreference(eventId) == TeamNumber || TeamNumber == 1){
                            SharedPreferenceController.putIntSharedPreference(eventId, TeamNumber+1);
                        }
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(EditTeam.this, "No Internet Connection",Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();

                params.put("EventID",EventId);
                params.put("TeamNumber",Integer.toString(TeamNumber));
                params.put("RagamIDList", RagamIdList);

                return params;
            }
        };

        eventRequest.setTag(DataProvider.SET_EVENT_SAVE_TEAM_MEMBERS_TAG);
        DataProvider.getInstance(this).addToRequestQueue(eventRequest);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving Team...");
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                DataProvider.getInstance(EditTeam.this).cancelRequest(DataProvider.SET_EVENT_SAVE_TEAM_MEMBERS_TAG);
            }
        });
        progressDialog.setCancelable(true);
        progressDialog.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BARCODE_REQUEST ){
            if(resultCode == RESULT_OK){
                try {
                    JSONObject tempUser = new JSONObject(data.getStringExtra("UserProfile"));
                    RagamIdList.add("'"+tempUser.getString("RagamID")+"'");
                    arrayAdapter.add(tempUser);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class EditTeamListAdapter extends ArrayAdapter<JSONObject> {

        private Context mContext;
        private ColorGenerator colorGenerator;
        private TextDrawable.IBuilder textDrawableBuilder;

        private EditTeamListAdapter(@NonNull Context context, ArrayList<JSONObject> list) {
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
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if (listItem == null)
                listItem = LayoutInflater.from(mContext).inflate(R.layout.row_edit_team, parent, false);

            //UI References
            ImageView nameImage = listItem.findViewById(R.id.name_image);
            TextView nameText = listItem.findViewById(R.id.name_text);
            TextView collegeText = listItem.findViewById(R.id.college_text);
            ImageView deleteButton = listItem.findViewById(R.id.delete_button);

            //List Data
            try {
                JSONObject currentJSONObject = getItem(position);
                String Name = currentJSONObject.getString("Name");
                String College = currentJSONObject.getString("College");
                final String RagamID = currentJSONObject.getString("RagamID");
                final int itemPosition = position;

                int imageColor = colorGenerator.getColor(Name);
                TextDrawable textDrawable = textDrawableBuilder.build(Name.toUpperCase().substring(0,1), imageColor);
                nameImage.setImageDrawable(textDrawable);

                nameText.setText(Name);
                collegeText.setText(College);

                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        createAlertDialog(RagamID ,itemPosition);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return listItem;
        }

        private void createAlertDialog(final String RagamID, final int position){
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Light_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(mContext);
            }
            builder.setTitle("Delete Person")
                    .setMessage("Are you sure you want to delete this person from this team?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            sendDeleteDataRequest(eventId, RagamID, position);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();
        }

        private void sendDeleteDataRequest(final String EventId, final String RagamId, final int position){

            StringRequest eventRequest = new StringRequest(
                    Request.Method.POST,
                    DataProvider.SET_EVENT_TEAM_MEMBER_DELETE,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            if(response.equals("SUCCESS")){
                                Toast.makeText(EditTeam.this,"Person removed from this Team", Toast.LENGTH_LONG).show();
                                RagamIdList.remove("'"+RagamId+"'");
                                remove(getItem(position));

                            }else {
                                Toast.makeText(EditTeam.this,"Deletion Failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                            Toast.makeText(EditTeam.this, "No Internet Connection",Toast.LENGTH_SHORT).show();
                        }
                    }
            ){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<>();

                    params.put("EventID",EventId);
                    params.put("RagamID",RagamId);

                    return params;
                }
            };

            eventRequest.setTag(DataProvider.SET_EVENT_TEAM_MEMBER_DELETE_TAG);
            DataProvider.getInstance(EditTeam.this).addToRequestQueue(eventRequest);

            progressDialog = new ProgressDialog(EditTeam.this);
            progressDialog.setMessage("Deleting Team Member...");
            progressDialog.setCancelable(false);
            progressDialog.show();

        }


    }

}
