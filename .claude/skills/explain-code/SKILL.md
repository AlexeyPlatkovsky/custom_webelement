---
name: explain-code
description: Explains code with visual diagrams and analogies. Use when explaining how code works, teaching about a codebase, or when the user asks "how does this work?"
---

When explaining code:

1. Start from the code that matters most to the question.
2. Explain the real control flow and data flow before discussing abstractions.
3. Include a small ASCII diagram when it clarifies ownership, lifecycle, or call flow.
4. Call out assumptions, extension points, or failure modes when they are relevant.

## Default Structure

1. **What it is** - one or two sentences describing the class or subsystem role.
2. **How it flows** - step-by-step execution path with file references.
3. **Key collaborators** - which classes or annotations it depends on.
4. **Gotchas** - common misunderstandings, hidden state, lifecycle rules, or threading concerns.

## Style Rules

- Use analogies only when they simplify the explanation; do not force them.
- Do not invent behavior that is not visible in the code. If something is inferred, label it as an inference.
- Prefer concrete file and method references over generic commentary.
- Keep diagrams compact and ASCII-only.
