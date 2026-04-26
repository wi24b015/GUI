package energy;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

public class EnergyController {

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
        System.out.println("GUI ruft API auf...");
        // Testweise Text ändern
        lblPoolUsed.setText("Lade Daten...");
    }

    @FXML
    public void onShowDataClick() {
        System.out.println("Historische Daten angefordert.");
    }
}