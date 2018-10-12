package io.onedev.server.web.page.project.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.OneDev;
import io.onedev.server.git.Contribution;
import io.onedev.server.git.Contributor;
import io.onedev.server.manager.CommitInfoManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Constants;
import io.onedev.server.util.Day;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.avatar.AvatarLink;
import io.onedev.server.web.component.link.UserLink;

@SuppressWarnings("serial")
public class ProjectContribsPage extends ProjectStatsPage {

	private final IModel<List<DayContribution>> overallContributionsModel = 
			new LoadableDetachableModel<List<DayContribution>>() {

		@Override
		protected List<DayContribution> load() {
			Map<Day, Contribution> map = OneDev.getInstance(CommitInfoManager.class).getOverallContributions(getProject());
			return sort(map);
		}
		
	};

	private String fromDayText;
	
	private String toDayText;
	
	private int commits;
	
	private int additions;
	
	private int deletions;
	
	private String orderBy = Contribution.Type.COMMITS.name();
	
	private String [] Month = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	
	public ProjectContribsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onDetach() {
		overallContributionsModel.detach();
		super.onDetach();
	}

	private List<DayContribution> sort(Map<Day, Contribution> map) {
		List<DayContribution> list = new ArrayList<>();
		for (Map.Entry<Day, Contribution> entry: map.entrySet())
			list.add(new DayContribution(entry.getKey(), entry.getValue()));
		Collections.sort(list);
		return list;
	}
	
	private String getJson(List<?> list) {
		try {
			return OneDev.getInstance(ObjectMapper.class).writeValueAsString(list);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	
	private int CalculateCommits(List<DayContribution> list) {
		int commitsCount=0;
		for(DayContribution attribute : list) {
			  commitsCount+=attribute.getContribution().getCommits();
		}
		return commitsCount;
	}
	private int CalculateAdditions(List<DayContribution> list) {
		int additionsCount=0;
		for(DayContribution attribute : list) {
			additionsCount+=attribute.getContribution().getAdditions();
		}
		return additionsCount;
	}
	private int CalculateDeletions(List<DayContribution> list) {
		int deletionsCount=0;
		for(DayContribution attribute : list) {
			deletionsCount+=attribute.getContribution().getDeletions();
		}
		return deletionsCount;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		Map<Day, Contribution> map = OneDev.getInstance(CommitInfoManager.class).getOverallContributions(getProject());
		List<DayContribution> OverallContributions=sort(map);
		WebMarkupContainer timeZoomContainer=new WebMarkupContainer("timeZoomContainer");
		WebMarkupContainer overallContributorsContainer =new WebMarkupContainer("overallContributorsContainer");
		WebMarkupContainer topContributorsContainer =new WebMarkupContainer("topContributorsContainer");
	
		Fragment timeZoomfrag=new Fragment("timeZoom", "timeZoomFrag", ProjectContribsPage.this);
		timeZoomfrag.add(new Label("time",new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				Day fromDay;
				if (StringUtils.isNotBlank(fromDayText)) {
					fromDay = new Day(Constants.DATE_FORMATTER.parseDateTime(fromDayText));
				} else {
					fromDay = OverallContributions.get(0).getDay();
				}
				
				Day toDay;
				if (StringUtils.isNotBlank(toDayText)) {
					toDay = new Day(Constants.DATE_FORMATTER.parseDateTime(toDayText));
				} else {
					toDay = OverallContributions.get(OverallContributions.size()-1).getDay();
				}
				
				return (Month[fromDay.getDateTime().getMonthOfYear()-1]+" "+fromDay.getDateTime().getDayOfMonth()+","+fromDay.getDateTime().getYear()+"  -  "+Month[toDay.getDateTime().getMonthOfYear()-1]+" "+toDay.getDateTime().getDayOfMonth()+","+toDay.getDateTime().getYear());
			}
		}));

		
		timeZoomContainer.add(timeZoomfrag);
		timeZoomContainer.setOutputMarkupId(true);
		add(timeZoomContainer);
		List<String> orderByChoices = new ArrayList<>();
		for (Contribution.Type type: Contribution.Type.values())
			orderByChoices.add(type.name());
		DropDownChoice<String> choice=new DropDownChoice<String>("orderBy", new PropertyModel<String>(this, "orderBy"), orderByChoices) {
			
		};
		choice.add(new OnChangeAjaxBehavior() {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(overallContributorsContainer);
				target.add(topContributorsContainer); 
			}
		});
		add(choice);
		
		
		Fragment overallContributionfrag=new Fragment("overallContribution","overallContributionFrag",ProjectContribsPage.this);
		
