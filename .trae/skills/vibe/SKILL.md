---
name: "vibe"
description: "Governed workflow skill for structured task execution: Clarify → Plan → Execute → Verify. Invoke when starting complex tasks or when workflow guidance is needed."
---

# Vibe - Governed Workflow Skill

A lightweight governed workflow system adapted from [Vibe-Skills](https://github.com/foryourhealth111-pixel/Vibe-Skills) for Trae IDE.

## Core Philosophy

Vibe-Skills tackles the core pain point: **"How do I manage skills and get work done efficiently and reliably?"**

Rather than diving in blindly, Vibe enforces a structured process that makes every step auditable.

## The Governed Workflow

### Phase 1: Clarify (vibe-want)

**Clarify the actual want before doing anything.**

- State the problem clearly
- Identify what success looks like
- Confirm scope and boundaries
- **Frozen Requirement**: Once confirmed, it won't silently change

```
When done: You can articulate "I want X, not Y, and Z is out of scope"
```

### Phase 2: Plan (vibe-how)

**Plan before executing. Use the simplest approach.**

- Break down into concrete steps
- Identify dependencies and risks
- Define verification for each step
- Keep the plan minimal

```
When done: You have a clear path with checkpoints
```

### Phase 3: Execute (vibe-do)

**Execute the plan. Stay disciplined.**

- Follow the plan unless there's clear reason to deviate
- If deviation is needed, return to Clarify phase
- Keep changes surgical

```
When done: Implementation matches the plan
```

### Phase 4: Verify

**Verify before delivery.**

- Test against the original requirement
- Check all defined checkpoints pass
- Confirm no unintended side effects
- Clean up any orphans from your changes

```
When done: You have proof the work is complete
```

## Task Complexity Grades

| Grade | Scope | Characteristics |
|-------|-------|------------------|
| **M** | Narrow, focused | Single step, clear boundaries |
| **L** | Medium complexity | Multi-step, needs planning |
| **XL** | Large, parallelizable | Split into bounded units under coordinator |

For **M-grade**: Simplify - you don't need full governed workflow overhead.

## Key Principles

### Intelligent Routing

Skills should activate based on context, not by memorizing all available skills. When you invoke vibe, the system routes to the appropriate skill for the current phase.

### Memory Continuity

- **Session**: Current progress, intermediate results
- **Project**: Confirmed decisions, architecture, conventions
- **Task**: Long-running context fragments
- **Knowledge**: Durable information worth retaining

### Safety Rules

- **No blind execution**: Always clarify before coding
- **No silent scope creep**: Frozen requirements stay frozen
- **No bulk deletion**: Dangerous operations require explicit confirmation
- **No silent fallbacks**: If something fails, say it explicitly

## Common Pain Points This Solves

| Problem | Vibe Solution |
|---------|---------------|
| Skills never activate | Context-based routing |
| Blind execution | Clarify → Verify enforced |
| Scope creep mid-task | Frozen requirement |
| Messy workspace | Semantic directory governance |
| AI bad habits | Built-in safety rules |

## When to Use

**Invoke vibe when:**
- Starting a complex multi-step task
- Requirements are vague or unclear
- Task involves multiple files or layers
- You need to coordinate several changes
- Previous attempts went off track

**Skip vibe for:**
- Simple typo fixes
- Obvious one-liners
- Trivial refactoring with clear scope

## Entry Points

| Command | Stops After |
|---------|-------------|
| `/vibe` or `$vibe` | Full governed flow |
| `vibe-want` | Clarification phase |
| `vibe-how` | Planning phase |
| `vibe-do` | Full execution (skipping ask/plan if already clear) |

## Source

Adapted from [Vibe-Skills](https://github.com/foryourhealth111-pixel/Vibe-Skills) by foryourhealth111-pixel
