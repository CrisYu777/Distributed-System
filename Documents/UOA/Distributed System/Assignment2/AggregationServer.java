import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;

public class AggregationServer {
    private static final int PORT = 4567;
    private static Map<String, JSONObject> weatherData = new HashMap<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/weather.json", new WeatherHandler());
        server.setExecutor(null); // creates a default executor
        System.out.println("Aggregation Server started on port " + PORT);
        server.start();
    }

    static class WeatherHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("GET".equals(method)) {
                handleGetRequest(exchange);
            } else if ("PUT".equals(method)) {
                handlePutRequest(exchange);
            } else {
                exchange.sendResponseHeaders(400, -1); // Bad Request
            }
        }

        private void handleGetRequest(HttpExchange exchange) throws IOException {
            System.out.println("Received a GET request.");
            String response = weatherData.isEmpty() ? "{}" : new JSONObject(weatherData).toString();
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private void handlePutRequest(HttpExchange exchange) throws IOException {
            System.out.println("Received a PUT request.");
            InputStream is = exchange.getRequestBody();
            StringBuilder textBuilder = new StringBuilder();
            try (Reader reader = new BufferedReader(new InputStreamReader(is))) {
                int c;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }
            }

            String requestBody = textBuilder.toString();
            System.out.println("Request content: " + requestBody);
            JSONObject jsonObject = new JSONObject(requestBody);
            String id = jsonObject.getString("id");
            weatherData.put(id, jsonObject);

            // Response
            String response = "Data received";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
