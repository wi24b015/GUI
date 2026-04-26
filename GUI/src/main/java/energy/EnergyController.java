package energy;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;


public class EnergyController {

    private static final String API_BASE_URL = "http://localhost:8080";

    private final HttpClient client = HttpClient.newHttpClient();

    // Verbindung zu den Labels (fx:id)
    @FXML private Label lblPoolUsed;
    @FXML private Label lblGridPortion;
    @FXML private Label lblProduced;
    @FXML private Label lblUsed;
    @FXML private Label lblGrid;

    // Verbindung zu den Kalendern
    @FXML private DatePicker dpStart;
    @FXML private DatePicker dpEnd;

      @FXML
    public void onRefreshClick() {
        try {
            String json = sendGet(API_BASE_URL + "/energy/current");
            CurrentEnergy current = parseCurrentEnergy(json);

            lblPoolUsed.setText(current.communityDepleted + "% used");
            lblGridPortion.setText(current.gridPortion + "%");
        } catch (Exception e) {
            lblPoolUsed.setText("API Fehler");
            lblGridPortion.setText("API Fehler");
            e.printStackTrace();
        }
    }

    @FXML
    public void onShowDataClick() {
        try {
            String start = dpStart.getValue().toString();
            String end = dpEnd.getValue().toString();

            String json = sendGet(API_BASE_URL + "/energy/historical?start=" + start + "&end=" + end);
            HistoricalEnergy[] values = parseHistoricalEnergyArray(json);

            double produced = Arrays.stream(values)
                    .mapToDouble(value -> value.communityProduced)
                    .sum();

            double used = Arrays.stream(values)
                    .mapToDouble(value -> value.communityUsed)
                    .sum();

            double grid = Arrays.stream(values)
                    .mapToDouble(value -> value.gridUsed)
                    .sum();

            lblProduced.setText(produced + " kWh");
            lblUsed.setText(used + " kWh");
            lblGrid.setText(grid + " kWh");
        } catch (Exception e) {
            lblProduced.setText("API Fehler");
            lblUsed.setText("API Fehler");
            lblGrid.setText("API Fehler");
            e.printStackTrace();
        }
    }

    private String sendGet(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    private CurrentEnergy parseCurrentEnergy(String json) {
        CurrentEnergy energy = new CurrentEnergy();
        energy.hour = extractString(json, "hour");
        energy.communityDepleted = extractDouble(json, "communityDepleted");
        energy.gridPortion = extractDouble(json, "gridPortion");
        return energy;
    }

    private HistoricalEnergy[] parseHistoricalEnergyArray(String json) {
        String trimmed = json == null ? "" : json.trim();
        if (trimmed.length() < 2 || "[]".equals(trimmed)) {
            return new HistoricalEnergy[0];
        }

        String content = trimmed.substring(1, trimmed.length() - 1).trim();
        if (content.isEmpty()) {
            return new HistoricalEnergy[0];
        }

        String[] objects = content.split("\\},\\s*\\{");
        HistoricalEnergy[] result = new HistoricalEnergy[objects.length];

        for (int i = 0; i < objects.length; i++) {
            String obj = objects[i];
            if (!obj.startsWith("{")) {
                obj = "{" + obj;
            }
            if (!obj.endsWith("}")) {
                obj = obj + "}";
            }

            HistoricalEnergy energy = new HistoricalEnergy();
            energy.hour = extractString(obj, "hour");
            energy.communityProduced = extractDouble(obj, "communityProduced");
            energy.communityUsed = extractDouble(obj, "communityUsed");
            energy.gridUsed = extractDouble(obj, "gridUsed");
            result[i] = energy;
        }

        return result;
    }

    private String extractString(String json, String key) {
        String marker = "\"" + key + "\"";
        int keyIndex = json.indexOf(marker);
        if (keyIndex < 0) return "";

        int colonIndex = json.indexOf(':', keyIndex + marker.length());
        if (colonIndex < 0) return "";

        int startQuote = json.indexOf('"', colonIndex + 1);
        if (startQuote < 0) return "";

        int endQuote = json.indexOf('"', startQuote + 1);
        if (endQuote < 0) return "";

        return json.substring(startQuote + 1, endQuote);
    }

    private double extractDouble(String json, String key) {
        String marker = "\"" + key + "\"";
        int keyIndex = json.indexOf(marker);
        if (keyIndex < 0) return 0.0;

        int colonIndex = json.indexOf(':', keyIndex + marker.length());
        if (colonIndex < 0) return 0.0;

        int i = colonIndex + 1;
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
            i++;
        }

        int start = i;
        while (i < json.length()) {
            char c = json.charAt(i);
            if ((c >= '0' && c <= '9') || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E') {
                i++;
            } else {
                break;
            }
        }

        if (start == i) return 0.0;

        return Double.parseDouble(json.substring(start, i));
    }

    public static class CurrentEnergy {
        public String hour;
        public double communityDepleted;
        public double gridPortion;
    }

    public static class HistoricalEnergy {
        public String hour;
        public double communityProduced;
        public double communityUsed;
        public double gridUsed;
    }
}