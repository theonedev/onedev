import { expect, test } from './fixtures.js';

for (const crawler of [false, true]) {
  test.describe(crawler ? 'crawler redirect' : 'browser redirect', () => {
    if (crawler) test.use({ userAgent: 'OneDev E2E crawler' });

    test('adds the site trailing slash without duplicating the project path', async ({ page, api, baseURL }) => {
      const project = await api.createProject();
      const siteUrl = new URL(`${project.name}/~site`, baseURL).href;
      const canonicalUrl = `${siteUrl}/`;
      const navigation = [];
      page.on('response', response => {
        if (response.request().isNavigationRequest() && response.frame() === page.mainFrame())
          navigation.push({ url: response.url(), status: response.status() });
      });

      // ProjectSiteFileResource passes "project/~site/" to RedirectToUrlException.
      // RedirectRequestHandler must rebase it against the current request;
      // leaving it unchanged sends the browser to "project/project/~site/".
      await page.goto(siteUrl);
      await expect(page).toHaveURL(canonicalUrl);

      // A new project's site has an empty directory listing. Verify that the
      // redirect reaches it instead of an unrelated project-not-found page.
      await expect(page.getByRole('heading', { name: 'Index of /', exact: true })).toBeVisible();
      expect(navigation).toEqual([
        // ServletWebResponse uses JavaScript for browsers and HTTP for crawlers.
        { url: siteUrl, status: crawler ? 302 : 200 },
        { url: canonicalUrl, status: 200 },
      ]);
    });
  });
}
