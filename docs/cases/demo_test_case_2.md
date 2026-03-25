---
Description: This test case is a demo for `ai-write-test` skill. After you've installed all tools and AI CLI(s) you can run it with `/ai-write-test docs/cases/demo_test_case.md` or simply ask your AI agent to automate this file.
---

Feature: Practice test automation: extend existing pages

Scenario: Check top menu items
Steps:
1. User opens Practice test automation main page
Expected result: The top menu items are: Home, Practice, Courses, Blog, Contact

Scenario: Blog: article has title and Published by sections
Steps:
1. User opens Practice test automation main page
2. User click Blog menu item
Expected result: Every article has Title and 'Published by' section with template 'Published by <author name> on <date>'