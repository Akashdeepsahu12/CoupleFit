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

5. **Continuous Documentation Synchronization (Every Step & Every Time)**:
   - On EVERY single step, phase, bugfix, architectural adjustment, or feature addition, all relevant `.md` files in `Documentation/` MUST be updated immediately and kept synchronized with the live codebase.
   - Proactively update [`Documentation/Fixinglist.md`](file:///D:/Couple_fit/CoupleFit/Documentation/Fixinglist.md), [`Documentation/triage.md`](file:///D:/Couple_fit/CoupleFit/Documentation/triage.md), [`Documentation/INDEX.md`](file:///D:/Couple_fit/CoupleFit/Documentation/INDEX.md), [`Documentation/phases.md`](file:///D:/Couple_fit/CoupleFit/Documentation/phases.md), and associated domain specifications at every step.
   - **Never delete any `.md` file** during cleanup or refactoring.

---

## 2. Key Specialist Agents Reference

- `kotlin-reviewer`: Verifies Kotlin coroutines, Flow lifecycle, Compose state hoisting, Room database queries, and architectural purity.
- `a11y-architect`: Verifies accessibility touch targets, semantics, color contrast (4.5:1 for body, 3:1 for large), and screen reader flow.
- `security-reviewer`: Audits Health Connect permissions (strictly read-only), Firestore security rules, Firebase auth, and sensitive user vitals.
- `firestore-rules-author`: Mandatory specialist agent for Cloud Firestore Security Rules (`firestore.rules`) authoring, refactoring, and adversarial testing.
- `code-reviewer`: Reviews code quality, maintainability, defensive design, and idiomatic patterns.
- `build-error-resolver`: Quickly diagnoses and resolves Gradle compilation and dependency issues with minimal diffs.
- `performance-optimizer`: Analyzes Compose recomposition bounds, GPU overdraw, memory leaks, and frame rate stability.
- `motion-reviewer`: Audits animation performance, MotionScore tiers (S/A/B/C/F), and frame budgets.
- `egm-review`: Coordinates multi-persona documentation review swarms (`egm-design`, `egm-codebase-details`, `egm-style`, etc.).

---

## 3. GSD Project Management & Execution Lifecycle Mandate

> **Mandatory Universal Rule for All Fixes & Features**:
> "As you manage this project, begin by leveraging /gsd-next to oversee the overall phase progression, and use /gsd-plan-phase to draft a structured milestone plan that clearly defines explicit acceptance criteria, task dependencies, and test bars. Once the plan is solid, employ /gsd-execute-phase to carry out the planned milestone tasks in strict dependency order, ensuring the context remains lean and appropriately right-sized throughout execution, and apply /impeccable throughout to ensure all generated work is high-quality, intentional, and free of AI slop. After execution, update the Markdown (.md) files in the documentation folder, and if any new .md file is created, move it into the documentation folder. After implementation, use /gsd-verify-work to execute automated test suites and deterministic verification checks against the established milestone criteria, confirming that all work meets the required standards. When the work is fully verified, finalize it by using /gsd-ship to check git status, commit with conventional messaging, and tag the release appropriately. Following shipment, run /gsd-cleanup to identify and safely eliminate unused files, orphan stubs, commented code, and stale artifacts—while ensuring that no Markdown (.md) files are deleted. Additionally, use /gsd-inbox to create a triage.md file and organize captured notes, tickets, and ideas into actionable milestones, then move that triage.md file into the documentation folder. Finally, apply /gsd-graphify to build a local knowledge graph that links files, symbols, documentation, and dependencies, providing a comprehensive view of the project’s structure."

As you manage this project, adhere strictly to the following 8-step execution lifecycle for every fix, refactor, and feature:
1. **Oversee Phase Progression (`/gsd-next`)**: Begin by leveraging `/gsd-next` to oversee overall phase progression and determine immediate priorities.
2. **Draft Milestone Plan (`/gsd-plan-phase`)**: Use `/gsd-plan-phase` to draft a structured milestone plan clearly defining explicit acceptance criteria, task dependencies, and test bars.
3. **Execute Milestone Tasks (`/gsd-execute-phase` with `/impeccable`)**: Once the plan is solid, employ `/gsd-execute-phase` to carry out planned milestone tasks in strict dependency order, ensuring context remains lean and appropriately right-sized throughout execution, and apply `/impeccable` throughout to ensure all generated work is high-quality, intentional, and free of AI slop. After execution, update the Markdown (`.md`) files in `Documentation/`, and if any new `.md` file is created, move it into `Documentation/`.
4. **Deterministic Verification (`/gsd-verify-work`)**: After implementation, use `/gsd-verify-work` to execute automated test suites (`.\gradlew.bat testDebugUnitTest`, `.\gradlew.bat compileDebugAndroidTestKotlin`) and deterministic verification checks against established milestone criteria, confirming that all work meets the required standards.
5. **Shipment & Tagging (`/gsd-ship`)**: When work is fully verified, finalize it by using `/gsd-ship` to check git status, commit with conventional messaging, and tag releases appropriately.
6. **Post-Shipment Cleanup (`/gsd-cleanup`)**: Following shipment, run `/gsd-cleanup` to safely eliminate unused files, orphan stubs, commented code, and stale artifacts—**while ensuring that no Markdown (`.md`) files are deleted**.
7. **Inbox Triage (`/gsd-inbox`)**: Additionally, use `/gsd-inbox` to create a `triage.md` file and organize captured notes, tickets, and ideas into actionable milestones, then move that `triage.md` file into `Documentation/`.
8. **Knowledge Graph (`/gsd-graphify`)**: Finally, apply `/gsd-graphify` to build a local knowledge graph linking files, symbols, documentation, and dependencies, providing a comprehensive view of the project’s structure.

<!-- ASTRYX:START -->
Astryx v0.7.13 · 174 components
CLI: run every command as `npx astryx <cmd>` (shown below as `astryx ...`).

SETUP (once, in app entry e.g. main.tsx) — without these, components render unstyled:
  import "@astryxdesign/core/reset.css";
  import "@astryxdesign/core/astryx.css";

WORKFLOW — start every page from a template, never from scratch:
1. `astryx build "<idea>"` — names the template to scaffold from and the parts it lacks.
2. `astryx template <name> <path>` — scaffold it; keep its frame, gap and padding; replace the content.
3. `astryx template <Block>` for parts it lacks; `astryx component <Name>` to read props before using one.
Changing an existing page? Keep it: skip step 2 and add blocks/components inside its sections.
Before you deliver a build, run `astryx doctor --built <dir>`; don't deliver it if it fails.

RULES:
- No <div> — components handle all layout and spacing.
- Read `astryx docs layout` before changing a template's frame.
- Dense data = rows (Table, List/Item), not Card-wrapped lists. Badge = counts only.
- Style with component props first, then tokens (var(--color-*|--spacing-*|--radius-*)). No raw hex/px. No StyleX/Tailwind compiler here — don't use xstyle/utility classes directly. Some templates use StyleX and need a compiler plugin; see `astryx docs styling-overview`.
- Palettes and custom colors go through the theme, never :root overrides. Run `astryx theme add <slug>` to import; `theme eject <slug>` only forks source. See `astryx docs theme`.
- SELF-CHECK: re-read the file; replace any raw <div>/<span>, imported .css/@apply, or hardcoded #hex/px with the component or a token.

`astryx help` lists every command. Key ones beyond the workflow:
  search "<query>"   find any component / hook / doc / template
  discover <words>   integrations you could add
  docs <topic>       getting-started, principles, tokens, theme … (`astryx docs` lists all)
  docs cli           commands and API reference
  docs authoring     build an integration package, and every field it holds
  upgrade --from <old version> --apply   run after a dependency bump
<!-- ASTRYX:END -->
