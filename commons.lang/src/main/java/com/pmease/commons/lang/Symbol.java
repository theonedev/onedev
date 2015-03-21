package com.pmease.commons.lang;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

public abstract class Symbol {
	
	@Nullable
	public Symbol parent;
	
	@Nullable
	public String name;
	
	public int lineNo;

	public List<Symbol> children = new ArrayList<>();

	protected String getSearchable() {
		return name;
	}
	
	public abstract Component render(String componentId);
	
	public List<String> getSearchables() {
		List<String> searchables = new ArrayList<>();
		String searchable = getSearchable();
		if (searchable != null)
			searchables.add(searchable);
		for (Symbol child: children) 
			searchables.addAll(child.getSearchables());
		return searchables;
	}
	
	public List<Symbol> search(String searchFor, boolean exactMatch, boolean caseSensitive) {
		List<Symbol> matches = new ArrayList<>();
		String searchable = getSearchable();
		if (searchable != null) {
			if (exactMatch) {
				if (caseSensitive) {
					if (searchable.equals(searchFor))
						matches.add(this);
				} else if (searchable.toLowerCase().equals(searchFor.toLowerCase())) {
					matches.add(this);
				}
			} else {
				if (caseSensitive) {
					if (searchable.startsWith(searchFor))
						matches.add(this);
				} else if (searchable.toLowerCase().startsWith(searchFor.toLowerCase())) {
					matches.add(this);
				}
			}
		}
		for (Symbol child: children)
			matches.addAll(child.search(searchFor, exactMatch, caseSensitive));
		
		return matches;
	}

	@Override
	public String toString() {
		return lineNo + ":" + name;
	}
	
}
