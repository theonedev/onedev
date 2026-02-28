package io.onedev.server.workspace;

import java.io.Serializable;

public class GitExecutionResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final byte[] stdout;

    private final byte[] stderr;

    private final int returnCode;

    public GitExecutionResult(byte[] stdout, byte[] stderr, int returnCode) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.returnCode = returnCode;
    }

    public byte[] getStdout() {
        return stdout;
    }

    public byte[] getStderr() {
        return stderr;
    }
    
    public int getReturnCode() {
        return returnCode;
    }
    
}
