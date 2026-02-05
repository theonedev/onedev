package io.onedev.server.ai;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import io.onedev.server.OneDev;
import io.onedev.server.model.Agent;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.LabelSpec;
import io.onedev.server.model.Pack;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.issue.field.spec.BooleanField;
import io.onedev.server.model.support.issue.field.spec.BuildChoiceField;
import io.onedev.server.model.support.issue.field.spec.CommitField;
import io.onedev.server.model.support.issue.field.spec.DateField;
import io.onedev.server.model.support.issue.field.spec.DateTimeField;
import io.onedev.server.model.support.issue.field.spec.FloatField;
import io.onedev.server.model.support.issue.field.spec.GroupChoiceField;
import io.onedev.server.model.support.issue.field.spec.IntegerField;
import io.onedev.server.model.support.issue.field.spec.IssueChoiceField;
import io.onedev.server.model.support.issue.field.spec.IterationChoiceField;
import io.onedev.server.model.support.issue.field.spec.PullRequestChoiceField;
import io.onedev.server.model.support.issue.field.spec.TextField;
import io.onedev.server.model.support.issue.field.spec.choicefield.ChoiceField;
import io.onedev.server.model.support.issue.field.spec.userchoicefield.UserChoiceField;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.pack.PackSupport;
import io.onedev.server.service.AgentAttributeService;
import io.onedev.server.service.AgentService;
import io.onedev.server.service.LabelSpecService;
import io.onedev.server.service.LinkSpecService;
import io.onedev.server.service.SettingService;

public class QueryDescriptions {
    
    private static String REACTION_CRITERIAS = """
            | '"Reaction: Thumbs Up Count"' 'is' ('not')? '"'Number'"'
            | '"Reaction: Thumbs Up Count"' 'is' ('greater'|'less') 'than' '"'Number'"'
            | '"Reaction: Thumbs Down Count"' 'is' ('not')? '"'Number'"'
            | '"Reaction: Thumbs Down Count"' 'is' ('greater'|'less') 'than' '"'Number'"'
            | '"Reaction: Smile Count"' 'is' ('not')? '"'Number'"'
            | '"Reaction: Smile Count"' 'is' ('greater'|'less') 'than' '"'Number'"'
            | '"Reaction: Tada Count"' 'is' ('not')? '"'Number'"'
            | '"Reaction: Tada Count"' 'is' ('greater'|'less') 'than' '"'Number'"'
            | '"Reaction: Confused Count"' 'is' ('not')? '"'Number'"'
            | '"Reaction: Confused Count"' 'is' ('greater'|'less') 'than' '"'Number'"'
            | '"Reaction: Heart Count"' 'is' ('not')? '"'Number'"'
            | '"Reaction: Heart Count"' 'is' ('greater'|'less') 'than' '"'Number'"'
            | '"Reaction: Rocket Count"' 'is' ('not')? '"'Number'"'
            | '"Reaction: Rocket Count"' 'is' ('greater'|'less') 'than' '"'Number'"'
            | '"Reaction: Eyes Count"' 'is' ('not')? '"'Number'"'
            | '"Reaction: Eyes Count"' 'is' ('greater'|'less') 'than' '"'Number'"'
        """.trim();

    private static SettingService getSettingService() {
        return OneDev.getInstance(SettingService.class);
    }

    private static LinkSpecService getLinkSpecService() {
        return OneDev.getInstance(LinkSpecService.class);
    }
    
