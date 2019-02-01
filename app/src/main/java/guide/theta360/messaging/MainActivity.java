package guide.theta360.messaging;

import android.os.AsyncTask;
import android.os.Bundle;
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
            final String token = "<アクセストークン>";
            final String user_id = "<User ID>";

            final String data = "{"
                    + "\"to\":\""
                    + user_id
                    + "\","
                    + "\"messages\":["
                    + "{\"type\":\"text\",\"text\":\"愛してる\"},"
                    + "{\"type\":\"text\",\"text\":\"めちゃめちゃ愛してる\"},"
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
            // 接続するための設定
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setRequestProperty("Accept", "application/json");

            // APIからの戻り値と送信するデータの設定を許可する
            connection.setDoOutput(true);

            return connection;
        }

        private void sendData(String data, HttpURLConnection connection) throws IOException {
            try (OutputStream out = connection.getOutputStream()) {
                // 接続
                connection.connect();

                // 送信するデータの書き込み
                out.write(data.getBytes("UTF-8"));
                out.flush();
                connection.getResponseCode();
            } finally {
                connection.disconnect();
            }
        }
    }
}