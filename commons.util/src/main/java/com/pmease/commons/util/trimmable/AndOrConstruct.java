package com.pmease.commons.util.trimmable;

import java.util.List;

public interface AndOrConstruct {

	Trimmable getSelf();
	
	List<? extends Trimmable> getMembers();
	
}
