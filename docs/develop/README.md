# Release Process

Releases are automated using the [Create Release GitHub workflow ](https://github.com/adobe/aem-eclipse-developer-tools/actions/workflows/release.yml). Manual follow-up steps are documented in this guide.

## Prerequisites

Before creating a release please check the following:

1. The [target platform definition](../../com.adobe.granite.ide.target-definition/com.adobe.granite.ide.target-definition.target) many only reference a released version of the Sling IDE tooling for Eclipse. The referenced p2 update site must start with `https://dist.apache.org/repos/dist/release/sling/eclipse/` and not with `https://nightlies.apache.org/sling/eclipse/`.

2. The release process uses a specific `RELEASE_GITHUB_TOKEN` (not your personal token). This token must be periodically renewed. When in doubt, ask the last person who performed a release. The token must be scoped to this repository only and have read and write access to actions, administration, and code.

## Release Steps

1. Go to the Actions tab and manually trigger the "Create Release" workflow with the desired release version

2. The GitHub Action will:
   - Build the project
   - Create a git tag
   - Generate a draft release
   - Upload the p2 update site zip

3. After the workflow completes:
   - Go to the Releases page on GitHub
   - Find the draft release
   - Use the GitHub UI to generate release notes
   - Publish the release
   - Mark it as the latest release (set as default)

4. As a final step, update the Eclipse Marketplace entry at:
   https://marketplace.eclipse.org/content/aem-developer-tools-eclipse
