package io.onedev.server.ai;

import java.util.Arrays;
import java.util.stream.Collectors;

import io.onedev.server.OneDev;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.LabelSpec;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.issue.field.spec.BooleanField;
import io.onedev.server.model.support.issue.field.spec.DateField;
import io.onedev.server.model.support.issue.field.spec.DateTimeField;
import io.onedev.server.model.support.issue.field.spec.GroupChoiceField;
import io.onedev.server.model.support.issue.field.spec.IntegerField;
import io.onedev.server.model.support.issue.field.spec.choicefield.ChoiceField;
import io.onedev.server.model.support.issue.field.spec.userchoicefield.UserChoiceField;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.service.BuildParamService;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.LabelSpecService;
import io.onedev.server.service.LinkSpecService;
import io.onedev.server.service.SettingService;

public class QueryDescriptions {
    
    private static SettingService getSettingService() {
        return OneDev.getInstance(SettingService.class);
    }

    private static LinkSpecService getLinkSpecService() {
        return OneDev.getInstance(LinkSpecService.class);
    }

    private static LabelSpecService getLabelSpecService() {
        return OneDev.getInstance(LabelSpecService.class);
    }

    private static BuildService getBuildService() {
        return OneDev.getInstance(BuildService.class);
    }

    private static BuildParamService getBuildParamService() {
        return OneDev.getInstance(BuildParamService.class);
    }
    
