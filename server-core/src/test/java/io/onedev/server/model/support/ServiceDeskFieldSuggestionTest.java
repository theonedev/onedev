package io.onedev.server.model.support;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.Serializable;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.buildspecmodel.inputspec.choiceinput.choiceprovider.Choice;
import io.onedev.server.buildspecmodel.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.AiSetting;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.model.support.issue.field.FieldUtils;
import io.onedev.server.model.support.issue.field.instance.FieldInstance;
import io.onedev.server.model.support.issue.field.instance.JevDecideValue;
import io.onedev.server.model.support.issue.field.instance.ScriptingValue;
import io.onedev.server.model.support.issue.field.instance.SpecifiedValue;
import io.onedev.server.model.support.issue.field.spec.choicefield.ChoiceField;
import io.onedev.server.model.support.issue.field.spec.userchoicefield.UserChoiceField;
import io.onedev.server.service.SettingService;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import jakarta.validation.Validator;

class ServiceDeskFieldSuggestionTest {

	@Editable
	public static class Fields implements Serializable {
		private static final long serialVersionUID = 1L;

		private String type;

		private List<String> assignees;

		@Editable
		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		@Editable
		public List<String> getAssignees() {
			return assignees;
		}

		public void setAssignees(List<String> assignees) {
			this.assignees = assignees;
		}
	}

	private ChoiceField field(String name, boolean multiple) {
		var field = new ChoiceField();
		field.setName(name);
		field.setAllowMultiple(multiple);
		var choices = new SpecifiedChoices();
		for (var value: List.of("Bug", "Other")) {
			var choice = new Choice();
			choice.setValue(value);
			choices.getChoices().add(choice);
		}
		field.setChoiceProvider(choices);
		return field;
	}

