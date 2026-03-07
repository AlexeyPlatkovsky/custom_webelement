# Contributing

## Branch Strategy

| Branch | Purpose |
|--------|---------|
| `master` | Stable, releasable code |
| `feature/<name>` | New features |
| `fix/<name>` | Bug fixes |
| `docs/<name>` | Documentation only |
| `claude/<name>` | AI-assisted development branches |

Create a branch from `master`, open a PR, and merge after review. Direct pushes to `master` are not allowed.

```bash
git checkout master
git pull origin master
git checkout -b feature/my-feature
```

## Development Setup

```bash
# Clone
git clone https://github.com/AlexeyPlatkovsky/custom_webelement.git
cd custom_webelement

# Build
./gradlew compileJava

# Run tests
./gradlew test

# Run a specific test class
./gradlew test --tests "tests.GooglePageTest"
```

## Code Style

- **Java 21** — use records, sealed classes, pattern matching, and text blocks where they improve clarity.
- **No wildcard imports** — import types explicitly.
- **Lombok** — use `@Getter`, `@Builder`, `@Slf4j` etc. where they reduce boilerplate. Do not use `@Data` on mutable domain objects.
- **No raw types** — parameterize all generics.
- **Checkstyle** — the build runs Checkstyle automatically. Fix violations before opening a PR (`./gradlew checkstyleMain`).
- **Package naming** — follow the existing lowercase convention (`core.web`, `ai.provider`, `utils.logging`).

## Writing Tests

- Unit tests go in `src/test/java/unit_tests/`.
- UI tests go in `src/test/java/tests/`.
- Page Object classes go in `src/test/java/pages/`.
- AI-generated tests go in `src/test/java/generated/` — review before committing.
- Tag tests with TestNG groups: `@Test(groups = "ui")` or `@Test(groups = "unit")`.
- Tag flaky or work-in-progress tests with `@Test(groups = "disabled")` to exclude from CI.

## Adding a New AI Provider

1. Create `src/main/java/ai/provider/<Name>Provider.java` implementing `AiProvider`.
2. Handle `AuthConfig.AuthType` — throw `UnsupportedAuthException` for unsupported types.
3. Add the provider case to `AiProviderFactory`.
4. Add config keys to `ai-provider.properties` (follow the existing naming pattern).
5. Document auth setup in `docs/guides/auth-setup.md`.
6. Add unit tests mocking HTTP responses for both `api_key` and `auth_token` (if supported).

## Pull Request Checklist

- [ ] `./gradlew compileJava` passes
- [ ] `./gradlew test -Psuite=unit` passes
- [ ] Checkstyle passes (`./gradlew checkstyleMain`)
- [ ] New public methods have Javadoc
- [ ] No API keys or tokens in committed files
- [ ] `ai-provider.properties` changes only add new keys (no removal of existing ones without discussion)
- [ ] Documentation updated if behavior changed

## Reporting Issues

Open an issue at [github.com/AlexeyPlatkovsky/custom_webelement/issues](https://github.com/AlexeyPlatkovsky/custom_webelement/issues).

Include:
- Java version (`java -version`)
- Browser and driver version
- Minimal reproduction (test class + page class)
- Full stack trace from the Allure report or console