    public static String getIssueQueryDescription() {
        var settingService = getSettingService();
        var linkSpecService = getLinkSpecService();
        
        var stateNames = new StringBuilder();
        for (var state: settingService.getIssueSetting().getStateSpecs()) {
            stateNames.append("  - ");
            stateNames.append(state.getName());
            if (state.getDescription() != null) {
                stateNames.append(": ").append(state.getDescription().replace("\n", " "));
            }
            stateNames.append("\n");
        }
        var fieldCriterias = new StringBuilder();
        for (var field: settingService.getIssueSetting().getFieldSpecs()) {
            if (field instanceof ChoiceField) {
                var choiceField = (ChoiceField) field;
                fieldCriterias.append("- " + field.getName().toLowerCase() + " criteria in form of: \""
                        + field.getName() + "\" is \"<" + field.getName().toLowerCase()
                        + " value>\" (quotes are required), where <" + field.getName().toLowerCase()
                        + " value> is one of below:\n");
                for (var choice : choiceField.getPossibleValues())
                    fieldCriterias.append("  - " + choice).append("\n");
            } else if (field instanceof UserChoiceField) {
                fieldCriterias.append("- " + field.getName().toLowerCase() + " criteria in form of: \""
                        + field.getName() + "\" is \"<login name of a user>\" (quotes are required)\n");
                fieldCriterias.append(
                        "- " + field.getName().toLowerCase() + " criteria for current user in form of: \""
                                + field.getName() + "\" is me (quotes are required)\n");
            } else if (field instanceof GroupChoiceField) {
                fieldCriterias.append("- " + field.getName().toLowerCase() + " criteria in form of: \""
                        + field.getName() + "\" is \"<group name>\" (quotes are required)\n");
            } else if (field instanceof BooleanField) {
                fieldCriterias.append("- " + field.getName().toLowerCase() + " is true criteria in form of: \""
                        + field.getName() + "\" is \"true\" (quotes are required)\n");
                fieldCriterias.append("- " + field.getName().toLowerCase() + " is false criteria in form of: \""
                        + field.getName() + "\" is \"false\" (quotes are required)\n");
            } else if (field instanceof DateField) {
                fieldCriterias.append("- " + field.getName().toLowerCase()
                        + " is before certain date criteria in form of: \"" + field.getName()
                        + "\" is before \"<date>\" (quotes are required), where <date> is of format YYYY-MM-DD\n");
                fieldCriterias.append("- " + field.getName().toLowerCase()
                        + " is after certain date criteria in form of: \"" + field.getName()
                        + "\" is after \"<date>\" (quotes are required), where <date> is of format YYYY-MM-DD\n");
            } else if (field instanceof DateTimeField) {
                fieldCriterias.append("- " + field.getName().toLowerCase()
                        + " is before certain date time criteria in form of: \"" + field.getName()
                        + "\" is before \"<date time>\" (quotes are required), where <date time> is of format YYYY-MM-DD HH:mm\n");
                fieldCriterias.append("- " + field.getName().toLowerCase()
                        + " is after certain date time criteria in form of: \"" + field.getName()
                        + "\" is after \"<date time>\" (quotes are required), where <date time> is of format YYYY-MM-DD HH:mm\n");
            } else if (field instanceof IntegerField) {
                fieldCriterias.append("- " + field.getName().toLowerCase()
                        + " is equal to certain integer criteria in form of: \"" + field.getName()
                        + "\" is \"<integer>\" (quotes are required), where <integer> is an integer\n");
                fieldCriterias.append("- " + field.getName().toLowerCase()
                        + " is greater than certain integer criteria in form of: \"" + field.getName()
                        + "\" is greater than \"<integer>\" (quotes are required), where <integer> is an integer\n");
                fieldCriterias.append("- " + field.getName().toLowerCase()
                        + " is less than certain integer criteria in form of: \"" + field.getName()
                        + "\" is less than \"<integer>\" (quotes are required), where <integer> is an integer\n");
            }
            fieldCriterias.append("- " + field.getName().toLowerCase() + " is not set criteria in form of: \""
                    + field.getName() + "\" is empty (quotes are required)\n");
        }
        var linkCriterias = new StringBuilder();
        for (var linkSpec: linkSpecService.query()) {
            linkCriterias.append("- criteria to list issues with any " + linkSpec.getName().toLowerCase()
                    + " matching certain criteria in form of: any \"" + linkSpec.getName()
                    + "\" matching(another criteria) (quotes are required)\n");
            linkCriterias.append("- criteria to list issues with all " + linkSpec.getName().toLowerCase()
                    + " matching certain criteria in form of: all \"" + linkSpec.getName()
                    + "\" matching(another criteria) (quotes are required)\n");
            linkCriterias.append("- criteria to list issues with some " + linkSpec.getName().toLowerCase()
                    + " in form of: has any \"" + linkSpec.getName() + "\" (quotes are required)\n");
            if (linkSpec.getOpposite() != null) {
                linkCriterias.append("- criteria to list issues with any "
                        + linkSpec.getOpposite().getName().toLowerCase()
                        + " matching certain criteria in form of: any \"" + linkSpec.getOpposite().getName()
                        + "\" matching(another criteria) (quotes are required)\n");
                linkCriterias.append("- criteria to list issues with all "
                        + linkSpec.getOpposite().getName().toLowerCase()
                        + " matching certain criteria in form of: all \"" + linkSpec.getOpposite().getName()
                        + "\" matching(another criteria) (quotes are required)\n");
                linkCriterias.append("- criteria to list issues with some " + linkSpec.getOpposite().getName().toLowerCase()
                        + " in form of: has any \"" + linkSpec.getOpposite().getName() + "\" (quotes are required)\n");
            }
        }
        var orderFields = new StringBuilder();
        for (var field: Issue.SORT_FIELDS.keySet()) {
            orderFields.append("- ").append(field).append("\n");
        }

        var description = 
                "A query string is one of below criteria:\n\n" +
                "- issue with specified number in form of: \"Number\" is \"#<issue number>\", or in form of: \"Number\" is \"<project key>-<issue number>\" (quotes are required)\n" +
                "- state criteria in form of: \"State\" is \"<state name>\" (quotes are required), where <state name> is one of below:\n" +
                stateNames + 
                fieldCriterias + 
                linkCriterias + 
                "- submitted by specified user criteria in form of: submitted by \"<login name of a user>\" (quotes are required)\n" +
                "- submitted by current user criteria in form of: submitted by me (quotes are required)\n" +
                "- submitted before certain date criteria in form of: \"Submit Date\" is until \"<date>\" (quotes are required), where <date> is of format YYYY-MM-DD HH:mm\n" +
                "- submitted after certain date criteria in form of: \"Submit Date\" is since \"<date>\" (quotes are required), where <date> is of format YYYY-MM-DD HH:mm\n" +
                "- updated before certain date criteria in form of: \"Last Activity Date\" is until \"<date>\" (quotes are required), where <date> is of format YYYY-MM-DD HH:mm\n" +
                "- updated after certain date criteria in form of: \"Last Activity Date\" is since \"<date>\" (quotes are required), where <date> is of format YYYY-MM-DD HH:mm\n" +
                "- confidential criteria in form of: confidential\n" +
                "- iteration criteria in form of: \"Iteration\" is \"<iteration name>\" (quotes are required)\n" +
                "- title contains specified text criteria in form of: \"Title\" contains \"<containing text>\" (quotes are required)\n" +
                "- description contains specified text criteria in form of: \"Description\" contains \"<containing text>\" (quotes are required)\n" +
                "- comment contains specified text criteria in form of: \"Comment\" contains \"<containing text>\" (quotes are required)\n" +
                "- project criteria in form of: \"Project\" is \"<project path pattern>\" (quotes are required)\n" +
                "- and criteria in form of <criteria1> and <criteria2>\n" +
                "- or criteria in form of <criteria1> or <criteria2>\n" +
                "- operator 'and' takes precedence over 'or' when used together, unless parentheses are used to group 'or' criterias\n" +
                "- not criteria in form of not(<criteria>)\n" +
                "\n" +
                "And can optionally add order clause at end of query string in form of: order by \"<field1>\" <asc|desc>,\"<field2>\" <asc|desc>,... (quotes are required), where <field> is one of below:\n\n" +
                orderFields + "\n" +
                "Issue, build or pull request can be referenced by their number in form of: #<number>, <project path>#<number> or <project key>-<number>\n" +
                "\n" + 
                "Leave empty to list all accessible issues";

        return description;
    }

