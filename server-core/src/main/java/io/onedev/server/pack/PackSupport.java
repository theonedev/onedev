package io.onedev.server.pack;

import io.onedev.server.model.Pack;
import org.apache.wicket.Component;

import java.util.Comparator;

public interface PackSupport {
	
	int getOrder();
	
	String getPackType();
	
	String getPackIcon();
	
	Component render(String componentId, Pack pack);
	
}
