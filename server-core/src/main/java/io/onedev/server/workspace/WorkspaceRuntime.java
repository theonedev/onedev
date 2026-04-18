package io.onedev.server.workspace;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;

public abstract class WorkspaceRuntime {

    private final Map<String, Shell> shells = new HashMap<>();

    private final Map<String, String> labels = new LinkedHashMap<>();
    
    public synchronized String openShell(Terminal terminal, String label) {
        var shellId = UUID.randomUUID().toString();
        shells.put(shellId, doOpenShell(new Terminal() {

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

        }));
        labels.put(shellId, label);

        return shellId;
    }

    public abstract GitExecutionResult executeGitCommand(String[] gitArgs);

    public synchronized Map<String, String> getShellLabels() {
        return new LinkedHashMap<>(labels);
    }

    public synchronized void terminateShell(String shellId) {
        labels.remove(shellId);
        var shell = shells.remove(shellId);
        if (shell != null)
            shell.terminate();
    }

    public synchronized void writeShellStdin(String shellId, String data) {
        var shell = shells.get(shellId);
        if (shell != null) 
            shell.writeToStdin(data);
    }

    public synchronized void resizeShell(String shellId, int rows, int cols) {
        var shell = shells.get(shellId);
        if (shell != null)
            shell.resize(rows, cols);
    }

    protected abstract Shell doOpenShell(Terminal terminal);
    
    public abstract void await();

    public Map<Integer, Integer> getPortMappings() {
        return Collections.emptyMap();
    }

}
