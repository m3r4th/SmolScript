package smolscript;

import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ScriptTask extends Task<Integer> {

    /**
     * command to be executed
     */
    private final String command;

    /**
     * number of times the command should be run
     */
    private final int numOfRuns;

    public ScriptTask(String command, int numOfRuns) {
        this.command = command;
        this.numOfRuns = numOfRuns;
    }

    @Override
    protected Integer call() throws Exception {
        // Build process in process builder and run it
        Process scriptProcess;
        String currentPath = new File("").getAbsolutePath();
        ProcessBuilder procBuilder = new ProcessBuilder(command.split(" "));
        procBuilder.directory(new File(currentPath));
        // Redirect std.err of process to std.out, so both can be read in real time without creating more threads
        procBuilder.redirectErrorStream(true);

        StringBuilder outputBuilder = new StringBuilder();
        // Run command for as many times, as specified by numOfRuns
        for (int i = 1; i <= numOfRuns; i++) {
            // 1 indicates process is running
            updateValue(1);

            // run the script
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
                    outputBuilder.append((char) c);
                    if (c == '\n') {
                        updateMessage(outputBuilder.toString());
                    }
                }
                if (!outputBuilder.isEmpty()) {
                    updateMessage(outputBuilder.toString());
                }
            }
            // Wait for process to terminate and forward if exit code is ok
            scriptProcess.waitFor();
            if (scriptProcess.exitValue() != 0) {
                // -1 Indicates bad exit
                updateValue(-1);
            } else {
                // 2 Indicates successful exit
                updateValue(2);
            }
            // update progress for progress bar and time estimation
            updateProgress(i, numOfRuns);
        }
        return 0;
    }
}
