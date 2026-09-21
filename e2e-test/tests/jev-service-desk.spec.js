import { randomUUID } from 'node:crypto';
import { expect } from './fixtures.js';
import { test as base } from './jev-fixtures.js';
import { startMailServer } from './mail-server.js';

async function readSetting(api, name) {
  const response = await api.request.get(`~api/settings/${name}`);
  expect(response.ok(), await response.text()).toBeTruthy();
  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

async function saveSetting(api, name, setting) {
  const response = await api.request.post(`~api/settings/${name}`, {
    headers: { 'Content-Type': 'application/json' }, data: JSON.stringify(setting),
  });
  expect(response.ok(), await response.text()).toBeTruthy();
}

const test = base.extend({
  serviceDesk: async ({ api, jev }, use, testInfo) => {
    const project = await api.createProject();
    const bugAssignee = await api.createUser();
    const supportAssignee = await api.createUser();
    await api.authorizeUser(project, bugAssignee);
    await api.authorizeUser(project, supportAssignee);
    const names = ['mail-service', 'service-desk', 'groovy-scripts', 'issue'];
    const originals = new Map(await Promise.all(names.map(async name => [name, await readSetting(api, name)])));
    const mail = await startMailServer(project, testInfo);
    const scriptName = `e2e-assignee-${randomUUID()}`;
    const fields = [
      // Deliberately precede Type: scripts must see Jev's final value regardless
      // of the order of the service desk field instances.
      { name: 'Assignees', valueProvider: { '@type': 'ScriptingValue', scriptName } },
      { name: 'Type', valueProvider: { '@type': 'JevDecideValue', value: ['Support Request'] } },
      { name: 'Priority', valueProvider: { '@type': 'JevDecideValue', value: ['Normal'] } },
      { name: 'E2E Labels', valueProvider: { '@type': 'JevDecideValue', value: ['Improvement', 'Task'] } },
      { name: 'E2E Source', valueProvider: { '@type': 'SpecifiedValue', value: ['Email'] } },
    ];
    try {
      const issue = structuredClone(originals.get('issue'));
      const type = issue.fieldSpecs.find(field => field.name === 'Type');
      issue.fieldSpecs.push({ ...structuredClone(type), name: 'E2E Labels', allowMultiple: true });
      issue.fieldSpecs.push({
        ...structuredClone(type), name: 'E2E Source',
        choiceProvider: { '@type': 'SpecifiedChoices', choices: [
          { value: 'Email', color: '#3699FF' }, { value: 'Web', color: '#1BC5BD' },
        ] },
      });
      await saveSetting(api, 'issue', issue);
      await saveSetting(api, 'groovy-scripts', [...originals.get('groovy-scripts'), {
        name: scriptName,
        content: [
          'import io.onedev.server.util.EditContext',
          'def type = EditContext.get().getInputValue("Type")',
          `return type == "Bug" ? ["${bugAssignee.userName}"] : ["${supportAssignee.userName}"]`,
        ],
      }]);
      await saveSetting(api, 'service-desk', { issueCreationSettings: [{
        applicableProjects: project.name, confidential: true, issueFields: fields,
      }] });
      await saveSetting(api, 'mail-service', mail.connector);
      await mail.waitForInbox();
      await use({ project, mail, bugAssignee, supportAssignee, fields });
    } finally {
      // Restore the connector before closing its mailbox, and remove service
      // desk project references before deleting the test project.
      const errors = [];
      for (const name of ['mail-service', 'service-desk']) {
        try { await saveSetting(api, name, originals.get(name)); } catch (error) { errors.push(error); }
      }
      try {
        const response = await api.request.delete(`~api/projects/${project.id}`);
        expect(response.ok(), await response.text()).toBeTruthy();
        api.projects = api.projects.filter(id => id !== project.id);
      } catch (error) { errors.push(error); }
      for (const name of ['groovy-scripts', 'issue']) {
        try { await saveSetting(api, name, originals.get(name)); } catch (error) { errors.push(error); }
      }
      try { await mail.close(); } catch (error) { errors.push(error); }
      if (errors.length) throw new AggregateError(errors, 'Service desk fixture cleanup failed');
    }
  },
});

const scenarios = [
  {
    name: 'confident decisions populate single and multiple choices before the assignee script',
    answers: { Type: ['Bug', 0.99], Priority: ['Major', 0.95], 'E2E Labels': ['Bug', 0.9] },
    expected: { Type: 'Bug', Priority: 'Major', 'E2E Labels': ['Bug'] },
    assignee: 'bugAssignee',
  },
  {
    name: 'a suggested support request selects the support assignee',
    answers: { Type: ['Support Request', 0.99], Priority: ['Major', 0.95], 'E2E Labels': ['Task', 0.9] },
    expected: { Type: 'Support Request', Priority: 'Major', 'E2E Labels': ['Task'] },
    assignee: 'supportAssignee',
  },
  {
    name: 'uncertain and unknown decisions retain configured fallbacks',
    answers: { Type: ['Bug', 0.84], Priority: ['unknown', 0.99], 'E2E Labels': ['Bug', 0.84] },
    expected: { Type: 'Support Request', Priority: 'Normal', 'E2E Labels': ['Improvement', 'Task'] },
    assignee: 'supportAssignee',
  },
  {
    name: 'Jev API failure still creates the issue using fallbacks', status: 503,
    answers: { Type: ['Bug', 0.99], Priority: ['Major', 0.99], 'E2E Labels': ['Bug', 0.99] },
    expected: { Type: 'Support Request', Priority: 'Normal', 'E2E Labels': ['Improvement', 'Task'] },
    assignee: 'supportAssignee',
  },
];

for (const scenario of scenarios) {
  test(`email to issue: ${scenario.name}`, async ({ page, api, jev, serviceDesk }) => {
    const { project, mail } = serviceDesk;
    const title = `Cannot sign in after password reset ${randomUUID()}`;
    const body = 'The password reset link succeeds, but signing in produces a server error.';
    const messageId = `<${randomUUID()}@example.test>`;
    await mail.send({ subject: title, text: body, messageId });

    const request = await jev.nextRequest();
    const state = JSON.parse(request.body.state);
    expect(state.project).toBe(project.name);
    expect(state.title).toBe(title);
    expect(state.description).toContain(body);
    expect(Object.values(request.body.questions).map(question =>
      question.instructions.match(/issue field '([^']+)'/)[1]).sort())
      .toEqual(['E2E Labels', 'Priority', 'Type']);
    jev.reply(request, scenario.answers, scenario.status);

    let issue;
    await expect(async () => {
      const issues = await api.json('get', '~api/issues', { params: {
        query: `"Project" is "${project.name}"`, offset: 0, count: 10,
      } });
      expect(issues).toHaveLength(1);
      [issue] = issues;
    }).toPass({ timeout: 30000, intervals: [250, 500, 1000] });
    expect(issue.title).toBe(title);
    expect(issue.description).toContain(body);
    expect(issue.messageId).toBe(messageId);
    expect(issue.confidential).toBe(true);
    expect(await api.json('get', `~api/issues/${issue.id}/fields`)).toMatchObject({
      ...scenario.expected, 'E2E Source': 'Email',
      Assignees: [serviceDesk[scenario.assignee].userName],
    });
    // Assignment notifications run after the issue transaction commits. Wait
    // for their watch to commit before cleanup removes the project's storage.
    await expect.poll(async () => api.json('get', `~api/issues/${issue.id}/watches`))
      .toEqual(expect.arrayContaining([expect.objectContaining({
        userId: serviceDesk[scenario.assignee].id, watching: true,
      })]));
    // A decision must not overwrite the configured fallback for later emails.
    const settings = await readSetting(api, 'service-desk');
    expect(settings.issueCreationSettings[0].issueFields.map(({ name, valueProvider }) => ({ name, valueProvider })))
      .toEqual(serviceDesk.fields);
    expect(jev.requests).toHaveLength(1);

    await page.goto(`${project.name}/~issues/${issue.number}`);
    await expect(page.locator('.issue-editable-title')).toContainText(title);
    await expect(page.locator('.issue-primary')).toContainText(body);
    await expect(page.locator('.field-values')).toContainText([scenario.expected.Type, scenario.expected.Priority]);
  });
}
