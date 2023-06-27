package BackgroundTasks;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.familymap.ServerProxy;
import com.example.familymap.DataCache;
import Result.*;
import Request.LoginRequest;

// This is the background thread task
// In this bground task class: need a constructor, a run method and a sendMessage method!
public class LoginBgroundTask implements Runnable {
    private final DataCache dataCacheInstance;  // Need to be able to access the dataCache class
    private final Handler messageHandler;       // Need send messages to Handler w/ info to be updated in this class
    private LoginRequest loginRequest;          // Need pass in the login request when do this task
    private final ServerProxy server;           // Need be able to connect to server so that can get the result from the login request in this class
    private final String serverHost;            // Need be able to connect to the server
    private final String serverPort;

    // Need be able to send the successfulness + first and last names back to the login screen fragment from this class
    private final static String SUCCESS_KEY = "success";
    private final static String FIRST_NAME_KEY = "firstName";
    private final static String LAST_NAME_KEY = "lastName";

    private String firstName;                   // Need find the first, last names in this class so that can return them in android toast!
    private String lastName;

    // Need pass handler created in login fragment into here bc receiving that handler will allow it to send message to handler
    public LoginBgroundTask(Handler messageHandler, LoginRequest loginRequest, String serverHost, String serverPort) {
        dataCacheInstance = DataCache.getInstance();   // Need create an instance of the dataCache class so that can access what is inside of there in this task!

        this.messageHandler = messageHandler;
        this.loginRequest = loginRequest;

        // Create an instance of the server proxy class so that can specify it's server host + port
        server = new ServerProxy();
        server.setServerHost(serverHost);
        server.setServerPort(serverPort);
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    // This is code that will run in bground thread so this is where we will get the info for the android toast, etc.
    // At end of run method, call sendMessage on handler that was passed into constructor (how communicate back to UI thread)
    @Override
    public void run() {
        // From the server, get the result from doing the login request
        LoginResponse result = server.login(loginRequest);

        // If have successful response from server request then grab the 1st, last names for the user logging
        // Grab them using the dataCache class have
        if (result.getSuccess()) {
            // Need get the first, last names of the user from the login result object using data cache class
            // So will use setData method from DataCache which is where set all of the data for the user that just logged in / registered so that are able to
            // work with this user in the app!
            // To call that func, need the people + event data for the user (need the data that are actually setting for the user) so get that from server via serverProxy
            AllPeopleResponse resultPersonsData = server.getPeople(result.getAuthToken());
            AllEventsResponse resultEventsData = server.getEvents(result.getAuthToken());

            // Now that actually have the data are setting for the user, set it for the user via data cache class!
            dataCacheInstance.setData(result.getPersonID(), resultPersonsData, resultEventsData);

            // Now grab the 1st, last names out of the data cache class
            firstName = dataCacheInstance.getFirstName();
            lastName = dataCacheInstance.getLastName();
        }// Actually send the data to the UI thread now!
        // Need to send the result from the run method to sendMessage!
        sendMessage(result);
    }

    // Send message from background thread to the UI thread
    // Remember: call sendMessage NOT handleMessage
    private void sendMessage(LoginResponse result) {
        Message message = Message.obtain();
        Bundle messageBundle = new Bundle();
        Boolean resultSuccess = result.getSuccess();

        // Need to send the first, last name data to the UI thread only if the result was a successful one
        if (resultSuccess) {
            messageBundle.putString(FIRST_NAME_KEY, firstName);
            messageBundle.putString(LAST_NAME_KEY, lastName);
        }

        // No matter if failed or succeeded in request for task, need send if succeeded or not to UI thread
        messageBundle.putBoolean(SUCCESS_KEY, resultSuccess);

        message.setData(messageBundle);
        messageHandler.sendMessage(message);  // Actually send the data to the UI thread now!
    }
}
