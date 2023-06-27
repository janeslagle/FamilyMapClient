package com.example.familymap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import Model.Event;
import Model.Person;

public class PersonActivity extends AppCompatActivity {

    private DataCache dataCache;
    public final static String PERSON_ID_KEY = "personID";
    public Person personSelected = null;  // Declare this person up here so that can use them when figure out relationship to the person that are making the
                                          // expandable person list for

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        dataCache = DataCache.getInstance();   // Create an instance of the datacache class so that are able to use stuff from it!

        // Get the person out of the person activity intent thing when moved over to the person activity page!
        Intent intent = getIntent();
        String personID = intent.getStringExtra(PERSON_ID_KEY);
        personSelected = dataCache.getPersonByID(personID);

        // Do everything for person's 1st, last names + gender
        // Make all their view things
        TextView personFirstNameView = findViewById(R.id.selectedPersonFirstName);
        TextView personLastNameView = findViewById(R.id.selectedPersonLastName);
        TextView personGenderView = findViewById(R.id.selectedPersonGender);

        // Now set text for each of these view fields to be the info have from the person that clicked on the person activity for
        personFirstNameView.setText(personSelected.getFirstName());
        personLastNameView.setText(personSelected.getLastName());

        // Figure out the selected person's gender so that can set them to be the correct gender in the view field!
        String selectedPersonGender = personSelected.getGender();
        if (selectedPersonGender.equals("f")) {
            personGenderView.setText("Female");
        }
        if (selectedPersonGender.equals("m")) {
            personGenderView.setText("Male");
        }

        // Now need to do all of the expandable list stuff
        ExpandableListView expandableListView = findViewById(R.id.personExpandableListViewID);

        // Get the family for the person that are making this person activity for via the data cache class!
        List<Person> selectedPersonsFamily = dataCache.getFamilyForPerson(personSelected);

        // Get all of the events for the person that are making this person activity for via the data cache class!
        // Do everything for the expandable list in the expandable list class like the lecture video example!
        // Need filter the events before plug them into here!!! Use data cache to do this
        Set<Event> filteredEventsSet = dataCache.filterBySettings();
        ArrayList<Event> filteredEvents = new ArrayList<>(filteredEventsSet);
        List<Event> selectedPersonsEvents = dataCache.getAllEventsForSpecifiedPerson(personSelected.getPersonID());
        filteredEvents.retainAll(selectedPersonsEvents);  // Only retain the events for the person filtering for

        expandableListView.setAdapter(new ExpandableListAdapter(selectedPersonsFamily, filteredEvents));
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

    // Need to make an ExpandableListAdapter class!
    private class ExpandableListAdapter extends BaseExpandableListAdapter {
        // Follow the example given in lecture video ahhhhhh!!!
        private static final int PEOPLE_GROUP_POSITION = 1;
        private static final int EVENT_GROUP_POSITION = 0;

        // This class needs the person's family and the person's events so need make them parameters for the class
        // And need make the constructor for the class
        private List<Person> selectedPersonFamily;
        private List<Event> selectedPersonEvents;

        public ExpandableListAdapter(List<Person> selectedPersonFamily, List<Event> selectedPersonEvents) {
            this.selectedPersonFamily = selectedPersonFamily;
            this.selectedPersonEvents = selectedPersonEvents;
        }

