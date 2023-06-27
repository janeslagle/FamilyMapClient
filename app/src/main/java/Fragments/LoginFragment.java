package Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Button;
import Request.LoginRequest;
import Request.RegisterRequest;
import BackgroundTasks.LoginBgroundTask;
import BackgroundTasks.RegisterBgroundTask;

import com.example.familymap.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginFragment extends Fragment {
    // Define all variables need for determining if login/register requests fail + for displaying Android toast part
    private final static String SUCCESS_KEY = "success";   // Stores if the login/register requests were successful or not
    private final static String FIRST_NAME_KEY = "firstName";
    private final static String LAST_NAME_KEY = "lastName";

    // Define all editable text fields, all radio buttons (for genders), login/register button variables will need on the login screen
    private EditText serverHost;
    private EditText serverPort;
    private EditText username;
    private EditText password;
    private EditText firstName;
    private EditText lastName;
    private EditText email;
    private RadioButton maleButton;
    private RadioButton femaleButton;
    private Button signInButton;
    private Button registerButton;

    // Store pointer to its current listener (this is the main activity)
    private Listener listener;

    // Will need for setting all of the listener clickers parts
    private View view;

    public interface Listener {
        // Will notify main method when either register or login buttons are clicked so that can move on to map fragment once done
        void notifyDone();
    }

    public void registerListener(Listener listener) {
        this.listener = listener;
    }

    public LoginFragment() {
        // Required empty public constructor
    }

   @Override
   public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
   }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Step (1) Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_login, container, false);

        // Step (2) Get all of the views for each parameter all at once to have ready to use later on
        serverHost = view.findViewById(R.id.serverHostID);
        serverPort = view.findViewById(R.id.serverPortID);
        username = view.findViewById(R.id.usernameID);
        password = view.findViewById(R.id.passwordID);
        firstName = view.findViewById(R.id.firstNameID);
        lastName = view.findViewById(R.id.lastNameID);
        email = view.findViewById(R.id.emailID);
        maleButton = view.findViewById(R.id.maleID);
        femaleButton = view.findViewById(R.id.femaleID);
        signInButton = view.findViewById(R.id.signInID);
        registerButton = view.findViewById(R.id.registerID);

        // Step (3) Now need cover everything in the login/register fields that come up on the login screen for the user
        // Start out by listening for when user inputs text into those editable fields
        // Go through + listen for them in order they appear on the screen for the user (in order the user will input them as)
        // Within this step: need go through all of the TextWatcher stuff (the before, on + after text changed stuff)

        // Note: will take care of listening for the gender when are doing the setting for the register button click bc that is only time will need the gender
        // (don't need gender for login)
        addTextChangedListenerForTextFields(serverHost);
        addTextChangedListenerForTextFields(serverPort);
        addTextChangedListenerForTextFields(username);
        addTextChangedListenerForTextFields(password);
        addTextChangedListenerForTextFields(firstName);
        addTextChangedListenerForTextFields(lastName);
        addTextChangedListenerForTextFields(email);

        // Step (4) This is the part where do the SetOnClickListener for login, register buttons where w/in that need
        // actually make the request that want make when click the button, do the task, etc.
        // Do separately for login + register
        signInButton.setOnClickListener(v -> {
            LoginRequest request = new LoginRequest(
                    username.getText().toString(), password.getText().toString());

            // Now that have made the request, need to start the login bground thread stuff
            // 1st step here = creating android.os.Handler class and using handleMessage method
            @SuppressLint("HandlerLeak") Handler uiThreadMessageHandler = new Handler() {
                @Override
                public void handleMessage(Message message) {
                    Bundle bundle = message.getData();
                    Boolean loginSuccessful = bundle.getBoolean(SUCCESS_KEY); // login bground task updates SUCCESS_KEY parameter using result get from request

                    // Now check if login request was successful or not (this is checked in bground task class in its run/send message methods)
                    // If was successful then want display Android toast w/ logged-in user's 1st, last names
                    if (loginSuccessful) {
                        Toast.makeText(view.getContext(),
                                bundle.getString(FIRST_NAME_KEY) + " " + bundle.getString(LAST_NAME_KEY),
                                Toast.LENGTH_LONG).show();

                        // AND need call notifyDone on the listener here bc have successfully pressed sign-in button so need move on to map fragment screen
                        // (which is what happens when call notifyDone func!!!)
                        listener.notifyDone();
                    }
                    // This is case when sign-in button fails
                    else {
                        Toast.makeText(view.getContext(), "Sign-in failed!!!", Toast.LENGTH_LONG).show();
                    }
                }
            };

            // 2nd step in login bground thread stuff = creating Runnable class for login bground task SO call the login bground task wrote!
            // Want pass the handler to this bground runnable task class
            LoginBgroundTask loginTask = new LoginBgroundTask(uiThreadMessageHandler, request, serverHost.getText().toString(),
                    serverPort.getText().toString());

            // 3rd + final step in login bground thread stuff = creating ExecutorService, submitting Runnable class to it
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(loginTask);
        });

        // Exact same as login button but replace all instances of login w/ register
        registerButton.setOnClickListener(v -> {

            // Before can make the register request, need figure out which gender button the user has pressed
            String gender = "will either be m or f";
            if (maleButton.isChecked()) {
                gender = "m";
            }
            else if (femaleButton.isChecked()) {
                gender = "f";
            }

            RegisterRequest request = new RegisterRequest(
                    username.getText().toString(),
                    password.getText().toString(),
                    email.getText().toString(),
                    firstName.getText().toString(),
                    lastName.getText().toString(),
                    gender);

            @SuppressLint("HandlerLeak") Handler uiThreadMessageHandler = new Handler() {
                @Override
                public void handleMessage(Message message) {
                    Bundle bundle = message.getData();
                    Boolean registerSuccessful = bundle.getBoolean(SUCCESS_KEY);

                    if (registerSuccessful) {
                        Toast.makeText(view.getContext(),
                                bundle.getString(FIRST_NAME_KEY) + " " + bundle.getString(LAST_NAME_KEY),
                                Toast.LENGTH_LONG).show();
                        listener.notifyDone();
                    } else {
                        Toast.makeText(view.getContext(), "Register Failed!!!", Toast.LENGTH_LONG).show();
                    }
                }
            };

            RegisterBgroundTask registerTask = new RegisterBgroundTask(uiThreadMessageHandler, request, serverHost.getText().toString(),
                    serverPort.getText().toString());

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(registerTask);
        });

        return view;   // DONE - hallelujah!
    }

    private void addTextChangedListenerForTextFields(EditText inputtedText) {
        inputtedText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (inputtedText == serverHost || inputtedText == serverPort || inputtedText == username || inputtedText == password) {
                    // Need be able to log in when only the serverHost, serverPort, username + password fields are inputted so cover that first
                    // Want enable the sign-button when all of these fields have anything written in them each (this is the bare min for the button to be enabled)
                    // bc should also work when ALL of the fields on login screen are inputted
                    signInButton.setEnabled(serverHost.getText().toString().length() > 0 &&
                            serverPort.getText().toString().length() > 0 &&
                            username.getText().toString().length() > 0 &&
                            password.getText().toString().length() > 0);

                    // Need to have this part here too or it will enable the register button when only the sign in fields are put in
                    registerButton.setEnabled(serverHost.getText().toString().length() > 0 &&
                            serverPort.getText().toString().length() > 0 &&
                            username.getText().toString().length() > 0 &&
                            password.getText().toString().length() > 0 &&
                            email.getText().toString().length() > 0 &&
                            firstName.getText().toString().length() > 0 &&
                            lastName.getText().toString().length() > 0);
                }
                else if (inputtedText == firstName || inputtedText == lastName || inputtedText == email){
                    // Then also need to fill in the fields needed for the register button
                    // For registering, need ALL fields input (except gender bc handle that in the register setonclick part)
                    registerButton.setEnabled(serverHost.getText().toString().length() > 0 &&
                            serverPort.getText().toString().length() > 0 &&
                            username.getText().toString().length() > 0 &&
                            password.getText().toString().length() > 0 &&
                            email.getText().toString().length() > 0 &&
                            firstName.getText().toString().length() > 0 &&
                            lastName.getText().toString().length() > 0);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}