	@Test
	void suggestsOnlyConfidentChoicesAndFallsBackOnUnavailableJev() throws Exception {
		var settings = mock(SettingService.class);
		var ai = new AiSetting();
		var jev = mock(JevSetting.class);
		ai.setJevSetting(jev);
		when(settings.getAiSetting()).thenReturn(ai);
		var issueSetting = new GlobalIssueSetting();
		issueSetting.setFieldSpecs(List.of(field("Type", false), field("Priority", false), field("Labels", true)));
		when(settings.getIssueSetting()).thenReturn(issueSetting);
		var project = new Project();
		project.setPath("support");
		var issue = mock(Issue.class);
		when(issue.getProject()).thenReturn(project);
		when(issue.getTitle()).thenReturn("Cannot log in");
		when(issue.getFieldBean(Fields.class)).thenReturn(new Fields());
		var client = mock(HttpClient.class);
		@SuppressWarnings("unchecked")
		var response = (HttpResponse<String>) mock(HttpResponse.class);
		when(response.statusCode()).thenReturn(200);
		when(response.body()).thenReturn("""
			{"answers": {
			  "field0": {"choice": "choice0", "confidence": 0.75},
			  "field1": {"choice": "choice0", "confidence": 0.7499},
			  "field2": {"choice": "choice0", "confidence": 0.99}
			}}
			""");
		when(client.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(response);
		when(jev.choose(anyString(), anyMap(), anyDouble())).thenAnswer(it ->
				new JevSetting().choose(client, it.getArgument(0),
						it.<Map<String, JevSetting.ChoiceQuestion>>getArgument(1), it.getArgument(2, Double.class)));
		var validator = mock(Validator.class);
		try (var oneDev = mockStatic(OneDev.class); var utils = mockStatic(FieldUtils.class, CALLS_REAL_METHODS)) {
			oneDev.when(() -> OneDev.getInstance(SettingService.class)).thenReturn(settings);
			oneDev.when(() -> OneDev.getInstance(Validator.class)).thenReturn(validator);
			utils.when(() -> FieldUtils.getFieldBeanClass(false)).thenReturn(Fields.class);
			var names = List.of("Type", "Priority", "Labels");
			assertEquals(Map.of("Type", "Bug", "Labels", List.of("Bug")), FieldUtils.suggestFieldValues(issue, names));
			verify(jev).choose(contains("Cannot log in"), anyMap(), eq(0.75));
			verify(client).send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());

			when(response.body()).thenReturn("{\"answers\":{\"field0\":{\"choice\":\"unknown\",\"confidence\":1}}}");
			assertTrue(FieldUtils.suggestFieldValues(issue, names).isEmpty());
			when(client.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
					.thenThrow(new IOException("Unavailable"));
			assertTrue(FieldUtils.suggestFieldValues(issue, names).isEmpty());
			clearInvocations(jev);
			assertTrue(FieldUtils.suggestFieldValues(issue, List.of()).isEmpty());
			ai.setJevSetting(null);
			assertTrue(FieldUtils.suggestFieldValues(issue, names).isEmpty());
			verifyNoInteractions(jev);
		}
	}

	@Test
	void assigneeScriptSeesJevDecisionAndRunsOnce() throws Exception {
		var settings = mock(SettingService.class);
		var ai = new AiSetting();
		var jev = mock(JevSetting.class);
		ai.setJevSetting(jev);
		when(settings.getAiSetting()).thenReturn(ai);
		var typeSpec = field("Type", false);
		((SpecifiedChoices) typeSpec.getChoiceProvider()).getChoices().get(1).setValue("Support Request");
		var assigneeSpec = new UserChoiceField();
		assigneeSpec.setName("Assignees");
		assigneeSpec.setAllowMultiple(true);
		var issueSetting = new GlobalIssueSetting();
		issueSetting.setFieldSpecs(List.of(typeSpec, assigneeSpec));
		when(settings.getIssueSetting()).thenReturn(issueSetting);
		var script = new GroovyScript();
		script.setName("GetDefaultAssignees");
		script.setContent(List.of("""
			import io.onedev.server.util.EditContext
			def type = EditContext.get().getInputValue("Type")
			return type == "Support Request" ? ["servicedesk"] : ["robin"]
			"""));
		when(settings.getGroovyScripts()).thenReturn(List.of(script));
		var type = new FieldInstance();
		type.setName("Type");
		var defaultType = new JevDecideValue();
		defaultType.setValue(List.of("Support Request"));
		type.setValueProvider(defaultType);
		var assignees = new FieldInstance();
		assignees.setName("Assignees");
		var provider = spy(new ScriptingValue());
		provider.setScriptName(script.getName());
		assignees.setValueProvider(provider);
		var project = new Project();
		project.setPath("support");
		var validator = mock(Validator.class);
		try (var oneDev = mockStatic(OneDev.class); var utils = mockStatic(FieldUtils.class, CALLS_REAL_METHODS)) {
			oneDev.when(() -> OneDev.getInstance(SettingService.class)).thenReturn(settings);
			oneDev.when(() -> OneDev.getInstance(Validator.class)).thenReturn(validator);
			utils.when(() -> FieldUtils.getFieldBeanClass(false)).thenReturn(Fields.class);
			// Also verify scripts see resolved fields when listed ahead of those fields.
			for (var fields: List.of(List.of(type, assignees), List.of(assignees, type))) {
				for (var outcome: List.of("bug", "support", "uncertain", "unconfigured")) {
					clearInvocations(provider);
					ai.setJevSetting(outcome.equals("unconfigured") ? null : jev);
					when(jev.choose(anyString(), anyMap(), eq(0.75))).thenReturn(switch (outcome) {
						case "bug" -> Map.of("field0", "choice0");
						case "support" -> Map.of("field0", "choice1");
						default -> Map.of();
					});
					var issue = new Issue();
					issue.setProject(project);
					issue.setTitle("Cannot log in");
					FieldUtils.populateFields(issue, fields);
					assertEquals(outcome.equals("bug") ? "Bug" : "Support Request", issue.getFieldValue("Type"));
					assertEquals(List.of(outcome.equals("bug") ? "robin" : "servicedesk"), issue.getFieldValue("Assignees"));
					assertEquals(List.of("Support Request"), defaultType.getValue());
					verify(provider).getValue();
				}
			}
		}
	}

	@Test
	void fallbackValuesParticipateInWorkflowReconciliationAndProviderIdentity() {
		var provider = new JevDecideValue();
		provider.setValue(new ArrayList<>(List.of("Old")));
		var specified = new SpecifiedValue();
		specified.setValue(new ArrayList<>(provider.getValue()));
		assertNotEquals(specified, provider);
		assertNotEquals(provider, specified);
		var instance = new FieldInstance();
		instance.setName("Type");
		instance.setValueProvider(provider);
		var resolution = new UndefinedFieldValuesResolution(Map.of("Old", "Other"), List.of());
		assertTrue(instance.fixUndefinedFieldValues(Map.of("Type", resolution)));
		assertEquals(List.of("Other"), provider.getValue());
	}
}
