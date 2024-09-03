import java.io.*;
import java.net.*;
import org.json.JSONObject;

public class GETClient {
    private LamportClock clock = new LamportClock();

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java GETClient <server-url>");
            return;
        }
        new GETClient().sendGetRequest(args[0]);
    }

    public void sendGetRequest(String serverUrl) {
        try {
            URL url = new URI(serverUrl).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            clock.tick();
            connection.setRequestProperty("Lamport-Clock", Integer.toString(clock.getTime()));

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                in.close();
                connection.disconnect();

                JSONObject jsonResponse = new JSONObject(content.toString());
                displayWeatherData(jsonResponse);
            } else {
                System.out.println("GET request failed. Response Code: " + responseCode);
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void displayWeatherData(JSONObject json) {
        json.keys().forEachRemaining(key -> {
            System.out.println(key + ": " + json.get(key));
        });
    }
}
