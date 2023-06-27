package Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.familymap.DataCache;
import com.example.familymap.PersonActivity;
import com.example.familymap.R;
import com.example.familymap.SearchActivity;
import com.example.familymap.SettingsActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Marker;

// Import everything need for making the menu buttons and their icons
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;

// Import everything need for adding the markers onto map
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Model.Event;
import Model.Person;

// Need class to implement onMarkerClickListener to make event markers clickable
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleMap.OnMarkerClickListener {
    private GoogleMap map;

    // Create variables for all of the fields that show up on the screen when user is using the map
    private TextView eventInfoTextBox;
    private ImageView littlePersonImage;
    private View view;
    private DataCache dataCache;
    private Map<String, Float> markerColors = new HashMap<>();
    private Polyline theSpouseLine;
    private Polyline lifeStoryline;
    private final ArrayList<Polyline> allLifeStoryLines = new ArrayList<>();
    private ArrayList<Polyline> paternalAncestorFamilyLines = new ArrayList<>();
    private ArrayList<Polyline> maternalAncestorFamilyLines = new ArrayList<>();

    private Polyline familyTreeLine;

    private final float DEFAULT_POLYLINE_WIDTH = 10.0f;
    private Person clickedPersonStored;
    private Event clickedEventStored;
    private final static String PERSON_ID_KEY = "personID";

    public MapFragment() {
        // Required empty public constructor
    }

    // Inflate the layout for fragment
    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        dataCache = DataCache.getInstance();     // get an instance of the datacache class that can work with throughout the entire map!

        super.onCreateView(layoutInflater, container, savedInstanceState);
        view = layoutInflater.inflate(R.layout.fragment_map, container, false);

        // NEED search + settings buttons as menu options here so need these 2 lines to be able to display those menu buttons on map screen
        setHasOptionsMenu(true);
        Iconify.with(new FontAwesomeModule());

        // Get the view thing for the eventInfoTextBox and the littlePersonImage parts of the screen
        eventInfoTextBox = view.findViewById(R.id.eventInfoTextBoxID);
        littlePersonImage = view.findViewById(R.id.littlePersonImageID);

        // Set the litte image spot on the map fragment xml layout page to be the android icon that want there!
        // Then later on when use the map, this icon will be replaced with the gender icons!
        Drawable androidIcon;
        androidIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_android).colorRes(R.color.android_icon);
        littlePersonImage.setImageDrawable(androidIcon);

        // Go to child fragment manager, get pointer to support map fragment (in the xml layout code)
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        // Once get pointer to that fragment: call getMapAsync on it to init the map, the very beginning
        // Pass it the callback (which is just ourselves that it shld call when map's ready)
        mapFragment.getMapAsync(this);

        return view;
    }

    // Need the search + settings buttons as menu options on the map so set those up!
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);

        // Create the actual menu items now for search + settings menu buttons and also set their icon images
        MenuItem searchButtonMenuItem = menu.findItem(R.id.searchMenuButtonID);
        // Lab spec says fa_search is the icon need for search menu button
        searchButtonMenuItem.setIcon(new IconDrawable(getActivity(), FontAwesomeIcons.fa_search)
                .colorRes(R.color.white).actionBarSize());

        MenuItem settingsButtonMenuItem = menu.findItem(R.id.settingsMenuButtonID);
        // Lab spec says fa_gear is icon need for settings menu button
        settingsButtonMenuItem.setIcon(new IconDrawable(getActivity(), FontAwesomeIcons.fa_gear)
                .colorRes(R.color.white).actionBarSize());
    }

    // Will switch over to search or settings menus/ activities from the map page if the user clicks on their icons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        if (item.getItemId() == R.id.searchMenuButtonID) {
            intent = new Intent(getActivity(), SearchActivity.class);
            startActivity(intent);
            return true;
        }
        else if (item.getItemId() == R.id.settingsMenuButtonID) {
            intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    // Stores pointer to the map, puts marker on Sydney, Australia haha
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.clear();      // Clear the map for each time come back to it
        map.setOnMapLoadedCallback(this);

        // Be able to zoom on the map!
        UiSettings settings = map.getUiSettings();
        settings.setZoomControlsEnabled(true);

        // Set the markers for all events and make sure all of those markers appear when first get on the map!
        createEventMarkers();

        // Now figure out if are on event activity or not
        if (dataCache.isHaveEventActivity()) {
            // If are in an event activity: don't want any menu options to appear so turn them off!
            setHasOptionsMenu(false);

            if (getArguments().getString("eventID") != null) {
                clickedEventStored = dataCache.getEventByID(getArguments().getString("eventID"));
                clickedPersonStored = dataCache.getPersonByID(clickedEventStored.getPersonID());

                // Now zoom into that event that was clicked in the activity
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(clickedEventStored.getLatitude(), clickedEventStored.getLongitude()),
                        5.0f));

                // Also need to put their name and their icon onto the screen!
                displayGenderIcon();
                displayEventInfo();

                // Also need to add all of the lines for them now!
                // Then re-draw all lines have
                displayLines();
            }
        }

        // Now that the markers are on the screen: make them clickable using onMarkerClickListener
        // So this implements everything need do when user clicks on any of the markers on map3
        map.setOnMarkerClickListener(this);
    }

    @Override
    public void onMapLoaded() {
        // You probably don't need this callback. It occurs after onMapReady and I have seen
        // cases where you get an error when adding markers or otherwise interacting with the map in
        // onMapReady(...) because the map isn't really all the way ready. If you see that, just
        // move all code where you interact with the map (everything after
        // map.setOnMapLoadedCallback(...) above) to here.
    }

    // Set all of the event markers on the map!
    public void createEventMarkers() {
        // First need get all of the event marker colors out of the data cache class!!!
        markerColors = dataCache.getEventTypeColors();

        Set<Event> allEvents = dataCache.filterBySettings();

        // Now create the events for events just grabbed from data cache
        for (Event event :  allEvents) {
            Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(event.getLatitude(), event.getLongitude())).
                    icon(BitmapDescriptorFactory.defaultMarker
                            (markerColors.get(event.getEventType().toLowerCase()))));

            marker.setTag(event);
        }
    }

    // Need override this func to make event markers clickable!!!
    // In this func: actually handle the click itself (this is what happens when the event marker is clicked!!!)
    // So will change the android icon to be gender icon + change the text in the bottom banner to have all of the event info
    // Will also make it so moves to person activity if the user clicks anywhere on the event info text box
    // Also need draw all lines for event that are clicked on - AHHHHHHHHHHHH!!!!
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        // Get the event that is in the marker just clicked
        clickedEventStored = (Event) marker.getTag();

        // When click on an event marker: need the event info and gender icon for the person whose
        // event it is to come up on the screen in the bottom banner

        // So get the person that is associated with the event just clicked on
        clickedPersonStored = dataCache.getPersonByID(clickedEventStored.getPersonID());

        // Now get the gender icon up on the screen (replace the green android man with the gender icon)
       displayGenderIcon();

       displayEventInfo();

        // Want move to person activity once event info is displayed in bottom banner
        // But do NOT want move to person activity if an event hasn't been selected yet
        // So move to person activity in here AFTER an event marker has been clicked!
        // (so need a listener for if the user clicks anywhere in the linear layout (from xml layout code) part of the screen
        // and if they do then need move over to the person activity screen!)
        // So do all of the view, setOnClickListener + onClick things that need do
        LinearLayout theBottomBanner = view.findViewById(R.id.mapLinearLayout);
        theBottomBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // When click the bottom banner: want to move over to the person activity so do that in here!
                Intent intent = new Intent(getActivity(), PersonActivity.class);
                intent.putExtra(PERSON_ID_KEY, clickedPersonStored.getPersonID());  // Need person info when move over to person act so that can pull up their info
                startActivity(intent);
            }
        });

        // Now need to set the 3 types of lines have for each event
        // BEFORE create lines for the event just clicked on - clear any lines that are on the screen from the previously clicked event!!!
        if (theSpouseLine != null) {
            theSpouseLine.remove();
        }
        clearLifeStoryLines();
        clearAllFamilyTreeLines();

        // ONLY want draw lines for events if their gender is enabled
        // (dont want draw lines to male events if male events arent turned on, etc.)
        // So check that the clickedPerson's gender is same as gender setting filters have set
        displayLines();

        return false;    // Return false is just a thing with these onMarkerClick funcs
    }

    // Draw all spouse lines for the person + event inputted
    public void createSpouseLines(Person clickedPerson, Event clickedEvent) {
        // Only want draw spouse lines if BOTH female, male events are enabled
        // Bc if one of the gender events are disabled and still draw spouse lines, will end up having lines that lead to now existing events
        if (dataCache.isMaleEvents() && dataCache.isFemaleEvents()) {
            // Only want create spouse line if the person in clicked event actually has spouse
            if (clickedPerson.getSpouseID() != null) {
                Person clickedPersonSpouse = dataCache.getPersonByID(clickedPerson.getSpouseID());

                // Get the 1st recorded event for the person's spouse also using data cache method
                // So get all of their events out and then get the first one since I have sorted them in order from oldest to most recent in the data cache
                Event clickedPersonSpouseFirstEvent = dataCache.getAllEventsForSpecifiedPerson(clickedPersonSpouse.getPersonID()).get(0);

                // Now actually add the line btw 2 events on map! (btw the clicked one and either the birth or 1st recorded event for the clicked person's spouse
                // Use addPolyline map method to add line btw 2 events
                LatLng startPoint = new LatLng(clickedEvent.getLatitude(), clickedEvent.getLongitude());                        // Line starts at clicked events marker
                LatLng endPoint = new LatLng(clickedPersonSpouseFirstEvent.getLatitude(), clickedPersonSpouseFirstEvent.getLongitude());  // Line ends at spouses 1st event

                PolylineOptions options = new PolylineOptions()
                        .add(startPoint)
                        .add(endPoint)
                        .color(Color.RED);

                theSpouseLine = map.addPolyline(options);
            }
        }
    }

    // Lines drawn connecting each event in person's life story (so the person associated with the selected event)
    // Lines are ordered chronologically (sorted events chronologically in data cache when store events for each person)
    public void createLifeStoryLines(Person clickedPerson) {
        // Get list of all of the clicked person's events via data cache class
        ArrayList<Event> allEventsList = dataCache.getAllEventsForSpecifiedPerson(clickedPerson.getPersonID());

        // These events shld be ordered chronologically already
        // So loop through all of them, draw line btw every 2 events that are next to each other starting with the oldest one, ending at most recent one
        // Only loop through size - 1 bc once on the last line, won't have another event to draw line to
        // So loop through and create each line btw each 2 events for the person one at a time
        for (int i = 0; i < allEventsList.size()-1; i++) {
            Event lineStartsAt = allEventsList.get(i);
            Event lineEndsAt = allEventsList.get(i+1);

            // Get all elements need to draw the poly line now
            LatLng startPoint = new LatLng(lineStartsAt.getLatitude(), lineStartsAt.getLongitude());
            LatLng endPoint = new LatLng(lineEndsAt.getLatitude(), lineEndsAt.getLongitude());

            PolylineOptions options = new PolylineOptions()
                    .add(startPoint)
                    .add(endPoint)
                    .color(Color.GREEN);

            // Create each individual line between the 2 chronological events
            lifeStoryline = map.addPolyline(options);

            // Since have multiple life story lines potentially for a person, store all of those in a list so that can clear all
            // of the lines at once when click on a new person
            allLifeStoryLines.add(lifeStoryline);
        }
    }

    // Could potentially have more than 1 life story line for a person so when click on a new person and need the previous persons story lines
    // to go away, need clear ALL of those lines at once or lines will be leftover for the previous person
    public void clearLifeStoryLines() {
        // Loop through all lines have stored, remove them one by one
        for (Polyline line : allLifeStoryLines) {
            line.remove();
        }
    }

    public void createFamilyTreeLines(Person clickedPerson, Event clickedEvent, float lineSize) {
        // Add all father side lines if they even have a father
        // Only draw father lines if male events are enabled
        // Only want draw father family lines if showFatherSide is enabled so include that as a check before go and draw all the father family tree lines
        if (clickedPerson.getFatherID() != null && dataCache.isMaleEvents() && dataCache.isFatherSide()) {
            // Then get the father out!
            Person theFather = dataCache.getPersonByID(clickedPerson.getFatherID());
            ArrayList<Event> allFatherEvents = dataCache.getAllEventsForSpecifiedPerson(theFather.getPersonID());

            // Make sure that the father really exists and that the father has recorded events before draw the line
            if (theFather != null && allFatherEvents != null) {
                // Then the father should have a first event so get that event out
                Event fatherFirstEvent = allFatherEvents.get(0);
                actuallyDrawFamilyTreeLine(clickedEvent, fatherFirstEvent, theFather, lineSize);

                // Recursively call func to draw lines past the 1st generation with the father + his 1st event to get his father's 1st event line, etc.
                // By doing this, will keep going until no generations left
                createFamilyTreeLines(theFather, fatherFirstEvent, lineSize - 5.0f);
            }
        }
        // Add all mother side lines too if they even have a mother
        // Only draw mother lines if female events are enabled
        // Same as father lines - only want draw mother side lines if isMotherSide is enabled
        if (clickedPerson.getMotherID() != null && dataCache.isFemaleEvents() && dataCache.isMotherSide()) {
            Person theMother = dataCache.getPersonByID(clickedPerson.getMotherID());
            ArrayList<Event> allMotherEvents = dataCache.getAllEventsForSpecifiedPerson(theMother.getPersonID());

            // Make sure that the mother really exists + that mother has recorded events before draw the line
            if (theMother != null && allMotherEvents != null) {
                // Then the mother should have a first event so get that event out
                Event motherFirstEvent = allMotherEvents.get(0);
                actuallyDrawFamilyTreeLine(clickedEvent, motherFirstEvent, theMother, lineSize);

                // Recursively call this func with the mother + her 1st event to get her mother's 1st event line, etc.
                // Will cover all mother generations by doing this
                createFamilyTreeLines(theMother, motherFirstEvent, lineSize - 5.0f);
            }
        }
    }

    // For creating the line between each person + their ancestor's 1st event
    public void actuallyDrawFamilyTreeLine(Event childEvent, Event parentEvent, Person theParent, float lineSize) {
        LatLng startPoint = new LatLng(childEvent.getLatitude(), childEvent.getLongitude());
        LatLng endPoint = new LatLng(parentEvent.getLatitude(), parentEvent.getLongitude());

        PolylineOptions options = new PolylineOptions()
                .add(startPoint)
                .add(endPoint)
                .color(Color.BLUE)
                .width(lineSize);

        familyTreeLine = map.addPolyline(options);

        // Add the line to either the dad or mom lines lists based on which one it is a line for
        if (theParent.getGender().equals("m")) {
            paternalAncestorFamilyLines.add(familyTreeLine);
        }
        if (theParent.getGender().equals("f")) {
            maternalAncestorFamilyLines.add(familyTreeLine);
        }
    }

    public void clearAllFamilyTreeLines() {
        // Clear all father lines
        for (Polyline line : paternalAncestorFamilyLines) {
            line.remove();
        }

        // Clear all mother lines
        for (Polyline line: maternalAncestorFamilyLines) {
            line.remove();
        }
    }

    // This will be called when the up button is clicked (so when settings have changed for what want displayed on map)
    // So when go back to the map it should have the new settings automatically implemented on there!
    @Override
    public void onResume() {
        super.onResume();

        // onResume is called when the activity is created so need to check for this here
        // because will not want change map if it hasn't even been made yet for the 1st time originally
        if (map != null) {
            // Clear everything from the map first
            map.clear();

            // Then re-draw everything based on the filters chosen in the settings (these setting choices are stored in the data cache)!
            createEventMarkers();

            // Then re-draw all lines have
            displayLines();
        }
    }

    // Call this twice in code so put it in a function so don't have duplicate code!
    // For displaying the gender icon onto the screen
    public void displayGenderIcon() {
        if (clickedPersonStored.getGender().equals("f")) {
            Drawable femaleIcon;
            femaleIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_female).colorRes(R.color.female_icon);

            // littlePersonImage in xml layout code stores the spot where want to put the gender icons so set it to be the female icon
            littlePersonImage.setImageDrawable(femaleIcon);
        }
        else if (clickedPersonStored.getGender().equals("m")) {
            Drawable maleIcon;
            maleIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_male).colorRes(R.color.male_icon);
            littlePersonImage.setImageDrawable(maleIcon);
        }
    }

    // For displaying event info on the bottom banner!
    public void displayEventInfo() {
        // Need to change the text view box in the bottom banner to have the clicked events info in there instead too!
        // So need replace the eventInfoTextBox in the xml layout code
        // In here need the: person's 1st name, last name, EVENT TYPE: event city, event country (event year)
        String theEventInfo = clickedPersonStored.getFirstName() + " " + clickedPersonStored.getLastName() + "\n"
                + clickedEventStored.getEventType().toUpperCase() + ": " + clickedEventStored.getCity() + ", " +
                clickedEventStored.getCountry() + " (" + clickedEventStored.getYear() + ")";

        eventInfoTextBox.setText(theEventInfo);
    }

    // Code need to call to display all lines (have to do a couple times in here!)
    public void displayLines() {
        if (clickedPersonStored != null) {
            String genderHave = clickedPersonStored.getGender();
            if (genderHave.equals("m") && dataCache.isMaleEvents() || genderHave.equals("f") && dataCache.isFemaleEvents()) {
                // Create spouse lines
                if (dataCache.isShowSpouseLines()) {
                    createSpouseLines(clickedPersonStored, clickedEventStored);
                }

                // Create life story line
                if (dataCache.isLifeStoryLines()) {
                    createLifeStoryLines(clickedPersonStored);
                }

                if (dataCache.isMotherSide() || dataCache.isFatherSide()) {
                    // Create family tree lines
                    if (dataCache.isFamilyTreeLines()) {
                        createFamilyTreeLines(clickedPersonStored, clickedEventStored, DEFAULT_POLYLINE_WIDTH);
                    }
                }
            }
        }
    }
}