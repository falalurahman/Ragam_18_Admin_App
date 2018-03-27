package com.falalurahman.ragamadmin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class WorkshopMain extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferenceController.initializeSharedPreferences(this);
        setTitle(SharedPreferenceController.getSharedPreference(SharedPreferenceController.EVENT_NAME_CONSTANT));

        Button mRegisteredParticipants = findViewById(R.id.registered);
        mRegisteredParticipants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WorkshopMain.this,RegisteredParticipantsWorkshop.class);
                startActivity(intent);
            }
        });

        Button mPresentParticipants = findViewById(R.id.present);
        mPresentParticipants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WorkshopMain.this,PresentParticipantsWorkshop.class);
                startActivity(intent);
            }
        });

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
