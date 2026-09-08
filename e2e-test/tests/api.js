import { randomUUID } from 'node:crypto';
import { expect } from '@playwright/test';

export const admin = {
  name: process.env.E2E_ADMIN_USER ?? 'admin',
  password: process.env.E2E_ADMIN_PASSWORD ?? 'admin',
};

export function credentials(user) {
  return { Authorization: `Basic ${Buffer.from(`${user.userName ?? user.name}:${user.password}`).toString('base64')}` };
}

/** REST fixture setup. Only resources created by this instance are cleaned up. */
export class FixturesApi {
  constructor(request) {
    this.request = request;
    this.projects = [];
    this.users = [];
    this.roles = new Map();
  }

  async json(method, url, options = {}) {
    const response = await this.request[method](url, options);
    expect(response.ok(), `${method} ${url}: ${await response.text()}`).toBeTruthy();
    return response.json();
  }

  async createProject(name = `e2e-${randomUUID()}`) {
    const id = await this.json('post', '~api/projects', { data: {
      name, codeManagement: true, wikiManagement: true, codeAnalysisSetting: {}, gitPackConfig: {},
    } });
    this.projects.push(id);
    return { id, name };
  }

  async createUser({ userName = `user-${randomUUID()}`, password = 'userpass1', type = 'Ordinary' } = {}) {
    const data = { name: userName, type: type.toUpperCase() };
    if (data.type === 'ORDINARY') {
      data.password = password;
      data.emailAddress = `${userName}@example.com`;
    } else if (data.type === 'AI') {
      data.aiSetting = { modelSetting: { baseUrl: 'http://127.0.0.1:1', name: 'dummy', timeoutSeconds: 30 } };
    }
    const id = await this.json('post', '~api/users', { data });
    this.users.push(id);
    return { id, userName, password };
  }

  async authorizeUser(project, user, roleName = 'Issue Reporter') {
    if (!this.roles.has(roleName))
      this.roles.set(roleName, await this.json('get', `~api/roles/ids/${encodeURIComponent(roleName)}`));
    return this.json('post', '~api/user-authorizations', {
      data: { projectId: project.id, userId: user.id, roleId: this.roles.get(roleName) },
    });
  }

  async createIssue(project, { title = `Issue ${randomUUID()}`, description, confidential = false, fields = {}, creator } = {}) {
    const id = await this.json('post', '~api/issues', {
      data: { projectId: project.id, title, description, confidential, fields },
      ...(creator ? { headers: credentials(creator) } : {}),
    });
    const issue = await this.json('get', `~api/issues/${id}`);
    return { id, title, url: `${project.name}/~issues/${issue.number}` };
  }

  async authorizeIssueUser(issue, user, authorizer = admin) {
    return this.json('post', '~api/issue-authorizations', {
      data: { issueId: issue.id, userId: user.id },
      headers: credentials(authorizer),
    });
  }

  async putFile(project, file, content, commitMessage = `Set up ${file}`) {
    return this.json('post', `~api/repositories/${project.id}/files/main/${file}`, { data: {
      '@type': 'FileCreateOrUpdateRequest', commitMessage,
      base64Content: Buffer.from(content).toString('base64'),
    } });
  }

  async dispose() {
    const errors = [];
    // Projects own issues and authorizations, so remove them before users.
    for (const url of [
      ...this.projects.map((id) => `~api/projects/${id}`),
      ...this.users.map((id) => `~api/users/${id}`),
    ]) {
      try {
        const response = await this.request.delete(url);
        expect(response.ok(), `Cleanup ${url}: ${await response.text()}`).toBeTruthy();
      } catch (error) {
        errors.push(error);
      }
    }
    await this.request.dispose();
    if (errors.length) throw new AggregateError(errors, 'Fixture cleanup failed');
  }
}
