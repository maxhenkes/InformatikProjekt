package informatikprojekt.zigbee.frontend;

import informatikprojekt.zigbee.backend.DataManager;
import informatikprojekt.zigbee.util.CommonUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.time.Duration;
import java.util.*;

public class ControllerOverview implements Initializable {
    public Circle ledRED;
    public Circle ledORANGE;
    public Circle ledGREEN;
    public Label tempValue;
    public Label humValue;
    public Label co2Value;
    public Label vocValue;
    public Label txtHinweis;
    public HBox titlepane;
    public DatePicker datePicker;
    List<String> hints = new LinkedList<>();

    int low = 0;
    int med = 0;
    int high = 0;

    private float temp, hum, co2, voc;
    public static ControllerOverview INSTANCE;

    Paint redColor;
    Paint orangeColor;
    Paint greenColor;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        INSTANCE = this;
        redColor = ledRED.getFill();
        orangeColor = ledORANGE.getFill();
        greenColor = ledGREEN.getFill();

        ledRED.setFill(Color.GRAY);
        ledORANGE.setFill(Color.GRAY);

/*        Spinner<LocalTime> spinner = new Spinner<>(new SpinnerValueFactory<>() {
            {
                setConverter(new LocalTimeStringConverter(DateTimeFormatter.ofPattern("HH:mm"), DateTimeFormatter.ofPattern("HH:mm")));
                if(getValue() == null){
                    setValue(LocalTime.MIDNIGHT);
                }
            }
            @Override
            public void decrement(int steps) {
                if (getValue() == null)
                    setValue(LocalTime.now());
                else {
                    LocalTime time = (LocalTime) getValue();
                    setValue(time.minusMinutes(steps));
                }
            }

            @Override
            public void increment(int steps) {
                if (this.getValue() == null)
                    setValue(LocalTime.now());
                else {
                    LocalTime time = (LocalTime) getValue();
                    setValue(time.plusMinutes(steps));
                }
            }
        });
        spinner.setEditable(true);
        spinner.setPrefWidth(80);
        spinner.setMinWidth(80);
        titlepane.getChildren().add(1,spinner);*/

    }

    public void startTimer() {

        Timer timer1 = new Timer();
        CommonUtils.registerTimer(timer1);
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    temp = DataManager.get().get15MinAverage("Temperatur");
                    hum = DataManager.get().get15MinAverage("Feuchtigkeit");
                    co2 = DataManager.get().get15MinAverage("CO2");
                    voc = DataManager.get().get15MinAverage("VOC");

                    resetEval();
                    tempValue.setText(String.format(Locale.US, "%.2f", temp));
                    humValue.setText(String.format(Locale.US, "%.2f", hum));
                    vocValue.setText(String.format(Locale.US, "%.2f", voc));
                    co2Value.setText(String.format(Locale.US, "%.2f", co2));
                    setValues();
                    setLEDs();
                    setHints();
                });
            }
        }, Duration.ofMinutes(2).toMillis(), Duration.ofMinutes(15).toMillis());

    }

    private void resetEval() {
        high = 0;
        med = 0;
        low = 0;
        hints.clear();
    }

    private void setValues() {
        evaluateCO2();
        evaluateTemp();
        evaluateHum();
        evaluateVOC();
    }

    private void evaluateVOC() {
        if (voc < 0.3) {
            low++;
        } else if (voc < 3) {
            med++;
            hints.add("Reizung oder Beeinträchtigung des Wohlbefindens durch flüchtige organische Stoffe.");
        } else {
            high++;
            hints.add("Reizungen, Unwohlsein und Kopfschmerzen durch erhöhte flüchtige organische Stoffe.");
        }
    }

    private void evaluateTemp() {
        if (temp >= 18 && temp <= 25) {
            low++;
            hints.add("Temperatur befindet sich im optimalen Bereich.");
        } else if (temp <= 18) {
            hints.add("Niedrige Temperaturen erhöhen das Ansteckungsrisiko.");
            med++;
        } else {
            low++;
        }
        //TODO: > 35c oder so warnung

    }

    private void evaluateHum() {
        if (hum >= 40 && hum <= 60) {
            low++;
            hints.add("Feuchtigkeit befindet sich im optimalen Bereich.");
        } else if (hum <= 40 && hum >= 20) {
            med++;
            hints.add("Niedrige Feuchtigkeit kann die Übertragungsreichweite durch Tröpfchen erhöhen.");
        } else if (hum >= 60 && hum <= 80) {
            med++;
            hints.add("Hohe Feuchtigkeit unterstützt das Wachstum von Bakterien und Pilzen.");
            hints.add("Hohe Feuchtigkeit erhöht das Allergie und Asthma Risiko.");
        } else if (hum >= 80) {
            high++;
            hints.add("Sehr hohe Feuchtigkeit unterstützt stark das Wachstum von Bakterien und Pilzen.");
            hints.add("Sehr hohe Feutchtigkeit erhöht das Allergie und Asthma Risiko deutlich.");
        } else {
            high++;
            hints.add("Sehr niedrige Feuchtigkeit kann die Übertragungsreichweite durch Tröpfchen deutlich erhöhen.");
        }
    }

    private void evaluateCO2() {
        if (co2 <= 1000) {
            hints.add("CO2 Level befinden sich im optimalen Bereich.");
            low++;
        } else if (co2 <= 2000) {
            hints.add("Erhöhte CO2 Werte verursachen Müdigkeit und allgemeines Unwohlsein.");
            med++;
        } else {
            high++;
            hints.add("Hohe CO2 Werte verursachen Kopfschmerzen, Schaflosigkeit, erhöhte Herzrate und verringern die Konzentration");
        }
    }

    private void setLEDs() {
        if (high > 0) {
            setLedRED();
        } else if (med > 0) {
            setLedOrange();
        } else {
            setLedGreen();
        }


    }

    private void setLedGreen() {
        ledGREEN.setFill(greenColor);
        ledRED.setFill(Color.GRAY);
        ledORANGE.setFill(Color.GRAY);
    }

    private void setLedOrange() {
        ledGREEN.setFill(Color.GRAY);
        ledRED.setFill(Color.GRAY);
        ledORANGE.setFill(orangeColor);
    }

    private void setLedRED() {
        ledGREEN.setFill(Color.GRAY);
        ledRED.setFill(redColor);
        ledORANGE.setFill(Color.GRAY);
    }

    private void setHints() {

        StringBuilder builder = new StringBuilder();

        for (String s : hints) {
            builder.append(s).append("\n");
        }

        txtHinweis.setText(builder.toString());

    }

    public void onBtnSetTime(ActionEvent actionEvent) {
    }
}
