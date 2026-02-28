package io.onedev.server.workspace;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;

public abstract class WorkspaceRuntime {
    
    private final Map<String, Shell> shells = new LinkedHashMap<>();
    
    public synchronized String openShell(Terminal terminal) {
        var shellId = UUID.randomUUID().toString();
        shells.put(shellId, doOpenShell(new Terminal() {

            @Override
            public void onShellOutput(String base64Data) {
                terminal.onShellOutput(base64Data);
            }

            @Override
            public void onShellExit() {
                shells.remove(shellId);
                terminal.onShellExit();
            }

        }));
        return shellId;
    }

    public synchronized List<String> getShellIds() {
        return new ArrayList<>(shells.keySet());
    }

    public synchronized void terminateShell(String shellId) {
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
    
}
