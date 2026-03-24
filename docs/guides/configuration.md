# Configuration Reference

## System Properties

Pass these on the command line with `-Dproperty=value` or set them in your TestNG suite / CI environment.

| Property | Description | Example |
|---|---|---|
| `driver` | Browser to use: `chrome`, `firefox`, `lambda` | `-Ddriver=chrome` |
| `browser_version` | Pin a specific browser driver version | `-Dbrowser_version=114` |
| `base_url` | Root URL prepended to relative `@PageURL` values | `-Dbase_url=https://app.example.com` |
| `screen_maximize` | Maximize the browser window on start | `-Dscreen_maximize=true` |
| `screen_resolution` | Screen resolution string passed to remote grid | `1920x1080` |
| `locale` | Locale hint for the browser or app under test | `en_US` |
| `platform` | Platform hint passed to remote capabilities | `Windows 11` |
| `os` | OS hint passed to remote capabilities | `Windows` |
| `remote_browser` | Browser name for remote execution | `chrome` |
| `remote_key` | Access key for the remote grid service | — |
| `remote_username` | Username for the remote grid service | — |
| `build_number` | Build number tag for remote session metadata | `42` |
| `test_rail_url` | TestRail instance URL for reporting integration | — |

## Element Highlight

Located at `src/main/resources/webelement.properties`.

| Key | Default | Description |
|---|---|---|
| `webelement.border.highlight` | `true` | Draw a border around each resolved element |
| `webelement.border.highlight.width` | `2px` | Border width |
| `webelement.border.highlight.color` | `red` | Border color (CSS color value) |
| `webelement.background.highlight` | `false` | Fill element background |
| `webelement.background.highlight.color` | `yellow` | Background fill color |

To disable highlighting during a CI run, set `webelement.border.highlight=false` in the properties file or override programmatically before the driver starts.

## Remote Execution

Remote properties live in `src/test/resources/remoteEnv.properties`. They are read when `driver=lambda`.

The remote WebDriver URL is resolved from `RemoteEnvProperties.REMOTE_URL_KEY`. Credentials (`remote_username`, `remote_key`) are passed as desired capabilities via `DriverCaps`.

## `@PageURL` and Base URL

`iPage.openPage()` resolves the URL as follows:

1. If `@PageURL` starts with `http`, it is used as-is (absolute URL).
2. Otherwise, `base_url` from system properties is prepended to the relative path.
3. For inherited page hierarchies, all `@PageURL` values from the chain are concatenated before the base URL is prepended.

```java
// Absolute — ignores base_url
@PageURL("https://duckduckgo.com/")
public class SearchPage extends iPage { ... }

// Relative — base_url + "/search"
@PageURL("/search")
public class SearchResultsPage extends iPage { ... }
```

## Log4j2

The logging configuration is at `src/main/resources/log4j2.xml`. Edit it to adjust console output levels or add file appenders. The framework uses SLF4J through Lombok's `@Slf4j`.
