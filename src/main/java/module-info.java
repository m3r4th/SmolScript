module smolscript {
    requires javafx.controls;
    requires javafx.fxml;


    opens smolscript to javafx.fxml;
    exports smolscript;
}