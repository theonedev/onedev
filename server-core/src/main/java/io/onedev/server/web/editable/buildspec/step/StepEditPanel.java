package io.onedev.server.web.editable.buildspec.step;

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
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.ImplementationRegistry;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.ParamSpecAware;
import io.onedev.server.buildspec.step.Step;
import io.onedev.server.util.match.MatchScoreProvider;
import io.onedev.server.util.match.MatchScoreUtils;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.asset.selectbytyping.SelectByTypingResourceReference;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.link.ViewStateAwareAjaxLink;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.EditableUtils;

@SuppressWarnings("serial")
abstract class StepEditPanel extends Panel implements BuildSpecAware, ParamSpecAware {

	private static final List<Class<? extends Step>> stepClasses = new ArrayList<>();
	
	static {
		ImplementationRegistry registry = AppLoader.getInstance(ImplementationRegistry.class);
		stepClasses.addAll(registry.getImplementations(Step.class));
		EditableUtils.sortAnnotatedElements(stepClasses);
	}
	
	private final List<Step> steps;
	
	private final int stepIndex;
	
	private Step step;
	
	public StepEditPanel(String id, List<Step> steps, int stepIndex) {
		super(id);
	
		this.steps = steps;
		this.stepIndex = stepIndex;
		
		if (stepIndex != -1)
			step = steps.get(stepIndex);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onError() {
				super.onError();
				RequestCycle.get().find(AjaxRequestTarget.class).add(this);
			}
			
		};
		
