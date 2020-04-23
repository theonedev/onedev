package io.onedev.server.util.usage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class Usage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final List<String> places = new ArrayList<>();
	
	public List<String> getPlaces() {
		return places;
	}
	
	public Usage add(String place) {
		places.add(place);
		return this;
	}
	
	public Usage add(Usage usage) {
		places.addAll(usage.getPlaces());
		return this;
	}
	
	public Usage prefix(String place) {
		for (int i=0; i<places.size(); i++) 
			places.set(i, place + ": " + places.get(i));
		return this;
	}
	
	@Nullable
	public String getInUseMessage(String thing) {
		if (!places.isEmpty()) {
			StringBuilder builder = new StringBuilder(thing + " is still being used in below places:\n");
			for (String place: places) 
				builder.append("    " + place).append("\n");
			String message = builder.toString();
			return message.substring(0, message.length()-1);
		} else {
			return null;
		}
	}
	
	public void checkInUse(String thing) {
		String inUseMessage = getInUseMessage(thing);
		if (inUseMessage != null)
			throw new InUseException(inUseMessage);
	}
	
}
