package com.falalurahman.ragamadmin;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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

public class PresentTeams extends AppCompatActivity {

    // UI references.
    private ListView mListView;
    private ProgressDialog progressDialog;

    private HashMap<Integer, ArrayList<String>> presentTeams;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_present_teams);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PresentTeams.this, EditTeam.class);
                startActivity(intent);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mListView = findViewById(R.id.listview);
    }

    @Override
    protected void onStart() {
        super.onStart();

        presentTeams = new HashMap<>();

        SharedPreferenceController.initializeSharedPreferences(this);
        eventId = SharedPreferenceController.getSharedPreference(SharedPreferenceController.EVENT_ID_CONSTANT);

        sendDataRequest(eventId);
    }

    private void populateListView(HashMap<Integer, ArrayList<String>> dataArray){

        PresentParticipant arrayAdapter = new PresentParticipant(this, dataArray);
        mListView.setAdapter(arrayAdapter);

    }

    private void sendDataRequest(final String EventId){

        StringRequest workshopRequest = new StringRequest(
                Request.Method.POST,
                DataProvider.GET_EVENT_PRESENT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONArray outputArray = new JSONArray(response);
                            for (int i = 0; i < outputArray.length(); i++) {
                                JSONObject tempObject = outputArray.getJSONObject(i);
                                int teamNumber = tempObject.getInt("TeamNumber");
                                String name = tempObject.getString("Name");
                                ArrayList<String> TeamList;
                                if(presentTeams.containsKey(teamNumber)){
                                    TeamList = presentTeams.get(teamNumber);

                                }else {
                                    TeamList = new ArrayList<>();
                                }
                                TeamList.add(name);
                                presentTeams.put(teamNumber, TeamList);
                            }

                            populateListView(presentTeams);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(PresentTeams.this, "No Internet Connection",Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();

                params.put("EventID",EventId);

                return params;
            }
        };

        workshopRequest.setTag(DataProvider.GET_EVENT_PRESENT_TAG);
        DataProvider.getInstance(this).addToRequestQueue(workshopRequest);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Downloading Data...");
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                DataProvider.getInstance(PresentTeams.this).cancelRequest(DataProvider.GET_EVENT_PRESENT_TAG);
            }
        });
        progressDialog.setCancelable(true);
        progressDialog.show();

    }

    public class PresentParticipant extends ArrayAdapter<Integer> {

        private Context mContext;

        private HashMap<Integer, ArrayList<String>> listData;

        private PresentParticipant(@NonNull Context context, HashMap<Integer, ArrayList<String>> list) {
            super(context, 0, new ArrayList<>(list.keySet()));
            mContext = context;
            listData = list;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if (listItem == null)
                listItem = LayoutInflater.from(mContext).inflate(R.layout.row_team_member, parent, false);

            //UI References
            TextView teamText = listItem.findViewById(R.id.team_text);
            TextView nameText = listItem.findViewById(R.id.name_text);
            ImageView deleteButton = listItem.findViewById(R.id.delete_button);

            final int teamNumber = getItem(position);
            ArrayList<String> names = listData.get(teamNumber);

            String allNames = "";
            for (String name: names){
                allNames += name + "\n";
            }

            teamText.setText("Team Number " + Integer.toString(teamNumber));
            nameText.setText(allNames.trim());
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    createAlertDialog(teamNumber);
                }
            });
            listItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(PresentTeams.this, EditTeam.class);
                    intent.putExtra("TeamNumber",teamNumber);
                    startActivity(intent);
                }
            });

            return listItem;
        }

        private void createAlertDialog(final int teamNumber){
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Light_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(mContext);
            }
            builder.setTitle("Delete Team")
                    .setMessage("Are you sure you want to delete this team?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            sendDeleteDataRequest(eventId, teamNumber);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();
        }

        private void sendDeleteDataRequest(final String EventId, final int TeamNumber){

            StringRequest eventRequest = new StringRequest(
                    Request.Method.POST,
                    DataProvider.SET_EVENT_DELETE,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            if(response.equals("SUCCESS")){
                                Toast.makeText(PresentTeams.this,"Team removed from Event", Toast.LENGTH_LONG).show();
                                remove(TeamNumber);

                            }else {
                                Toast.makeText(PresentTeams.this,"Deletion Failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                            Toast.makeText(PresentTeams.this, "No Internet Connection",Toast.LENGTH_SHORT).show();
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

            eventRequest.setTag(DataProvider.SET_EVENT_DELETE_TAG);
            DataProvider.getInstance(PresentTeams.this).addToRequestQueue(eventRequest);

            progressDialog = new ProgressDialog(PresentTeams.this);
            progressDialog.setMessage("Deleting Team...");
            progressDialog.setCancelable(false);
            progressDialog.show();

        }


    }

}
