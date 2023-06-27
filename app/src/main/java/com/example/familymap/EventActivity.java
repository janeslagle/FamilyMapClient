package com.example.familymap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import Fragments.MapFragment;

public class EventActivity extends AppCompatActivity {
    public final static String EVENT_ID_KEY = "eventID";
    private DataCache dataCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        dataCache = DataCache.getInstance();   // Create an instance of the datacache class so that are able to use stuff from it!
        dataCache.setHaveEventActivity(true);

        // Get the event info. from when went over to this activity (from the intent, get the eventID key want, the event centering on here)
        Intent intent = getIntent();
        String eventID = intent.getStringExtra(EVENT_ID_KEY);

        FragmentManager fragmentManager = this.getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.eventFragmentFrameLayout);

        if(fragment == null) {
            // Create a map fragment with the event that clicked on in the Event activity (just like how created a login fragment in the main act)
            fragment = createMapFragment(eventID);

            // Replace the fragment to be a map fragment that want show up on the screen when do an event activity!
            fragmentManager.beginTransaction()
                    .replace(R.id.eventFragmentFrameLayout, fragment)
                    .commit();
        }
    }

    // So that when press the up button it takes you back to the map and the map will be in the same exact state it was before
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return true;
    }

    // Just like createLoginFragment method have in main act
    private MapFragment createMapFragment(String eventID) {
        // Create an instance of the map fragment
        MapFragment theMapFragment = new MapFragment();

        // Set the map fragment to have the event that clicked this activity for! (So that will center on the event automatically with all of its lines)
        Bundle arguments = new Bundle();
        arguments.putString(EVENT_ID_KEY, eventID);
        theMapFragment.setArguments(arguments);

        return theMapFragment;
    }
}