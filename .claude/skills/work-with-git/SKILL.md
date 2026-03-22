---
name: work-with-git
description: Branch strategy and git safety rules for this project. Use when creating branches, preparing commits, or deciding how to structure git work.
---

## Branch Naming

| Prefix | When to use |
|--------|-------------|
| `feature/<name>` | New features |
| `fix/<name>` | Bug fixes |
| `refactor/<name>` | Internal restructuring without behavior change |
| `docs/<name>` | Documentation only |
| `ai/<name>` | AI-assisted development when no better prefix fits |

## Inspect First

Before suggesting any branch action, inspect the repo state first:

```bash
git status --short
git branch --show-current
git branch --list
git symbolic-ref refs/remotes/origin/HEAD
```

- Do not assume the correct base branch without checking the repo.
- Do not switch branches or pull automatically.
- If the worktree is dirty, stay on the current branch unless the user explicitly approves a branch change.
- When a new branch is needed, suggest the repo's active integration branch or remote default branch as the base. In this repo that may be `master`, but do not hardcode it.
- If `refs/remotes/origin/HEAD` is unavailable locally, inspect the local branches and ask before fetching remote state.

## Branch Creation Example

Only suggest commands like these after the user approves branch creation or switching:

```bash
git checkout <base-branch>
git pull origin <base-branch>
git checkout -b <prefix>/my-change
```

## Branch Decision Rule

- **Trivial** (typo, doc link, log wording, local rename): branch optional.
- **Non-trivial** (new feature, API change, multi-file, test or behavior change): suggest a branch before starting substantial work.

## Git Safety Rules

- Never commit without explicit user permission.
- Never push without explicit user permission.
- Never create or switch branches without user approval.
- Never run `git pull` or `git checkout <branch>` just because a skill suggests it; inspect first and wait for approval.
- Never rewrite history, force-push, reset, or revert user changes without permission.
- If unexpected user edits overlap target files, stop and clarify before proceeding.
- Keep commits focused. Do not include unrelated files from a dirty worktree.
- Prefer `git status --short` and targeted `git diff -- <path>` before staging.
- If a task only updates skills or docs, keep the branch and commit scoped to those files.
- Direct pushes to `master` are not allowed; always open a PR.
