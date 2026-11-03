import { expect, test, admin } from './fixtures.js';
import { login } from './helpers.js';

const hclSource = [
  '# hash comment',
  '// slash comment',
  '/* block comment',
  '   continued comment */',
  'resource "example_server" "web" {',
  '  count = 1.25e+2',
  '  enabled = true',
  '  name = "hello ${var.name}!"',
  '  nested = "${jsonencode({key = "${var.name}"})} suffix"',
  '  escaped = "$${literal} %%{literal}"',
  '  script = <<-EOT',
  '    # literal heredoc text',
  '    %{ for item in var.items ~}',
  '    ${item}',
  '    %{ endfor ~}',
  '    EOT',
  '  after = 42',
  '}',
  '',
].join('\n');

async function expectToken(scope, style, text) {
  const token = scope.locator(`.cm-${style}`).and(scope.getByText(text, { exact: true })).first();
  await expect(token).toBeVisible();
  await expect(token).toHaveText(text);
}

async function expectHclHighlighting(scope) {
  await expectToken(scope, 'comment', '# hash comment');
  await expectToken(scope, 'comment', '// slash comment');
  await expectToken(scope, 'comment', '/* block comment');
  await expectToken(scope, 'comment', '   continued comment */');
  await expectToken(scope, 'keyword', 'resource');
  await expectToken(scope, 'number', '1.25e+2');
  await expectToken(scope, 'atom', 'true');
  await expectToken(scope, 'punctuation', '${');
  await expectToken(scope, 'variable', 'var');
  await expectToken(scope, 'def', 'jsonencode');
  await expectToken(scope, 'string', ' suffix"');
  await expectToken(scope, 'string', '"$${literal} %%{literal}"');
  await expectToken(scope, 'string', '    # literal heredoc text');
  await expectToken(scope, 'keyword', 'endfor');
  await expectToken(scope, 'string', '    EOT');
  // The heredoc must end, so subsequent attributes are highlighted as code.
  await expectToken(scope, 'number', '42');
}

for (const file of ['main.tf', 'values.tfvars', 'terragrunt.hcl']) {
  test(`highlights ${file} in the blob viewer`, async ({ page, api }) => {
    const project = await api.createProject();
    await api.putFile(project, file, hclSource);
    await login(page, admin.name, admin.password);
    await page.goto(`${project.name}/~files/main/${file}`);

    await expectHclHighlighting(page.locator('.source-view .CodeMirror'));
  });
}

for (const file of ['main.tf.json', 'values.tfvars.json']) {
  test(`uses JSON highlighting for ${file}`, async ({ page, api }) => {
    const project = await api.createProject();
    await api.putFile(project, file, '{"count": 42, "enabled": true, "name": "example"}\n');
    await login(page, admin.name, admin.password);
    await page.goto(`${project.name}/~files/main/${file}`);

    const source = page.locator('.source-view .CodeMirror');
    await expectToken(source, 'property', '"count"');
    await expectToken(source, 'number', '42');
    await expectToken(source, 'atom', 'true');
    await expectToken(source, 'string', '"example"');
  });
}

for (const mode of ['UNIFIED', 'SPLIT']) {
  test(`highlights Terraform in ${mode.toLowerCase()} diffs`, async ({ page, api, baseURL }) => {
    await page.context().addCookies([{ name: 'onedev.server.diff.viewmode', value: mode, url: baseURL }]);
    const project = await api.createProject();
    const added = await api.putFile(project, 'main.tf', hclSource);
    await login(page, admin.name, admin.password);
    await page.goto(`${project.name}/~commits/${added.commitHash}`);

    const diff = page.locator('.blob-text-diff');
    await expectHclHighlighting(diff);

    // Exercise both revisions, including a number embedded in a changed line.
    const changed = await api.putFile(project, 'main.tf', hclSource.replace('after = 42', 'after = 99'));
    await page.goto(`${project.name}/~commits/${changed.commitHash}`);
    await expectToken(diff, 'number', '42');
    await expectToken(diff, 'number', '99');
    await expectToken(diff, 'keyword', 'endfor');
  });
}
