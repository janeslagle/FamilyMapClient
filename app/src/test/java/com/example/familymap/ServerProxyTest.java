package com.example.familymap;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import Model.User;
import Result.*;
import Request.*;

public class ServerProxyTest {
    private ServerProxy serverProxy;

    // Have a parameter that stores a user that can login in / register for testing the login, etc.
    private final User ME = new User("jeslagle", "myPassword", "jane@email.com", "Jane",
            "Slagle", "f", "ME_YO");
    private final User Dolores = new User("dolores", "doloresPassword", "email",
            "Dolores", "Smith", "f", "dolores_YO");
    private final User JustinBieber = new User("bieber", "justinPass", "justinEmail",
            "Justin", "Bieber", "m", "Justin_YO");
    private final User Sam = new User("Sam", "password", "email", "Sam", "Sammy",
            "m", "sam_YO");

    private final User Rebecah = new User("Rebecah", "password", "email", "Rebecah", "K",
            "f", "rebecah_YO");
    private final RegisterRequest registerME = new RegisterRequest(ME.getUsername(), ME.getPassword(),
            ME.getEmail(), ME.getFirstName(), ME.getLastName(), ME.getGender());

    private final RegisterRequest registerDolores = new RegisterRequest(Dolores.getUsername(), Dolores.getPassword(),
            Dolores.getEmail(), Dolores.getFirstName(), Dolores.getLastName(), Dolores.getGender());

    private final RegisterRequest registerJustin = new RegisterRequest(JustinBieber.getUsername(), JustinBieber.getPassword(),
            JustinBieber.getEmail(), JustinBieber.getFirstName(), JustinBieber.getLastName(), JustinBieber.getGender());

    private final RegisterRequest registerSam = new RegisterRequest(Sam.getUsername(), Sam.getPassword(), Sam.getEmail(),
            Sam.getFirstName(), Sam.getLastName(), Sam.getGender());

    private final RegisterRequest registerRebecah = new RegisterRequest(Rebecah.getUsername(), Rebecah.getPassword(), Rebecah.getEmail(),
            Rebecah.getFirstName(), Rebecah.getLastName(), Rebecah.getGender());

    // Want connect to the server proxy each time go through a test so set up the server proxy here
    @Before
    public void setUp() {
        serverProxy = new ServerProxy();
        serverProxy.setServerHost("localhost");
        serverProxy.setServerPort("8080");
    }

    // Want make sure that the login worked and was successful when give it a user that is registered already
    @Test
    public void loginPass() {
        // Have register me before can log in
        RegisterResponse meRegistered = serverProxy.register(registerME);
        LoginRequest requestME = new LoginRequest(ME.getUsername(), ME.getPassword());
        LoginResponse meLoggedIn = serverProxy.login(requestME);

        // When a person logs in they will have a success boolean that should be true
        assertTrue(meLoggedIn.getSuccess());

        // The user should also be associated with an authtoken + person when log them in so get their personID, authtokenID out
        assertNotNull(meLoggedIn.getAuthToken());
        assertNotNull(meLoggedIn.getPersonID());

        // Also when a user logs in they will have a username, that username should be same username that was in the request when logged them in
        // And it should be the same username for their register request, response as well that had in here
        assertNotNull(meLoggedIn.getUsername());
        assertEquals(meLoggedIn.getUsername(), meRegistered.getUsername());
        assertEquals(meLoggedIn.getUsername(), requestME.getUsername());
    }

