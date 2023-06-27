package com.example.familymap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Model.Event;
import Model.Person;
import Result.AllEventsResponse;
import Result.AllPeopleResponse;

public class DataCache {
    private static DataCache instance;

    public static DataCache getInstance() {
        if (instance == null) {
            instance = new DataCache();
        }
        return instance;
    }

    private DataCache() {
    }
    private Person user;
    private final Set<Person> maternalAncestors = new HashSet<>();
    private final Set<Person> paternalAncestors = new HashSet<>();
    private final Set<Person> userPeople = new HashSet<>();
    private final Set<Event>  userEvents = new HashSet<>();
    private final Map<String, Person> personByID = new HashMap<>();
    private final Map<String, Event> eventByID = new HashMap<>();
    private final Map<String, ArrayList<Event>> eventsForPeople = new HashMap<>();
    private final Set<String> eventTypes = new HashSet<>();               // Set that will store all of the different event types that have in data
    private final Map<String, Float> eventTypeColors = new HashMap<>();   // Will store all of the colors associated with each different event type
    private final Set<Event> maleEvents = new HashSet<>();
    private final Set<Event> femaleEvents = new HashSet<>();

    // Will store all of the colors that the markers could be in here! These colors are the bitmap descriptor ones
    private final ArrayList<Float> availableColors = new ArrayList<Float>() {{
        add(210f);
        add(240f);
        add(180f);
        add(120f);
        add(300f);
        add(30f);
        add(0f);
        add(330f);
        add(270f);
        add(60f);
        }
    };

    // Everything need for settings menu
    private static boolean showLifeStoryLines = true;
    private static boolean showFamilyTreeLines = true;
    private static boolean showSpouseLines = true;
    private static boolean showFatherSide = true;
    private static boolean showMotherSide = true;
    private static boolean showMaleEvents = true;
    private static boolean showFemaleEvents = true;
    private boolean haveEventActivity = false;         // Will use to figure out if are in the event activity or not

    // Method that will set all of the data need for the inputted user with its inputted people and events as well
    public void setData(String userPersonID, AllPeopleResponse people, AllEventsResponse events) {
        // Set the people data
        setPeople(people);

        // Get the user out so that can get their first and last names out
        user = getPersonByID(userPersonID);
        setBothSidesAncestors(userPersonID);  // Also store all of the ancestors for both sides

        // Set the event data
        setEvents(events);

        // Now call method where actually set all of the different event type marker colors
        setMarkerColors();   // Should be able to access eventTypeColors now via DataCache class whenever want to!
    }

    private void setBothSidesAncestors(String userPersonID) {
        Person theUser = getPersonByID(userPersonID);

        String userFatherID = theUser.getFatherID();
        Person theFather = getPersonByID(userFatherID);

        String userMotherID = theUser.getMotherID();
        Person theMother = getPersonByID(userMotherID);

        if (userFatherID != null) {
            paternalAncestors.add(theFather);

            // Now need recurse over all the people on their side + get all of the father side people
            getOneSideEntirely(userFatherID, false);
        }
        if (userMotherID != null) {
            maternalAncestors.add(theMother);

            // Now need recurse over all the people on their side + get all of the mother side people
            getOneSideEntirely(userMotherID, true);
        }
    }

    // Use bool to tell you which side want to add the ancestors to!
    private void getOneSideEntirely(String sidePersonID, boolean partOfMaternalSide) {
        Person sidePerson = getPersonByID(sidePersonID);

        String sidePersonMotherID = sidePerson.getMotherID();
        Person sidePersonMother = getPersonByID(sidePersonMotherID);

        String sidePersonFatherID = sidePerson.getFatherID();
        Person sidePersonFather = getPersonByID(sidePersonFatherID);

        if (sidePersonMotherID != null) {
            if (partOfMaternalSide) {
                maternalAncestors.add(sidePersonMother);
            }
            else {
                paternalAncestors.add(sidePersonMother);
            }
            getOneSideEntirely(sidePersonMotherID, partOfMaternalSide);
        }

        if (sidePersonFatherID != null) {
            // Check if they are on the paternal side
            if (partOfMaternalSide) {
                maternalAncestors.add(sidePersonFather);
            }
            else {
                paternalAncestors.add(sidePersonFather);
            }
            getOneSideEntirely(sidePersonFatherID, partOfMaternalSide);
        }
    }

    public void setPeople(AllPeopleResponse people) {
        List<Person> allPeopleList = people.getEventData();

        // Loop through all the people
        for (Person person : allPeopleList) {
            // Add the people for the user
            userPeople.add(person);
            personByID.put(person.getPersonID(), person);
        }
    }

