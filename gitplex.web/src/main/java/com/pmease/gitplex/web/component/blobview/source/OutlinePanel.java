package com.pmease.gitplex.web.component.blobview.source;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmease.commons.lang.Symbol;
import com.pmease.gitplex.core.GitPlex;

@SuppressWarnings("serial")
class OutlinePanel extends Panel {

	private final List<Symbol> symbols;
	
	public OutlinePanel(String id, List<Symbol> symbols) {
		super(id);
		this.symbols = symbols;
	}

	private List<Symbol> getChildSymbols(@Nullable Symbol parentSymbol) {
		List<Symbol> children = new ArrayList<>();
		for (Symbol symbol: symbols) {
			if (symbol.getParent() == parentSymbol)
				children.add(symbol);
		}
		return children;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		NestedTree<Symbol> tree;
		add(tree = new NestedTree<Symbol>("tree", new ITreeProvider<Symbol>() {

			@Override
			public void detach() {
			}

			@Override
			public Iterator<? extends Symbol> getRoots() {
				return getChildSymbols(null).iterator();
			}

			@Override
			public boolean hasChildren(Symbol symbol) {
				return !getChildSymbols(symbol).isEmpty();
			}

			@Override
			public Iterator<? extends Symbol> getChildren(Symbol symbol) {
				return getChildSymbols(symbol).iterator();
			}

			@Override
			public IModel<Symbol> model(Symbol symbol) {
				return Model.of(symbol);
			}
			
		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new HumanTheme());				
			}

			@Override
			protected Component newContentComponent(String id, IModel<Symbol> nodeModel) {
				Fragment fragment = new Fragment(id, "nodeFrag", OutlinePanel.this);
				final Symbol symbol = nodeModel.getObject();
				
				fragment.add(new Image("icon", symbol.getIcon()));
				
				WebMarkupContainer link = new WebMarkupContainer("link") {

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);

						String tokenPos;
						try {
							tokenPos = GitPlex.getInstance(ObjectMapper.class).writeValueAsString(symbol.getPos());
						} catch (JsonProcessingException e) {
							throw new RuntimeException(e);
						}
						String script = String.format("gitplex.sourceview.gotoSymbol('%s', %s);", 
								OutlinePanel.this.getMarkupId(), tokenPos);
						tag.put("onclick", script);
					}
					
				};
				link.add(symbol.render("label"));
				fragment.add(link);
				
				return fragment;
			}
			
		});		
		
		for (Symbol root: getChildSymbols(null))
			tree.expand(root);
		
		setOutputMarkupId(true);
	}
	
}
