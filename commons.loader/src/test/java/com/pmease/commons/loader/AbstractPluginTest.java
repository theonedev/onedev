package com.pmease.commons.loader;

import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.google.common.collect.ImmutableSet;

public class AbstractPluginTest {

	private PluginManager pluginManager;
	
	private AbstractPlugin plugin1, plugin2, plugin3;
	
	@Before
	public void before() {
		plugin1 = spy(new AbstractPlugin() {

			@Override
			public Collection<?> getExtensions() {
				return null;
			}
			
		});
		plugin1.setId("plugin1");
		plugin1.setDependencyIds(ImmutableSet.of("plugin2", "plugin3"));
		
		plugin2 = spy(new AbstractPlugin() {

			@Override
			public Collection<?> getExtensions() {
				return null;
			}
			
		});
		plugin2.setId("plugin2");
		plugin2.setDependencyIds(ImmutableSet.of("plugin3"));

		plugin3 = spy(new AbstractPlugin() {

			@Override
			public Collection<?> getExtensions() {
				return null;
			}
			
		});
		plugin3.setId("plugin3");
		
		Set<AbstractPlugin> plugins = new HashSet<AbstractPlugin>();
		plugins.add(plugin1);
		plugins.add(plugin2);
		plugins.add(plugin3);
		pluginManager = new DefaultPluginManager(plugins);
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
