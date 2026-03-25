---
Description: This test case is a demo for `ai-write-test` skill. After you've installed all tools and AI CLI(s) you can run it with `/ai-write-test docs/cases/demo_test_case.md` or simply ask your AI agent to automate this file.
---

Feature: Practice test automation

Background:
Given I start on https://practicetestautomation.com/

Scenario: Test login page is available
Given user opens main page
When user clicks PRACTICE menu item
Then user sees link to the Test Login Page
When user clicks Test Login Page link
Then Test Login page is opened

Scenario: Login to the resource
Given user opens Test Login page
When user enters login and password
And clicks Submit button
Then Logged In Successfully page is opened
And label 'Congratulations student. You successfully logged in!' is visible