package smolscript;

import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Extension of JavaFX Task Class
 * Runs the specified command for the specified number of times
 * Updates the application thread about the process status
 */
public class ScriptTask extends Task<Integer> {

    /**
     * command to be executed
     */
    private final String command;

    /**
     * number of times the command should be run
     */
    private final int numOfRuns;

    /**
     * Value that is passed to application thread.
     * 1st bit set indicates that value of 3rd bit should be used to update GUI
     * 2nd bit set indicates script process is running
     * 3rd bit set indicates bad exit of last script run
     */
    private int value = 0;

    /**
     * Constructor of ScriptTask
     *
     * @param command   The command that should be executed by the task
     * @param numOfRuns The number of times the command should be run
     */
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
            // set second bit, to indicate that the process is running to the application thread
            value |= 2;
            updateValue(value);

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
            // unset second bit to indicate process stopped running
            value &= 5;
            //updateValue(value);
            // set first bit to indicate valid exit value
            value |= 1;
            if (scriptProcess.exitValue() != 0) {
                // set third bit to indicate bad exit
                value |= 4;
                updateValue(value);
            } else {
                // unset third bit to indicate successful exit
                value &= 3;
                updateValue(value);
            }
            // update progress for progress bar and time estimation
            updateProgress(i, numOfRuns);
        }
        return 0;
    }
}