    public static String getIssueQueryDescription() {
        var settingService = getSettingService();
        var linkSpecService = getLinkSpecService();
        
        var fieldCriterias = new ArrayList<String>();
        var choiceFieldValueRules = new ArrayList<String>();
        int choiceFieldValueRuleIndex = 0;
        for (var field: settingService.getIssueSetting().getFieldSpecs()) {
            if (field instanceof ChoiceField) {                
                var choiceField = (ChoiceField) field;                
                var fieldValueRuleName = "ChoiceFieldValue" + (choiceFieldValueRuleIndex++);
                fieldCriterias.add("'\"" + field.getName() + "\"' 'is' ('not')? '\"'" + fieldValueRuleName + "'\"'");
                choiceFieldValueRules.add(choiceField.getPossibleValues().stream().map(it->"'" + it.replace("'", "\\'") + "'").collect(joining("\n    | ")));
            } else if (field instanceof UserChoiceField) {
                fieldCriterias.add("'\"" + field.getName() + "\"' 'is' ('not')? '\"'LoginNameOfUser'\"'");
                fieldCriterias.add("'\"" + field.getName() + "\"' 'is' ('not')? 'me'");
            } else if (field instanceof GroupChoiceField) {
                fieldCriterias.add("'\"" + field.getName() + "\"' 'is' ('not')? '\"'GroupName'\"'");
            } else if (field instanceof BooleanField) {
                fieldCriterias.add("'\"" + field.getName() + "\"' 'is' ('not')? '\"'('true'|'false')'\"'");
            } else if (field instanceof DateField) {
                fieldCriterias.add("'\"" + field.getName() + "\"' 'is' ('before'|'after') '\"'DateDescription'\"'");
            } else if (field instanceof DateTimeField) {
                fieldCriterias.add("'\"" + field.getName() + "\"' 'is' ('before'|'after') '\"'DateTimeDescription'\"'");
            } else if (field instanceof IntegerField) {
                fieldCriterias.add("'\"" + field.getName() + "\"' 'is' ('not')? '\"'Integer'\"'");
                fieldCriterias.add("'\"" + field.getName() + "\"' 'is' ('greater'|'less') 'than' '\"'Integer'\"'");
            } else if (field instanceof FloatField) {
                fieldCriterias.add("'\"" + field.getName() + "\"' 'is' ('greater'|'less') 'than' '\"'Float'\"'");
            } else if (field instanceof TextField) {
                fieldCriterias.add("'\"" + field.getName() + "\"' 'is' ('not')? '\"'Text'\"'");
                fieldCriterias.add("'\"" + field.getName() + "\"' 'contains' '\"'Text'\"'");
            } else if (field instanceof PullRequestChoiceField || field instanceof BuildChoiceField || field instanceof IssueChoiceField) {
                fieldCriterias.add("'\"" + field.getName() + "\"' 'is' ('not')? '\"'EntityReference'\"'");
            } else if (field instanceof CommitField) {
                fieldCriterias.add("'\"" + field.getName() + "\"' 'is' ('not')? '\"'CommitReference'\"'");
            } else if (field instanceof IterationChoiceField) {
                fieldCriterias.add("'\"" + field.getName() + "\"' 'is' ('not')? '\"'IterationNameOrPattern'\"'");
            }
            fieldCriterias.add("'\"" + field.getName() + "\"' 'is' ('not')? 'empty'");
        }

        var linkCriterias = new ArrayList<String>();
        for (var linkSpec: linkSpecService.query()) {
            linkCriterias.add("'any' '\"" + linkSpec.getName() + "\"' 'matching' '('criteria')'");
            linkCriterias.add("'all' '\"" + linkSpec.getName() + "\"' 'matching' '('criteria')'");
            linkCriterias.add("'has any' '\"" + linkSpec.getName() + "\"'");
            if (linkSpec.getOpposite() != null) {
                linkCriterias.add("'any' '\"" + linkSpec.getOpposite().getName() + "\"' 'matching' '('criteria')'");
                linkCriterias.add("'all' '\"" + linkSpec.getOpposite().getName() + "\"' 'matching' '('criteria')'");
                linkCriterias.add("'has any' '\"" + linkSpec.getOpposite().getName() + "\"'");
            }
        }

        var description = String.format("""
                A structured query should conform to below ANTLR grammar:

                issueQuery
                    : criteria ('order by' '"'OrderField'"' ('asc'|'desc') (',' '"'OrderField'"' ('asc'|'desc'))*)?
                    ;

                criteria
                    : '"Number"' 'is' ('not')? '"'EntityReference'"'
                    | '"Number"' 'is' ('greater'|'less') 'than' '"'EntityReference'"'
                    | '"State"' 'is' ('not')? '"'StateName'"'
                    | '"State"' 'is' ('after'|'before') '"'StateName'"'
                    %s
                    %s
                    | 'submitted by' '"'LoginNameOfUser'"'
                    | 'submitted by me'
                    | 'watched by' '"'LoginNameOfUser'"'
                    | 'watched by me'
                    | 'ignored by' '"'LoginNameOfUser'"'
                    | 'ignored by me'
                    | 'commented by' '"'LoginNameOfUser'"'
                    | 'commented by me'
                    | 'mentioned' '"'LoginNameOfUser'"'
                    | 'mentioned me'
                    | 'fixed in commit' '"'CommitReference'"'
                    | 'fixed in current commit'
                    | 'fixed in build' '"'EntityReference'"'
                    | 'fixed in current build'
                    | 'fixed in pull request' '"'EntityReference'"'
                    | 'fixed in current pull request'
                    | 'fixed between' revision 'and' revision
                    | '"Submit Date"' 'is' ('until'|'since') '"'DateDescription'"' 
                    | '"Last Activity Date"' 'is' ('until'|'since') '"'DateDescription'"'
                    | 'confidential'
                    | '"Spent Time"' 'is' ('greater'|'less') 'than' '"'TimePeriodDescription'"'
                    | '"Spent Time"' 'is' ('not')? '"'TimePeriodDescription'"'
                    | '"Estimated Time"' 'is' ('greater'|'less') 'than' '"'TimePeriodDescription'"'
                    | '"Estimated Time"' 'is' ('not')? '"'TimePeriodDescription'"'
                    | '"Progress"' 'is' ('greater'|'less') 'than' '"'Float'"'
                    | '"Iteration"' 'is' ('not')? '"'IterationNameOrPattern'"'
                    | '"Iteration"' 'is' ('not')? 'empty'
                    | '"Title"' 'contains' '"'Text'"'
                    | '"Description"' 'contains' '"'Text'"'
                    | '"Comment"' 'contains' '"'Text'"'
                    | '"Comment Count"' 'is' ('not')? '"'Number'"'
                    | '"Comment Count"' 'is' ('greater'|'less') 'than' '"'Number'"'
                    | '"Vote Count"' 'is' ('not')? '"'Number'"'
                    | '"Vote Count"' 'is' ('greater'|'less') 'than' '"'Number'"'
                    %s
                    | '"Project"' 'is current'
                    | '"Project"' 'is' ('not')? '"'ProjectPathOrPattern'"'
                    | criteria 'and' criteria
                    | criteria 'or' criteria
                    | 'not('criteria')'
                    | '('criteria')'
                    ;

                StateName
                    : %s
                    ;

                %s

                revision
                    : 'commit' '"'CommitReference'"'
                    | 'build' '"'EntityReference'"'
                    | 'branch' '"'BranchReference'"'
                    | 'tag' '"'TagReference'"'
                    ;

                EntityReference
                    : '#'Number
                    | ProjectPath'#'Number
                    | ProjectKey'-'Number
                    ;

                CommitReference
                    : (ProjectPath':')?CommitHash
                    ;

                BranchReference
                    : (ProjectPath':')?BranchName
                    ;

                TagReference
                    : (ProjectPath':')?TagName
                    ;

                OrderField
                    : %s
                    ;

                WS  
                    : [ ]+ -> skip 
                    ;

                Please note:
                    1. "LoginNameOfUser" should be retrieved via tool 'getLoginName' if available, with parameter set to user name
                    2. Use an empty query to list all accessible issues""", 
                    fieldCriterias.stream().map(it->"    | " + it + "\n").collect(joining("")).trim(),
                    linkCriterias.stream().map(it->"    | " + it + "\n").collect(joining("")).trim(),
                    REACTION_CRITERIAS,
                    settingService.getIssueSetting().getStateSpecs().stream().map(it->"'" + it.getName() + "'").collect(joining("\n    | ")).trim(), 
                    IntStream.range(0, choiceFieldValueRules.size()).mapToObj(i -> "ChoiceFieldValue" + i + "\n    : " + choiceFieldValueRules.get(i) + "\n    ;\n\n").collect(joining("")).trim(),
                    Issue.SORT_FIELDS.keySet().stream().map(it->"'" + it + "'").collect(joining("\n    | ")).trim());

        return description;
    }

