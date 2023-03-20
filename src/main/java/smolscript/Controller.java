package smolscript;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.io.BufferedWriter;
import java.io.File;
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
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        String currentPath = new File("").getAbsolutePath();
        if (isWindows) {
            // TODO Check num of executions
            // TODO Create task thread
            ScriptTask task = new ScriptTask("cmd /c kotlinc -script script.kts", 1);
            task.messageProperty().addListener(((observable, oldValue, newValue) -> outputArea.appendText(newValue)));

            Thread taskThread = new Thread(task);
            taskThread.setDaemon(true);
            taskThread.start();
        }
    }

    @FXML
    protected void onSaveButtonClick() {
        String code = scriptArea.getText();

        // Write script to file
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("script.kts", false));
            writer.write(code);
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error when writing to file!");
            //TODO add error popup
        }
    }
}