    // In here: store events in chronological order!!!! NEED TO AHHHHHHH!!!!!
    public void setEvents(AllEventsResponse events) {
        List<Event> allEventsList = events.getEventData();

        // Loop through all the events
        for (Event event : allEventsList) {
            // Add the events for the user
            userEvents.add(event);
            eventByID.put(event.getEventID(), event);

            // Add the event type to the set where are storing all of the different event types have
            String eventType = event.getEventType().toLowerCase();
            eventTypes.add(eventType);

            // Add the event to list that stores its gender bc will need for filtering settings on map!
            // So first get the person associated with the event so that can get their gender out of it
            Person thePersonWithEvent = getPersonByID(event.getPersonID());

            // Check if the person is male, if are then add to set where storing all male events
            if (thePersonWithEvent.getGender().equals("m")) {
                maleEvents.add(event);
            }
            else if (thePersonWithEvent.getGender().equals("f")) {
                femaleEvents.add(event);
            }

            // Need add all events have for each person stored in data (have a map eventsForPeople to do so)
            // NEED add all events for each person in the order in which they occured (from earliest to most recent) for lines on map
            // So first check if personID in map already has an event list (if already have events stored for the specific person)
            if (eventsForPeople.containsKey(event.getPersonID())) {
                // Keep track of all of the events added for the specific person at in map
                ArrayList<Event> newEventList = eventsForPeople.get(event.getPersonID());

                // Cover adding birth + death events 1st bc those are easy!
                // If the person has a birth event, add it first to their list
                if (event.getEventType().equals("birth")) {
                    newEventList.add(0, event);
                }
                else if (event.getEventType().equals("death")) {
                    int endIndex = newEventList.size();  // If person has death event, add it as their very last event in their list
                    newEventList.add(endIndex, event);
                }
                // Other case is that the event currently on is neither a birth or death event
                // Then need loop through all other events have stored for the person + place the event currently on in the correct position
                else {
                    // So loop through all events have for the person
                    // Since adding the events one at a time, loop all events currently have in the list and compare the event adding to each stored event
                    for (int i = 0; i < newEventList.size(); i++) {
                        Event otherExistingEvent = newEventList.get(i);

                        if (event.getYear() < otherExistingEvent.getYear()) {
                            // Then need add it before event already in there
                            // (need replace the existing event with this one, want it to go in the spot where this event is)
                            newEventList.add(i, event);
                            break;
                        }
                        // Events sorted primarily by year but if the years are equal then sort by event type
                        // normalized to lower-case
                        else if (event.getYear().equals(otherExistingEvent.getYear())) {
                            // Want to sort the event type that comes first alphabetically to be sorted before the other one
                            if (event.getEventType().toLowerCase().compareTo(otherExistingEvent.getEventType().toLowerCase()) < 0) {
                                newEventList.add(i, event);
                                break;
                            }
                        }
                        // Special case for if on last event in the list
                        int numEvents = newEventList.size();
                        if (i == numEvents - 1) {
                            // If we're on the last event in the list then need add the event at the last spot in the list
                            newEventList.add(numEvents, event);
                        }
                    }
                }
                eventsForPeople.put(event.getPersonID(), newEventList);
            }
            // If don't already have an event list then create one + add the event currently at to it
            else {
                ArrayList<Event> newEventList = new ArrayList<>();
                newEventList.add(event);
                eventsForPeople.put(event.getPersonID(), newEventList);
            }
        }
    }

    // Where actually set the different colors for all of the different event types have
    // This makes a map from event types to colors!!! (So stores each event type have with a different color)
    // Maps each event type to different colors from the list of available google maps colors have!
    public void setMarkerColors() {
        int colorCounter = 0;

        // Loop through all of the different event types have
        for (String eventType : eventTypes) {
            // Mod 10 bc have 10 colors in our color map!!!
            int colorIndex = colorCounter % 10;
            eventTypeColors.put(eventType, availableColors.get(colorIndex));
            colorCounter++;
        }
    }

    // Clear all data have stored for when a user logs out on the app!
    public void clearStoredDataAndSettings() {
        // Clear all parameters have that store all the data!
        userPeople.clear();
        userEvents.clear();
        personByID.clear();
        eventByID.clear();
        eventsForPeople.clear();
        eventTypes.clear();
        eventTypeColors.clear();
        maleEvents.clear();
        femaleEvents.clear();

        // Also need reset all of the settings have! (Need undo any settings that the user had set before they logged out)
        // So just re-init all of the settings booleans to be true (bc want them all to be true when person logs in)
        showLifeStoryLines = true;
        showFamilyTreeLines = true;
        showSpouseLines = true;
        showFatherSide = true;
        showMotherSide = true;
        showMaleEvents = true;
        showFemaleEvents = true;
    }

