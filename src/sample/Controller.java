package sample;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.Optional;

public class Controller {

    @FXML
    private LineChart<String, Number> lineChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private GridPane gridPane;

    @FXML
    private Button button;

    @FXML
    private DatePicker startPicker;

    @FXML
    private DatePicker endPicker;

    public void initialize() {
        startPicker.setValue(LocalDate.of(2017, 4, 1));
        endPicker.setValue(LocalDate.of(2017, 4, 30));

        button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                update();
            }
        });

        startPicker.valueProperty().addListener(new ChangeListener<LocalDate>() {
            @Override
            public void changed(ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) {
                update();
            }
        });

        endPicker.valueProperty().addListener(new ChangeListener<LocalDate>() {
            @Override
            public void changed(ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) {
                update();
            }
        });
    }

    private void update() {
        if (startPicker.getValue().compareTo(endPicker.getValue()) > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid range");
            alert.setContentText("Start date must be before end date!");
            alert.showAndWait();
        } else {
            UpdateTask task = new UpdateTask(() -> {
                API api = new API(startPicker.getValue(), endPicker.getValue());
                Platform.runLater(() -> {
                    drawChart(api);
                });
            });
            new Thread(task).start();
        }
    }

    private void drawChart(API api) {
        lineChart.setTitle("Exchange Rate");
        lineChart.setAnimated(false);
        xAxis.setLabel("date");
        yAxis.setLabel("exchange");

        lineChart.getData().clear();

        Optional<APIData> min = api.getMin("EUR");
        Optional<APIData> max = api.getMax("EUR");
        if (min.isPresent() && max.isPresent()) {
            yAxis.setAutoRanging(false);
            yAxis.setLowerBound(min.get().getRates().get("EUR") - 0.005 * min.get().getRates().get("EUR"));
            yAxis.setUpperBound(max.get().getRates().get("EUR") + 0.005 * max.get().getRates().get("EUR"));
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("EUR");
        for (APIData apiData : api.getValues()) {
            series.getData().add(new XYChart.Data<String, Number>(apiData.getDate(), apiData.getRates().get("EUR")));
        }
        lineChart.getData().add(series);

        if (min.isPresent()) {
            XYChart.Series<String, Number> seriesMin = new XYChart.Series<>();
            seriesMin.setName("MIN");
            seriesMin.getData().add(new XYChart.Data<String, Number>(min.get().getDate(), min.get().getRates().get("EUR")));
            lineChart.getData().add(seriesMin);
        }

        if (max.isPresent()) {
            XYChart.Series<String, Number> seriesMax = new XYChart.Series<>();
            seriesMax.setName("MAX");
            seriesMax.getData().add(new XYChart.Data<String, Number>(max.get().getDate(), max.get().getRates().get("EUR")));
            lineChart.getData().add(seriesMax);
        }
    }
}
