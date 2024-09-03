import java.io.*;
import java.net.*;
import org.json.JSONObject;

public class ContentServer {
    private LamportClock clock = new LamportClock();

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ContentServer <server-url> <file-path>");
            return;
        }
        try {
            new ContentServer().sendPutRequest(args[0], args[1]);
        } catch (URISyntaxException e) {
            System.out.println("Error: Invalid URL syntax.");
            e.printStackTrace();
        }
    }

    public void sendPutRequest(String serverUrl, String filePath) throws URISyntaxException {
        try {
            System.out.println("Reading data from file: " + filePath);
            JSONObject jsonData = readDataFromFile(filePath);
            jsonData.put("lamport_clock", clock.incrementAndGet());

            System.out.println("Sending data to server: " + serverUrl);
            URL url = new URI(serverUrl + "/weather.json").toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonData.toString().getBytes());
                os.flush();
            }

            int responseCode = connection.getResponseCode();
            System.out.println("Response code from server: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                System.out.println("Data successfully uploaded.");
            } else {
                System.out.println("Failed to upload data. Server responded with code: " + responseCode);
            }

            connection.disconnect();
        } catch (IOException e) {
            System.out.println("Error: Unable to send data to server.");
            e.printStackTrace();
        }
    }

    private JSONObject readDataFromFile(String filePath) throws IOException {
        JSONObject jsonData = new JSONObject();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    jsonData.put(parts[0].trim(), parts[1].trim());
                }
            }
        }
        return jsonData;
    }
}
