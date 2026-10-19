package io.onedev.server.service.impl;

import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.event.cluster.NodeDisconnected;
import io.onedev.server.oem.OemService;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OemAlertTest {

	@Test
	void clusterAlertBrandsOwnedTextWithoutRewritingServerNames() throws Exception {
		var productName = "Acme ${website} $1\\ %s {}";
		var serverName = "OneDev ${productName} %s {}";
		var oem = mock(OemService.class);
		when(oem.getProductName()).thenReturn(productName);
		var cluster = mock(ClusterService.class);
		when(cluster.isLeaderServer()).thenReturn(true);
		when(cluster.getServerName("192.0.2.1")).thenReturn(serverName);
		var service = spy(new DefaultAlertService());
		doNothing().when(service).alert(anyString(), anyString(), anyBoolean());
		var clusterField = DefaultAlertService.class.getDeclaredField("clusterService");
		clusterField.setAccessible(true);
		clusterField.set(service, cluster);

		try (var oneDev = mockStatic(OneDev.class)) {
			oneDev.when(() -> OneDev.getInstance(OemService.class)).thenReturn(oem);
			service.on(new NodeDisconnected("192.0.2.1", true));
		}

		var server = "192.0.2.1 (" + serverName + ")";
		verify(service).alert("Server '" + server + "' cannot be reached",
				"Server '" + server + "' is part of " + productName + " cluster, but cannot be reached for some reason",
				false);
	}

}
