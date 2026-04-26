package energy;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;


public class EnergyController {

    private static final String API_BASE_URL = "http://localhost:8080";

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

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
            CurrentEnergy current = mapper.readValue(json, CurrentEnergy.class);

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
            HistoricalEnergy[] values = mapper.readValue(json, HistoricalEnergy[].class);

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