        // Have 2 groups in this expandable list (1 for the person's events, 1 for the person's family)
        @Override
        public int getGroupCount() {
            return 2;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            switch(groupPosition) {
                case PEOPLE_GROUP_POSITION:
                    return selectedPersonFamily.size();
                case EVENT_GROUP_POSITION:
                    return selectedPersonEvents.size();
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
        }

        // Tells you which group you want to be expanding (the person or event one)
        // Gets the group based off of it's title is how I am using this function
        @Override
        public Object getGroup(int groupPosition) {
            switch(groupPosition) {
                case PEOPLE_GROUP_POSITION:
                    return R.string.personFamilyListTile;
                case EVENT_GROUP_POSITION:
                    return R.string.lifeEventsListTitle;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
        }

        // Returns the child from the group working with!
        // Returns the group stuff for the child at the position inputting
        @Override
        public Object getChild(int groupPosition, int childPosition) {
            switch(groupPosition) {
                case PEOPLE_GROUP_POSITION:
                    return selectedPersonFamily.get(childPosition);
                case EVENT_GROUP_POSITION:
                    return selectedPersonEvents.get(childPosition);
                default:
                    throw new IllegalArgumentException("Unrecognized Group Position: " + groupPosition);
            }
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            // Put the layout for both groups in the person_item_group list!
            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.person_item_group, parent, false);
            }

            // Put the id title that have in the person_item_group into here!
            TextView titleView = convertView.findViewById(R.id.listTitle);

            // Put the title for the list to be whatever have specified in the strings xml file
            switch (groupPosition) {
                case PEOPLE_GROUP_POSITION:
                    titleView.setText(R.string.personFamilyListTile);
                    break;
                case EVENT_GROUP_POSITION:
                    titleView.setText(R.string.lifeEventsListTitle);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View itemView;

            // Where get the layout of each expandable group correct!
            switch(groupPosition) {
                case PEOPLE_GROUP_POSITION:
                    // Specify that are working with the person_item xml here!
                    itemView = getLayoutInflater().inflate(R.layout.person_item, parent, false);
                    initializeFamilyView(itemView, childPosition);
                    break;
                case EVENT_GROUP_POSITION:
                    // Event group here so want use the event_item xml layout!!!
                    itemView = getLayoutInflater().inflate(R.layout.event_item, parent, false);
                    initializeLifeEventsView(itemView, childPosition);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }

            return itemView;
        }

        private void initializeFamilyView(View personItemView, final int childPosition) {
            // Get the person who is in the family list at the given child position
            Person correspondingPerson = selectedPersonFamily.get(childPosition);

            // Need display the gender icon for the person in the list (have a spot for the gender in xml code so need get image view out)
            // So set everything up for what the icon should look like (male or female and it's color)
            ImageView personGender = personItemView.findViewById(R.id.genderIcon);
            Drawable theGenderImage;

            if (correspondingPerson.getGender().equals("m")) {
                theGenderImage = new IconDrawable(PersonActivity.this, FontAwesomeIcons.fa_male).colorRes(R.color.male_icon);
            }
           else {
                theGenderImage = new IconDrawable(PersonActivity.this, FontAwesomeIcons.fa_female).colorRes(R.color.female_icon);
            }

            // Now actually set the gender icon to be what want it to be
            personGender.setImageDrawable(theGenderImage);

           // Now that have covered the gender icon, need to cover getting the person's name
            TextView theirName = personItemView.findViewById(R.id.thePersonName);

            // Now actually set their name to what their name is!
            String getTheirNameNow = correspondingPerson.getFirstName() + " " + correspondingPerson.getLastName();
            theirName.setText(getTheirNameNow);

            // Now need get the next section of the person's screen which is displaying their relationship to the person!
            TextView theirRelationshipToPerson = personItemView.findViewById(R.id.relationshipToPerson);
            String relationshipCategory = "Will figure this out below!";

            // Figure out what their relationship actually is to the person so that can set it correctly
            // Compare the person currently on to the person that have selected for the person activity (for the person actually making the list for)
            if (correspondingPerson.getPersonID().equals(personSelected.getFatherID())) {
                relationshipCategory = "Father";
            }
            if (correspondingPerson.getPersonID().equals(personSelected.getMotherID())) {
                relationshipCategory = "Mother";
            }
            if (correspondingPerson.getPersonID().equals(personSelected.getSpouseID())) {
                relationshipCategory = "Spouse";
            }
            if (correspondingPerson.getFatherID() != null) {
                if (correspondingPerson.getFatherID().equals(personSelected.getPersonID())) {
                    relationshipCategory = "Child";
                }
            }
            if (correspondingPerson.getMotherID() != null) {
                if (correspondingPerson.getMotherID().equals(personSelected.getPersonID())) {
                    relationshipCategory = "Child";
                }
            }

            theirRelationshipToPerson.setText(relationshipCategory);

            // Now need set the on click listener thing
            // When click on anything in the person expandable list: clicking a person from the list starts a NEW person activity with them
            // So start a new person activity here with the person that was inputted into here's child position!
            personItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PersonActivity.this, PersonActivity.class);
                    intent.putExtra(PERSON_ID_KEY, correspondingPerson.getPersonID());  // Need person info when move over to person act so that can pull up their info
                    startActivity(intent);
                }
            });
        }

        private void initializeLifeEventsView(View eventItemView, final int childPosition) {
            // Get the event + it's corresponding person out
            Event correspondingEvent = selectedPersonEvents.get(childPosition);
            Person eventsCorrespPerson = dataCache.getPersonByID(correspondingEvent.getPersonID());

            // Now set the location event marker icon to be displayed for each event have!

            // First need get the image view
            ImageView eventMarker = eventItemView.findViewById(R.id.markerIcon);
            Drawable theEventIcon;
            theEventIcon = new IconDrawable(PersonActivity.this, FontAwesomeIcons.fa_map_marker).colorRes(R.color.green);
            eventMarker.setImageDrawable(theEventIcon);

            // Now need set the event info text view field to be the info from the event currently on
            TextView eventInfoView = eventItemView.findViewById(R.id.eventInfo);
            String getEventInfo = correspondingEvent.getEventType().toUpperCase() + ": " + correspondingEvent.getCity() +
                    ", " + correspondingEvent.getCountry() + " (" + correspondingEvent.getYear() + ")";
            eventInfoView.setText(getEventInfo);

            // Now set the text for the person's name
            TextView theirFullName = eventItemView.findViewById(R.id.eventPerson);
            String getTheirName = eventsCorrespPerson.getFirstName() + " " + eventsCorrespPerson.getLastName();
            theirFullName.setText(getTheirName);

            // Now need do the set on click stuff (when a person clicks an event from the person activity page:
            // NEED an event activity to start up for it! So need move over to the event activity at this exact point!)
            eventItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PersonActivity.this, EventActivity.class);   // Want move over to the event activity!
                    intent.putExtra(EventActivity.EVENT_ID_KEY, correspondingEvent.getEventID());
                    startActivity(intent);
                }
            });
        }

        // Change to match the examples!!!
        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}