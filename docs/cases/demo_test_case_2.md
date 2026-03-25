---
Description: This test case is a demo for `ai-write-test` skill. It should be run after `docs/cases/demo_test_case.md` so you can check that Ai can not only create but and update and extend existing code.
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