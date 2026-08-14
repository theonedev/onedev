package io.onedev.server.workspace;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.junit.Test;

import io.onedev.agent.workspace.FileData;
import io.onedev.agent.workspace.GitExecutionResult;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;

public class WorkspaceRuntimeTest {

    @Test
    public void shouldBuildPublishedPortUrlsWithoutTailscale() {
        var runtime = new TestRuntime("workspace.example.com", Map.of(3000, 49152), null);

        assertEquals("http://workspace.example.com:49152", runtime.getPortUrls().get(3000));
    }

    @Test
    public void shouldBuildContainerPortUrlsWithTailscale() {
        var runtime = new TestRuntime("workspace.example.com", Map.of(3000, 49152), "100.64.0.1");

        assertEquals("http://100.64.0.1:3000", runtime.getPortUrls().get(3000));
    }

    @Test
    public void shouldBracketIpv6TailscaleAddress() {
        var runtime = new TestRuntime("workspace.example.com", Map.of(3000, 49152), "fd7a:115c:a1e0::1");

        assertEquals("http://[fd7a:115c:a1e0::1]:3000", runtime.getPortUrls().get(3000));
    }

    private static class TestRuntime extends WorkspaceRuntime {

        private final String portHost;

        private final Map<Integer, Integer> portMappings;

        private final String tailscaleIp;

        TestRuntime(String portHost, Map<Integer, Integer> portMappings, @Nullable String tailscaleIp) {
            this.portHost = portHost;
            this.portMappings = new LinkedHashMap<>(portMappings);
            this.tailscaleIp = tailscaleIp;
        }

        @Override
        public GitExecutionResult executeGitCommand(String[] gitArgs) {
            throw new UnsupportedOperationException();
        }

        @Override
        public FileData readFileData(String path) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected Shell doOpenShell(String shellId, Terminal terminal) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void await() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getPortHost() {
            return portHost;
        }

        @Override
        public Map<Integer, Integer> getPortMappings() {
            return portMappings;
        }

        @Override
        protected String getTailscaleIp() {
            return tailscaleIp;
        }

    }

}