    public static String getPullRequestQueryDescription() {
        var description = String.format("""
                A structured query should conform to below ANTLR grammar:

                pullRequestQuery
                    : criteria ('order by' '"'OrderField'"' ('asc'|'desc') (',' '"'OrderField'"' ('asc'|'desc'))*)?
                    ;

                criteria
                    : '"Number"' 'is' ('not')? '"'EntityReference'"'
                    | '"Number"' 'is' ('greater'|'less') 'than' '"'EntityReference'"'
                    | 'open'
                    | 'merged'
                    | 'discarded'
                    | '"Source Branch"' 'is' ('not')? '"'BranchNameOrPattern'"'
                    | '"Souce Project"' 'is' ('not')? '"'ProjectPathOrPattern'"'
                    | '"Target Branch"' 'is' ('not')? '"'BranchNameOrPattern'"'
                    | '"Merge Strategy"' 'is' ('not')? '"'MergeStrategy'"'
                    | '"Label"' 'is' ('not')? '"'LabelName'"'
                    | 'ready to merge'
                    | 'has pending reviews'
                    | 'has unsuccessful builds'
                    | 'has unfinished builds'
                    | 'has merge conflicts'
                    | 'assigned to' '"'LoginNameOfUser'"'
                    | 'approved by' '"'LoginNameOfUser'"'
                    | 'to be reviewed by' '"'LoginNameOfUser'"'
                    | 'to be changed by' '"'LoginNameOfUser'"'
                    | 'to be merged by' '"'LoginNameOfUser'"'
                    | 'requested for changes by' '"'LoginNameOfUser'"'
                    | 'need action of' '"'LoginNameOfUser'"'
                    | 'assigned to me'
                    | 'approved by me'
                    | 'to be reviewed by me'
                    | 'to be changed by me'
                    | 'to be merged by me'
                    | 'requested for changes by me'
                    | 'someone requested for changes'
                    | 'mentioned' '"'LoginNameOfUser'"'
                    | 'mentioned me'
                    | 'need action of' '"'LoginNameOfUser'"'
                    | 'need my action'
                    | 'submitted by' '"'LoginNameOfUser'"'
                    | 'submitted by me'
                    | '"Submit Date"' 'is' ('until'|'since') '"'DateDescription'"'
                    | '"Last Activity Date"' 'is' ('until'|'since') '"'DateDescription'"'
                    | '"Close Date"' 'is' ('until'|'since') '"'DateDescription'"'
                    | 'includes issue' '"'EntityReference'"'
                    | 'includes commit' '"'CommitReference'"'
                    | '"Title"' 'contains' '"'Text'"'
                    | '"Description"' 'contains' '"'Text'"'
                    | '"Comment"' 'contains' '"'Text'"'
                    | '"Comment Count"' 'is' ('not')? '"'Number'"'
                    | '"Comment Count"' 'is' ('greater'|'less') 'than' '"'Number'"'
                    %s
                    | '"Project"' 'is' ('not')? '"'ProjectPathOrPattern'"'
                    | 'watched by' '"'LoginNameOfUser'"'
                    | 'watched by me'
                    | 'ignored by' '"'LoginNameOfUser'"'
                    | 'ignored by me'
                    | 'commented by' '"'LoginNameOfUser'"'
                    | 'commented by me'
                    | criteria 'and' criteria
                    | criteria 'or' criteria
                    | 'not('criteria')'
                    | '('criteria')'
                    ;
                
                EntityReference
                    : '#'Number
                    | ProjectPath'#'Number
                    | ProjectKey'-'Number
                    ;

                CommitReference
                    : (ProjectPath':')?CommitHash
                    ;

                BranchReference
                    : (ProjectPath':')?BranchName
                    ;

                TagReference
                    : (ProjectPath':')?TagName
                    ;

                MergeStrategy
                    : %s
                    ;

                LabelName
                    : %s
                    ;

                OrderField
                    : %s
                    ;

                WS  
                    : [ ]+ -> skip 
                    ;

                Please note:
                    1. "LoginNameOfUser" should be retrieved via tool 'getLoginName' if available, with parameter set to user name
                    2. Use an empty query to list all accessible pull requests""", 
                    REACTION_CRITERIAS,
                    Arrays.stream(MergeStrategy.values()).map(it->"'" + it.name() + "'").collect(joining("\n    | ")).trim(), 
                    getLabelSpecs().stream().map(it->"'" + it.getName() + "'").collect(joining("\n    | ")).trim(), 
                    PullRequest.SORT_FIELDS.keySet().stream().map(it->"'" + it + "'").collect(joining("\n    | ")).trim());

        return description;
    }

