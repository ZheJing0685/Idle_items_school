---
name: "superpowers"
description: "Complete software development methodology: brainstorming → writing-plans → TDD execution → code review. Invoke when starting development tasks to ensure proper spec, plan, and verification."
---

# Superpowers - Software Development Methodology

Adapted from [obra/superpowers](https://github.com/obra/superpowers) for Trae IDE. A complete methodology that ensures every task goes through proper spec, plan, and verification.

## Core Philosophy

> "It doesn't just jump into trying to write code. Instead, it steps back and asks you what you're really trying to do."

**Key Principles:**
- **Spec before Code**: Never write code without approved specification
- **Bite-sized Tasks**: Break work into 2-5 minute chunks with exact file paths
- **RED-GREEN-REFACTOR**: Always write tests first
- **Evidence over Claims**: Verify before declaring success

---

## The Superpowers Workflow

### Phase 1: Brainstorming (Before Writing Code)

**"Tease out a spec out of the conversation."**

Activates when you're about to build something new.

- Use Socratic questioning to refine rough ideas
- Explore alternatives and tradeoffs
- Present design in small chunks for validation
- Save approved design to a design document

```
When done: You have a shared spec document that both you and the AI understand
```

**Key Questions to Ask:**
- What problem are we really solving?
- What does success look like?
- What are we NOT building (scope boundaries)?
- What could go wrong?

---

### Phase 2: Writing Plans (After Design Approval)

**"Put together an implementation plan clear enough for a junior engineer to follow."**

Breaks work into bite-sized tasks (2-5 minutes each).

**Every task must include:**
- Exact file paths to modify
- Complete code or change description
- Verification steps

```
When done: You have a sequence of atomic, verifiable tasks
```

**Task Template:**
```
## Task N: [Brief Description]
### Files: [exact paths]
### Change: [what to do]
### Verify: [how to confirm it works]
```

---

### Phase 3: Test-Driven Development (During Implementation)

**"Enforces RED-GREEN-REFACTOR cycle."**

For every code change:

1. **RED**: Write a failing test first
2. **GREEN**: Write minimal code to make it pass
3. **REFACTOR**: Clean up code while keeping tests green

```
Rule: Delete any code written before tests.
```

**Anti-Patterns to Avoid:**
- Writing code before writing tests
- Writing tests after code (" confirmation tests")
- Tests that don't actually fail when they should
- Over-testing (test everything vs. test what matters)

---

### Phase 4: Code Review (Between Tasks)

**"Reviews against plan, reports issues by severity."**

Review checklist:
- [ ] Does the code match the spec?
- [ ] Are tests properly written (RED first)?
- [ ] Are there any critical issues?
- [ ] Does it follow existing patterns?

**Severity Levels:**
| Level | Action |
|-------|--------|
| Critical | Block progress until fixed |
| Major | Fix before moving on |
| Minor | Note and continue |
| Nitpick | Optional improvement |

---

### Phase 5: Finishing (When Tasks Complete)

**"Verifies tests, presents options."**

Final verification:
- All tests pass
- No unintended side effects
- Code matches spec

**Options:**
- Merge/PR immediately
- Keep working branch
- Discard if approach changed

---

## Subagent-Driven Development

For large tasks, dispatch fresh subagent per task with two-stage review:

**Stage 1: Spec Compliance**
- Does it match the task description?
- Are file paths correct?
- Does it solve the stated problem?

**Stage 2: Code Quality**
- Are there any code smells?
- Is it simple (YAGNI)?
- Are tests comprehensive?

---

## Systematic Debugging

When something is broken, use the 4-phase process:

### Phase 1: Reproduce
Make the failure consistent and observable.

### Phase 2: Isolating
Narrow down to the exact cause.
- Binary search through code
- Check recent changes
- Isolate the minimal case

### Phase 3: Fix
Apply the minimal fix.

### Phase 4: Verify
Confirm the fix works and no regressions.

**Techniques:**
- Root-cause tracing: Why did this happen?
- Defense-in-depth: Multiple layers of checks
- Condition-based waiting: Don't assume timing

---

## Integration with Existing Rules

This skill works alongside:

| Rule/Skill | Role |
|------------|------|
| **Karpathy Guidelines** | Code behavior (Think, Simplify, Surgical, Goal-Driven) |
| **Vibe Workflow** | Task workflow (Clarify → Plan → Execute → Verify) |
| **Superpowers** | Development methodology (Spec → Plan → TDD → Review) |

**Superpowers adds:**
- Mandatory brainstorming/spec phase
- RED-GREEN-REFACTOR discipline
- Two-stage code review
- Subagent dispatch for large tasks

---

## When to Use

| Situation | Skill Phase |
|-----------|-------------|
| Starting new feature | brainstorming + writing-plans |
| Bug fix | systematic-debugging + verification |
| Refactoring | writing-plans + test-driven-development |
| Large task | subagent-driven-development |
| Between tasks | requesting-code-review |
| Task complete | finishing-a-development-branch |

---

## Source

Adapted from [obra/superpowers](https://github.com/obra/superpowers) by obra (Jesse Vincent)
