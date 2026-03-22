# Orchestration Policy

> **Main agent only.** Subagents (architect, engineer, code-reviewer, writer) do not follow these instructions - they have their own definitions in `.claude/agents/`.

## Default: Work Directly in the Current Thread

Spawning an agent has overhead. Unless delegation clearly improves the outcome, **work inline**. Most tasks do not need agents.

**Never spawn an agent for:**
- A single file or small related set of changes
- Docs, comments, naming, or formatting only
- A narrow, low-risk bug fix
- Anything answerable or fixable faster inline than via delegation

## When Delegation Pays Off

Consider delegating when the task is **non-trivial** (see `AGENTS.md` §Trivial Vs Non-Trivial) AND at least one is true:

- Structured planning is needed before edits (scope is unclear, multiple packages involved, risk is high)
- The implementation spans multiple phases and benefits from a focused implementer
- A post-implementation read-only review materially reduces risk
- Doc updates are needed alongside code changes

## Preferred Roles

| Role | When to use |
|---|---|
| `architect` | Non-trivial tasks needing an implementation plan, risk/affected-file analysis, or scope clarification before edits |
| `engineer` | Code changes in `src/main/java/` and `src/test/java/` - tests, refactors, fixes, feature work |
| `code-reviewer` | After non-trivial implementation - read-only review to surface CRITICAL/HIGH/MEDIUM/LOW findings |
| `writer` | When behavior, setup, extension points, architecture, or workflow changes require doc updates |

## Workflow for Non-Trivial Tasks

1. Classify the task using `AGENTS.md` Trivial Vs Non-Trivial.
2. **Trivial → work directly. Stop here.**
3. Non-trivial: run `architect` first unless the task is fully specified and planning adds no value.
4. Run `engineer` to implement. Engineer owns validation and must run the smallest meaningful checks.
5. Before running `code-reviewer`, collect the actual changed-file list and diff from the current implementation state. Pass that material to `code-reviewer`; pass the plan only as supplemental
   context.
6. Run `code-reviewer` after implementation.
7. If `code-reviewer` finds CRITICAL, HIGH, or MEDIUM issues: send the reviewer findings and any `docs/reviews/...` report path back to `engineer`, then rerun `code-reviewer`. A reviewer-owned
`docs/reviews/...` file is expected loop state and must not block the next engineer pass.
8. **Hard limit: 3 engineer/reviewer rounds.** After that, stop looping and report unresolved findings.
9. Run `writer` only when docs need material updates.

## Constraints

- Do not spawn agents to bypass repo rules, validation steps, or approval requirements.
- Subagents follow the same `AGENTS.md` rules and local skills as the main agent.
- When handing off, name the relevant skills explicitly if the match matters.
- Keep write ownership clear: avoid having two agents edit the same file in parallel.