    public static String getBuildQueryDescription() {
        var description = String.format("""
                A structured query should conform to below ANTLR grammar:

                buildQuery
                    : criteria ('order by' '"'OrderField'"' ('asc'|'desc') (',' '"'OrderField'"' ('asc'|'desc'))*)?
                    ;

                criteria
                    : '"Number"' 'is' ('not')? '"'EntityReference'"'
                    | '"Number"' 'is' ('greater'|'less') 'than' '"'EntityReference'"'
                    | 'sucessful'
                    | 'failed'
                    | 'cancelled'
                    | 'timed out'
                    | 'finished'
                    | 'running'
                    | 'waiting'
                    | 'pending'
                    | 'submitted by' '"'LoginNameOfUser'"'
                    | 'submitted by me'
                    | 'cancelled by' '"'LoginNameOfUser'"'
                    | 'cancelled by me'
                    | 'depends on' '"'EntityReference'"'
                    | 'dependencies of' '"'EntityReference'"'
                    | 'ran on' '"'AgentName'"'
                    | 'fixed issue' '"'EntityReference'"'
                    | '"Project"' 'is' ('not')? '"'ProjectPathOrPattern'"'
                    | '"Job"' 'is' ('not')? '"'JobNameOrPattern'"'
                    | '"Version"' 'is' ('not')? '"'VersionNameOrPattern'"'
                    | '"Branch"' 'is' ('not')? '"'BranchNameOrPattern'"'
                    | '"Tag"' 'is' ('not')? '"'TagNameOrPattern'"'
                    | '"Param"' 'is' ('not')? '"'ParamName'"'
                    | '"Label"' 'is' ('not')? '"'LabelName'"'
                    | '"Pull Request"' 'is' ('not')? '"'EntityReference'"'
                    | '"Commit"' 'is' ('not')? '"'CommitReference'"'
                    | '"Submit Date"' 'is' ('until'|'since') '"'DateDescription'"'
                    | '"Pending Date"' 'is' ('until'|'since') '"'DateDescription'"'
                    | '"Running Date"' 'is' ('until'|'since') '"'DateDescription'"'
                    | '"Finish Date"' 'is' ('until'|'since') '"'DateDescription'"'
                    | criteria 'and' criteria
                    | criteria 'or' criteria
                    | 'not('criteria')'
                    | '('criteria')'
                    ;

                EntityReference
                    : '#'Number
                    | ProjectPath'#'Number
                    | ProjectKey'-'Number
                    ;

                CommitReference
                    : (ProjectPath':')?CommitHash
                    ;

                LabelName
                    : %s
                    ;

                OrderField
                    : %s
                    ;

                WS  
                    : [ ]+ -> skip 
                    ;

                Please note:
                    1. "LoginNameOfUser" should be retrieved via tool 'getLoginName' if available, with parameter set to user name
                    2. Use an empty query to list all accessible builds""", 
                    getLabelSpecs().stream().map(it->"'" + it.getName() + "'").collect(joining("\n    | ")).trim(), 
                    Build.SORT_FIELDS.keySet().stream().map(it->"'" + it + "'").collect(joining("\n    | ")).trim());

        return description;
    }

