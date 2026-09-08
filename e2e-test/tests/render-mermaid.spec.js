import { expect, test, admin } from './fixtures.js';
import { login } from './helpers.js';

test('renders Mermaid classes with numeric hex colors', async ({ page, api }) => {
  const project = await api.createProject();
  const issue = await api.createIssue(project, { description: [
    '```mermaid',
    'flowchart TD',
    '    classDef oldError fill:#f8d7da,stroke:#999,color:#333',
    '    A@{ shape: hex, label: "Error Node" }',
    '    class A oldError',
    '```',
  ].join('\n') });
  await login(page, admin.name, admin.password);
  await page.goto(issue.url);

  const styledNode = page.locator('.mermaid svg .node.oldError');
  await expect(styledNode).toBeVisible();
  await expect(styledNode).toContainText('Error Node');
});
