package com.example.chatbotproject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    BroadcastReceiver broadcastReceiver;
    SmsMessage[] messages;
    SmsManager sms;
    Handler handler;
    Bundle bundle;
    TextView textView;
    TextView textView2;
    TextView textView3;
    IntentFilter intentFilter;
    String messageBody;
    public static String phoneNumber;
    ArrayList<String> states;
    int currentState = 0;
    int stage2 = 0;
    String replyBack ="";
    String[][] differentMessages;
    //Boolean awkwardness = true;
    Boolean positive = false;
    private static final int PERMISSION_REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS},0); }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS},0); }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},0); }
        textView = findViewById(R.id.id_text);
        textView2 = findViewById(R.id.textView);
        textView3 = findViewById(R.id.textView3);
        handler = new Handler();
        intentFilter = new IntentFilter();
        states = new ArrayList<>();
        differentMessages = new String[5][4];
        sms = SmsManager.getDefault();
        states.add("Greeting State");
        states.add("Negotiating State");
        states.add("Response to Suggestions State");
        states.add("Final Offer State");
        states.add("Responding to Decision State");
        differentMessages[0][0] = "Hello";
        differentMessages[0][1] = "Hi";
        differentMessages[0][2]= "Hey";
        differentMessages[0][3] = "Greetings";
        differentMessages[1][0] = "3 years, $90 million with $42 million guaranteed.";
        differentMessages[1][1] = "3 years, $87 million with $45 million guaranteed.";
        differentMessages[1][2] = "3 years, $96 million with $30 million guaranteed.";
        differentMessages[1][3] = "3 years, $99 million with $37 million guaranteed.";
        differentMessages[2][0] = "Well that is more years on your contract! Our max is still 3 years";
        differentMessages[2][1] = "We're giving you a lot of guaranteed money already, don't you think?";
        differentMessages[2][2] = "Son, you're already getting a great amount of money here! More than anywhere else";
        differentMessages[2][3] = "We're giving you a good amount of years, a lot of money, and tons of guaranteed money! This is an offer you cannot refuse!";
        differentMessages[3][0] = "Well our final offer is the same as above. Take it or leave it.";
        differentMessages[3][1]= "We will give you more money over the years but the guaranteed money stays the same.";
        differentMessages[3][2]= "We wil not change the amount of money over the years but we will give you more guaranteed money";
        differentMessages[3][3] = "Alright, we will give you 4 years on your contract instead of 3 years. The amount of money per year remains the same.";
        differentMessages[4][0] = "Great! Looking forward to a long and healthy relationship!";
        differentMessages[4][1] = "Wonderful! We can't wait for you to tear it up in a Miami Heat uniform!";
        differentMessages[4][2] = "Alright then! You won't be getting a better offer than this from any other team. Act soon or we will move on";
        differentMessages[4][3]= "Well that is unfortunate! Good luck somewhere else!";

        if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED) {
                String permissions[] = {Manifest.permission.SEND_SMS};
                requestPermissions(permissions, PERMISSION_REQUEST_CODE);
            }
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
                    bundle = intent.getExtras();
                    //intentFilter = w IntentFilter("android.provider.Telephony.SMS_RECEIVED");
                    if(bundle != null){
                        try {
                            Object[] pdus = (Object[]) bundle.get("pdus");
                            messages = new SmsMessage[pdus.length];
                            for (int i = 0; i < pdus.length; i++) {
                                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                                messageBody = messages[i].getMessageBody();
                                phoneNumber = messages[i].getOriginatingAddress();
                            }
                            handler.postDelayed(sendSMS(messageBody), 4000);
                            if(currentState==5){
                                textView3.setText("Deal is complete");
                            }
                            else if(currentState==6){
                                textView3.setText("Deal is on Hold");
                            }
                            else if(currentState==7){
                                textView3.setText("Deal is no good");
                            }
                            else{
                                textView3.setText("Deal is in progress");
                            }
                            textView2.setText("Currently Talking to: "+phoneNumber.substring(0,2)+" ("+phoneNumber.substring(2,5)+") "+phoneNumber.substring(5,8)+"-"+phoneNumber.substring(8));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }

            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    public Runnable sendSMS(String message){
        final String getMessage = message;
        Runnable sendMessage = new Runnable() {
            @Override
            public void run() {
                if(currentState < 5) {
                    if (currentState == 0 && (getMessage.toLowerCase().contains("hello") || getMessage.toLowerCase().contains("hi") || getMessage.toLowerCase().contains("hey"))) {
                        textView.setText(states.get(currentState));
                        replyBack = differentMessages[currentState][(int) (Math.random() * 4)];
                        replyBack += "\nThis is Albert Sebastian, the Miami Heat General Manager.\nAre you ready to negotiate a contract with our organization?";
                        currentState++;

                    } else if (currentState == 1 && (getMessage.toLowerCase().contains("yes") || getMessage.toLowerCase().contains("ready") || getMessage.toLowerCase().contains("contract")) || getMessage.toLowerCase().contains("negotiate")) {
                        replyBack = "Great! Our offer to you is: " + differentMessages[currentState][(int) (Math.random() * 4)];
                        replyBack += "\nAny Suggestions?";
                        textView.setText(states.get(currentState));
                        currentState++;

                    } else if (currentState == 2) {
                        if (getMessage.toLowerCase().contains("more years")) {
                            replyBack = differentMessages[currentState][0];
                            stage2 = 0;
                            textView.setText(states.get(currentState));
                            currentState++;
                        } else if (getMessage.toLowerCase().contains("more money") || getMessage.toLowerCase().contains("increase in money")) {
                            replyBack = differentMessages[currentState][2];
                            stage2 = 2;
                            textView.setText(states.get(currentState));
                            currentState++;
                        } else if (getMessage.toLowerCase().contains("guaranteed")) {
                            replyBack = differentMessages[currentState][1];
                            stage2 = 1;
                            textView.setText(states.get(currentState));
                            currentState++;
                        }else if(getMessage.toLowerCase().contains("all") || getMessage.toLowerCase().contains("better") || getMessage.toLowerCase().contains("could be") || getMessage.toLowerCase().contains("more")) {
                            replyBack = differentMessages[currentState][3];
                            stage2 = 3;
                            textView.setText(states.get(currentState));
                            currentState++;
                        } else if (getMessage.toLowerCase().contains("no") || getMessage.toLowerCase().contains("accept") || getMessage.toLowerCase().contains("agree") || getMessage.toLowerCase().contains("good") || getMessage.toLowerCase().contains("great")) {
                            replyBack = "Are you sure that you want to accept this deal?";
                            textView.setText(states.get(currentState));
                            currentState++;
                        } else {
                            replyBack = "I am sorry but can u please explain.";
                            Toast.makeText(MainActivity.this, "Currently Confused", Toast.LENGTH_LONG).show();
                        }
                    } else if (currentState == 3){
                        if (getMessage.toLowerCase().contains("believe")|| getMessage.toLowerCase().contains("should") || getMessage.toLowerCase().contains("deserve") || getMessage.toLowerCase().contains("fair") || getMessage.toLowerCase().contains("offer") || getMessage.toLowerCase().contains("counteroffer"))
                        {
                            int x = (int) (Math.random() * 100);
                            if (x < 50) {
                                replyBack = differentMessages[currentState][0];
                            } else {
                                replyBack = "Alright then! This is our final proposition to you.\n" + differentMessages[currentState][(int) (Math.random() * 3) + 1];
                            }
                            textView.setText(states.get(currentState));
                            currentState++;
                        }
                        else if(getMessage.toLowerCase().contains("yes") || getMessage.toLowerCase().contains("sure")){
                            replyBack= "Are you positive that you want to sign with us?\nWe need the commitment from you!";
                            textView.setText(states.get(currentState));
                            currentState++;
                        }
                    } else if (currentState == 4) {
                        if (getMessage.toLowerCase().contains("not") || getMessage.toLowerCase().contains("decline") || getMessage.toLowerCase().contains("no") || getMessage.toLowerCase().contains("isn't going to work") || getMessage.contains("do not")) {
                            replyBack = differentMessages[currentState][3];
                            textView.setText(states.get(currentState));
                            currentState =7;
                            //positive = true;
                        } else if (getMessage.toLowerCase().contains("wait") || getMessage.toLowerCase().contains("hold") || getMessage.toLowerCase().contains("other")) {
                            replyBack = differentMessages[currentState][2];
                            textView.setText(states.get(currentState));
                            currentState = 6;
                        } else if (getMessage.toLowerCase().contains("accept") || getMessage.toLowerCase().contains("yes") || getMessage.toLowerCase().contains("deal") || getMessage.toLowerCase().contains("agree")) {
                            replyBack = differentMessages[currentState][(int)(Math.random()*2)];
                            textView.setText(states.get(currentState));
                            currentState = 5;
                        }
                        else {
                            replyBack = "Sorry. Can u be more clear?";
                            Toast.makeText(MainActivity.this, "Currently Confused", Toast.LENGTH_LONG).show();
                        }
                    } else{
                        replyBack = "Sorry. That was not the response we were looking for. Please clarify.";
                    }
                    Log.d("TAG", "Reply " + replyBack);
                    sms.sendTextMessage(phoneNumber.substring(8), null, replyBack, null, null);
                }
            }
        };
        return sendMessage;
    }
}
