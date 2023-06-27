package com.example.familymap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import Model.Event;
import Model.Person;

public class SearchActivity extends AppCompatActivity {
    // Want the people to show up above the events
    private static final int PERSON_RESULTS = 0;
    private static final int EVENT_RESULTS = 1;

    private DataCache dataCache;
    private Set<Event> allEvents;
    private Set<Person> allPeople;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dataCache = DataCache.getInstance();
        allPeople = dataCache.getUserPeople();
        allEvents = dataCache.filterBySettings();

        // Get the searchView + recyclerview out of the layout stuff
        SearchView searchView = findViewById(R.id.searchSearchView);

        RecyclerView recyclerView = findViewById(R.id.searchRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));

        // Need to set a listener for the search view (for if the user starts to type something in to search it!)
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            // When the user types text into the search view
            // Have to get the data out first!
            @Override
            public boolean onQueryTextChange(String searchedText) {
                // When user is searching here: they are searching for people and events
                // So keep track of the people + events that have any part of the inputtedText in them
                ArrayList<Person> peopleResults = new ArrayList<>();
                ArrayList<Event> eventResults = new ArrayList<>();

                if (searchedText.length() >= 1) {
                    // See if any of the people in the DB contain any part of the searchedText, if they do then keep track of them in peopleResults
                    for (Person person: allPeople) {
                        // Search people's 1st, last names so go through both
                        // Want ignore case when search
                        if (person.getFirstName().toLowerCase().contains(searchedText.toLowerCase()) ||
                                person.getLastName().toLowerCase().contains(searchedText.toLowerCase())) {
                            peopleResults.add(person);
                        }
                    }

                    // See if any of the events in the DB contain any part of the searchedText, keep track of those that do
                    // Want event to come up if matches any part of events countries, cities, event types, years
                    // Also want the events for the person that searched to come up too
                    for (Event event: allEvents) {
                        // Get person associated with event
                        Person associatedPerson = dataCache.getPersonByID(event.getPersonID());

                        // Put all in 1 if statement or event will be added to the results twice
                        if (event.getCountry().toLowerCase().contains(searchedText.toLowerCase()) ||
                                event.getCity().toLowerCase().contains(searchedText.toLowerCase()) ||
                                event.getEventType().toLowerCase().contains(searchedText.toLowerCase()) ||
                                Integer.toString(event.getYear()).contains(searchedText) ||
                                associatedPerson.getFirstName().toLowerCase().contains(searchedText.toLowerCase()) ||
                                associatedPerson.getLastName().toLowerCase().contains(searchedText.toLowerCase())) {
                            eventResults.add(event);
                        }
                    }
                }

                // Will define this class below (like in the lecture video example)
                // Init it with the searched people + searched events results
                SearchResultsAdapter adapter = new SearchResultsAdapter(peopleResults, eventResults);
                recyclerView.setAdapter(adapter);

                return true;
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

    private class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsViewHolder> {
        private final List<Person> people;
        private final List<Event> events;

        SearchResultsAdapter(List<Person> people, List<Event> events) {
            this.people = people;
            this.events = events;
        }

        @Override
        public int getItemViewType(int position) {
            return position < people.size() ? PERSON_RESULTS : EVENT_RESULTS;
        }

        @NonNull
        @Override
        public SearchResultsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;

            // Everything for people, use person_item xml that made for person/event activities
            if(viewType == PERSON_RESULTS) {
                view = getLayoutInflater().inflate(R.layout.person_item, parent, false);
            }
            // Everything for events, user event_item xml that made
            else {
                view = getLayoutInflater().inflate(R.layout.event_item, parent, false);
            }

            return new SearchResultsViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchResultsViewHolder holder, int position) {
            if(position < people.size()) {
                holder.bind(people.get(position));
            } else {
                holder.bind(events.get(position - people.size()));
            }
        }

        @Override
        public int getItemCount() {
            return people.size() + events.size();
        }
    }

    // This is what the user sees when they make the search (need gender icons, location icons, person name + event info)
    private class SearchResultsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView firstLine;
        private final TextView secondLine;
        private final ImageView iconImage;
        private final int viewType;
        private Person person;
        private Event event;

        SearchResultsViewHolder(View view, int viewType) {
            super(view);
            this.viewType = viewType;

            view.setOnClickListener(this);

            if(viewType == PERSON_RESULTS) {
                // Need get the gender icon out + their 1st, last name
                iconImage = view.findViewById(R.id.genderIcon);
                firstLine = view.findViewById(R.id.thePersonName);
                secondLine = null;   // With people search results: only show their name, nothing else when they come up
            } else {
                iconImage = view.findViewById(R.id.markerIcon);
                firstLine = view.findViewById(R.id.eventInfo);
                secondLine = view.findViewById(R.id.eventPerson);
            }
        }

        private void bind(Person person) {
            this.person = person;

            // Set the icon image, first line + 2nd lines to be what they should be for a person!
            Drawable theGenderIcon;
            if (person.getGender().equals("m")) {
                theGenderIcon = new IconDrawable(SearchActivity.this, FontAwesomeIcons.fa_male).colorRes(R.color.male_icon);
                iconImage.setImageDrawable(theGenderIcon);
            }
            else {
                theGenderIcon = new IconDrawable(SearchActivity.this, FontAwesomeIcons.fa_female).colorRes(R.color.female_icon);
                iconImage.setImageDrawable(theGenderIcon);
            }

            String theirName = person.getFirstName() + " " + person.getLastName();
            firstLine.setText(theirName);
        }

        private void bind(Event event) {
            this.event = event;
            Person eventsPerson = dataCache.getPersonByID(event.getPersonID());

            // Set the icon, event info and the persons name!
            Drawable theEventIcon;
            theEventIcon = new IconDrawable(SearchActivity.this, FontAwesomeIcons.fa_map_marker).colorRes(R.color.green);
            iconImage.setImageDrawable(theEventIcon);

            String eventInfo = event.getEventType() + ": " + event.getCity() +
                    ", " + event.getCountry() + " (" + event.getYear() + ")";
            firstLine.setText(eventInfo);

            String theirName = eventsPerson.getFirstName() + " " + eventsPerson.getLastName();
            secondLine.setText(theirName);
        }

        // What want to happen when someone clicks on any of the search results!
        @Override
        public void onClick(View view) {
            if(viewType == PERSON_RESULTS) {
                // If they click on a person then want to start a person activity for the person clicked on!
                String personIDClickedOn = this.person.getPersonID();

                Intent intent = new Intent(SearchActivity.this, PersonActivity.class);
                intent.putExtra(PersonActivity.PERSON_ID_KEY, personIDClickedOn);
                startActivity(intent);
            } else {
                // If they click on an event then want to start an event activity for them!
                String eventIDClickedOn = this.event.getEventID();

                Intent intent = new Intent(SearchActivity.this, EventActivity.class);
                intent.putExtra(EventActivity.EVENT_ID_KEY, eventIDClickedOn);
                startActivity(intent);
            }
        }
    }
}