    private static List<LabelSpec> getLabelSpecs() {
        return OneDev.getInstance(LabelSpecService.class).query();
    }

    public static String getPackQueryDescription() {
        var packSupports = new ArrayList<>(OneDev.getExtensions(PackSupport.class));		

        var description = String.format("""
                A structured query should conform to below ANTLR grammar:

                packQuery
                    : criteria ('order by' '"'OrderField'"' ('asc'|'desc') (',' '"'OrderField'"' ('asc'|'desc'))*)?
                    ;

                criteria
                    : 'published by me'
                    | 'published by user' '"'LoginNameOfUser'"'
                    | 'published by build' '"'EntityReference'"'
                    | 'published by project' '"'ProjectPathOrPattern'"'
                    | '"Project"' 'is' ('not')? '"'ProjectPathOrPattern'"'
                    | '"Type"' 'is' ('not')? '"'PackType'"'
                    | '"Name"' 'is' ('not')? '"'PackName'"'
                    | '"Version"' 'is' ('not')? '"'PackVersion'"'
                    | '"Label"' 'is' ('not')? '"'LabelName'"'
                    | '"Publish Date"' 'is' ('until'|'since') '"'DateDescription'"'
                    | criteria 'and' criteria
                    | criteria 'or' criteria
                    | 'not('criteria')'
                    | '('criteria')'
                    ;

                EntityReference
                    : '#'Number
                    | ProjectPath'#'Number
                    | ProjectKey'-'Number
                    ;

                PackType
                    : %s
                    ;

                LabelName
                    : %s
                    ;

                OrderField
                    : %s
                    ;

                WS  
                    : [ ]+ -> skip 
                    ;

                Please note:
                    1. "LoginNameOfUser" should be retrieved via tool 'getLoginName' if available, with parameter set to user name
                    2. Use an empty query to list all accessible packages""", 
                    packSupports.stream().map(it->"'" + it.getPackType() + "'").collect(joining("\n    | ")).trim(), 
                    getLabelSpecs().stream().map(it->"'" + it.getName() + "'").collect(joining("\n    | ")).trim(), 
                    Pack.SORT_FIELDS.keySet().stream().map(it->"'" + it + "'").collect(joining("\n    | ")).trim());
        
        return description;
    }

