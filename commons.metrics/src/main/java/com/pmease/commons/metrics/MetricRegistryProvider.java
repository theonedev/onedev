package com.pmease.commons.metrics;

import java.lang.management.ManagementFactory;

import javax.inject.Provider;
import javax.inject.Singleton;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;

@Singleton
public class MetricRegistryProvider implements Provider<MetricRegistry> {

	@Override
	public MetricRegistry get() {
		MetricRegistry registry = new MetricRegistry();
		registry.register("jvm.buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
		registry.register("jvm.gc", new GarbageCollectorMetricSet());
		registry.register("jvm.memory", new MemoryUsageGaugeSet());
		registry.register("jvm.threads", new ThreadStatesGaugeSet());

		return registry;
	}

}
