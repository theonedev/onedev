package com.pmease.gitop.web.common.wicket.form;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.FeedbackMessagesModel;
import org.apache.wicket.markup.ComponentTag;

/**
 * Copied from https://github.com/55minutes/fiftyfive-wicket/blob/master/fiftyfive-wicket-core/src/main/java/fiftyfive/wicket/feedback/FeedbackStyle.java
 * 
 * Automatically adds an appropriate CSS feedback class to a component if that component or any of
 * its descendants have feedback messages.
 * <p>
 * For example, let's say our Java looks like this:
 * 
 * <pre class="example">
 * add(new RequiredTextField("username").add(FeedbackStyle.INSTANCE));</pre>
 * <p>
 * Our HTML is:
 * 
 * <pre class="example">
 * &lt;input type="text" wicket:id="username" /&gt;</pre>
 * <p>
 * Now, when our text field has a validation message to report, for example when the user submits
 * the form without filling in the required value, our component will render like this:
 * 
 * <pre class="example">
 * &lt;input type="text" class="feedbackPanelERROR" /&gt;</pre>
 * <p>
 * Notice how the {@code <input>} gains the appropriate CSS class.
 * 
 */
public class FeedbackStyle extends Behavior {
  private static final long serialVersionUID = 1L;
  
  public static final FeedbackStyle INSTANCE = new FeedbackStyle();

  @Override
  public void onComponentTag(Component c, ComponentTag tag) {
    List<FeedbackMessage> msgs = newFeedbackMessagesModel(c).getObject();
    if (msgs.size() > 0) {
      StringBuffer newClassValue = new StringBuffer(tag.getAttributes().getString("class", ""));
      Set<String> classes = new HashSet<String>();
      for (FeedbackMessage m : msgs) {
        String cssClass = getCssClass(m);
        if (classes.add(cssClass)) {
          if (newClassValue.length() > 0) {
            newClassValue.append(" ");
          }
          newClassValue.append(cssClass);
        }
      }
      tag.put("class", newClassValue.toString());
      tag.setModified(true);
    }
  }

  protected FeedbackMessagesModel newFeedbackMessagesModel(Component c) {
    FeedbackMessagesModel model = new FeedbackMessagesModel(c);
    if (c instanceof MarkupContainer) {
      model.setFilter(new ContainerFeedbackMessageFilter((MarkupContainer) c));
    } else {
      model.setFilter(new ComponentFeedbackMessageFilter(c));
    }
    return model;
  }

  protected String getCssClass(FeedbackMessage msg) {
    return "feedbackPanel" + msg.getLevelAsString();
  }
}
