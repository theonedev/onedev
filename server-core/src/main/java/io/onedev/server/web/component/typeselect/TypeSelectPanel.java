package io.onedev.server.web.component.typeselect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.ImplementationRegistry;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.util.Similarities;
import io.onedev.server.web.asset.selectbytyping.SelectByTypingResourceReference;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.link.ViewStateAwareAjaxLink;
import io.onedev.server.web.editable.EditableUtils;

@SuppressWarnings("serial")
public abstract class TypeSelectPanel<T extends Serializable> extends Panel {

	private final List<Class<? extends T>> types;
	
	@SuppressWarnings("unchecked")
	public TypeSelectPanel(String id) {
		super(id);
		
		List<Class<?>> typeArguments = ReflectionUtils.getTypeArguments(TypeSelectPanel.class, getClass());
		Class<? extends T> baseType = (Class<T>) typeArguments.get(0);
		
		ImplementationRegistry registry = AppLoader.getInstance(ImplementationRegistry.class);
		types = new ArrayList<>(registry.getImplementations(baseType));
		EditableUtils.sortAnnotatedElements(types);
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
				attributes.setChannel(new AjaxChannel("type-choice", AjaxChannel.Type.DROP));
			}

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				String searchInput = StringUtils.trimToNull(searchField.getInput());
				
				List<TreeNode> nodes = getNodes();
				
				NestedTree<TreeNode> tree;
				if (searchInput != null) {
					List<TreeNode> similarNodes = new Similarities<TreeNode>(nodes) {

						@Override
						protected double getSimilarScore(TreeNode item) {
							String fullName = item.getName();
							if (item instanceof TypeNode) {
								TypeNode<?> typeNode = (TypeNode<?>) item;
								String groupName = EditableUtils.getGroup(typeNode.type);
								if (groupName != null)
									fullName = groupName + "/" + fullName;
							}
							return Similarities.getSimilarScore(
									StringUtils.deleteWhitespace(fullName), 
									StringUtils.deleteWhitespace(searchInput));
						}
						
					};
					
					List<TreeNode> filteredNodes = new ArrayList<>();
					
					Set<String> addedGroups = new HashSet<>();
					for (TreeNode matchNode: similarNodes) {
						if (matchNode instanceof TypeNode) {
							@SuppressWarnings("unchecked")
							TypeNode<T> currentNode = (TypeNode<T>) matchNode;
							filteredNodes.add(currentNode);
							String group = EditableUtils.getGroup(currentNode.type);
							if (group != null && addedGroups.add(group)) 
								filteredNodes.add(new GroupNode(group));
						}
					}
					tree = newTree(filteredNodes, new HashSet<>(filteredNodes));
				} else {
					tree = newTree(nodes, new HashSet<>());
				}
				
