package smolscript;

import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ScriptTask extends Task<Integer> {

    private final String command;

    private final int numOfRuns;

    public ScriptTask(String command, int numOfRuns) {
        this.command = command;
        this.numOfRuns = numOfRuns;
    }

    @Override
    protected Integer call() throws Exception {
        // Run script
        Process scriptProcess;
        String currentPath = new File("").getAbsolutePath();
        ProcessBuilder procBuilder = new ProcessBuilder(command.split(" "));
        procBuilder.directory(new File(currentPath));
        procBuilder.redirectErrorStream(true);
        StringBuilder messageBuilder = new StringBuilder();

        for (int i = 1; i <= numOfRuns; i++) {
            updateValue(1);
            try {
                scriptProcess = procBuilder.start();
            } catch (IOException e) {
                System.err.println("Error on attempting to run script.");
                e.printStackTrace();
                return -1;
            }

            // Read script output
            try (InputStreamReader isr = new InputStreamReader(scriptProcess.getInputStream())) {
                int c;
                while ((c = isr.read()) >= 0) {
                    messageBuilder.append((char) c);
                    if (c == '\n') {
                        updateMessage(messageBuilder.toString());
                    }
                }
                if (!messageBuilder.isEmpty()) {
                    updateMessage(messageBuilder.toString());
                }
            }
            scriptProcess.waitFor();
            if (scriptProcess.exitValue() > 0) {
                updateValue(-1);
            } else {
                updateValue(0);
            }
            updateProgress(i, numOfRuns);
        }
        return null;
    }
}
