package com.example.familymap;

import java.io.*;
import java.net.*;
import com.google.gson.Gson;

import Result.*;
import Request.*;

// Creates HTTP requests, sends to server to call web APIs, get responses, etc.

public class ServerProxy {
    // Specifies the serverHost, serverPort that are going to run all of these on
    private String serverHost;
    private String serverPort;

    // Need to set the serverhost, serverport to be ones specified above! otherwise it won't know which ones to use
    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

    // Login request is a POST http method
    public LoginResponse login(LoginRequest request) {
        try {
            // Create a URL indicating where the server is running, and which web API operation we want to call
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/user/login");

            // Start constructing our HTTP request
            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            // Specify that we are sending an HTTP POST request
            http.setRequestMethod("POST");

            // Indicate that this request will contain an HTTP request body
            http.setDoOutput(true);    // There is a request body

            http.addRequestProperty("Accept", "application/json");

            // Connect to the server and send the HTTP request
            http.connect();

            // Get the output stream containing the HTTP request body
            Writer reqBody = new OutputStreamWriter(http.getOutputStream());

            Gson gson = new Gson();
            gson.toJson(request, reqBody);

            // Close the request body output stream, indicating that the request is complete
            reqBody.close();

            LoginResponse result;
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream resBody = http.getInputStream();
                String resData = readString(resBody);

                result = gson.fromJson(resData, LoginResponse.class);
            } else {
                // If enter here then have a FAILED error response so need getErrorStream here!!!
                InputStream resBody = http.getErrorStream();
                String resData = readString(resBody);
                result = gson.fromJson(resData, LoginResponse.class);
            }
            http.disconnect();
            return result;            // Return the success response
        } catch (IOException e) {
            // An exception was thrown, so display the exception's stack trace
            e.printStackTrace();
            return null;
        }
    }

    public RegisterResponse register(RegisterRequest request) {
        try {
            // Create a URL indicating where the server is running, and which web API operation we want to call
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/user/register");

            // Start constructing our HTTP request
            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            // Specify that we are sending an HTTP POST request
            http.setRequestMethod("POST");

            // Indicate that this request will contain an HTTP request body
            http.setDoOutput(true);    // There is a request body

            http.addRequestProperty("Accept", "application/json");

            // Connect to the server and send the HTTP request
            http.connect();

            // Get the output stream containing the HTTP request body
            Writer reqBody = new OutputStreamWriter(http.getOutputStream());

            Gson gson = new Gson();
            gson.toJson(request, reqBody);

            // Close the request body output stream, indicating that the request is complete
            reqBody.close();

            RegisterResponse result;
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream resBody = http.getInputStream();
                String resData = readString(resBody);

                result = gson.fromJson(resData, RegisterResponse.class);
            } else {
                // If enter here then have a FAILED error response so need getErrorStream here!!!
                InputStream resBody = http.getErrorStream();
                String resData = readString(resBody);
                result = gson.fromJson(resData, RegisterResponse.class);
            }
            http.disconnect();
            return result;            // Return the success response
        }
        catch (IOException e) {
            // An exception was thrown, so display the exception's stack trace
            e.printStackTrace();
            return null;
        }
    }

    // getPeople, getEvents are GET methods, NOT POST ones!!!
    // Have the authtoken for the user getting ppl/events for as the inputted parameter in both getPeople and getEvents
    // bc need that authtoken in order to actually get the people and events out of the DB/from server so just input it straight in
    // Also will need to call getPeople, getEvents on the login + register results to get their first, last names out with the dataCache class
    // And the authtoken is something that can use to access the user from BOTH the login + register results objects
    public AllPeopleResponse getPeople(String authtoken) {
        try {
            // Create a URL indicating where the server is running, and which web API operation we want to call
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/person");

            // Start constructing our HTTP request
            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            // Specify that we are sending an HTTP POST request
            http.setRequestMethod("GET");

            // Indicate that this request will not contain an HTTP request body
            http.setDoOutput(false);

            // Add an auth token to the request in the HTTP "Authorization" header
            http.addRequestProperty("Authorization", authtoken);

            // Specify that we would like to receive the server's response in JSON
            // format by putting an HTTP "Accept" header on the request (this is not
            // necessary because our server only returns JSON responses, but it
            // provides one more example of how to add a header to an HTTP request).
            http.addRequestProperty("Accept", "application/json");

            // Connect to the server and send the HTTP requesthttp.setDoOutput(false);
            http.connect();

            AllPeopleResponse result;
            Gson gson = new Gson();
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream resBody = http.getInputStream();
                String resData = readString(resBody);

                result = gson.fromJson(resData, AllPeopleResponse.class);
            } else {
                // If enter here then have a FAILED error response!
                InputStream resBody = http.getErrorStream();
                String resData = readString(resBody);
                result = gson.fromJson(resData, AllPeopleResponse.class);
            }
            http.disconnect();
            return result;            // Return the success response
        } catch (IOException e) {
            // An exception was thrown, so display the exception's stack trace
            e.printStackTrace();
            return null;
        }
    }

    public AllEventsResponse getEvents(String authtoken) {
        try {
            // Create a URL indicating where the server is running, and which web API operation we want to call
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/event");

            // Start constructing our HTTP request
            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            // Specify that we are sending an HTTP POST request
            http.setRequestMethod("GET");

            // Indicate that this request will not contain an HTTP request body
            http.setDoOutput(false);

            // Add an auth token to the request in the HTTP "Authorization" header
            http.addRequestProperty("Authorization", authtoken);

            // Specify that we would like to receive the server's response in JSON
            // format by putting an HTTP "Accept" header on the request (this is not
            // necessary because our server only returns JSON responses, but it
            // provides one more example of how to add a header to an HTTP request).
            http.addRequestProperty("Accept", "application/json");

            // Connect to the server and send the HTTP request
            http.connect();

            AllEventsResponse result;
            Gson gson = new Gson();
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream resBody = http.getInputStream();
                String resData = readString(resBody);

                result = gson.fromJson(resData, AllEventsResponse.class);
            } else {
                // If enter here then have a FAILED error response!
                InputStream resBody = http.getErrorStream();
                String resData = readString(resBody);
                result = gson.fromJson(resData, AllEventsResponse.class);
            }
            http.disconnect();
            return result;            // Return the success response
        } catch (IOException e) {
            // An exception was thrown, so display the exception's stack trace
            e.printStackTrace();
            return null;
        }
    }

    // From given Client class - nice!
    private static String readString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader sr = new InputStreamReader(is);
        char[] buf = new char[1024];
        int len;
        while ((len = sr.read(buf)) > 0) {
            sb.append(buf, 0, len);
        }
        return sb.toString();
    }
}

