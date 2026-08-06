# Release process

1. Update `CHANGELOG.md` and the version in `pom.xml`.
2. Run `mvn clean verify`.
3. Run the independent consumer test under `examples/java`.
4. Merge the reviewed change into `main`.
5. Create and push a signed or annotated tag matching the POM, for example
   `v1.0.0`.
6. The release workflow validates the tag, signs artifacts and deploys them to
   GitHub Packages.

The repository must define `MAVEN_GPG_PRIVATE_KEY` and
`MAVEN_GPG_PASSPHRASE` secrets. `GITHUB_TOKEN` is supplied by GitHub Actions.

Consumers authenticate to GitHub Packages through their Maven `settings.xml`
using server id `github`.
