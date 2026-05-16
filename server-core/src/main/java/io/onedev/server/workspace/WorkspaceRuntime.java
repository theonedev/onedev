package io.onedev.server.workspace;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import io.onedev.agent.workspace.FileData;
import io.onedev.agent.workspace.GitExecutionResult;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;

public abstract class WorkspaceRuntime {

    private final Map<String, Shell> shells = new HashMap<>();

    private final Map<String, String> labels = new LinkedHashMap<>();

    public String openShell(Terminal terminal, String label) {
        var shellId = UUID.randomUUID().toString();
        var shell = doOpenShell(new Terminal() {

            @Override
            public void onShellOutput(String base64Data) {
                terminal.onShellOutput(base64Data);
            }

            @Override
            public void onShellExit() {
                labels.remove(shellId);
                shells.remove(shellId);
                terminal.onShellExit();
            }

        });

        synchronized (this) {
            shells.put(shellId, shell);
            labels.put(shellId, label);
        }

        return shellId;
    }

    public abstract GitExecutionResult executeGitCommand(String[] gitArgs);

    /**
     * Read a file inside the workspace work directory. Implementations decide
     * how to access the file (local file system, agent round-trip, kubernetes
     * exec, etc.). Return {@code null} if the file does not exist.
     */
    @Nullable
    public abstract FileData readFileData(String path);

    public synchronized Map<String, String> getShellLabels() {
        return new LinkedHashMap<>(labels);
    }

    public void terminateShell(String shellId) {
        Shell shell;
        synchronized (this) {
            shell = shells.remove(shellId);
            labels.remove(shellId);
        }
        if (shell != null)
            shell.terminate();
    }

    public void writeShellStdin(String shellId, String data) {
        Shell shell;
        synchronized (this) {
            shell = shells.get(shellId);
        }
        if (shell != null) 
            shell.writeToStdin(data);
    }

    public void resizeShell(String shellId, int rows, int cols) {
        Shell shell;
        synchronized (this) {
            shell = shells.get(shellId);
        }
        if (shell != null)
            shell.resize(rows, cols);
    }

    protected abstract Shell doOpenShell(Terminal terminal);
    
    public abstract void await();

    public abstract String getPortHost();

    public Map<Integer, Integer> getPortMappings() {
        return Collections.emptyMap();
    }

}