		form.add(new AjaxLink<Void>("close") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(StepEditPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		form.add(new DropdownLink("type") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Label("label", new LoadableDetachableModel<String>() {

					@Override
					protected String load() {
						if (step != null)
							return Step.getGroupedType(step.getClass());
						else
							return "Please select type";
					}
					
				}));
			}
			
			@Override
			protected void onInitialize(FloatingPanel dropdown) {
				super.onInitialize(dropdown);
				dropdown.add(AttributeAppender.append("class", "step-type-choice"));
			}
			
			private List<TreeNode> getNodes() {
				List<TreeNode> nodes = new ArrayList<>();
				
				Set<String> addedGroups = new HashSet<>();
				for (Class<? extends Step> stepClass: stepClasses) {
					String group = EditableUtils.getGroup(stepClass);
					if (group != null && addedGroups.add(group))
						nodes.add(new GroupNode(group));
					nodes.add(new StepNode(stepClass));
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
							StepNode stepNode = (StepNode) node;
							if (EditableUtils.getGroup(stepNode.stepClass) == null)
								childNodes.add(node);
						}
					}
				} else {
					for (TreeNode node: nodes) {
						if (node instanceof StepNode) {
							StepNode stepNode = (StepNode) node;
							if (parentNode.getName().equals(EditableUtils.getGroup(stepNode.stepClass)))
								childNodes.add(node);
						}
					}
				}
				return childNodes;
			}

			private NestedTree<TreeNode> newTree(FloatingPanel dropdown, List<TreeNode> nodes, Set<TreeNode> state) {
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
							Fragment fragment = new Fragment(id, "groupNodeFrag", StepEditPanel.this);
							
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
							Fragment fragment = new Fragment(id, "stepNodeFrag", StepEditPanel.this);
							
							StepNode stepNode = (StepNode) node;
							AjaxLink<Void> link = new ViewStateAwareAjaxLink<Void>("link") {

								@Override
								public void onClick(AjaxRequestTarget target) {
									try {
										Step newStep = stepNode.stepClass.newInstance();
										if (step != null) {
											newStep.setName(step.getName());
											newStep.setCondition(step.getCondition());
										}
										step = newStep;
										
										String description = getDescription();
										if (description != null)
											form.replace(new Label("description", description).setEscapeModelStrings(false));
										else
											form.replace(new WebMarkupContainer("description").setVisible(false));
										
										BeanEditor editor = BeanContext.edit("editor", step);
										form.replace(editor);
										target.add(form);
										
										String script = String.format("onedev.server.form.markDirty($('#%s'));", 
												form.getMarkupId());
										target.appendJavaScript(script);
									} catch (InstantiationException | IllegalAccessException e) {
										throw new RuntimeException(e);
									}
									dropdown.close();
								}
								
							};
							link.add(new Label("label", EditableUtils.getDisplayName(stepNode.stepClass)));
							
							for (TreeNode each: nodes) {
								if (each instanceof StepNode) {
									if (node.equals(each))
										link.add(AttributeAppender.append("class", "active"));
									break;
								}
							}
							fragment.add(link);
							
							String description = StringUtils.trimToNull(StringUtils.substringBefore(
									EditableUtils.getDescription(stepNode.stepClass), "."));
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
			protected Component newContent(String id, FloatingPanel dropdown) {
				Fragment fragment = new Fragment(id, "stepTypeChoiceFrag", StepEditPanel.this) {
					
					@Override
					public void renderHead(IHeaderResponse response) {
						super.renderHead(response);
						
						response.render(JavaScriptHeaderItem.forReference(new SelectByTypingResourceReference()));
						
						String script = String.format("$('#%s>input').selectByTyping('#%s>div');", 
								getMarkupId(), getMarkupId());
						response.render(OnDomReadyHeaderItem.forScript(script));
					}
					
				};
				
				TextField<String> searchField = new TextField<>("input", Model.of(""));
				fragment.add(searchField);
				
				searchField.add(new OnTypingDoneBehavior(100) {
					
					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.setChannel(new AjaxChannel("step-choice", AjaxChannel.Type.DROP));
					}

					@Override
					protected void onTypingDone(AjaxRequestTarget target) {
						String searchInput = StringUtils.trimToNull(searchField.getInput());
						
						List<TreeNode> nodes = getNodes();
						
						NestedTree<TreeNode> tree;
						if (searchInput != null) {
							MatchScoreProvider<TreeNode> matchScoreProvider = new MatchScoreProvider<TreeNode>() {

								@Override
								public double getMatchScore(TreeNode object) {
									String fullName = object.getName();
									if (object instanceof StepNode) {
										StepNode stepNode = (StepNode) object;
										String groupName = EditableUtils.getGroup(stepNode.stepClass);
										if (groupName != null)
											fullName = groupName + "/" + fullName;
									}
									return MatchScoreUtils.getMatchScore(
											StringUtils.deleteWhitespace(fullName), 
											StringUtils.deleteWhitespace(searchInput));
								}
								
							};
							
							List<TreeNode> matchNodes = MatchScoreUtils.filterAndSort(nodes, matchScoreProvider);
							
							List<TreeNode> filteredNodes = new ArrayList<>();
							
							Set<String> addedGroups = new HashSet<>();
							for (TreeNode matchNode: matchNodes) {
								if (matchNode instanceof StepNode) {
									StepNode currentNode = (StepNode) matchNode;
									filteredNodes.add(currentNode);
									String group = EditableUtils.getGroup(currentNode.stepClass);
									if (group != null && addedGroups.add(group)) 
										filteredNodes.add(new GroupNode(group));
								}
							}
							tree = newTree(dropdown, filteredNodes, new HashSet<>(filteredNodes));
						} else {
							tree = newTree(dropdown, nodes, new HashSet<>());
						}
						
						fragment.replace(tree);
						target.add(tree);
					}
					
				});	
				
				fragment.add(newTree(dropdown, getNodes(), new HashSet<>()));
				fragment.setOutputMarkupId(true);
				
				return fragment;
			}

		});
		
		String description = getDescription();
		if (description != null)
			form.add(new Label("description", description).setEscapeModelStrings(false));
		else
			form.add(new WebMarkupContainer("description").setVisible(false));
		
		if (step != null) 
			form.add(BeanContext.edit("editor", step));
		else
			form.add(new WebMarkupContainer("editor").setOutputMarkupPlaceholderTag(true).setVisible(false));
		
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				if (stepIndex != -1) 
					steps.set(stepIndex, step);
				else 
					steps.add(step);
				
				onSave(target);
			}
			
		});
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(StepEditPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		form.setOutputMarkupId(true);
		
		add(form);
	}
	
	private String getDescription() {
		if (step != null) {
			return StringUtils.trimToNull(StringUtils.stripStart(
					EditableUtils.getDescription(step.getClass()), "."));
		} else {
			return null;
		}
	}
	
	protected abstract void onSave(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);

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
	
	private static class StepNode implements TreeNode {
		
		private final Class<? extends Step> stepClass;
		
		public StepNode(Class<? extends Step> stepClass) {
			this.stepClass = stepClass;
		}

		@Override
		public String getName() {
			return EditableUtils.getDisplayName(stepClass);
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			} else if (other instanceof StepNode) {
				StepNode otherNode = (StepNode) other;
				return new EqualsBuilder().append(stepClass, otherNode.stepClass).isEquals();
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			if (getName() == null)
				return super.hashCode();
			else
				return new HashCodeBuilder(17, 37).append(stepClass).toHashCode();
		}
		
	}
}
