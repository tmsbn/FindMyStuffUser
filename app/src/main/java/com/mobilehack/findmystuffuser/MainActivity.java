package com.mobilehack.findmystuffuser;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {


    String sendIP = "http://10.73.242.154:8000";

    int port = 8000;

    TextView statusTextView;
    Button sendButton;
    Button serverButton;
    ImageView speakButton;
    String lastWord = "";
    ImageView foundImageView;

    AndroidWebServer androidWebServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder().build();
        AndroidNetworking.initialize(getApplicationContext(), okHttpClient);

        foundImageView = findViewById(R.id.foundImage);
        statusTextView = (TextView) findViewById(R.id.statusText);

        androidWebServer = new AndroidWebServer(this, port, new AndroidWebServer.ServerInterface() {

            @Override
            public void getResponse(String response) {

                statusTextView.setText(response);


            }
        });


        sendButton = (Button) findViewById(R.id.sendButton);

        serverButton = (Button) findViewById(R.id.serverButton);
        speakButton = findViewById(R.id.speakButton);

        statusTextView.setText(R.string.readyMessage_txt);

        if (!androidWebServer.isAlive()) {

            try {
                androidWebServer.start();
                serverButton.setText(getText(R.string.stop_server_txt));
                statusTextView.setText(getString(R.string.serverListenTxt) + getIpAccess() + port);

            } catch (IOException e) {
                e.printStackTrace();
                statusTextView.setText(e.getMessage());

            }
        }

        serverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!androidWebServer.isAlive()) {

                    try {
                        androidWebServer.start();
                        serverButton.setText(getText(R.string.stop_server_txt));
                        statusTextView.setText(getString(R.string.serverListenTxt) + getIpAccess() + port);

                    } catch (IOException e) {
                        e.printStackTrace();
                        statusTextView.setText(e.getMessage());
                        //textView.setText(R.string.serverNotRunning_txt);

                    }
                } else {
                    androidWebServer.stop();
                    statusTextView.setText(R.string.serverNotRunning_txt);
                    serverButton.setText(getText(R.string.start_server_txt));
                }

            }
        });


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AndroidNetworking.post(sendIP)
                        .addQueryParameter("message", lastWord)
                        .setTag("test")
                        .setPriority(Priority.MEDIUM)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // do anything with response
                                try {
                                    statusTextView.setText("got response:" + response.getString("message"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(ANError error) {
                                // handle error

                                statusTextView.setText(error.getErrorDetail());
                                error.printStackTrace();
                            }
                        });
            }


        });


        serverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!androidWebServer.isAlive()) {

                    try {
                        androidWebServer.start();
                        serverButton.setText(getText(R.string.stop_server_txt));
                        statusTextView.setText(getString(R.string.serverListenTxt) + getIpAccess() + port);

                    } catch (IOException e) {
                        e.printStackTrace();
                        statusTextView.setText(e.getMessage());
                        //textView.setText(R.string.serverNotRunning_txt);

                    }
                } else {
                    androidWebServer.stop();
                    statusTextView.setText(R.string.serverNotRunning_txt);
                    serverButton.setText(getText(R.string.start_server_txt));
                }

            }
        });

        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, 10);
                } else {
                    Toast.makeText(MainActivity.this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    protected void onStop() {

        //androidWebServer.stop();
        super.onStop();
    }



    /**
     * @param encodedString
     * @return bitmap (from given string)
     */
    public Bitmap stringToBitmap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }


    private String getIpAccess() {

        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        final String formatedIpAddress = String.format(Locale.ENGLISH, "%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        return "http://" + formatedIpAddress + ":";
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getSpeechInput() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //statusTextView.setText(result.get(0));
                    String sentence = result.get(0);
                    lastWord = sentence.substring(sentence.lastIndexOf(" ") + 1);
                    lastWord = "bottle";

                    AndroidNetworking.post(sendIP)
                            .addQueryParameter("message", lastWord)
                            .setTag("test")
                            .setPriority(Priority.MEDIUM)
                            .build()
                            .getAsJSONObject(new JSONObjectRequestListener() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    // do anything with response
                                    try {
                                        statusTextView.setText("got response:" + response.getString("message"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onError(ANError error) {
                                    // handle error

                                    statusTextView.setText(error.getErrorDetail());
                                    error.printStackTrace();
                                }
                            });


                }
                break;
        }
    }
}
