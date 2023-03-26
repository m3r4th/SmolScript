package smolscript;

import javafx.concurrent.Task;

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
        Process scriptProcess;
        try {
            String currentPath = new File("").getAbsolutePath();
            ProcessBuilder procBuilder = new ProcessBuilder(command.split(" "));
            procBuilder.directory(new File(currentPath));
            procBuilder.redirectErrorStream(true);
            updateValue(1);
            scriptProcess = procBuilder.start();
        } catch (IOException e) {
            System.err.println("Error on attempting to run script.");
            e.printStackTrace();
            return -1;
        }

        // Read script output
        // TODO read std.err too
        // TODO Send progress
        try (InputStreamReader isr = new InputStreamReader(scriptProcess.getInputStream())) {
            int c;
            StringBuilder messageBuilder = new StringBuilder();
            while ((c = isr.read()) >= 0) {
                messageBuilder.append((char) c);
                if (c == '\n') {
                    updateMessage(messageBuilder.toString());
                }
            }
            if (!messageBuilder.isEmpty()) {
                updateMessage(messageBuilder.toString());
            }
            messageBuilder.setLength(0);
        }
        scriptProcess.waitFor();
        if (scriptProcess.exitValue() > 0) {
            updateValue(-1);
        } else {
            updateValue(0);
        }
        return null;
    }
}
