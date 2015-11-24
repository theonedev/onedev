package com.pmease.commons.antlr.codeassist;

public abstract class TokenElementSpec extends ElementSpec {

	private static final long serialVersionUID = 1L;
	
	protected final int type;

	public TokenElementSpec(CodeAssist codeAssist, String label, Multiplicity multiplicity, int type) {
		super(codeAssist, label, multiplicity);
		
		this.type = type;
	}

	/**
	 * Get token type of this element
	 * 
	 * @return
	 * 		token type of the element, 0 if the element refers to a fragment rule, -1 if the element 
	 * 		represents EOF
	 */
	public int getType() {
		return type;
	}

}
