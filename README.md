# RICOH THETA Plug-in Messaging App - Sending Love with THETA

_This article is translated by @jcasman from the original [here](https://qiita.com/ueue/items/ecb5032c923531a45408) (JAPANESE)._

## Introduction

Hello, this is [@ueue](https://qiita.com/ueue) from RICOH. I’m a middle aged man who doesn’t recover from  summertime fatigue till, oh, the end of the year. Do you tell your loved ones that you love them? If you are like me, sometimes it’s hard to say “I love you” directly to loved ones. So, I’ve made a plug-in to send a love note via the messaging service LINE using RICOH THETA. 

For this plug-in, set the THETA to wireless client mode and keep it connected to a network, then utilize the LINE messaging API to send out messages. 

This is the basic idea. 

![Sending%20I%20Love%20You%20Message%20through%20LINE|690x303](https://community.theta360.guide/uploads/default/original/2X/f/fee45749893bcc446d753067f5de059dd909202d.png)

For people wondering “What are plug-ins?” here is a quick explanation. The newest model (as of August 6, 2018), the RICOH THETA V, uses an Android-based OS, so functionality can be expanded by installing apps. In the world of THETA, apps are called plug-ins. 

If you are interested in developing plug-ins, [please register for the Partner Program](https://api.ricoh/products/theta-plugin/).


A key point of this article, for development purposes, is connecting a THETA to a desktop computer using USB and Wi-Fi simultaneously. THETA is set not to be connected to Wi-Fi when connected to USB. However, this makes it hard to develop plug-ins that access the network. So, I’m going to introduce a trick that can only be used in developer mode that allows a THETA to be connected to both USB and Wi-Fi. 

Here is what the development looks like when using this trick. 

![development%20environment|671x382](https://community.theta360.guide/uploads/default/original/2X/c/c6294a5d001e48e4cd84563eb2874f92c7afa043.png)

## Preparing the LINE Messaging API

Let’s prepare to send a message to LINE. From the [LINE Developer console](https://developers.line.me/en/), create a provider and channel and issue an access token (long term). For this procedure, I’ve referenced [this article](https://qiita.com/nkjm/items/38808bbc97d6927837cd) (JAPANESE). 

When you are testing for yourself, write down the User ID, which is in the Developer console’s channel main settings. When you send a message to this User ID, the message is send to you. 

When you want to send a message to your loved one, set up Webhook, use the QR code in the same channel main settings, guide the loved one to register to the channel (app) as a friend. As soon as your loved one is added as a friend, the loved one’s User ID will be sent to Webhook, so please write down the ID.

The way to send message from the created app is in [the Messaging API Reference called Sending Push Messages](https://developers.line.me/en/reference/messaging-api/#send-push-message).

Below is how you can send message using the curl command.

    curl -v -X POST https://api.line.me/v2/bot/message/push \
    -H 'Content-Type:application/json' \
    -H 'Authorization: Bearer <Access Token>' \
    -d '{
        "to": "<User ID>",
        "messages":[
            {
                "type":"text",
                "text":"I love you"
            },
            {
                "type":"text",
                "text":"I love you so much"
            },
            {
              "type": "sticker",
              "packageId": "1",
              "stickerId": "409"
            }
        ]
    }'


Input the access token issued above in `<access token>`, and input the User ID you wrote down in <User ID>. To test it first, input your User ID. 

For Message Objects, please see [this link](https://developers.line.me/en/reference/messaging-api/#message-objects). 

To send a stamp, input “sticker” for the type value, and input sticker ID and package ID for the stamp you want to send. For correspondence between stamps and package ID and sticker ID, please see [this list](https://developers.line.me/en/reference/messaging-api/#message-objects).

You’ve succeeded if you inputted the above curl command from a terminal and received a message on LINE. 

## Connecting the THETA to a Network

It is possible to connect the THETA to a network, by switching THETA to wireless client mode. Please see “[Connecting to a Smartphone via Wireless LAN](https://theta360.com/en/support/manual/v/content/prepare/prepare_06.html)” for the procedure.

## How to Activate THETA’s Wi-Fi While Connected to the USB

For development, connect the THETA to a desktop computer via USB. The Wi-Fi indicator will turn off when the THETA is connected. 

![No%20Wi-Fi|200x267](https://community.theta360.guide/uploads/default/original/2X/e/e72bd8f48909b4093f01d2af30a0a32e0b97c640.jpg)
_No Wi-Fi - Compare to next image below_

THETA is set so that Wi-Fi is unavailable while connected to USB. But this will make development more difficult for network connected plug-ins. However, in development mode, Wi-Fi becomes available while connected to USB using the command below. 

`adb  shell settings put global usb_debug true`

Disconnect USB from the THETA once, wait for Wi-Fi to become available, then connect the THETA to USB again. If the Wi-Fi indicator, which was off before, stays lit, then you have succeeded. 

![Wi-Fi%20indicator%20on|200x267](https://community.theta360.guide/uploads/default/original/2X/9/985e972048f319039abc11edfb8ee2826a3d2552.png)
_Wi-Fi indicator on_

In this state, since USB and Wi-Fi connections are both available, it becomes possible to install an apk using Android Studio, and also confirm the network connection. 

## Explanation of the Plug-in Code

Below is the plug-in source code. 

    package com.theta360.pluginapplication;

    import android.os.AsyncTask;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.KeyEvent;

    import com.theta360.pluginlibrary.activity.PluginActivity;
    import com.theta360.pluginlibrary.callback.KeyCallback;

    import java.io.IOException;
    import java.io.OutputStream;
    import java.net.HttpURLConnection;
    import java.net.URL;

    public class MainActivity extends PluginActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setKeyCallback(new KeyCallback() {

                @Override
                public void onKeyDown(int keyCode, KeyEvent event) {
                    new SendMessageTask().execute();
                }

                public void onKeyUp(int keyCode, KeyEvent event) {
                }

                public void onKeyLongPress(int keyCode, KeyEvent event) {
                }
            });
        }

        private static class SendMessageTask extends AsyncTask<Void, Void, Void> {

            @Override
            public Void doInBackground(Void... params) {
                final String token = "<Access Token>";
                final String user_id = "<User ID>";

                final String data = "{"
                        + "\"to\":\""
                        + user_id
                        + "\","
                        + "\"messages\":["
                        + "{\"type\":\"text\",\"text\":\"I love you\"},"
                        + "{\"type\":\"text\",\"text\":\"I love you so much\"},"
                        + "{\"type\":\"sticker\",\"packageId\":\"1\",\"stickerId\":\"4\"}"
                        + "]}";
                try {
                    final String url = "https://api2.line.me/v2/bot/message/push";
                    HttpURLConnection connection = createConnection(url, token);
                    sendData(data, connection);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            private HttpURLConnection createConnection(String url, String token) throws IOException {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                // Settings for connecting
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + token);
                connection.setRequestProperty("Accept", "application/json");

                // Allows settings for return value from API and data to be sent
                connection.setDoOutput(true);

                return connection;
            }

            private void sendData(String data, HttpURLConnection connection) throws IOException {
                try (OutputStream out = connection.getOutputStream()) {
                    // Connecting
                    connection.connect();

                    // Write data to be sent
                    out.write(data.getBytes("UTF-8"));
                    out.flush();
                    connection.getResponseCode();
                } finally {
                    connection.disconnect();
                }
            }
        }
    }

Here are some things to be careful of. 

There was an error message when the official Messaging API domain was set to api.line.me in the API’s URL specified part, so it was switched to api2.line.me which was registered in CNAME.

`final String url = "https://api2.line.me/v2/bot/message/push";`

I have re-written the access token’s value and User ID in this sample code. 

    final String token = "<Access Token";
    final String user_id = "<User ID>";

For the parameters for the access token and User ID, I recommend setting up using the [5 steps shown here](https://theta360.com/en/support/manual/v/content/plugin/plugin_02.html).  (_Trans - This is the English version of the link included in the original article, but it is unclear if this is related information._)

From the plug-in side, the Web server will start inside the plug-in [as described here](https://api.ricoh/docs/theta-plugin/how-to-use/#using-a-web-server). For how to implement, I am hoping someone else in [the RICOH THETA plug-in community](https://community.theta360.guide/c/theta-api-usage/plugin) will explain further.

For handling exceptions, I’m not including that here, so please implement it on your own. 

## Experimenting with Running the Plug-in

I’m going to be a guinea pig and send the love note to my wife. First, I’m going to send her the QR code, in order to have her register with the created channel (app) as a friend. 

![not-my-husband|280x500](https://community.theta360.guide/uploads/default/original/2X/2/2101370dfed680465241b9164181134b88acd1d7.png)

Yikes. A stumble. She thinks I’m a fake. My wife is very careful about internet scams. I failed to have her register with the app as friend. (I went for reality, and didn’t tell my wife ahead of time that I was doing this. It backfired.)

Since that didn’t work, I changed the User ID to me, and ran it. Here’s the screenshot. 

![love%20you|280x500](https://community.theta360.guide/uploads/default/original/2X/1/12ebd42335f678381f69ca95e6fd6208ee72046c.png)

So, that’s actually me sending a message to myself and replying to myself. Not quite as romantic as originally intended.

I noticed that you cannot confirm if you got a reply or not with the THETA. (You can confirm using the Webhook’s URL.)

I am planning to write a sequel to this article for a method to send 360 degrees images via LINE. LINE’s viewer is compatible of handling 360 degrees images, so I think that will look great. 

## Conclusion

* I’ve explained a way to connect a THETA in developer mode to a desktop computer via USB and Wi-Fi at the same time. 

* I’ve created a plug-in to send LINE messages using a THETA connected to Wi-Fi. 

* This is compatible not only to LINE but to other cloud service APIs. 

If you are interested in THETA plug-in development, [please register for the partner program](https://www8.webcas.net/db/pub/ricoh/thetaplugin/create/input)! 

Please be aware that the THETA with its serial number registered with the program will no longer be eligible for standard end-user support. 

For detailed information regarding partner program please see [here](https://api.ricoh/products/theta-plugin/).

The registration form is [here](https://www8.webcas.net/db/pub/ricoh/thetaplugin/create/input).