    // Store all of the family members for an inputted person
    // For person activity (want list the person's father, mother, spouse and any children)
    public List<Person> getFamilyForPerson(Person thePerson) {
        List<Person> thePersonsFamily = new ArrayList<>();

        // Add their spouse, mom + dad if they have them
        String theFatherID = thePerson.getFatherID();
        if (theFatherID != null) {
            thePersonsFamily.add(getPersonByID(theFatherID));
        }

        String theMotherID = thePerson.getMotherID();
        if (theMotherID != null) {
            thePersonsFamily.add(getPersonByID(theMotherID));
        }

        String theSpouseID = thePerson.getSpouseID();
        if (theSpouseID != null) {
            thePersonsFamily.add(getPersonByID(theSpouseID));
        }

        // Now add their children if they have any...
        // Loop through all of the userPeople have (all of the people have for the user that just logged in / registered on the app)
        // and check if the person has a mother or father and if they do then check if their mom or dad is the person that inputted into here
        for (Person person: userPeople) {
            // Don't want to add a person as their own family member
            if (person.getPersonID() != thePerson.getPersonID()) {
                if (person.getFatherID() != null) {
                    if (person.getFatherID().equals(thePerson.getPersonID())) {
                        // Then the person getting family for is their dad
                        thePersonsFamily.add(person);
                    }
                }
                if (person.getMotherID() != null) {
                    if (person.getMotherID().equals(thePerson.getPersonID())) {
                        // Then the person getting the family for is their mom
                        thePersonsFamily.add(person);
                    }
                }
            }
        }

        // At this point have added the dad, mom, spouse and children for the person wanted to get the family for!
        return thePersonsFamily;
    }

    public Set<Event> filterBySettings() {
        Set<Event> allTotalEvents = getUserEvents();
        Set<Event> allEvents = new HashSet<>();
        allEvents.addAll(allTotalEvents);

        if (!isMaleEvents()) {
            allEvents.removeAll(getAllMaleEvents());
        }
        if (!isFemaleEvents()) {
            allEvents.removeAll(getAllFemaleEvents());
        }
        if (!isFatherSide()) {
            // Only want use the mothers side
            for (Person person : getUserPeople()) {
                if (getPaternalAncestors().contains(person)) {
                    ArrayList<Event> personsEvents = getAllEventsForSpecifiedPerson(person.getPersonID());
                    allEvents.removeAll(personsEvents);
                }
            }
        }
        if (!isMotherSide()) {
            for (Person person : getUserPeople()) {
                if (getMaternalAncestors().contains(person)) {
                    ArrayList<Event> personsEvents = getAllEventsForSpecifiedPerson(person.getPersonID());
                    allEvents.removeAll(personsEvents);
                }
            }
        }
        return allEvents;
    }

    // Return the person object that is paired with the given personID
    public Person getPersonByID(String personID) {
        return personByID.get(personID);
    }

    public Event getEventByID(String eventID) {
        return eventByID.get(eventID);
    }

    // Get the user that are working with
    public Person getUser() {
        return user;
    }

    // Use the getUser method to get the first name for that user
    public String getFirstName() {
        return getUser().getFirstName();
    }

    // Use the getUser method to get the last name for that user
    public String getLastName() {
        return getUser().getLastName();
    }

    // So that able to use the event marker colors in the map fragment!!!
    public Map<String, Float> getEventTypeColors() {
        return eventTypeColors;
    }

    public Set<Event> getUserEvents() {
        return userEvents;
    }
    public Set<Person> getUserPeople() {return userPeople;}

    public ArrayList<Event> getAllEventsForSpecifiedPerson(String personID) {
        return eventsForPeople.get(personID);
    }

    public Set<Event> getAllMaleEvents() {
        return maleEvents;
    }

    public Set<Event> getAllFemaleEvents() {
        return femaleEvents;
    }

    public Set<Person> getPaternalAncestors() {return paternalAncestors;}
    public Set<Person> getMaternalAncestors() {return maternalAncestors;}

    // Everything for settings now
    public boolean isLifeStoryLines() {
        return showLifeStoryLines;
    }
    public void setLifeStoryLines(boolean showLifeStoryLines) {
        this.showLifeStoryLines = showLifeStoryLines;
    }

    public boolean isFamilyTreeLines() {
        return showFamilyTreeLines;
    }
    public void setFamilyYreeLines(boolean showFamilyTreeLines) {
        this.showFamilyTreeLines = showFamilyTreeLines;
    }
    public boolean isShowSpouseLines() {
        return showSpouseLines;
    }
    public void setSpouseLines(boolean showSpouseLines) {
        this.showSpouseLines = showSpouseLines;
    }
    public boolean isFatherSide() {
        return showFatherSide;
    }
    public void setFatherSide(boolean showFatherSide) {
        this.showFatherSide = showFatherSide;
    }
    public boolean isMotherSide() {
        return showMotherSide;
    }
    public void setMotherSide(boolean showMotherSide) {
        this.showMotherSide = showMotherSide;
    }
    public boolean isMaleEvents() {
        return showMaleEvents;
    }
    public void setMaleEvents(boolean showMaleEvents) {
        this.showMaleEvents = showMaleEvents;
    }
    public boolean isFemaleEvents() {
        return showFemaleEvents;
    }
    public void setFemaleEvents(boolean showFemaleEvents) {
        this.showFemaleEvents = showFemaleEvents;
    }

    public boolean isHaveEventActivity() {
        return haveEventActivity;
    }

    public void setHaveEventActivity(boolean haveEventActivity) {
        this.haveEventActivity = haveEventActivity;
    }
}
