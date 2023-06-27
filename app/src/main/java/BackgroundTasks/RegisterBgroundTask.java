package BackgroundTasks;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.familymap.DataCache;
import com.example.familymap.ServerProxy;

import Request.RegisterRequest;
import Result.AllEventsResponse;
import Result.AllPeopleResponse;
import Result.RegisterResponse;

// This is the bground task for the register request
public class RegisterBgroundTask implements Runnable{
    private final DataCache dataCacheInstance;  // Need to be able to access the dataCache class
    private final Handler messageHandler;
    private RegisterRequest registerRequest;
    private final ServerProxy server;
    private final String serverHost;
    private final String serverPort;
    private final static String SUCCESS_KEY = "success";
    private final static String FIRST_NAME_KEY = "firstName";
    private final static String LAST_NAME_KEY = "lastName";

    private String firstName;
    private String lastName;

    // Create a constructor for the task class (want pass in handler that created before into here)
    // Recieving a handler in the constructor will allow it to send message to handler that should cause
    // something to happen back on the UI thread
    public RegisterBgroundTask(Handler messageHandler, RegisterRequest registerRequest, String serverHost, String serverPort) {
        dataCacheInstance = DataCache.getInstance();   // Need create an instance of the dataCache class so that can access what is inside of there in this task!

        this.messageHandler = messageHandler;
        this.registerRequest = registerRequest;

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
        RegisterResponse result = server.register(registerRequest);

        // If have successful response from server request then need grab 1st, last names from user registered via DataCache + serverproxy classes
        if (result.getSuccess()) {
            // Get the people + events data for the user registering from the ServerProxy
            AllPeopleResponse resultPersonsData = server.getPeople(result.getAuthtoken());
            AllEventsResponse resultEventsData = server.getEvents(result.getAuthtoken());

            // Now that actually have the data are setting for the user, set it for the user via data cache class!
            dataCacheInstance.setData(result.getPersonID(), resultPersonsData, resultEventsData);

            // Now grab the 1st, last names out of the data cache class
            firstName = dataCacheInstance.getFirstName();
            lastName = dataCacheInstance.getLastName();
        }
        sendMessage(result);
    }

    // Send message from background thread to the UI thread
    private void sendMessage(RegisterResponse result) {
        Message message = Message.obtain();
        Bundle messageBundle = new Bundle();
        Boolean resultSuccess = result.getSuccess();

        // Need to send the first, last name data to the UI thread only if the result was a successful one
        if (resultSuccess) {
            messageBundle.putString(FIRST_NAME_KEY, firstName);
            messageBundle.putString(LAST_NAME_KEY, lastName);
        }

        // No matter if failed or succeeded in request for task, need send if succeeded or not to UI thread
        messageBundle.putBoolean(SUCCESS_KEY, result.getSuccess());

        message.setData(messageBundle);
        messageHandler.sendMessage(message);   // Actually send the data to the UI thread now!
    }
}
