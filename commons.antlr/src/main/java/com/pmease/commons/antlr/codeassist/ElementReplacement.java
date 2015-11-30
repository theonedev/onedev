package com.pmease.commons.antlr.codeassist;

class ElementReplacement {
	
	// represent the node to be suggested at current caret
	Node node; 
	
	// represent content of the node to be suggested
	String content;  
	
	// represent start position of node in current input
	int begin; 
	
	/*
	 *  represent end position of node in current input, or take value of caret if
	 *  current input does not match the node spec around caret. Text between 
	 *  "begin" and "end" in current input will be replaced by "content" (and 
	 *  append possible mandatories literals).  
	 */
	int end; 
	
	// caret position in suggested content
	int caret;
	
	// text to be displayed to user for this suggested content  
	String label;
	
	// description to be displayed to user for this suggested content
	String description;

}