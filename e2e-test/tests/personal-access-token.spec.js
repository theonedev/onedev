import { expect, test } from './fixtures.js';

test.use({ trace: 'off' });

test('a personal access token grants REST access to a private project', async ({ api, request }) => {
  const owner = await api.createUser();
  const project = await api.createProject();
  await api.authorizeUser(project, owner, 'Code Reader');

  const projectPath = `~api/projects/${project.id}`;
  const anonymous = await request.get(projectPath);
  expect(anonymous.status()).toBe(401);

  const created = await api.request.post('~api/access-tokens', {
    data: { ownerId: owner.id, description: 'E2E private project access', hasOwnerPermissions: true },
  });
  expect(created.status()).toBe(200);
  const { value } = await created.json();
  expect(typeof value === 'string' && value.length > 0, 'Creation returns a token secret').toBe(true);

  const authorized = await request.get(projectPath, {
    headers: { Authorization: `Bearer ${value}` },
  });
  expect(authorized.status()).toBe(200);
  expect(await authorized.json()).toMatchObject({ id: project.id, name: project.name });
});