    public static String getPullRequestQueryDescription() {
        var labelSpecService = getLabelSpecService();
        
        var orderFields = new StringBuilder();
        for (var field : PullRequest.SORT_FIELDS.keySet()) {
            orderFields.append("- ").append(field).append("\n");
        }

        var labelNames = labelSpecService.query().stream().map(LabelSpec::getName).collect(Collectors.joining(", "));
        var mergeStrategyNames = Arrays.stream(MergeStrategy.values()).map(MergeStrategy::name).collect(Collectors.joining(", "));

        var description = 
                "A query string is one of below criteria:\n\n" +
                "- pull request with specified number in form of: \"Number\" is \"#<pull request number>\", or in form of: \"Number\" is \"<project key>-<pull request number>\" (quotes are required)\n" +
                "- open criteria in form of: open\n" +
                "- merged criteria in form of: merged\n" +
                "- discarded criteria in form of: discarded\n" +
                "- source branch criteria in form of: \"Source Branch\" is \"<branch name>\" (quotes are required)\n" +
                "- target branch criteria in form of: \"Target Branch\" is \"<branch name>\" (quotes are required)\n" +
                "- merge strategy criteria in form of: \"Merge Strategy\" is \"<merge strategy>\" (quotes are required), where <merge strategy> is one of: " + mergeStrategyNames + "\n" +
                "- label criteria in form of: \"Label\" is \"<label name>\" (quotes are required), where <label name> is one of: " + labelNames + "\n" +
                "- ready to merge criteria in form of: ready to merge\n" +
                "- waiting for someone to review criteria in form of: has pending reviews\n" +
                "- some builds are unsuccessful criteria in form of: has unsuccessful builds\n" +
                "- some builds are not finished criteria in form of: has unfinished builds\n" +
                "- has merge conflicts criteria in form of: has merge conflicts\n" +
                "- assigned to specified user criteria in form of: assigned to \"<login name of a user>\" (quotes are required)\n" +
                "- approved by specified user criteria in form of: approved by \"<login name of a user>\" (quotes are required)\n" +
                "- to be reviewed by specified user criteria in form of: to be reviewed by \"<login name of a user>\" (quotes are required)\n" +
                "- to be changed by specified user criteria in form of: to be changed by \"<login name of a user>\" (quotes are required)\n" +
                "- to be merged by specified user criteria in form of: to be merged by \"<login name of a user>\" (quotes are required)\n" +
                "- requested for changes by specified user in form of: requested for changes by \"<login name of a user>\" (quotes are required)\n" +
                "- need action of specified user criteria in form of: need action by \"<login name of a user>\" (quotes are required)\n" +
                "- assigned to current user criteria in form of: assigned to me\n" +
                "- approved by current user criteria in form of: approved by me\n" +
                "- to be reviewed by current user criteria in form of: to be reviewed by me\n" +
                "- to be changed by current user criteria in form of: to be changed by me\n" +
                "- to be merged by current user criteria in form of: to be merged by me\n" +
                "- requested for changes by current user in form of: requested for changes by me\n" +
                "- requested for changes by any user criteria in form of: someone requested for changes\n" +
                "- need action of current user criteria in form of: need my action\n" +
                "- submitted by specified user criteria in form of: submitted by \"<login name of a user>\" (quotes are required)\n" +
                "- submitted by current user criteria in form of: submitted by me (quotes are required)\n" +
                "- submitted before certain date criteria in form of: \"Submit Date\" is until \"<date>\" (quotes are required), where <date> is of format YYYY-MM-DD HH:mm\n" +
                "- submitted after certain date criteria in form of: \"Submit Date\" is since \"<date>\" (quotes are required), where <date> is of format YYYY-MM-DD HH:mm\n" +
                "- updated before certain date criteria in form of: \"Last Activity Date\" is until \"<date>\" (quotes are required), where <date> is of format YYYY-MM-DD HH:mm\n" +
                "- updated after certain date criteria in form of: \"Last Activity Date\" is since \"<date>\" (quotes are required), where <date> is of format YYYY-MM-DD HH:mm\n" +
                "- closed (merged or discarded) before certain date criteria in form of: \"Close Date\" is until \"<date>\" (quotes are required), where <date> is of format YYYY-MM-DD HH:mm\n" +
                "- closed (merged or discarded) after certain date criteria in form of: \"Close Date\" is since \"<date>\" (quotes are required), where <date> is of format YYYY-MM-DD HH:mm\n" +
                "- includes specified issue criteria in form of: includes issue \"<issue reference>\" (quotes are required)\n" +
                "- includes specified commit criteria in form of: includes commit \"<commit hash>\" (quotes are required)\n" +                
                "- title contains specified text criteria in form of: \"Title\" contains \"<containing text>\" (quotes are required)\n" +
                "- description contains specified text criteria in form of: \"Description\" contains \"<containing text>\" (quotes are required)\n" +
                "- comment contains specified text criteria in form of: \"Comment\" contains \"<containing text>\" (quotes are required)\n" +
                "- project criteria in form of: \"Project\" is \"<project path pattern>\" (quotes are required)\n" +
                "- and criteria in form of <criteria1> and <criteria2>\n" +
                "- or criteria in form of <criteria1> or <criteria2>\n" +
                "- operator 'and' takes precedence over 'or' when used together, unless parentheses are used to group 'or' criterias\n" +
                "- not criteria in form of not(<criteria>)\n" +
                "\n" +
                "And can optionally add order clause at end of query string in form of: order by \"<field1>\" <asc|desc>,\"<field2>\" <asc|desc>,... (quotes are required), where <field> is one of below:\n\n" +
                orderFields + "\n" +
                "Issue, build or pull request can be referenced by their number in form of: #<number>, <project path>#<number> or <project key>-<number>\n" +
                "\n" +
                "Leave empty to list all accessible pull requests";

        return description;
    }