				replace(tree);
				target.add(tree);
			}
			
		});	
		
		add(newTree(getNodes(), new HashSet<>()));
		setOutputMarkupId(true);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<TreeNode> getNodes() {
		List<TreeNode> nodes = new ArrayList<>();
		
		Set<String> addedGroups = new HashSet<>();
		for (Class<? extends T> type: types) {
			String group = EditableUtils.getGroup(type);
			if (group != null && addedGroups.add(group))
				nodes.add(new GroupNode(group));
			nodes.add(new TypeNode(type));
		}
		
		return nodes;
	}
	
	protected List<TreeNode> getChildNodes(List<TreeNode> nodes, TreeNode parentNode) {
		List<TreeNode> childNodes = new ArrayList<>();
		if (parentNode == null) {
			for (TreeNode node: nodes) {
				if (node instanceof GroupNode) {
					childNodes.add(node);
				} else {
					@SuppressWarnings("unchecked")
					TypeNode<T> typeNode = (TypeNode<T>) node;
					if (EditableUtils.getGroup(typeNode.type) == null)
						childNodes.add(node);
				}
			}
		} else {
			for (TreeNode node: nodes) {
				if (node instanceof TypeNode) {
					@SuppressWarnings("unchecked")
					TypeNode<T> typeNode = (TypeNode<T>) node;
					if (parentNode.getName().equals(EditableUtils.getGroup(typeNode.type)))
						childNodes.add(node);
				}
			}
		}
		return childNodes;
	}

	private NestedTree<TreeNode> newTree(List<TreeNode> nodes, Set<TreeNode> state) {
		NestedTree<TreeNode> tree = new NestedTree<TreeNode>("content", new ITreeProvider<TreeNode>() {

				@Override
				public void detach() {
				}

				@Override
				public Iterator<? extends TreeNode> getRoots() {
					return getChildNodes(nodes, null).iterator();
				}

				@Override
				public boolean hasChildren(TreeNode node) {
					return node instanceof GroupNode;
				}

				@Override
				public Iterator<? extends TreeNode> getChildren(TreeNode node) {
					return getChildNodes(nodes, node).iterator();
				}

				@Override
				public IModel<TreeNode> model(TreeNode object) {
					return Model.of(object);
				}
				
		}, Model.ofSet(state)) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new HumanTheme());				
			}

			@Override
			protected Component newContentComponent(String id, IModel<TreeNode> nodeModel) {
				TreeNode node = nodeModel.getObject();
				
				if (node instanceof GroupNode) {
					Fragment fragment = new Fragment(id, "groupNodeFrag", TypeSelectPanel.this);
					
					AjaxLink<Void> link = new ViewStateAwareAjaxLink<Void>("link") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							if (getState(node) == State.COLLAPSED)
								expand(node);
							else
								collapse(node);
						}
						
					};
					link.add(new Label("label", node.getName()));
					fragment.add(link);
					
					return fragment;
				} else {
					Fragment fragment = new Fragment(id, "typeNodeFrag", TypeSelectPanel.this);
					
					@SuppressWarnings("unchecked")
					TypeNode<T> typeNode = (TypeNode<T>) node;
					AjaxLink<Void> link = new ViewStateAwareAjaxLink<Void>("link") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							onSelect(target, typeNode.type);
						}
						
					};
					link.add(new Label("label", EditableUtils.getDisplayName(typeNode.type)));
					
					for (TreeNode each: nodes) {
						if (each instanceof TypeNode) {
							if (node.equals(each))
								link.add(AttributeAppender.append("class", "active"));
							break;
						}
					}
					fragment.add(link);
					
					String description = StringUtils.trimToNull(StringUtils.substringBefore(
							EditableUtils.getDescription(typeNode.type), "."));
					if (description != null) 
						link.add(new Label("description", description).setEscapeModelStrings(false));
					else 
						link.add(new WebMarkupContainer("description").setVisible(false));
					
					return fragment;
				}
			}
			
		};		

		tree.setOutputMarkupId(true);
		
		return tree;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new TypeSelectCssResourceReference()));
		response.render(JavaScriptHeaderItem.forReference(new SelectByTypingResourceReference()));
		
		String script = String.format("$('#%s>.type-select>input').selectByTyping('#%s>.type-select>div');", 
				getMarkupId(), getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
	protected abstract void onSelect(AjaxRequestTarget target, Class<? extends T> type);
	
	private static interface TreeNode extends Serializable {
		
		String getName();
		
	}

	private static class GroupNode implements TreeNode {
		
		private final String name;
		
		public GroupNode(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			} else if (other instanceof GroupNode) {
				GroupNode otherNode = (GroupNode) other;
				return new EqualsBuilder().append(getName(), otherNode.getName()).isEquals();
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			if (getName() == null)
				return super.hashCode();
			else
				return new HashCodeBuilder(17, 37).append(getName()).toHashCode();
		}
		
	}
	
	private static class TypeNode<T> implements TreeNode {
		
		private final Class<T> type;
		
		public TypeNode(Class<T> type) {
			this.type = type;
		}

		@Override
		public String getName() {
			return EditableUtils.getDisplayName(type);
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			} else if (other instanceof TypeNode) {
				@SuppressWarnings("unchecked")
				TypeNode<T> otherNode = (TypeNode<T>) other;
				return new EqualsBuilder().append(type, otherNode.type).isEquals();
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			if (getName() == null)
				return super.hashCode();
			else
				return new HashCodeBuilder(17, 37).append(type).toHashCode();
		}
		
	}
}
