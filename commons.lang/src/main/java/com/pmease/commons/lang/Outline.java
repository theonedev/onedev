package com.pmease.commons.lang;

import java.io.Serializable;
import java.util.List;

public interface Outline extends Serializable {
	List<LangToken> getSymbols();
}
