package smolscript;

import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ScriptTask extends Task<Integer> {

    private String command;

    private int numOfRuns;

    public ScriptTask(String command, int numOfRuns) {
        this.command = command;
        this.numOfRuns = numOfRuns;
    }

    @Override
    protected Integer call() throws Exception {
        // Run script
        Process scriptProcess = null;
        try {
            String currentPath = new File("").getAbsolutePath();
            scriptProcess = Runtime.getRuntime().exec(command,
                    null, new File(currentPath));
        } catch (IOException e) {
            System.err.println("Error on attempting to run script.");
            e.printStackTrace();
        }

        // Read script output
        //TODO Replace buffered reader (Inputstream reader?)
        BufferedReader reader = new BufferedReader(new InputStreamReader(scriptProcess.getInputStream()));
        String line = "";
        while ((line = reader.readLine()) != null) {
            // Pass line of output to application thread
            updateMessage(line);
        }
        return null;
    }
}
