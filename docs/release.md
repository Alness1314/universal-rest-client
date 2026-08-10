# Release process

1. Update `CHANGELOG.md` and the version in `pom.xml`.
2. Run `mvn clean verify`.
3. Run the independent consumer test under `examples/java`.
4. Merge the reviewed change into `main`.
5. Create and push a signed or annotated tag matching the POM, for example
   `v1.1.1`.
6. The release workflow validates the tag and deploys the artifacts to GitHub
   Packages. GitHub Actions supplies the required `GITHUB_TOKEN`.

The optional Maven `release` profile signs artifacts with GPG and is intended
for repositories such as Maven Central. GitHub Packages does not activate that
profile, so this initial publication does not require GPG secrets.

Consumers authenticate to GitHub Packages through their Maven `settings.xml`
using server id `github`.
