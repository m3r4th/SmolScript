package smolscript;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Controller {

    @FXML
    private TextArea scriptArea;

    @FXML
    protected void onRunButtonClick() {
        onSaveButtonClick();
        // TODO run script
    }

    @FXML
    protected void onSaveButtonClick() {
        String code = scriptArea.getText();

        // Write code to file
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