    public static String getCommitQueryDescription() {
        var description = """
                A structured query should conform to below ANTLR grammar:

                commitQuery
                    : criteria+
                    ;

                criteria
                    : ('before('|'after(') DateDescription ')'
                    | 'committer(' CommitterNameAndEmail ')' // committer is specified user
                    | 'author(' AuthorNameAndEmail ')' // author is specified user
                    | 'message(' Text ')' // commit message contains specified text
                    | 'path(' FilePath ')' // commit touches specified file
                    | 'authored-by-me' 
                    | 'committed-by-me'
                    | ('until')? revision // until specified revision
                    | 'since' revision // since specified revision
                    ;

                revision
                    : 'commit(' CommitHash ')' 
                    | 'build(' '#'Number ')'
                    | 'branch(' BranchName ')'
                    | 'tag(' TagName ')'
                    | 'default-branch'
                    ;

                WS  
                    : [ ]+ -> skip 
                    ;
                """;        

        return description;
    }    

    public static String getProjectQueryDescription() {
        var description = String.format("""
                A structured query should conform to below ANTLR grammar:

                projectQuery
                    : criteria ('order by' '"'OrderField'"' ('asc'|'desc') (',' '"'OrderField'"' ('asc'|'desc'))*)?
                    ;

                criteria
                    : 'owned by' '"'LoginNameOfUser'"'
                    | 'owned by me'
                    | 'owned by none'
                    | 'has outdated replicas'
                    | 'without enough replicas'
                    | 'missing storage'
                    | 'children of' '"'ProjectPathOrPattern'"'
                    | 'forks of' '"'ProjectPathOrPattern'"'
                    | 'roots'
                    | 'leafs'
                    | 'fork roots'
                    | '"Name"' 'is' ('not')? '"'ProjectNameOrPattern'"'
                    | '"Key"' 'is' ('not')? '"'ProjectKeyOrPattern'"'
                    | '"Path"' 'is' ('not')? '"'ProjectPathOrPattern'"'
                    | '"Label"' 'is' ('not')? '"'LabelName'"'
                    | '"Description"' 'contains' '"'Text'"'
                    | '"Id"' 'is' ('not')? '"'Number'"'
                    | '"Id"' 'is' ('greater'|'less') 'than' '"'Number'"'
                    | '"Service Desk Email Address"' 'is' ('not')? '"'EmailAddressOrPattern'"'
                    | '"Last Activity Date"' 'is' ('until'|'since') '"'DateDescription'"'
                    | criteria 'and' criteria
                    | criteria 'or' criteria
                    | 'not('criteria')'
                    | '('criteria')'
                    ;

                LabelName
                    : %s
                    ;

                OrderField
                    : %s
                    ;

                WS  
                    : [ ]+ -> skip 
                    ;

                Please note:
                    1. "LoginNameOfUser" should be retrieved via tool 'getLoginName' if available, with parameter set to user name
                    2. Use an empty query to list all accessible projects""", 
                    getLabelSpecs().stream().map(it->"'" + it.getName() + "'").collect(joining("\n    | ")).trim(), 
                    Project.SORT_FIELDS.keySet().stream().map(it->"'" + it + "'").collect(joining("\n    | ")).trim());
        
        return description;
    }    

