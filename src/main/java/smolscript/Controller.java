package smolscript;

import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Controller {

    @FXML
    private TextArea scriptArea;
    @FXML
    private TextArea outputArea;
    @FXML
    private Circle exitIndicator;
    @FXML
    private Circle runningIndicator;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private TextField numOfRunsField;


    public void initialize() {
        String text = "";
        try {
            text = new String(Files.readAllBytes(Paths.get("script.kts")));
        } catch (IOException ignored) {
        }

        if (!text.equals("")) {
            scriptArea.setText(text);
        } else {
            scriptArea.setText("println(\"Hello World\")");
        }

        numOfRunsField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[0-9]{0,8}")) {
                numOfRunsField.setText(oldValue);
            } else {
                if (!newValue.equals("") && Integer.parseInt(newValue) < 1) {
                    numOfRunsField.setText(oldValue);
                }
            }
        });
    }

    @FXML
    protected void onRunButtonClick() {
        onSaveButtonClick();
        runScript();
    }

    private void runScript() {
        outputArea.clear();

        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        if (isWindows) {
            int numOfRuns = Integer.parseInt(numOfRunsField.getText());
            ScriptTask task = new ScriptTask("cmd /c kotlinc -script script.kts", numOfRuns);
            progressBar.progressProperty().bind(task.progressProperty());
            task.messageProperty().addListener(((observable, oldValue, newValue) -> {
                outputArea.setText(newValue);
                // use append to get textArea to scroll to the bottom
                outputArea.appendText("");
            }));
            task.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == null) {
                    return;
                }
                switch (newValue) {
                    case 1 -> setRunning();
                    case 0 -> {
                        setFinished();
                        setSuccessfulExit();
                    }
                    case -1 -> {
                        setFinished();
                        setBadExit();
                    }
                }
            });

            Thread taskThread = new Thread(task);
            taskThread.setDaemon(true);
            taskThread.start();
        }
        // TODO implement for Linux
    }

    @FXML
    protected void onSaveButtonClick() {
        String code = scriptArea.getText();

        // Write script to file
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("script.kts", false));
            writer.write(code);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.err.println("Error when writing to file!");
            //TODO add error popup
        }
    }

    private void setRunning() {
        runningIndicator.setFill(Paint.valueOf("00ff00"));
    }

    private void setFinished() {
        runningIndicator.setFill(Paint.valueOf("ffffff"));
    }

    private void setSuccessfulExit() {
        exitIndicator.setFill(Paint.valueOf("00ff00"));
    }

    private void setBadExit() {
        exitIndicator.setFill(Paint.valueOf("ff0000"));
    }
}