    // Want make sure that the login fails when it is supposed to
    @Test
    public void loginFail() {
        // Know the test will fail if try to log in with username or password that is not a registered user already
        LoginRequest wrongRequest = new LoginRequest("NOTjeslagle", "myPassword");        // Try login with wrong username, correct password
        LoginRequest anotherWrongRequest = new LoginRequest("jeslagle", "NOTmyPassword"); // Try login with corret username, wrong password

        // Now try log them in and should get fail response
        LoginResponse wrongResponseOne = serverProxy.login(wrongRequest);
        LoginResponse anotherWrongResponse = serverProxy.login(anotherWrongRequest);

        // Make sure the responses are failed success booleans
        assertFalse(wrongResponseOne.getSuccess());
        assertFalse(anotherWrongResponse.getSuccess());

        // Make sure the response has a null authtoken, personID, username or password
        assertNull(wrongResponseOne.getAuthToken());
        assertNull(wrongResponseOne.getPersonID());
        assertNull(wrongResponseOne.getUsername());

        assertNull(anotherWrongResponse.getAuthToken());
        assertNull(anotherWrongResponse.getPersonID());
        assertNull(anotherWrongResponse.getUsername());
    }

    // Want make sure when try to register a new user that it actually registers them
    @Test
    public void registerPass() {
        // Get register response for the register request made above of the different user!
        RegisterResponse doloresResponse = serverProxy.register(registerDolores);

        // Make sure the registered person has the correct authtoken, username, personID and success
        assertTrue(doloresResponse.getSuccess());

        assertNotNull(doloresResponse.getAuthtoken());
        assertNotNull(doloresResponse.getPersonID());
        assertNotNull(doloresResponse.getUsername());

        assertEquals(doloresResponse.getUsername(), Dolores.getUsername());
        assertEquals(registerDolores.getUsername(), doloresResponse.getUsername());
    }

    // Want make sure when try register user already in database that it fails!
    @Test
    public void registerFail() {
        // Try to register the same person twice should cause it to fail!
        RegisterResponse justinResponse = serverProxy.register(registerJustin);

        // Make sure registering Justin for first time once works
        assertTrue(justinResponse.getSuccess());
        assertNotNull(justinResponse.getAuthtoken());
        assertNotNull(justinResponse.getPersonID());
        assertNotNull(justinResponse.getUsername());
        assertEquals(justinResponse.getUsername(), JustinBieber.getUsername());
        assertEquals(registerJustin.getUsername(), justinResponse.getUsername());

        // Now make sure it fails when try to register Justin again for a second time
        RegisterResponse registerJustinAgain = serverProxy.register(registerJustin);

        // Make sure it fails!
        assertFalse(registerJustinAgain.getSuccess());
        assertNull(registerJustinAgain.getAuthtoken());
        assertNull(registerJustinAgain.getPersonID());
        assertNull(registerJustinAgain.getUsername());
    }

    @Test
    public void getPeoplePass() {
        // Register someone
        RegisterResponse samResponse = serverProxy.register(registerSam);
        AllPeopleRequest request = new AllPeopleRequest(samResponse.getAuthtoken());
        AllPeopleResponse response = serverProxy.getPeople(request.getAuthToken());

        // Check that data is not null and that get true success bool
        assertTrue(response.getSuccess());
        assertNotNull(response.getEventData());
    }

    @Test
    public void getPeopleFail() {
        // Will fail if input an invalid authtoken
        AllPeopleResponse failResponse = serverProxy.getPeople("BlahBlahBlahBlahBlah");

        // Make sure it fails (data should be null and success response should be false)
        assertFalse(failResponse.getSuccess());
        assertNull(failResponse.getEventData());
    }

    @Test
    public void getEventsPass() {
        // Register someone
        RegisterResponse rebecahResponse = serverProxy.register(registerRebecah);
        AllPeopleRequest rebecahRequest = new AllPeopleRequest(rebecahResponse.getAuthtoken());
        AllPeopleResponse rebecahEvents = serverProxy.getPeople(rebecahRequest.getAuthToken());

        // Check that data is not null and that get true success bool
        assertTrue(rebecahResponse.getSuccess());
        assertNotNull(rebecahEvents.getEventData());
    }

    @Test
    public void getEventsFail() {
        // Will fail if input an invalid authtoken
        AllEventsResponse failEventResponse = serverProxy.getEvents("thisDoesntExist");

        // Make sure it fails (data should be null and success response should be false)
        assertFalse(failEventResponse.getSuccess());
        assertNull(failEventResponse.getEventData());
    }


}