		AbstractDefaultAjaxBehavior ondatazoom = new AbstractDefaultAjaxBehavior(){
		  @Override
		  protected void respond(AjaxRequestTarget _target)
		  {
		    String fromday = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("fromday").toString();
		    String today = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("today").toString();
		    fromDayText=fromday.replaceAll("/", "-");
		    toDayText=today.replaceAll("/", "-");
		    _target.add(topContributorsContainer);
		    _target.add(timeZoomContainer);
		  }
		  
			
		};
		AbstractDefaultAjaxBehavior onrestore=new AbstractDefaultAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				target.add(overallContributorsContainer);
			}
		};
		overallContributionfrag.add(ondatazoom);
		overallContributionfrag.add(onrestore);
		overallContributionfrag.add(new AbstractPostAjaxBehavior() {
			@Override
			protected void respond(AjaxRequestTarget target) {
			}
			@Override 
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				String jsonContributionList = getJson(OverallContributions);
				String script = String.format("onedev.server.stats.contribs.onDomReady(%s,'%s','%s','%s');",jsonContributionList,
						orderBy,fromDayText,toDayText
						) 
					;
				response.render(OnDomReadyHeaderItem.forScript(script));
				response.render(JavaScriptHeaderItem.forScript("function myFunction(fromday,today) {Wicket.Ajax.get({'u':'"+ ondatazoom.getCallbackUrl() +"&fromday=' + fromday+ '&today=' +today})}", "myFunction"));
				response.render(JavaScriptHeaderItem.forScript("function redraw(fromday,today) {Wicket.Ajax.get({'u':'"+ onrestore.getCallbackUrl() +"&fromday=' + fromday+ '&today=' +today})}", "redraw"));

			}
			
		});
		
		overallContributorsContainer.add(overallContributionfrag);
		overallContributorsContainer.setOutputMarkupId(true);
		add(overallContributorsContainer);
		
		topContributorsContainer.setOutputMarkupId(true);
		add(topContributorsContainer);
		topContributorsContainer.add(new ListView<Contributor>("topContributors", new LoadableDetachableModel<List<Contributor>>() {

			@Override
			protected List<Contributor> load() {
				List<DayContribution> overallContributions = overallContributionsModel.getObject();
				int size = overallContributions.size();
				if (size != 0) {
					Day fromDay;
					if (StringUtils.isNotBlank(fromDayText)) {
						fromDay = new Day(Constants.DATE_FORMATTER.parseDateTime(fromDayText));
					} else {
						fromDay = overallContributions.get(0).getDay();
					}
					
					Day toDay;
					if (StringUtils.isNotBlank(toDayText)) {
						toDay = new Day(Constants.DATE_FORMATTER.parseDateTime(toDayText));
					} else {
						toDay = overallContributions.get(size-1).getDay();
					}
					
					return OneDev.getInstance(CommitInfoManager.class)
							.getTopContributors(getProject(), 100, Contribution.Type.valueOf(orderBy), fromDay, toDay);
				} else {
					return new ArrayList<>();
				}
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Contributor> item) {
				Contributor userContribution = item.getModelObject();
				Fragment fragment = new Fragment("user", "userFrag", ProjectContribsPage.this);
				fragment.add(new AvatarLink("avatar", userContribution.getUser()));
				fragment.add(new UserLink("link", userContribution.getUser()));
				int i=item.getIndex();
				Map<Day,Contribution> dailyContributions=userContribution.getDailyContributions();
				List<DayContribution> userDailyContribution=sort(dailyContributions);

				commits=CalculateCommits(userDailyContribution);
			    additions=CalculateAdditions(userDailyContribution);
			    deletions=CalculateDeletions(userDailyContribution);
				fragment.add(new Label("commits",String.valueOf(commits)+"commits   "));
				fragment.add(new Label("additions",String.valueOf(additions)+"++"));
				fragment.add(new Label("deletions",String.valueOf(deletions)+"--"));
				fragment.add(new Label("No","#"+(i+1)));
				item.add(fragment);
				Fragment linefragment=new Fragment("userline", "userlineFrag", ProjectContribsPage.this);
				
				linefragment.add(new AbstractDefaultAjaxBehavior() {
					
					@Override
					protected void respond(AjaxRequestTarget target) {
					}
				
					@Override
					public void renderHead(Component component, IHeaderResponse response) {
						super.renderHead(component, response);
						String jsonUserDailyContribution=getJson(userDailyContribution);
						Day startDay,endDay;
					    startDay=userDailyContribution.get(0).getDay();
					    endDay=userDailyContribution.get(userDailyContribution.size()-1).getDay();
						int gapday=(int)((endDay.getDate().getTime()-startDay.getDate().getTime())/ (1000*3600*24));	
						String script = String.format(
								"onedev.server.stats.contribs.ondrawLinesReady('%d',%s,'%s','%d');",i,jsonUserDailyContribution,orderBy,gapday); 
						response.render(OnDomReadyHeaderItem.forScript(script));
					}
				});
				item.add(linefragment);
				linefragment.setOutputMarkupId(true);
			}
		});
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(getProject().getFacade());
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new ProjectStatsResourceReference()));
	}
}