    public static String getBuildQueryDescription() {
        var buildService = getBuildService();
        var buildParamService = getBuildParamService();
        var labelSpecService = getLabelSpecService();
        
        var orderFields = new StringBuilder();
        for (var field : Build.SORT_FIELDS.keySet()) {
            orderFields.append("- ").append(field).append("\n");
        }

        var jobNames = buildService.getJobNames(null).stream().collect(Collectors.joining(", "));
        var paramNames = buildParamService.getParamNames(null).stream().collect(Collectors.joining(", "));
        var labelNames = labelSpecService.query().stream().map(LabelSpec::getName).collect(Collectors.joining(", "));

        var description = 
                "A query string is one of below criteria:\n\n" +
                "- build with specified number in form of: \"Number\" is \"#<build number>\", or in form of: \"Number\" is \"<project key>-<build number>\" (quotes are required)\n" +
                "- criteria to check if version/job contains specified text in form of: ~<containing text>~\n" +
                "- sucessful criteria in form of: sucessful\n" +
                "- failed criteria in form of: failed\n" +
                "- cancelled criteria in form of: cancelled\n" +
                "- timed out criteria in form of: timed out\n" +
                "- finished criteria in form of: finished\n" +
                "- running criteria in form of: running\n" +
                "- waiting criteria in form of: waiting\n" +
                "- pending criteria in form of: pending\n" +
                "- submitted by specified user criteria in form of: submitted by \"<login name of a user>\" (quotes are required)\n" +
                "- submitted by current user criteria in form of: submitted by me (quotes are required)\n" +
                "- cancelled by specified user criteria in form of: cancelled by \"<login name of a user>\" (quotes are required)\n" +
                "- cancelled by current user criteria in form of: cancelled by me (quotes are required)\n" +
                "- depends on specified build criteria in form of: depends on \"<build reference>\" (quotes are required)\n" +
                "- dependencies of specified build criteria in form of: dependencies of \"<build reference>\" (quotes are required)\n" +
                "- fixed specified issue criteria in form of: fixed issue \"<issue reference>\" (quotes are required)\n" +
                "- job criteria in form of: \"Job\" is \"<job name>\" (quotes are required), where <job name> is one of: " + jobNames + "\n" +
                "- version criteria in form of: \"Version\" is \"<version>\" (quotes are required)\n" +                
                "- branch criteria in form of: \"Branch\" is \"<branch name>\" (quotes are required)\n" +
                "- tag criteria in form of: \"Tag\" is \"<tag name>\" (quotes are required)\n" +
                "- param criteria in form of: \"<param name>\" is \"<param value>\" (quotes are required), where <param name> is one of: " + paramNames + "\n" +
                "- label criteria in form of: \"Label\" is \"<label name>\" (quotes are required), where <label name> is one of: " + labelNames + "\n" +
                "- pull request criteria in form of: \"Pull Request\" is \"<pull request reference>\" (quotes are required)\n" +
                "- commit criteria in form of: \"Commit\" is \"<commit hash>\" (quotes are required)\n" +
                "- before certain date criteria in form of: \"Submit Date\" is until \"<date>\" (quotes are required), where <date> is of format YYYY-MM-DD HH:mm\n" +
                "- after certain date criteria in form of: \"Submit Date\" is since \"<date>\" (quotes are required), where <date> is of format YYYY-MM-DD HH:mm\n" +
                "- project criteria in form of: \"Project\" is \"<project path pattern>\" (quotes are required)\n" +
                "- and criteria in form of <criteria1> and <criteria2>\n" +
                "- or criteria in form of <criteria1> or <criteria2>\n" +
                "- operator 'and' takes precedence over 'or' when used together, unless parentheses are used to group 'or' criterias\n" +
                "- not criteria in form of not(<criteria>)\n" +
                "\n" +
                "And can optionally add order clause at end of query string in form of: order by \"<field1>\" <asc|desc>,\"<field2>\" <asc|desc>,... (quotes are required), where <field> is one of below:\n\n" +
                orderFields + "\n" +
                "Issue, build or pull request can be referenced by their number in form of: #<number>, <project path>#<number> or <project key>-<number>\n" +
                "\n" +
                "Leave empty to list all accessible builds";

        return description;
    }

}
