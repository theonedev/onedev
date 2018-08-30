package io.onedev.server.search.commit;

import java.io.Serializable;

class Revision implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public boolean since;
	
	public boolean until;
	
	public String value;
	
}