    public static String getAgentQueryDescription() {
        var agentService = OneDev.getInstance(AgentService.class);
        var attributeService = OneDev.getInstance(AgentAttributeService.class);
        
        var description = String.format("""
                A structured query should conform to below ANTLR grammar:

                agentQuery
                    : criteria ('order by' '"'OrderField'"' ('asc'|'desc') (',' '"'OrderField'"' ('asc'|'desc'))*)?
                    ;

                criteria
                    : 'online'
                    | 'offline'
                    | 'paused'
                    | 'has running builds'
                    | 'has attribute' '"'AttributeName'"'
                    | 'not used since' '"'DateDescription'"'
                    | 'ever used since' '"'DateDescription'"'
                    | 'ran build' '"'EntityReference'"'
                    | '"Name"' 'is' ('not')? '"'AgentNameOrPattern'"'
                    | '"Ip Address"' 'is' ('not')? '"'IPAddressOrPattern'"'
                    | '"Os"' 'is' ('not')? '"'(OsName|OsNamePattern)'"'
                    | '"Os Arch"' 'is' ('not')? '"'(OsArch|OsArchPattern)'"'
                    | '"Os Version"' 'is' ('not')? '"'OsVersionOrPattern'"'
                    | '"'AttributeName'"' 'is' ('not')? '"'AttributeValue'"'
                    | criteria 'and' criteria
                    | criteria 'or' criteria
                    | 'not('criteria')'
                    | '('criteria')'
                    ;

                OsName
                    : %s
                    ;

                OsArch
                    : %s
                    ;

                AttributeName
                    : %s
                    ;

                OrderField
                    : %s
                    ;

                WS  
                    : [ ]+ -> skip 
                    ;

                Please note:
                    1. "LoginNameOfUser" should be retrieved via tool 'getLoginName' if available, with parameter set to user name
                    2. Use an empty query to list all accessible agents""", 
                    agentService.getOsNames().stream().map(it->"'" + it + "'").collect(joining("\n    | ")).trim(), 
                    agentService.getOsArchs().stream().map(it->"'" + it + "'").collect(joining("\n    | ")).trim(), 
                    attributeService.getAttributeNames().stream().map(it->"'" + it + "'").collect(joining("\n    | ")).trim(), 
                    Agent.SORT_FIELDS.keySet().stream().map(it->"'" + it + "'").collect(joining("\n    | ")).trim());
        
        return description;
    }        
}
