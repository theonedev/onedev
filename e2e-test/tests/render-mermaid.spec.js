import { expect, test } from './fixtures.js';
import { createProject, login } from './helpers.js';

test('renders Mermaid classes with numeric hex colors', async ({ page }) => {
  await login(page, 'admin', 'admin');
  const projectName = await createProject(page);

  await page.goto(`${projectName}/~issues/new`);
  await page.getByPlaceholder('Input title here').fill(`Mermaid diagram ${Date.now()}`);
  await page.locator('.new-issue > .description textarea').fill([
    '```mermaid',
    'flowchart TD',
    '    classDef oldError fill:#f8d7da,stroke:#999,color:#333',
    '    A@{ shape: hex, label: "Error Node" }',
    '    class A oldError',
    '```',
  ].join('\n'));
  await page.getByRole('button', { name: 'Save' }).click();

  const styledNode = page.locator('.mermaid svg .node.oldError');
  await expect(styledNode).toBeVisible();
  await expect(styledNode).toContainText('Error Node');
});
