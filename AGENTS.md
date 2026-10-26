# CoupleFit Multi-Agent Operating Manual (`AGENTS.md`)

> **Active Ecosystem Catalog**: Defined in [`Documentation/agents1.md`](file:///D:/Couple_fit/CoupleFit/Documentation/agents1.md)
> **Active Testing Framework**: Google ARTEMIS Mobile Automation Harness

This document establishes the binding development rules for any autonomous agent or human pair-programmer working on the **CoupleFit** codebase.

---

## 1. Golden Rules for CoupleFit Development

1. **Multi-Agent Specialist Routing**:
   - Consult [`Documentation/agents1.md`](file:///D:/Couple_fit/CoupleFit/Documentation/agents1.md) for specialist prompt guidelines.
   - For all architectural decisions, invoke `planner` or `code-architect`.
   - For all Kotlin and Compose code, invoke `kotlin-reviewer` and `build-error-resolver`.
   - For every change touching UI or accessibility, invoke `a11y-architect` to enforce WCAG 2.2 Level AA compliance (44x44dp targets, TalkBack semantics, high-contrast text).
   - Before completing any task, run `code-reviewer` and `security-reviewer`.

2. **Mandatory ARTEMIS Live Device Testing**:
   - Every fix, refactor, or new feature MUST be verified on a live Android device or active emulator (`emulator-5554`) via ARTEMIS.
   - Run `mobile_get_device_state(view_type="hierarchy")` to inspect the UI tree and verify accessibility bounds.
   - Run `mobile_get_device_state(view_type="screenshot")` to visually confirm typography, layout, animations, and absence of visual clipping.
   - No task is marked "Resolved" without concrete screenshot evidence and test logs.

3. **Continuous Local Test Green Policy**:
   - `./gradlew testDebugUnitTest` must pass with 100% success at all times.
   - `./gradlew compileDebugAndroidTestKotlin` must compile without errors.
   - Never remove or bypass tests; fix underlying code to conform to contracts.

4. **Master Defect & Audit Tracking**:
   - Track all findings, bug fixes, and feature progress in [`Documentation/Fixinglist.md`](file:///D:/Couple_fit/CoupleFit/Documentation/Fixinglist.md).
   - Document root causes, solutions, test results, and ARTEMIS verification runs.

---

## 2. Key Specialist Agents Reference

- `kotlin-reviewer`: Verifies Kotlin coroutines, Flow lifecycle, Compose state hoisting, Room database queries, and architectural purity.
- `a11y-architect`: Verifies accessibility touch targets, semantics, color contrast (4.5:1 for body, 3:1 for large), and screen reader flow.
- `security-reviewer`: Audits Health Connect permissions (strictly read-only), Firestore security rules, Firebase auth, and sensitive user vitals.
- `code-reviewer`: Reviews code quality, maintainability, defensive design, and idiomatic patterns.
- `build-error-resolver`: Quickly diagnoses and resolves Gradle compilation and dependency issues with minimal diffs.
- `performance-optimizer`: Analyzes Compose recomposition bounds, GPU overdraw, memory leaks, and frame rate stability.

---

## 3. GSD Project Management & Execution Lifecycle Mandate

As you manage this project, adhere strictly to the following execution sequence:
1. **Oversee Phase Progression (`/gsd-next`)**: Begin by leveraging `/gsd-next` to oversee overall phase progression and determine immediate priorities.
2. **Draft Milestone Plan (`/gsd-plan-phase`)**: Use `/gsd-plan-phase` to draft a structured milestone plan clearly defining explicit acceptance criteria, task dependencies, and test bars.
3. **Execute Milestone Tasks (`/gsd-execute-phase`)**: Once the plan is solid, employ `/gsd-execute-phase` to carry out planned milestone tasks in strict dependency order, ensuring context remains lean and right-sized.
4. **Deterministic Verification (`/gsd-verify-work`)**: Apply `/gsd-verify-work` to execute automated test suites and deterministic verification checks against established milestone criteria, confirming standards are met.
5. **Shipment & Tagging (`/gsd-ship`)**: Finalize verified work with `/gsd-ship` to check git status, commit with conventional messaging, and tag releases.
6. **Post-Shipment Cleanup (`/gsd-cleanup`)**: Run `/gsd-cleanup` to safely eliminate unused files, orphan stubs, commented code, and stale artifacts—**never delete Markdown (`.md`) files**.
7. **Inbox Triage (`/gsd-inbox`)**: Use `/gsd-inbox` to create `triage.md` and organize captured notes, tickets, and ideas into actionable milestones.
8. **Knowledge Graph (`/gsd-graphify`)**: Apply `/gsd-graphify` to build a local knowledge graph linking files, symbols, documentation, and dependencies into a comprehensive view.

