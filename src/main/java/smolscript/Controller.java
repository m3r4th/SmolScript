package smolscript;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Controller {

    @FXML
    private TextArea scriptArea;

    @FXML
    private TextArea outputArea;

    @FXML
    protected void onRunButtonClick() {
        onSaveButtonClick();
        runScript();
    }

    private void runScript() {
        outputArea.clear();

        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        if (isWindows) {
            // TODO Check num of executions
            ScriptTask task = new ScriptTask("cmd /c kotlinc -script script.kts", 1);
            task.messageProperty().addListener(((observable, oldValue, newValue) -> {
                outputArea.setText(newValue);
                // use append to get textArea to scroll to the bottom
                outputArea.appendText("");
            }));

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
}