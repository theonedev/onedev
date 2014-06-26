package com.pmease.commons.wicket.jquery;

import de.agilecoders.wicket.jquery.JQuery.AbstractFunction;

@SuppressWarnings("serial")
public class FunctionWithParams extends AbstractFunction {

	public FunctionWithParams(String functionName, String...params) {
		super(functionName);
		
		for (String param: params) 
			addParameter(param);
	}

}
