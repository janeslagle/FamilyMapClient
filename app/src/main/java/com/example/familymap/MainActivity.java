package com.example.familymap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

// Import all fragments that will need to embed into the main activity
import Fragments.LoginFragment;
import Fragments.MapFragment;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements LoginFragment.Listener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get pointer to fragment manager
        FragmentManager fragmentManager = this.getSupportFragmentManager();

        // Ask fragment manager for pointer to fragment in frame layout in main act xml code
        // Initially this is empty so fragment will come back null
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentFrameLayout);

        // Fragment will be null when app starts so create an instance of 1st fragment want which is login fragment
        if(fragment == null) {
            // Create login fragment object
            fragment = createLoginFragment();

            // Then insert login fragment into the frame layout so that it shows up when app first runs!
            // Use fragment manager to add fragment just created to frame layout that is in xml layout (embed it in frame layout here!)
            fragmentManager.beginTransaction()
                    .add(R.id.fragmentFrameLayout, fragment)
                    .commit();
        } else {
            // If fragment is not null, the MainActivity was destroyed and recreated
            // so we need to reset listener to the new instance of the fragment
            if(fragment instanceof LoginFragment) {
                ((LoginFragment) fragment).registerListener(this);
            }
        }
    }

    // Will create an instance of the login fragment so that can embed it in the main activity how want to!
    private Fragment createLoginFragment() {
        // Create an instance of login fragment
        LoginFragment fragment = new LoginFragment();

        // Attach main activity to the fragment as a listener (so that are able to swap to map fragment once done w/ login fragment)
        fragment.registerListener(this);
        return fragment;
    }

    // This is code that the main method wants execute when the login or register buttons are clicked from the login screen
    // This method gets called on the main activity, where do the fragment swap!
    @Override
    public void notifyDone() {
        // Get pointer to fragment manager
        FragmentManager fragmentManager = this.getSupportFragmentManager();

        // Create instance of the fragment want switch over to (the map fragment)
        Fragment fragment = new MapFragment();

        // Then replace the login fragment with the map fragment in the frame layout!
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentFrameLayout, fragment)
                .commit();
    }
}