package io.onedev.server.web.component.filterabletree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.web.behavior.OnTypingDoneBehavior;

@SuppressWarnings("serial")
public abstract class FilterableTreePanel<T extends Serializable> extends GenericPanel<T> {

	public FilterableTreePanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField = new TextField<>("input", Model.of(""));
		add(searchField);
		
		searchField.add(new OnTypingDoneBehavior(100) {
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setChannel(new AjaxChannel("tree-filter", AjaxChannel.Type.DROP));
			}

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				String searchInput = searchField.getInput();
				
				List<T> matchNodes = getNodes(searchInput);
				
				List<T> filteredNodes = new ArrayList<>();
				for (T matchNode: matchNodes) {
					T currentNode = matchNode;
					while (currentNode != null) {
						if (!filteredNodes.contains(currentNode))
							filteredNodes.add(currentNode);
						currentNode = getParentNode(currentNode);
					}
				}
				
				NestedTree<T> tree = newTree(filteredNodes, searchInput);
				replace(tree);
				target.add(tree);
			}
			
		});	
		
		add(newTree(getNodes(null), null));
		
		setOutputMarkupId(true);
	}

	private ITreeProvider<T> newTreeProvider(List<T> nodes) {
		return new ITreeProvider<T>() {

			@Override
			public void detach() {
			}

			@Override
			public Iterator<? extends T> getRoots() {
				return getChildNodes(nodes, null).iterator();
			}

			@Override
			public boolean hasChildren(T node) {
				return !getChildNodes(nodes, node).isEmpty();
			}

			@Override
			public Iterator<? extends T> getChildren(T node) {
				return getChildNodes(nodes, node).iterator();
			}

			@Override
			public IModel<T> model(T object) {
				return Model.of(object);
			}
			
		};		
	}
	
	private NestedTree<T> newTree(List<T> nodes, @Nullable String matchWith) {
		IModel<HashSet<T>> state;
		
		if (matchWith != null) 
			state = new Model<HashSet<T>>(new HashSet<>(nodes));
		else 
			state = new Model<HashSet<T>>(new HashSet<>(getChildNodes(nodes, null)));
		
		NestedTree<T> tree = new NestedTree<T>("content", newTreeProvider(nodes), state) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new HumanTheme());				
			}

			@Override
			protected Component newContentComponent(String id, IModel<T> nodeModel) {
				return renderNode(nodes, nodeModel.getObject());
			}
			
		};		
		
		tree.setOutputMarkupId(true);
		
		return tree;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		String script = String.format("$('#%s>.filterable-tree>input').selectByTyping('#%s>.filterable-tree>div');", 
				getMarkupId(), getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	protected abstract List<T> getNodes(@Nullable String matchWith);
	
	protected abstract List<T> getChildNodes(List<T> nodes, @Nullable T parentNode);
	
	@Nullable
	protected abstract T getParentNode(T childNode);
	
	protected abstract Component renderNode(List<T> nodes, T node);
	
}
