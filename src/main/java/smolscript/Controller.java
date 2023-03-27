package smolscript;

import javafx.fxml.FXML;
import javafx.scene.control.*;
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

    @FXML
    private Label timeIndicator;

    private long scriptStartTime = 0;

    private long weightedAverageRuntime = 0;

    /**
     * This function is called when launching to program.
     * Sets up various GUI elements
     */
    public void initialize() {
        // Load script file into scriptArea (Gui) if one exists, otherwise insert hello world
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

        // Set up TextBox for number of runs to only accept integers >= 1
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

    /**
     * Runs the script that was previously saved to "script.kts"
     */
    private void runScript() {
        outputArea.clear();
        if (numOfRunsField.getText().equals("")) {
            numOfRunsField.setText("1");
        }
        int numOfRuns = Integer.parseInt(numOfRunsField.getText());

        // Check for OS and set command accordingly
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        ScriptTask task;
        scriptStartTime = System.currentTimeMillis();
        if (isWindows) {
            task = new ScriptTask("cmd /c kotlinc -script script.kts", numOfRuns);
        } else {
            task = new ScriptTask("kotlinc -script script.kts", numOfRuns);
        }

        // Bind progress bar to progress in scriptTask
        progressBar.progressProperty().bind(task.progressProperty());

        // Display the output that is sent by the scriptTask
        task.messageProperty().addListener(((observable, oldValue, newValue) -> {
            outputArea.setText(newValue);
            // use append to get textArea to scroll to the bottom
            outputArea.appendText("");
        }));

        // Read values sent by scriptTask, which indicate the process status, and update GUI.
        task.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue == 0) {
                return;
            }
            // check if second bit is set, which indicates running script
            if ((newValue & 2) > 0) {
                setRunning();
            } else {
                setFinished();
            }
            // check if third bit is set, which indicates non-zero exit code
            if ((newValue & 1) > 0) {
                if ((newValue & 4) > 0) {
                    setBadExit();
                } else {
                    setSuccessfulExit();
                }
            }
        });

        // calculate and display estimated remaining time when progress is updated by scriptTask
        task.progressProperty().addListener((observable, oldValue, newValue) -> {
            long timeThisRun = System.currentTimeMillis() - scriptStartTime;

            if (weightedAverageRuntime == 0) {
                weightedAverageRuntime = timeThisRun;
            } else {
                // 50/50 weighting between the newest run and all previous runs
                weightedAverageRuntime = (timeThisRun + weightedAverageRuntime) / 2;
            }

            // display estimated time in seconds or minutes
            float timeInSec = weightedAverageRuntime * (numOfRuns - (numOfRuns * newValue.floatValue())) / 1000;
            if (timeInSec > 600) {
                float timeInMin = timeInSec / 60;
                timeIndicator.setText((long) timeInMin + " m");
            } else {
                timeIndicator.setText((long) timeInSec + " s");
            }
            // Reset variables
            scriptStartTime = System.currentTimeMillis();
            if (Math.abs(newValue.floatValue() - 1.0) < 0.000001) {
                weightedAverageRuntime = 0;
                scriptStartTime = 0;
            }
        });

        //Start running the task
        Thread taskThread = new Thread(task);
        taskThread.setDaemon(true);
        taskThread.start();
    }

    /**
     * Saves the text in scriptArea to the script file "script.kts"
     */
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
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Error when writing to file.");
            errorAlert.showAndWait();
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