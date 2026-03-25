# Test Case Template

Copy one of the two formats below, fill in the required fields, then invoke `/ai-write-test` with the file path.

## Format A — BDD (Given / When / Then)

```markdown
Feature: <feature name>
Auth required (optional): yes | no

Background:
  Given I start on <page name>

Scenario: <scenario name>
  Given <precondition>
  When  <action>
  Then  <expected outcome>

Test data:
  - key: value
```

## Format B — Steps + Expected Result

```markdown
Feature: <feature name>
Auth required (optional): yes | no

Starting page: <page name>

Scenario: <scenario name>
Steps:
  1. <setup or precondition>
  2. <action>
  3. <further actions if needed>
Expected result: <what should be true after the steps>

Test data:
  - key: value
```

## Rules

- **One scenario per file.** Split multi-scenario files before running.
- **Required fields** — `Feature`, one scenario, scenario steps, expected outcome, and any referenced `Test data`.
- **Starting page** — provide it explicitly, or make it clear from the first step ("User opens X", "User logged in as Admin"). The skill will infer it; it will only ask if the entry point is genuinely ambiguous.
- **Auth required** — optional. You can provide it explicitly, or imply it through the steps ("User logs in as Admin", credentials in test data). The skill will infer it; it will only ask if auth state is unclear.
- **No `Pages` or URL fields needed.** The skill discovers pages and URLs by walking the scenario path.
- **Test data must be complete.** Every value referenced in the steps must have a matching key in `Test data`.
- **Either format is accepted.** Use whichever is clearer for the scenario.

## Examples

**Format A:**

```markdown
Feature: Component playground text input
Auth required (optional): no

Background:
  Given I start on the Component Playground page

Scenario: User enters text in the class-scoped component input
  Given the class-scoped demo component is visible
  When  I type "hello world" into the component input
  Then  the component value field displays "hello world"

Test data:
  - inputText: hello world
  - expectedValue: hello world
```

**Format B:**

```markdown
Feature: Component playground text input
Auth required (optional): no

Starting page: Component Playground page

Scenario: User enters text in the class-scoped component input
Steps:
  1. The class-scoped demo component is visible
  2. Type "hello world" into the component input
Expected result: The component value field displays "hello world"

Test data:
  - inputText: hello world
  - expectedValue: hello world
```
