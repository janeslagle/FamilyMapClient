package com.example.familymap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {
    private DataCache dataCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dataCache = DataCache.getInstance();   // Create an instance of the datacache class so that are able to use stuff from it!

        // Need do all of the findViewById's that have for each switch button!
        Switch fatherSideSwitch = findViewById(R.id.fatherSideSwitch);
        Switch motherSideSwitch = findViewById(R.id.motherSideSwitch);
        Switch maleEventSwitch = findViewById(R.id.maleEventSwitch);
        Switch femaleEventSwitch = findViewById(R.id.femaleEventSwitch);
        Switch lifeStoryLineSwitch = findViewById(R.id.lifeStoryLineSwitchButton);
        Switch familyTreeLineSwitch = findViewById(R.id.familyTreeLineSwitch);
        Switch spouseLineSwitch = findViewById(R.id.spouseLineSwitch);
        LinearLayout logoutUser = findViewById(R.id.logout);    // Want log user out if they press anywhere on the logoutUser part of the settings screen

        // Now set all of the switch buttons to be checked based on the bool have stored in the data cache
        // that says whether want them checked or not (only have this for the switch buttons - not the logout part)
        fatherSideSwitch.setChecked(dataCache.isFatherSide());
        motherSideSwitch.setChecked(dataCache.isMotherSide());
        maleEventSwitch.setChecked(dataCache.isMaleEvents());
        femaleEventSwitch.setChecked(dataCache.isFemaleEvents());
        lifeStoryLineSwitch.setChecked(dataCache.isLifeStoryLines());
        familyTreeLineSwitch.setChecked(dataCache.isFamilyTreeLines());
        spouseLineSwitch.setChecked(dataCache.isShowSpouseLines());

        // Now need do the setOnClickListener for them all now and want to set it so that set it if the button is checked!
        fatherSideSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataCache.setFatherSide(fatherSideSwitch.isChecked());
            }
        });

        motherSideSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataCache.setMotherSide(motherSideSwitch.isChecked());
            }
        });

        fatherSideSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataCache.setFatherSide(fatherSideSwitch.isChecked());
            }
        });

        maleEventSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataCache.setMaleEvents(maleEventSwitch.isChecked());
            }
        });

        femaleEventSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataCache.setFemaleEvents(femaleEventSwitch.isChecked());
            }
        });

        lifeStoryLineSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataCache.setLifeStoryLines(lifeStoryLineSwitch.isChecked());
            }
        });

        familyTreeLineSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataCache.setFamilyYreeLines(familyTreeLineSwitch.isChecked());
            }
        });

        spouseLineSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataCache.setSpouseLines(!dataCache.isShowSpouseLines());
            }
        });

        // Need set the on click listener for the logout part too!
        logoutUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Need clear the datacache so that when one user logs out and another logs in the previous peron's data isn't still there!
                // This will also reset the settings to all be enabled when someone logs out
                dataCache.clearStoredDataAndSettings();

                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
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
}