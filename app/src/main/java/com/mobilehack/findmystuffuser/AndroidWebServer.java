package com.mobilehack.findmystuffuser;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class AndroidWebServer extends NanoHTTPD {

    AppCompatActivity context;
    ServerInterface serverInterface;

    public AndroidWebServer(AppCompatActivity context, int port, ServerInterface serverInterface) {
        super(port);
        this.context = context;
        this.serverInterface = serverInterface;
    }

    public AndroidWebServer(String hostname, int port) {
        super(hostname, port);
    }

    @Override
    public Response serve(IHTTPSession session) {

        //Log.d("hello", "world");


        final Map<String, List<String>> parms = session.getParameters();
        String msg = "";


        if (parms.get("image") != null) {

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {

                    context.runOnUiThread(new Runnable() {
                        public void run() {

                            String nearObjects = parms.get("image").get(0);
                            String msg = "Object found near " + nearObjects;
                            serverInterface.getResponse(msg);
                        }
                    });

                }
            });


            msg += "got message'";
        } else {
            msg += "Didn't get message";
        }

        JSONObject jsonObj = new JSONObject();

        try {
            jsonObj.put("message", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }



        return newFixedLengthResponse(Response.Status.OK, "text/json", jsonObj.toString());
    }


    public interface ServerInterface {


        public void getResponse(String response);
    }


}