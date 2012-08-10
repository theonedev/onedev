package com.pmease.commons.wicket;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupResourceStream;

public class MergedMarkupFragment implements IMarkupFragment {

	private List<MarkupElement> markupElements = new ArrayList<MarkupElement>();
	
	private IMarkupFragment fragment1, fragment2;
	
	public MergedMarkupFragment(IMarkupFragment fragment1, IMarkupFragment fragment2) {
		this.fragment1 = fragment1;
		this.fragment2 = fragment2;
		
		Iterator<MarkupElement> it1 = fragment1.iterator();
		while (it1.hasNext())
			markupElements.add(it1.next());
		
		Iterator<MarkupElement> it2 = fragment2.iterator();
		while (it2.hasNext())
			markupElements.add(it2.next());
	}
	
	@Override
	public Iterator<MarkupElement> iterator() {
		return markupElements.iterator();
	}

	@Override
	public MarkupElement get(int index) {
		return markupElements.get(index);
	}

	@Override
	public MarkupResourceStream getMarkupResourceStream() {
		return null;
	}

	@Override
	public int size() {
		return markupElements.size();
	}

	@Override
	public IMarkupFragment find(String id) {
		IMarkupFragment result = fragment1.find(id);
		if (result != null)
			return result;
		else
			return fragment2.find(id);
	}

	@Override
	public String toString(boolean markupOnly) {
		return fragment1.toString(markupOnly) + " " + fragment2.toString(markupOnly);
	}

}
