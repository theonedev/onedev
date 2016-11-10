package com.gitplex.commons.loader;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.gitplex.commons.loader.AbstractPlugin;
import com.gitplex.commons.loader.DefaultPluginManager;
import com.gitplex.commons.loader.Plugin;
import com.gitplex.commons.loader.PluginManager;
import com.google.common.collect.ImmutableSet;

public class AbstractPluginTest {

	private PluginManager pluginManager;
	
	private AbstractPlugin plugin1, plugin2, plugin3;
	
	@Before
	public void before() {
		plugin1 = spy(new AbstractPlugin() {

			@Override
			public void start() {
			}

			@Override
			public void stop() {
			}
		});
		plugin1.setId("plugin1");
		plugin1.setDependencyIds(ImmutableSet.of("plugin2", "plugin3"));
		
		plugin2 = spy(new AbstractPlugin() {

			@Override
			public void start() {
			}

			@Override
			public void stop() {
			}
		});
		plugin2.setId("plugin2");
		plugin2.setDependencyIds(ImmutableSet.of("plugin3"));

		plugin3 = spy(new AbstractPlugin() {

			@Override
			public void start() {
			}

			@Override
			public void stop() {
			}
		});
		plugin3.setId("plugin3");
		
		Set<Plugin> plugins = new HashSet<Plugin>();
		plugins.add(plugin1);
		plugins.add(plugin2);
		plugins.add(plugin3);
		pluginManager = new DefaultPluginManager(plugins, Executors.newCachedThreadPool());
	}
	
	@Test
	public void shouldStartPluginsInCorrectOrder() {
		pluginManager.start();
		
		InOrder inOrder = inOrder(plugin1, plugin2, plugin3);
		
		inOrder.verify(plugin3).start();
		inOrder.verify(plugin2).start();
		inOrder.verify(plugin1).start();

		inOrder.verify(plugin1).postStart();
		inOrder.verify(plugin2).postStart();
		inOrder.verify(plugin3).postStart();
	}

	@Test
	public void shouldStopPluginsInCorrectOrder() {
		pluginManager.stop();
		
		InOrder inOrder = inOrder(plugin1, plugin2, plugin3);
		
		inOrder.verify(plugin3).preStop();
		inOrder.verify(plugin2).preStop();
		inOrder.verify(plugin1).preStop();

		inOrder.verify(plugin1).stop();
		inOrder.verify(plugin2).stop();
		inOrder.verify(plugin3).stop();
	}
}
