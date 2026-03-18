package io.onedev.server.web.page.ai.session;

import static org.junit.Assert.assertEquals;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.Test;

public class AiSessionListPageTest {

@Test
public void shouldResolveDefaultPageState() {
var params = new PageParameters();
assertEquals(AiSessionListPage.Filter.ACTIVE, AiSessionListPage.resolveFilter(params));
assertEquals(AiSessionListPage.Tab.SESSIONS, AiSessionListPage.resolveTab(params));
assertEquals(AiSessionListPage.AgentFilter.ALL, AiSessionListPage.resolveAgentFilter(params));
assertEquals(AiSessionListPage.RunStateFilter.ALL, AiSessionListPage.resolveRunStateFilter(params));
assertEquals(null, AiSessionListPage.resolveProjectPath(params));
}

@Test
public void shouldResolveExplicitPageState() {
var params = AiSessionListPage.paramsOf(
		AiSessionListPage.Filter.ALL,
		AiSessionListPage.Tab.AGENTS,
		AiSessionListPage.AgentFilter.COPILOT,
		"demo/project",
		AiSessionListPage.RunStateFilter.FAILED);
assertEquals(AiSessionListPage.Filter.ALL, AiSessionListPage.resolveFilter(params));
assertEquals(AiSessionListPage.Tab.AGENTS, AiSessionListPage.resolveTab(params));
assertEquals(AiSessionListPage.AgentFilter.COPILOT, AiSessionListPage.resolveAgentFilter(params));
assertEquals(AiSessionListPage.RunStateFilter.FAILED, AiSessionListPage.resolveRunStateFilter(params));
assertEquals("demo/project", AiSessionListPage.resolveProjectPath(params));
}

@Test
public void shouldFallbackForUnknownValues() {
var params = new PageParameters();
params.add("filter", "bogus");
params.add("tab", "bogus");
params.add("agent", "bogus");
params.add("status", "bogus");
assertEquals(AiSessionListPage.Filter.ACTIVE, AiSessionListPage.resolveFilter(params));
assertEquals(AiSessionListPage.Tab.SESSIONS, AiSessionListPage.resolveTab(params));
assertEquals(AiSessionListPage.AgentFilter.ALL, AiSessionListPage.resolveAgentFilter(params));
assertEquals(AiSessionListPage.RunStateFilter.ALL, AiSessionListPage.resolveRunStateFilter(params));
}

@Test
public void shouldOmitBlankProjectFromParams() {
	var params = AiSessionListPage.paramsOf(
			AiSessionListPage.Filter.HISTORY,
			AiSessionListPage.Tab.SESSIONS,
			AiSessionListPage.AgentFilter.CLAUDE,
			" ",
			AiSessionListPage.RunStateFilter.COMPLETED);
	assertEquals(null, AiSessionListPage.resolveProjectPath(params));
}

}
