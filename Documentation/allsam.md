# Antigravity Master Ecosystem Directory (`allsam.md`) 🌐⚡

> **Comprehensive Reference Catalog of All Installed Agents, Modular Skills & MCP Servers**
> **CoupleFit AI Pair-Programming Environment** | Single Source of Truth

---

## 📊 Executive System Overview

| Dimension | Total Count | Providers & Frameworks |
| :--- | :---: | :--- |
| **Specialist Subagents** | **121** (118 specialist + 3 core) | ECC (71), GSD-Core (35), Google EGM (10), Firebase (1), Motion (1), DeepMind Core (3) |
| **Modular Skills** | **893+** | Across 23 Installed Plugins, Suites & Frameworks |
| **Active System MCP Servers** | **7** | Artemis, Chrome DevTools, Motion, Paper, REA, Expo, Mobbin |
| **Ecosystem MCP Servers** | **8** | Nexus, Itô Compute, Jira, GitHub, Firecrawl, Supabase, Context7, GSD MCP |

---

## 📑 Table of Contents
- [1. Specialist Subagents Directory (121 Agents)](#1-specialist-subagents-directory)
  - [1.1 Core Built-In Agents (3 Agents)](#11-core-built-in-agents)
  - [1.2 ECC Specialized Engineering Agents (71 Agents)](#12-ecc-specialized-engineering-agents)
  - [1.3 GSD-Core Spec-Driven Agents (35 Agents)](#13-gsd-core-spec-driven-agents)
  - [1.4 Google EGM Multi-Persona Swarm (10 Agents)](#14-google-egm-multi-persona-swarm)
  - [1.5 Cloud Security & Animation Specialist Agents (2 Agents)](#15-cloud-security--animation-specialist-agents)
- [2. Modular Skills Directory (1,727+ Skills)](#2-modular-skills-directory)
  - [2.1 UI, Visual Craft, Glassmorphism & Motion](#21-ui-visual-craft-glassmorphism--motion)
  - [2.2 Testing, TDD, Rigor & Systematic Debugging](#22-testing-tdd-rigor--systematic-debugging)
  - [2.3 Software Architecture, System Design & Full-Stack Craft](#23-software-architecture-system-design--full-stack-craft)
  - [2.4 Spec-Driven Development & Milestone Lifecycle (Open GSD)](#24-spec-driven-development--milestone-lifecycle-open-gsd)
  - [2.5 Enterprise Engineering, Multi-Agent Loops & Evals (ECC)](#25-enterprise-engineering-multi-agent-loops--evals-ecc)
  - [2.6 Google Elephant-Goldfish Model Governance (EGM)](#26-google-elephant-goldfish-model-governance-egm)
  - [2.7 Cloud Firebase & Security Engineering](#27-cloud-firebase--security-engineering)
  - [2.8 Stitch UI Generative Suite & Cross-Platform Porting](#28-stitch-ui-generative-suite--cross-platform-porting)
  - [2.9 Mobile App Research & Patterns (Mobbin)](#29-mobile-app-research--patterns-mobbin)
  - [2.10 Garry Tan gstack Founder Suite](#210-garry-tan-gstack-founder-suite)
  - [2.11 Modular UX & Design Ops Library (Designer Skills)](#211-modular-ux--design-ops-library-designer-skills)
  - [2.12 Documents, Media & Tooling (Anthropics)](#212-documents-media--tooling-anthropics)
- [3. Model Context Protocol (MCP) Architecture](#3-model-context-protocol-mcp-architecture)
  - [3.1 Active System MCP Servers & Tool Suites](#31-active-system-mcp-servers--tool-suites)
  - [3.2 Extended Ecosystem MCP Servers Registry](#32-extended-ecosystem-mcp-servers-registry)
- [4. Master Intent Router & Invocation Guide](#4-master-intent-router--invocation-guide)

---

## 1. Specialist Subagents Directory

> Subagents run as autonomous background threads with isolated contexts. Invoke them via `invoke_subagent(TypeName, Role, Prompt)` or configure swarm teams via `swarm(action="add", ...)`.

### 1.1 Core Built-In Agents
| Agent Name | Provider | Default Model | Role & Core Purpose |
| :--- | :--- | :---: | :--- |
| **`self`** | `Antigravity Core` | `inherit` | Subagent that inherits the parent agent's full configuration including tools, system prompt, and model. Used for isolated parallel tasks. |
| **`research`** | `Antigravity Core` | `inherit` | Research subagent with read-only tools for exploring codebase, web documentation, and deep inspection in an isolated sandbox. |
| **`image-generator`** | `Antigravity Core` | `inherit` | Generates or edits visual images and design assets from a brief and replies with the saved file paths. |

### 1.2 ECC Specialized Engineering Agents (71 Agents)
> Source: [github.com/affaan-m/ecc](https://github.com/affaan-m/ecc)

| Agent Name | Model | Primary Specialty & Routing Purpose |
| :--- | :---: | :--- |
| **`a11y-architect`** | `pro` | Accessibility Architect specializing in WCAG 2.2 compliance for Web and Native platforms. Use PROACTIVELY when designing UI components, establishing design systems, or auditing code for inclusive user experiences. |
| **`agent-evaluator`** | `pro` | Evaluates agent output against 5-axis quality rubric (accuracy, completeness, clarity, actionability, conciseness). Use after any non-trivial task when the user wants a quality assessment, or when the agent-self-evaluation skill is active. Produces structured scorecard with evidence and improvement suggestions. |
| **`architect`** | `pro` | Software architecture specialist for system design, scalability, and technical decision-making. Use PROACTIVELY when planning new features, refactoring large systems, or making architectural decisions. |
| **`bengali-reviewer`** | `pro` | Reviews code handling Bengali (Bangla) text for Unicode correctness, proper normalization, script-aware processing, and internationalization best practices. Use when code processes, displays, or stores Bengali text. |
| **`build-error-resolver`** | `pro` | Build and TypeScript error resolution specialist. Use PROACTIVELY when build fails or type errors occur. Fixes build/type errors only with minimal diffs, no architectural edits. Focuses on getting the build green quickly. |
| **`chief-of-staff`** | `pro` | Personal communication chief of staff that triages email, Slack, LINE, and Messenger. Classifies messages into 4 tiers (skip/info_only/meeting_info/action_required), generates draft replies, and enforces post-send follow-through via hooks. Use when managing multi-channel communication workflows. |
| **`code-architect`** | `pro` | Designs feature architectures by analyzing existing codebase patterns and conventions, then providing implementation blueprints with concrete files, interfaces, data flow, and build order. |
| **`code-explorer`** | `pro` | Deeply analyzes existing codebase features by tracing execution paths, mapping architecture layers, and documenting dependencies to inform new development. |
| **`code-reviewer`** | `pro` | Expert code review specialist. Proactively reviews code for quality, security, and maintainability. Use immediately after writing or modifying code. MUST BE USED for all code changes. |
| **`code-simplifier`** | `pro` | Simplifies and refines code for clarity, consistency, and maintainability while preserving behavior. Focus on recently modified code unless instructed otherwise. |
| **`comment-analyzer`** | `flash` | Analyze code comments for accuracy, completeness, maintainability, and comment rot risk. |
| **`commit-reviewer`** | `pro` | Reviews a staged diff and drafts a Conventional Commits-compliant message matching the repository's actual history norms. Use before committing, or when asked to write or check a commit message. |
| **`conversation-analyzer`** | `flash` | Use this agent when analyzing conversation transcripts to find behaviors worth preventing with hooks. Triggered by /hookify without arguments. |
| **`cpp-build-resolver`** | `pro` | C++ build, CMake, and compilation error resolution specialist. Fixes build errors, linker issues, and template errors with minimal changes. Use when C++ builds fail. |
| **`cpp-reviewer`** | `pro` | Expert C++ code reviewer specializing in memory safety, modern C++ idioms, concurrency, and performance. Use for all C++ code changes. MUST BE USED for C++ projects. |
| **`csharp-reviewer`** | `pro` | Expert C# code reviewer specializing in .NET conventions, async patterns, security, nullable reference types, and performance. Use for all C# code changes. MUST BE USED for C# projects. |
| **`dart-build-resolver`** | `pro` | Dart/Flutter build, analysis, and dependency error resolution specialist. Fixes `dart analyze` errors, Flutter compilation failures, pub dependency conflicts, and build_runner issues with minimal, surgical changes. Use when Dart/Flutter builds fail. |
| **`database-reviewer`** | `pro` | PostgreSQL database specialist for query optimization, schema design, security, and performance. Use PROACTIVELY when writing SQL, creating migrations, designing schemas, or troubleshooting database performance. Incorporates Supabase best practices. |
| **`django-build-resolver`** | `pro` | Django/Python build, migration, and dependency error resolution specialist. Fixes pip/Poetry errors, migration conflicts, import errors, Django configuration issues, and collectstatic failures with minimal changes. Use when Django setup or startup fails. |
| **`django-reviewer`** | `pro` | Expert Django code reviewer specializing in ORM correctness, DRF patterns, migration safety, security misconfigurations, and production-grade Django practices. Use for all Django code changes. MUST BE USED for Django projects. |
| **`doc-updater`** | `flash` | Documentation and codemap specialist. Use PROACTIVELY for updating codemaps and documentation. Generates docs/CODEMAPS/*, updates READMEs and guides. Backs the /update-codemaps and /update-docs commands. |
| **`docs-lookup`** | `flash` | When the user asks how to use a library, framework, or API or needs up-to-date code examples, use Context7 MCP to fetch current documentation and return answers with examples. Invoke for docs/API/setup questions. |
| **`e2e-runner`** | `pro` | End-to-end testing specialist using Vercel Agent Browser (preferred) with Playwright fallback. Use PROACTIVELY for generating, maintaining, and running E2E tests. Manages test journeys, quarantines flaky tests, uploads artifacts (screenshots, videos, traces), and ensures critical user flows work. |
| **`fastapi-reviewer`** | `pro` | Reviews FastAPI applications for async correctness, dependency injection, Pydantic schemas, security, OpenAPI quality, testing, and production readiness. |
| **`flutter-reviewer`** | `pro` | Flutter and Dart code reviewer. Reviews Flutter code for widget best practices, state management patterns, Dart idioms, performance pitfalls, accessibility, and clean architecture violations. Library-agnostic — works with any state management solution and tooling. |
| **`fsharp-reviewer`** | `pro` | Expert F# code reviewer specializing in functional idioms, type safety, pattern matching, computation expressions, and performance. Use for all F# code changes. MUST BE USED for F# projects. |
| **`gan-evaluator`** | `pro` | GAN Harness — Evaluator agent. Tests the live running application via Playwright, scores against rubric, and provides actionable feedback to the Generator. |
| **`gan-generator`** | `pro` | GAN Harness — Generator agent. Implements features according to the spec, reads evaluator feedback, and iterates until quality threshold is met. |
| **`gan-planner`** | `pro` | GAN Harness — Planner agent. Expands a one-line prompt into a full product specification with features, sprints, evaluation criteria, and design direction. |
| **`go-build-resolver`** | `pro` | Go build, vet, and compilation error resolution specialist. Fixes build errors, go vet issues, and linter warnings with minimal changes. Use when Go builds fail. |
| **`go-reviewer`** | `pro` | Expert Go code reviewer specializing in idiomatic Go, concurrency patterns, error handling, and performance. Use for all Go code changes. MUST BE USED for Go projects. |
| **`harmonyos-app-resolver`** | `pro` | HarmonyOS application development expert specializing in ArkTS and ArkUI. Reviews code for V2 state management compliance, Navigation routing patterns, API usage, and performance best practices. Use for HarmonyOS/OpenHarmony projects. |
| **`harness-optimizer`** | `pro` | Improve local agent-harness configuration reliability and cost using eval-driven grading (pass@k/pass^k) derived from the eval-harness skill. |
| **`healthcare-reviewer`** | `pro` | Reviews healthcare application code for clinical safety, CDSS accuracy, PHI compliance, and medical data integrity. Specialized for EMR/EHR, clinical decision support, and health information systems. |
| **`homelab-architect`** | `pro` | Designs home and small-lab network plans from hardware inventory, goals, and operator experience level, with safe staged changes and rollback guidance. |
| **`java-build-resolver`** | `pro` | Java/Maven/Gradle build, compilation, and dependency error resolution specialist. Automatically detects Spring Boot or Quarkus and applies framework-specific fixes. Fixes build errors, Java compiler errors, and Maven/Gradle issues with minimal changes. Use when Java builds fail. |
| **`java-reviewer`** | `pro` | Expert Java code reviewer for Spring Boot and Quarkus projects. Automatically detects the framework and applies the appropriate review rules. Covers layered architecture, JPA/Panache, MongoDB, security, and concurrency. MUST BE USED for all Java code changes. |
| **`kotlin-build-resolver`** | `pro` | Kotlin/Gradle build, compilation, and dependency error resolution specialist. Fixes build errors, Kotlin compiler errors, and Gradle issues with minimal changes. Use when Kotlin builds fail. |
| **`kotlin-reviewer`** | `pro` | Kotlin and Android/KMP code reviewer. Reviews Kotlin code for idiomatic patterns, coroutine safety, Compose best practices, clean architecture violations, and common Android pitfalls. |
| **`loop-operator`** | `pro` | Operate autonomous agent loops, monitor progress, and intervene safely when loops stall. |
| **`marketing-agent`** | `pro` | Marketing strategist and copywriter for campaign planning, audience research, positioning, copy creation, and content review. Covers landing pages, email sequences, social posts, ad copy, short-form video scripts, and content calendars. Use when the user wants to plan or execute a product launch or marketing campaign. |
| **`mle-reviewer`** | `pro` | Production machine-learning engineering reviewer for data contracts, feature pipelines, training reproducibility, offline/online evaluation, model serving, monitoring, and rollback. Use when ML, MLOps, model training, inference, feature store, or evaluation code changes. |
| **`network-architect`** | `pro` | Designs enterprise or multi-site network architecture from requirements, using existing network skills for focused routing, validation, automation, and troubleshooting detail. |
| **`network-config-reviewer`** | `pro` | Reviews router and switch configurations for security, correctness, stale references, risky change-window commands, and missing operational guardrails. |
| **`network-troubleshooter`** | `pro` | Diagnoses network connectivity, routing, DNS, interface, and policy symptoms with a read-only OSI-layer workflow and evidence-backed root cause summary. |
| **`opensource-forker`** | `flash` | Fork any project for open-sourcing. Copies files, strips secrets and credentials (20+ patterns), replaces internal references with placeholders, generates .env.example, and cleans git history. First stage of the opensource-pipeline skill. |
| **`opensource-packager`** | `flash` | Generate complete open-source packaging for a sanitized project. Produces CLAUDE.md, setup.sh, README.md, LICENSE, CONTRIBUTING.md, and GitHub issue templates. Makes any repo immediately usable with Claude Code. Third stage of the opensource-pipeline skill. |
| **`opensource-sanitizer`** | `pro` | Verify an open-source fork is fully sanitized before release. Scans for leaked secrets, PII, internal references, and dangerous files using 20+ regex patterns. Generates a PASS/FAIL/PASS-WITH-WARNINGS report. Second stage of the opensource-pipeline skill. Use PROACTIVELY before any public release. |
| **`performance-optimizer`** | `pro` | Performance analysis and optimization specialist. Use PROACTIVELY for identifying bottlenecks, optimizing slow code, reducing bundle sizes, and improving runtime performance. Profiling, memory leaks, render optimization, and algorithmic improvements. |
| **`php-reviewer`** | `pro` | Expert PHP code reviewer specializing in PSR-12 compliance, PHP type system, Eloquent ORM patterns, security, and performance. Use for all PHP code changes. MUST BE USED for PHP projects. |
| **`planner`** | `pro` | Expert planning specialist for complex features and refactoring. Use PROACTIVELY when users request feature implementation, architectural changes, or complex refactoring. Automatically activated for planning tasks. |
| **`pr-test-analyzer`** | `pro` | Review pull request test coverage quality and completeness, with emphasis on behavioral coverage and real bug prevention. |
| **`python-reviewer`** | `pro` | Expert Python code reviewer specializing in PEP 8 compliance, Pythonic idioms, type hints, security, and performance. Use for all Python code changes. MUST BE USED for Python projects. |
| **`pytorch-build-resolver`** | `pro` | PyTorch runtime, CUDA, and training error resolution specialist. Fixes tensor shape mismatches, device errors, gradient issues, DataLoader problems, and mixed precision failures with minimal changes. Use when PyTorch training or inference crashes. |
| **`rag-pipeline-reviewer`** | `pro` | Reviews RAG (Retrieval-Augmented Generation) pipelines for retrieval quality, chunking strategy, embedding choices, and evaluation coverage. Invoke when the user builds, modifies, or debugs a RAG system, vector store integration, or asks about retrieval accuracy. |
| **`react-build-resolver`** | `pro` | Diagnose and fix React build failures across Vite, webpack, Next.js, CRA, Parcel, esbuild, and Bun. Handles JSX/TSX compile errors, hydration mismatches, server/client component boundary failures, missing types, and bundler-specific configuration issues with minimal, surgical changes. MUST BE USED when a React build fails. |
| **`react-reviewer`** | `pro` | Expert React/JSX code reviewer specializing in hook correctness, render performance, server/client component boundaries, accessibility, and React-specific security. Use for any change touching .tsx/.jsx files or React component logic. MUST BE USED for React projects. |
| **`refactor-cleaner`** | `pro` | Dead code cleanup and consolidation specialist. Use PROACTIVELY for removing unused code, duplicates, and refactoring. Runs analysis tools (knip, depcheck, ts-prune) to identify dead code and safely removes it. |
| **`ruby-reviewer`** | `pro` | Expert Ruby and Rails code reviewer specializing in idiomatic Ruby, Active Record query safety, Rails security defaults, and RSpec/Minitest quality. Use for all Ruby code changes. MUST BE USED for Ruby and Rails projects. |
| **`rust-build-resolver`** | `pro` | Rust build, compilation, and dependency error resolution specialist. Fixes cargo build errors, borrow checker issues, and Cargo.toml problems with minimal changes. Use when Rust builds fail. |
| **`rust-reviewer`** | `pro` | Expert Rust code reviewer specializing in ownership, lifetimes, error handling, unsafe usage, and idiomatic patterns. Use for all Rust code changes. MUST BE USED for Rust projects. |
| **`security-reviewer`** | `pro` | Security vulnerability detection and remediation specialist. Use PROACTIVELY after writing code that handles user input, authentication, API endpoints, or sensitive data. Flags secrets, SSRF, injection, unsafe crypto, and OWASP Top 10 vulnerabilities. |
| **`seo-specialist`** | `pro` | SEO specialist for technical SEO audits, on-page optimization, structured data, Core Web Vitals, and content/keyword mapping. Use for site audits, meta tag reviews, schema markup, sitemap and robots issues, and SEO remediation plans. |
| **`silent-failure-hunter`** | `pro` | Review code for silent failures, swallowed errors, bad fallbacks, and missing error propagation. |
| **`spec-miner`** | `pro` | Extracts behavioral specs from existing codebases for OpenSpec. Produces flat Requirement and Invariant blocks with structured metadata (entities, enforced, id, test anchors). Outputs openspec/specs/<capability>/spec.md. Fully self-bootstrapping — no dependency on codebase-onboarding. Use when onboarding a brownfield project to spec-driven development. |
| **`swift-build-resolver`** | `pro` | Swift/Xcode build, compilation, and dependency error resolution specialist. Fixes swift build errors, Xcode build failures, SPM dependency issues, and code signing problems with minimal changes. Use when Swift builds fail. |
| **`swift-reviewer`** | `pro` | Expert Swift code reviewer specializing in protocol-oriented design, value semantics, ARC memory management, Swift Concurrency, and idiomatic patterns. Use for all Swift code changes. MUST BE USED for Swift projects. |
| **`tdd-guide`** | `pro` | Test-Driven Development specialist enforcing write-tests-first methodology. Use PROACTIVELY when writing new features, fixing bugs, or refactoring code. Ensures 80%+ test coverage. |
| **`type-design-analyzer`** | `pro` | Analyze type design for encapsulation, invariant expression, usefulness, and enforcement. |
| **`typescript-reviewer`** | `pro` | Expert TypeScript/JavaScript code reviewer specializing in type safety, async correctness, Node/web security, and idiomatic patterns. Use for all TypeScript and JavaScript code changes. MUST BE USED for TypeScript/JavaScript projects. |
| **`vue-reviewer`** | `pro` | Expert Vue.js code reviewer specializing in Composition API correctness, reactivity pitfalls, component architecture, template security, and Vue-specific performance. Use for any change touching .vue, .ts/.js files with Vue imports, or Vue ecosystem code (Pinia, Vue Router, Nuxt). MUST BE USED for Vue projects. |

### 1.3 GSD-Core Spec-Driven Agents (35 Agents)
> Source: [github.com/open-gsd/gsd-core](https://github.com/open-gsd/gsd-core)

| Agent Name | Model | Primary Specialty & Routing Purpose |
| :--- | :---: | :--- |
| **`gsd-advisor-researcher`** | `pro` | Researches a single gray area decision and returns a structured comparison table with rationale. Spawned by discuss-phase advisor mode. |
| **`gsd-ai-researcher`** | `pro` | Researches a chosen AI framework's official docs to produce implementation-ready guidance — best practices, syntax, core patterns, and pitfalls distilled for the specific use case. Writes the Framework Quick Reference and Implementation Guidance sections of AI-SPEC.md. Spawned by /gsd:ai-integration-phase orchestrator. |
| **`gsd-assumptions-analyzer`** | `pro` | Deeply analyzes codebase for a phase and returns structured assumptions with evidence. Spawned by discuss-phase assumptions mode. |
| **`gsd-code-fixer`** | `pro` | Applies fixes to code review findings from REVIEW.md. Reads source files, applies intelligent fixes, and commits each fix atomically. Spawned by /gsd:code-review --fix. |
| **`gsd-code-reviewer`** | `pro` | Reviews source files for bugs, security issues, and code quality problems. Produces structured REVIEW.md with severity-classified findings. Spawned by /gsd:code-review. |
| **`gsd-codebase-mapper`** | `pro` | Explores codebase and writes structured analysis documents. Spawned by map-codebase with a focus area (tech, arch, quality, concerns). Writes documents directly to reduce orchestrator context load. |
| **`gsd-debug-session-manager`** | `pro` | Manages multi-cycle /gsd:debug checkpoint and continuation loop in isolated context. Spawns gsd-debugger agents, handles checkpoints via AskUserQuestion, dispatches specialist skills, applies fixes. Returns compact summary to main context. Spawned by /gsd:debug command. |
| **`gsd-debugger`** | `pro` | Investigates bugs using scientific method, manages debug sessions, handles checkpoints. Spawned by /gsd:debug orchestrator. |
| **`gsd-doc-classifier`** | `pro` | Classifies a single planning document as ADR, PRD, SPEC, DOC, or UNKNOWN. Extracts title, scope summary, and cross-references. Spawned in parallel by /gsd:ingest-docs. Writes a JSON classification file and returns a one-line confirmation. |
| **`gsd-doc-synthesizer`** | `pro` | Synthesizes classified planning docs into a single consolidated context. Applies precedence rules, detects cross-ref cycles, enforces LOCKED-vs-LOCKED hard-blocks, and writes INGEST-CONFLICTS.md with three buckets (auto-resolved, competing-variants, unresolved-blockers). Spawned by /gsd:ingest-docs. |
| **`gsd-doc-verifier`** | `pro` | Verifies factual claims in generated docs against the live codebase. Returns structured JSON per doc. |
| **`gsd-doc-writer`** | `pro` | Writes and updates project documentation. Spawned with a doc_assignment block specifying doc type, mode (create/update/supplement), and project context. |
| **`gsd-dom-verifier`** | `pro` | Verifies live-DOM acceptance criteria for a completed execution wave using a browser MCP server. Writes DOM-VERIFY.md. Additive — never blocks a wave. Spawned by the live-dom-uat capability at execute:wave:post. |
| **`gsd-domain-researcher`** | `pro` | Researches the business domain and real-world application context of the AI system being built. Surfaces domain expert evaluation criteria, industry-specific failure modes, regulatory context, and what "good" looks like for practitioners in this field — before the eval-planner turns it into measurable rubrics. Spawned by /gsd:ai-integration-phase orchestrator. |
| **`gsd-eval-auditor`** | `pro` | Retroactive audit of an implemented AI phase's evaluation coverage. Checks implementation against the AI-SPEC.md evaluation plan. Scores each eval dimension as COVERED/PARTIAL/MISSING. Produces a scored EVAL-REVIEW.md with findings, gaps, and remediation guidance. Spawned by /gsd:eval-review orchestrator. |
| **`gsd-eval-planner`** | `pro` | Designs a structured evaluation strategy for an AI phase. Identifies critical failure modes, selects eval dimensions with rubrics, recommends tooling, and specifies the reference dataset. Writes the Evaluation Strategy, Guardrails, and Production Monitoring sections of AI-SPEC.md. Spawned by /gsd:ai-integration-phase orchestrator. |
| **`gsd-executor`** | `pro` | Executes GSD plans with atomic commits, deviation handling, checkpoint protocols, and state management. Spawned by execute-phase orchestrator or execute-plan command. |
| **`gsd-framework-selector`** | `pro` | Presents an interactive decision matrix to surface the right AI/LLM framework for the user's specific use case. Produces a scored recommendation with rationale. Spawned by /gsd:ai-integration-phase and /gsd-select-framework orchestrators. |
| **`gsd-integration-checker`** | `pro` | Verifies cross-phase integration and E2E flows. Checks that phases connect properly and user workflows complete end-to-end. |
| **`gsd-intel-updater`** | `pro` | Analyzes codebase and writes structured intel files to .planning/intel/. |
| **`gsd-mempalace-curator`** | `pro` | Ship-time MemPalace curation — writes the session diary, proposes/creates cross-project tunnels, mirrors extract-learnings into the temporal KG, and runs wing-scoped drawer pruning. Spawned at ship:post by the mempalace capability. |
| **`gsd-nyquist-auditor`** | `pro` | Fills Nyquist validation gaps by generating tests and verifying coverage for phase requirements |
| **`gsd-pattern-mapper`** | `pro` | Analyzes codebase for existing patterns and produces PATTERNS.md mapping new files to closest analogs. Read-only codebase analysis spawned by /gsd:plan-phase orchestrator before planning. |
| **`gsd-phase-researcher`** | `pro` | Researches how to implement a phase before planning. Produces RESEARCH.md consumed by gsd-planner. Spawned by /gsd:plan-phase orchestrator. |
| **`gsd-plan-checker`** | `pro` | Verifies plans will achieve phase goal before execution. Goal-backward analysis of plan quality. Spawned by /gsd:plan-phase orchestrator. |
| **`gsd-planner`** | `pro` | Creates executable phase plans with task breakdown, dependency analysis, and goal-backward verification. Spawned by /gsd:plan-phase orchestrator. |
| **`gsd-project-researcher`** | `pro` | Researches domain ecosystem before roadmap creation. Produces files in .planning/research/ consumed during roadmap creation. Spawned by /gsd:new-project or /gsd:new-milestone orchestrators. |
| **`gsd-research-synthesizer`** | `pro` | Synthesizes research outputs from parallel researcher agents into SUMMARY.md. Spawned by /gsd:new-project after 4 researcher agents complete. |
| **`gsd-roadmapper`** | `pro` | Creates project roadmaps with phase breakdown, requirement mapping, success criteria derivation, and coverage validation. Spawned by /gsd:new-project orchestrator. |
| **`gsd-security-auditor`** | `pro` | Verifies threat mitigations from PLAN.md threat model exist in implemented code. Returns structured security verdict (SECURED / OPEN_THREATS / ESCALATE). Spawned by /gsd:secure-phase. |
| **`gsd-ui-auditor`** | `pro` | Retroactive 6-pillar visual audit of implemented frontend code. Produces scored UI-REVIEW.md. Spawned by /gsd:ui-review orchestrator. |
| **`gsd-ui-checker`** | `pro` | Validates UI-SPEC.md design contracts against 7 quality dimensions. Produces BLOCK/FLAG/PASS verdicts. Spawned by /gsd:ui-phase orchestrator. |
| **`gsd-ui-researcher`** | `pro` | Produces UI-SPEC.md design contract for frontend phases. Reads upstream artifacts, detects design system state, asks only unanswered questions. Spawned by /gsd:ui-phase orchestrator. |
| **`gsd-user-profiler`** | `pro` | Analyzes extracted session messages across 8 behavioral dimensions to produce a scored developer profile with confidence levels and evidence. Spawned by profile orchestration workflows. |
| **`gsd-verifier`** | `pro` | Verifies phase goal achievement through goal-backward analysis. Checks codebase delivers what phase promised, not just that tasks completed. Creates VERIFICATION.md report. |

### 1.4 Google EGM Multi-Persona Swarm (10 Agents)
> Source: [github.com/google/elephant-goldfish-model](https://github.com/google/elephant-goldfish-model)

| Agent Name | Model | Multi-Persona Architectural Role |
| :--- | :---: | :--- |
| **`egm-bootstrap-orchestrator`** | `pro` | Orchestrates directory README documentation, partitions subtrees, and coordinates worker subagents. |
| **`egm-codebase-details`** | `pro` | Pedantic Technical Quality Auditor verifying that documentation accurately and exhaustively matches actual codebase implementation and existing library reuse opportunities. |
| **`egm-comprehension`** | `pro` | Naive Reader persona auditing documents with zero assumed institutional knowledge, flagging undefined terms, acronyms, and comprehension gaps. |
| **`egm-design`** | `pro` | Hostile Design Critic persona focused on system architecture, scalability, infrastructure economics, and unmitigated risks. |
| **`egm-implementation-readiness`** | `pro` | Pragmatic Tech Lead persona verifying that implementation plans contain unambiguous, fully actionable execution instructions without leaving design decisions to the implementer. |
| **`egm-inventive-catalyst`** | `pro` | Inventive problem-solving Goldfish persona applying TRIZ principles to identify and eliminate system-level contradictions without compromise. |
| **`egm-structural`** | `pro` | Structural Layout Validator and Document Architect persona focused on symmetry, heading hierarchy, formatting, and template compliance. |
| **`egm-style`** | `pro` | Technical Copy Editor persona focused on clarity, precision, brevity, objective framing, and stripping fluff. |
| **`egm-synthesizer`** | `pro` | Intermediate Swarm Compiler and Aggregator coordinating parallel Goldfish reviewers, deduplicating findings, and compiling deliverable reports. |
| **`egm-zoom-out`** | `pro` | Skeptical Architect persona focused on high-altitude simplicity, minimalism, YAGNI, and codebase reuse. |

### 1.5 Cloud Security & Animation Specialist Agents (2 Agents)
| Agent Name | Suite | Model | Primary Specialty & Routing Purpose |
| :--- | :--- | :---: | :--- |
| **`firestore-rules-author`** | `Firebase Security Suite` | `pro` | MANDATORY specialist subagent for Cloud Firestore Security Rules (firestore.rules). Whenever your task requires creating, authoring, or modifying Firestore Security Rules (firestore.rules), you MUST delegate rules authoring to this subagent rather than writing firestore.rules directly in the main agent. |
| **`motion-reviewer`** | `Motion Animation Suite` | `pro` | Audits animation performance across a directory or project and reports MotionScore tiers. Use when the scope is more than one file — discovery pulls in a lot of source, and each area can be audited independently. |

---

## 2. Modular Skills Directory

> Skills are on-demand engineering runbooks, specialized scripts, and architectural methodologies loaded into memory upon slash command trigger or semantic intent match.

### 2.1 UI, Visual Craft, Glassmorphism & Motion
| Skill / Command | Plugin Suite | Operational Focus |
| :--- | :--- | :--- |
| `/animate` | `emilkowalski-skills` | Build an animation from scratch, making the decisions in the order that determines whether it feels right — should it animate at all, what purpose, which tool, which properties, which curve and duration, how it interrupts, how it exits. Writes the implementation. Use when asked to animate something, add motion, make a component feel alive, or build a transition. For critiquing existing motion use review-animations; for auditing a whole codebase use improve-animations. |
| `/animate-expo` | `emilkowalski-skills` | Build animations in React Native and Expo, making the decisions in the order that determines whether they feel right — should it animate, which thread it runs on, which properties, spring or timing, how the gesture hands off, how it degrades. Writes the implementation with Reanimated, Gesture Handler, Expo Router and expo-haptics. Use when animating anything in an Expo app, adding gestures, sheets, screen transitions, press feedback or haptics, or fixing motion that stutters on device. For web animation use `animate`. |
| `/animation-performance` | `emilkowalski-skills` | Keep web animations inside the frame budget — animate composite-only properties, keep motion off the main thread when the page is busy, and find the cause when an animation drops frames. Covers the layout/paint/composite pipeline, cheaper property swaps, CSS vs WAAPI vs requestAnimationFrame, Motion's hardware-accelerated path, React re-rendering per frame, inherited CSS variable recalculation, blur cost, and when `will-change` actually helps. Use when an animation is janky, choppy, stutters, or drops frames; when choosing which property to animate; when deciding between CSS and JS animation on performance grounds; or when reaching for `will-change` or GPU layers. For choosing easing, duration, or whether to animate at all use animate; for reviewing motion as a whole use review-animations. |
| `/animation-vocabulary` | `emilkowalski-skills` | Reverse-lookup glossary that turns a vague description of a web animation or motion effect into its exact term ("the bouncy thing when a popover opens" → Pop in; "the iOS rubber-band scroll" → Rubber-banding). Use when the user asks "what's it called when…", or describes a motion effect without knowing its name and wants the right word to prompt an AI or designer with. For naming an effect, not designing or building one. |
| `/apple-design` | `emilkowalski-skills` | Apple's approach to interface design and fluid, physical motion, translated for the web. Use when building or reviewing gesture-driven UI, spring animations, drag/swipe/sheet interactions, momentum and interruptible transitions, translucent materials and depth, typography (optical sizing, tracking, leading), reduced-motion, or the design foundations (feedback, spatial consistency, restraint) behind Apple-style interfaces. |
| `/ask-sonner` | `emilkowalski-skills` | Guide to Sonner, the React toast library — install and wire up the Toaster, pick the right toast() call, promise and loading toasts, updating, dismissing and persisting toasts, styling, theming and icons, positioning and multiple toasters. Use when working with Sonner or troubleshooting it — toasts that don't appear, appear twice, lose their styles, ignore Tailwind classes, sit behind a modal, or don't follow dark mode. |
| `/break-ui` | `emilkowalski-skills` | Try to break a piece of UI by feeding it worst-case data — long names, unbreakable emails, one-letter names, missing fields, huge counts, zero items, long labels, non-Latin text, emoji, extreme numbers — then render it behind a "Demo data / Worst case" toggle and report everything that broke, with the fix for each. Use when the user asks to stress-test, break, or find edge cases in a component or screen, or to "try the worst case". For visual design critique use emil-design-eng; for motion use review-animations. |
| `/choreograph` | `emilkowalski-skills` | Coordinate several elements that animate together — decide what moves, in what order, how much they overlap, and what stays still — so a group reads as one intentional motion instead of a pile of separate animations. Use when building or fixing a staggered list, a hero entrance, a view swap inside a container, a multi-step flow, an element leaving while its siblings close the gap, a morph between rich states, or any interaction with several moving parts (icon + background + lines, panel + content, items flying into a target). Triggers on — stagger, staggerChildren, delayChildren, animation-delay, sequence, sequencing, orchestration, choreography, cascade, wave, timeline, "one after another", "at the same time", crossfade, popLayout, mode="wait", AnimatePresence mode, hero entrance, list entrance, multi-step, step transition, morph, useAnimate, Promise.all animations, "feels mechanical", "feels robotic", "too much going on", "feels slow to open". |
| `/emil-design-eng` | `emilkowalski-skills` | This skill encodes Emil Kowalski's philosophy on UI polish, component design, animation decisions, and the invisible details that make software feel great. |
| `/find-animation-opportunities` | `emilkowalski-skills` | Search a codebase or UI for places that don't animate but should, and reject everything that shouldn't. Read-only; it proposes motion with exact values, it does not implement it. Use when the user asks "what could be animated here?" or wants to "make this feel more alive". For fixing existing animations, use improve-animations or review-animations instead. |
| `/gestures` | `emilkowalski-skills` | Build drag, swipe, and sheet interactions that track the finger 1:1 and stay interruptible. Use when building or fixing anything the user drags, swipes, flings, holds, or dismisses with a gesture — drawers, sheets, swipe-to-dismiss toasts and cards, drag-to-reorder, pull-to-refresh, hold-to-confirm — or when a gesture feels laggy, disconnected from the finger, jumps on release, or can't be caught mid-animation. Triggers on — gesture, drag, draggable, swipe, swipe to dismiss, fling, momentum, velocity, drawer, sheet, bottom sheet, snap points, Vaul, dragConstraints, dragElastic, dragMomentum, onDragEnd, whileDrag, useMotionValue, rubber-band, overscroll, pull to refresh, hold to delete, press and hold, touch-action, dismiss threshold, "doesn't follow my finger", "jumps when I let go", "can't grab it mid-animation". |
| `/improve-animations` | `emilkowalski-skills` | Survey a codebase's animation and motion code as a senior motion advisor, then produce a prioritized audit and self-contained implementation plans for other agents (or cheaper models) to execute. Read-only on source code — it plans improvements, it does not apply them. Use when the user asks to "improve the animations", "audit the motion", "make this app feel better", or wants a roadmap of animation fixes rather than a review of a single diff. |
| `/mobile-native` | `emilkowalski-skills` | Make a web app feel native on a phone — the small CSS and meta-tag fixes that separate "a website in a browser" from something that feels installed. Covers sticky hover states, tap highlight flashes, the 100vh bug, inputs that zoom the page, laggy taps, pull-to-refresh hijacking scroll, content under the notch, long-press selecting button text, carousels that scroll the wrong way, mismatched status bars, and the rule that you test on real hardware. Use when a web app is being built for or reviewed on mobile, when something "works in Chrome but feels wrong on my phone", when building a PWA, a bottom sheet, a carousel, a full-screen layout, or any touch interaction. For motion itself use animate; for React Native use animate-expo. |
| `/pick-ui-library` | `emilkowalski-skills` | Pick the right library for a given frontend task from a curated, opinionated list — numbers, OTP inputs, charts, command menus, virtualization, drag and drop, toasts, state, styling, and more. Only runs when explicitly invoked; it does not trigger on its own. |
| `/prototype` | `emilkowalski-skills` | Build multiple genuinely different versions of a UI piece you describe, rendered behind a visual picker so you can flip through them live and promote the one that feels right. Only runs when explicitly invoked; it does not trigger on its own. |
| `/remove-animations` | `emilkowalski-skills` | Find the animations an interface doesn't need and remove them — motion on keyboard actions, high-frequency surfaces, functional data, and decoration with no purpose — then clean up what they leave behind. Edits code, but only to subtract; it never adds or retunes motion. Use when the user says the app "feels slow", "has too much animation", "feels over-animated", wants to "tone down the motion", or asks what animations to remove. For finding places that should animate, use find-animation-opportunities; for fixing animations that should stay, use improve-animations or review-animations. |
| `/review-animations` | `emilkowalski-skills` | Reviews animation and motion code against a high craft bar derived from Emil Kowalski's design engineering philosophy. Default to flagging; approval is earned. |
| `/write-swift` | `emilkowalski-skills` | How to write modern Swift well — modeling with value types, Swift 6 data-race safety and approachable concurrency (@concurrent, main-actor-by-default, actors, task groups), protocols and generics (some vs any), API design, performance and ARC, Swift Testing, macros, and the modern language features agents don't know about yet. Use when writing, reviewing, or migrating Swift, or when a concurrency error, a hang, a data race, a retain cycle, or a performance problem needs fixing. |
| `/impeccable` | `impeccable` | Use when the user wants to design, redesign, shape, critique, audit, polish, simplify, harden, animate, or otherwise improve a frontend interface: websites, landing pages, dashboards, product UI, components, forms, settings, onboarding, empty states, and native iOS or Android screens. Handles UX review, hierarchy, layout, typography, color, motion, UX copy, accessibility, responsive behavior, edge cases, and capturing or documenting design systems (DESIGN.md, tokens). Also for making bland UI bolder, loud UI quieter, or ambitious visual effects. Not for backend-only or non-UI tasks. |
| `/banner-design` | `ui-ux-pro-max-skill` | Design banners for social media, ads, website heroes, creative assets, and print. Multiple art direction options with optional generated or supplied visuals. Actions: design, create, generate banner. Platforms: Facebook, Twitter/X, LinkedIn, YouTube, Instagram, Google Display, website hero, print. Styles: minimalist, gradient, bold typography, photo-based, illustrated, geometric, retro, glassmorphism, 3D, neon, duotone, editorial, collage. |
| `/brand` | `ui-ux-pro-max-skill` | Brand voice, visual identity, messaging frameworks, asset management, brand consistency. Activate for branded content, tone of voice, marketing assets, brand compliance, style guides. |
| `/design` | `ui-ux-pro-max-skill` | Comprehensive design skill: brand identity, design tokens, UI styling, logo generation (55 styles, Gemini, Atlas Cloud, or MuAPI AI), corporate identity program (50 deliverables, CIP mockups), HTML presentations (Chart.js), banner design (22 styles, social/ads/web/print), icon design (15 styles, SVG, Gemini 3.1 Pro), social photos (HTML→screenshot, multi-platform). Actions: design logo, create CIP, generate mockups, build slides, design banner, generate icon, create social photos, social media images, brand identity, design system. Platforms: Facebook, Twitter, LinkedIn, YouTube, Instagram, Pinterest, TikTok, Threads, Google Ads. |
| `/design-system` | `ui-ux-pro-max-skill` | Token architecture, component specifications, and slide generation. Three-layer tokens (primitive→semantic→component), CSS variables, spacing/typography scales, component specs, strategic slide creation. Use for design tokens, systematic design, brand-compliant presentations. |
| `/slides` | `ui-ux-pro-max-skill` | Create strategic HTML presentations with Chart.js, design tokens, responsive layouts, copywriting formulas, and contextual slide strategies. |
| `/ui-styling` | `ui-ux-pro-max-skill` | Create beautiful, accessible user interfaces with shadcn/ui components (built on Radix UI + Tailwind), Tailwind CSS utility-first styling, and canvas-based visual designs. Use when building user interfaces, implementing design systems, creating responsive layouts, adding accessible components (dialogs, dropdowns, forms, tables), customizing themes and colors, implementing dark mode, generating visual designs and posters, or establishing consistent styling patterns across applications. |
| `/ui-ux-pro-max` | `ui-ux-pro-max-skill` | UI/UX design intelligence for web, mobile, and desktop. This skill should be used when designing, building, reviewing, or fixing interfaces, including pages, components, design systems, accessibility, interaction, responsive layout, typography, color, charts, and stack-specific UI implementation. Searchable local data: 79 searchable styles (50 active), 192 product palettes and reasoning profiles, 74 font pairings, 119 UX guidelines, 105 icons, 17 GSAP presets, 25 chart types, and 22 stacks. |
| `/antislop` | `antislop` | Anti Slop: Rules for AI Coding Agents. The core filter. Load always to stop generic AI slop. |
| `/antislop-code` | `antislop` | Code comment hygiene for AI coding agents: remove generic AI-slop comments, keep the valuable ones, never touch the code. |
| `/antislop-copywriting` | `antislop` | Copy and text skill for antislop. Use when writing or editing prose: headlines, tone, CTAs, and anti-AI-writing patterns. Load with the core. |
| `/antislop-human` | `antislop` | Human and accessibility skill for antislop. Contrast, keyboard, focus, states, and interaction affordances for real people. Includes the contrast checker. |
| `/antislop-layoutmobile` | `antislop` | Mobile layout skill for antislop. Use for layouts that reflow across screen sizes, phone to desktop: grids, overflow, tap targets. Load with the core. |
| `/antislop-ui` | `antislop` | UI and visual skill for antislop. Use when building or editing any interface: color, layout, components, motion, and page performance. Load with the core. |
| `/design-taste-frontend` | `taste-skill` | >- |
| `/huashu-design` | `huashu-design` | HuaShu Design — Studio-grade visual design engine using HTML for high-fidelity prototypes, 1920x1080 slide decks, animations, infographics, and expert design critiques. 100% enforces a mandatory 3-Direction Gate (producing 3 distinct initial concepts for user selection before full implementation). Triggers on: prototype, slide deck, presentation, pitch deck, motion design, visual style exploration, design critique, UI mockup, MP4/GIF export, make something beautiful. Not for backend-heavy or production web applications. |
| `/liqui-design` | `liqui-design` | Liqui Design liquid glass design system and component catalog. Surfaces that refract what is behind them with displacement maps, specular rims, adaptive frosted materials, and a two-material philosophy for React (shadcn/Base UI) and native Jetpack Compose. |
| `/motion` | `motion` | > |

### 2.2 Testing, TDD, Rigor & Systematic Debugging
| Skill / Command | Plugin Suite | Operational Focus |
| :--- | :--- | :--- |
| `/brainstorming` | `superpowers` | You MUST use this before any creative work - creating features, building components, adding functionality, modifying behavior, or planning anything new, in software or out of it (a talk, a business, a renovation). |
| `/diagnosing-superpowers` | `superpowers` | Use when a superpowers session went wrong and your human partner wants to know why — repeated work, ignored plans, stumbles, poor results, a skill that didn't fire, "it took too long", "why is it so expensive", "what is it doing" — or wants to build a bug report for the superpowers maintainers, for the current session or a past one identified by id or path, on any harness. |
| `/dispatching-parallel-agents` | `superpowers` | Use when facing 2+ independent tasks that can be worked on without shared state or sequential dependencies |
| `/executing-plans` | `superpowers` | Use when executing an implementation plan in the current session as the implementer yourself — your human partner chose inline execution, or no subagent tool is available |
| `/finishing-a-development-branch` | `superpowers` | Use when implementation is complete, all tests pass, and you need to decide how to integrate the work |
| `/receiving-code-review` | `superpowers` | Use when receiving code review feedback, before implementing suggestions, especially if feedback seems unclear or technically questionable - requires technical rigor and verification, not performative agreement or blind implementation |
| `/requesting-code-review` | `superpowers` | Use when completing tasks, implementing major features, or before merging to verify work meets requirements |
| `/subagent-driven-development` | `superpowers` | Use when executing implementation plans with independent tasks in the current session |
| `/systematic-debugging` | `superpowers` | Use when encountering any bug, test failure, or unexpected behavior, before proposing fixes |
| `/test-driven-development` | `superpowers` | Use when implementing any feature or bugfix, before writing implementation code |
| `/using-git-worktrees` | `superpowers` | Use when starting feature work that needs isolation from current workspace or before executing implementation plans - ensures an isolated workspace exists via native tools or git worktree fallback |
| `/using-superpowers` | `superpowers` | Use when starting any conversation - establishes how to find and use skills, requiring skill invocation before ANY response including clarifying questions |
| `/verification-before-completion` | `superpowers` | Use when about to claim work is complete, fixed, or passing, before committing or creating PRs - requires running verification commands and confirming output before making any success claims; evidence before assertions always |
| `/writing-plans` | `superpowers` | Use when you have a spec or requirements for a multi-step task, before touching code |
| `/writing-skills` | `superpowers` | Use when creating new skills, editing existing skills, or verifying skills work before deployment |
| `/playwright-cli` | `playwright-cli` | Automate browser interactions, test web pages and work with Playwright tests. |

### 2.3 Software Architecture, System Design & Full-Stack Craft
| Skill / Command | Plugin Suite | Operational Focus |
| :--- | :--- | :--- |
| `/api-and-interface-design` | `agent-skills` | Guides stable API and interface design. Use when designing APIs, module boundaries, or any public interface. Use when creating REST or GraphQL endpoints, defining type contracts between modules, or establishing boundaries between frontend and backend. |
| `/browser-testing-with-devtools` | `agent-skills` | Tests in real browsers via Chrome DevTools MCP. Use when building or debugging anything that runs in a browser. Use when you need to inspect the DOM, capture console errors, analyze network requests, profile performance, or verify visual output with real runtime data. Requires the chrome-devtools MCP server to be configured. |
| `/ci-cd-and-automation` | `agent-skills` | Automates CI/CD pipeline setup. Use when setting up or modifying build and deployment pipelines. Use when you need to automate quality gates, configure test runners in CI, or establish deployment strategies. |
| `/code-review-and-quality` | `agent-skills` | Conducts multi-axis code review. Use before merging any change. Use when reviewing code written by yourself, another agent, or a human. Use when you need to assess code quality across multiple dimensions before it enters the main branch. Use when asked to review a diff or a pull request, even when the diff is pasted inline. |
| `/code-simplification` | `agent-skills` | Simplifies code for clarity. Use when refactoring code for clarity without changing behavior. Use when code works but is harder to read, maintain, or extend than it should be. Use when reviewing code that has accumulated unnecessary complexity. |
| `/constraint-driven-development` | `agent-skills` | Establishes a project's quality bar as a written contract and stops agents quietly lowering it. Interviews the user on which dimensions matter, supplies sane default thresholds when they have no number in mind, records everything in CONSTRAINTS.md, and watches the diff for a weakened bar — new @ts-ignore or eslint-disable suppressions, skipped or deleted tests, assertions stripped out, unimplemented stubs, thresholds edited down. Use when no quality bar is written down, when the user says "set up constraints" or "define our standards", when the user wants dimensions they care about — accessibility, web performance, coverage — set up as enforced constraints, when an agent keeps silencing checks or skipping tests to get to green, when you need a coverage or performance threshold and don't know what number to pick, or when an agent writes more code than anyone will read. |
| `/context-engineering` | `agent-skills` | Optimizes agent context setup. Use when starting a new session, when agent output quality degrades, when switching between tasks, or when you need to configure rules files and context for a project. |
| `/debugging-and-error-recovery` | `agent-skills` | Guides systematic root-cause debugging. Use when tests fail, builds break, something that worked yesterday broke, behavior doesn't match expectations, or you encounter any unexpected error. Use when you need to figure out what broke and why — a systematic approach to finding and fixing the root cause rather than guessing. |
| `/deprecation-and-migration` | `agent-skills` | Manages deprecation and migration. Use when removing old systems, APIs, or features. Use when migrating users from one implementation to another. Use when migrating a database schema in production, such as renaming or dropping a column without downtime (expand/contract). Use when deciding whether to maintain or sunset existing code. |
| `/documentation-and-adrs` | `agent-skills` | Records decisions and documentation. Use when you need to document an architecture decision (ADR) or the reasoning behind a design choice, when changing public APIs, shipping features, or when you need to record context that future engineers and agents will need to understand the codebase. |
| `/doubt-driven-development` | `agent-skills` | Subjects every non-trivial decision to a fresh-context adversarial review before it stands. Use when you want every assumption cross-examined before proceeding, when stress-testing a plan for hidden failure modes, when correctness matters more than speed, when working in unfamiliar code, when stakes are high (production auth, security-sensitive logic, a high-stakes migration, irreversible operations), or any time a confident output would be cheaper to verify now than to debug later. |
| `/frontend-ui-engineering` | `agent-skills` | Builds production-quality, accessible, responsive user-facing UIs. Use when building or modifying interfaces and pages, creating components, implementing layouts, meeting WCAG accessibility requirements, managing state, or when the output needs to look and feel production-quality rather than AI-generated. |
| `/git-workflow-and-versioning` | `agent-skills` | Structures git workflow practices. Use when making any code change. Use when committing, branching, resolving conflicts, splitting uncommitted work in a messy working tree into clean atomic commits, opening or reviewing a pull request (PR), pushing to a remote, or when you need to organize work across multiple parallel streams. Use when cutting a release, choosing a semantic version bump, tagging, or writing a changelog. |
| `/idea-refine` | `agent-skills` | Refines raw ideas into sharp, actionable concepts through structured divergent and convergent thinking. Use when an idea is still vague, when you need to stress-test assumptions before committing to a plan, or when you want to expand options before converging on one. Triggers on "ideate", "refine this idea", or "stress-test my plan". |
| `/incremental-implementation` | `agent-skills` | Delivers changes incrementally in thin, verifiable slices. Use when implementing any feature or change that touches more than one file, or when picking up the next task from a plan. Use when rolling a change out behind a feature flag, when you're about to write a large amount of code at once, or when a task feels too big to land in one step. |
| `/interview-me` | `agent-skills` | Extracts what the user actually wants instead of what they think they should want. Achieves this through one-question-at-a-time interview until ~95% confidence about the underlying intent. Use when an ask is underspecified ("build me X" without "for whom" or "why now"), when the user explicitly invokes ("interview me", "grill me", "are we sure?", "stress-test my thinking"), or when you catch yourself silently filling in ambiguous requirements before any plan, spec, or code exists. |
| `/observability-and-instrumentation` | `agent-skills` | Instruments code so production behavior is visible and diagnosable. Use when adding logging, metrics, tracing, or alerting. Use when shipping any feature that runs in production and you need evidence it works. Use when production issues are reported but you can't tell what happened from the available data. |
| `/performance-optimization` | `agent-skills` | Optimizes application performance across frontend, backend, queries, and databases. Use when performance requirements exist, when you suspect performance regressions, when Core Web Vitals or load times need improvement, when N+1 query patterns need fixing, or when profiling reveals bottlenecks. |
| `/planning-and-task-breakdown` | `agent-skills` | Breaks work into ordered tasks. Use when you have a spec or clear requirements and need to break work into implementable tasks. Use when a task feels too large to start, when you need to estimate scope, or when parallel work is possible. |
| `/security-and-hardening` | `agent-skills` | Hardens code against vulnerabilities. Use when auditing an input handler for vulnerabilities, when handling user input, authentication, data storage, or external integrations, or when checking a login flow is safe against the OWASP Top Ten. Use when building any feature that accepts untrusted data, manages user sessions, or interacts with third-party services. Use when auditing dependencies for known vulnerabilities, triaging package-manager audit findings, or assessing supply-chain risk in a new package. Use when personal data or privacy compliance (GDPR, CCPA) is involved. |
| `/shipping-and-launch` | `agent-skills` | Prepares production launches. Use when preparing to deploy to production, or when asking what needs to be in place before shipping. Use when you need a pre-launch checklist, when setting up monitoring, when planning a staged rollout, or when you need a rollback strategy. |
| `/source-driven-development` | `agent-skills` | Grounds every implementation decision in official documentation. Use when you want to verify an approach against the official docs before implementing it, or when you want authoritative, source-cited code free from outdated patterns. Use when building with any framework or library where correctness matters. |
| `/spec-driven-development` | `agent-skills` | Creates specs before coding. Use when starting a new project, feature, or significant change and no specification exists yet. Use when drafting a PRD or requirements document with objectives and scope, or when requirements are unclear, ambiguous, or only exist as a vague idea. Use when a single requirement spans several independently testable capabilities and needs decomposing into a capability map of modules before specifying. |
| `/test-driven-development` | `agent-skills` | Drives development with tests using the red-green-refactor loop. Use when implementing any logic, fixing any bug, or changing any behavior. Use when you need to prove that code works, when a bug report arrives, or when you're about to modify existing functionality. Use when asked to add tests to a function, fix a bug in it or change its behavior, even when the code is pasted inline and the request does not mention tests. |
| `/using-agent-skills` | `agent-skills` | Discovers and invokes agent skills. Use when starting a session, or when you need to decide which skill or workflow applies to the piece of work at hand. This is the meta-skill that governs how all other skills are discovered and invoked. |
| `/ask-matt` | `mattpocock-skills` | Ask which skill or flow fits your situation. A router over the skills in this repo. |
| `/code-review` | `mattpocock-skills` | Review the changes since a fixed point (commit, branch, tag, or merge-base) along two axes: Standards (does the code follow this repo's documented coding standards?) and Spec (does the code match what the originating issue/spec asked for?). Runs both reviews in parallel sub-agents and reports them side by side. Use when the user wants to review a branch, a PR, work-in-progress changes, or asks to \"review since X\". |
| `/codebase-design` | `mattpocock-skills` | Shared vocabulary for designing deep modules. Use when the user wants to design or improve a module's interface, find deepening opportunities, decide where a seam goes, make code more testable or AI-navigable, or when another skill needs the deep-module vocabulary. |
| `/diagnosing-bugs` | `mattpocock-skills` | Diagnosis loop for hard bugs and performance regressions. Use when the user says "diagnose"/"debug this", or reports something broken/throwing/failing/slow. |
| `/domain-modeling` | `mattpocock-skills` | Build and sharpen a project's domain model. Use when discussing codebase terminology, writing or editing a GLOSSARY.md, or recording or editing an ADR. |
| `/grill-me` | `mattpocock-skills` | A relentless interview to sharpen a plan or design. |
| `/grill-with-docs` | `mattpocock-skills` | A relentless interview to sharpen a plan or design, which also creates docs (ADR's and glossary) as we go. |
| `/grilling` | `mattpocock-skills` | Grill the user relentlessly about a plan, decision, or idea. Use when the user wants to stress-test their thinking, or uses any 'grill' trigger phrases. |
| `/handoff` | `mattpocock-skills` | Compact the current conversation into a handoff document for another agent to pick up. |
| `/implement` | `mattpocock-skills` | Implement a piece of work based on a spec or set of tickets. |
| `/implement-spec` | `mattpocock-skills` | Implement the result of /to-spec and /to-tickets in code. |
| `/improve-codebase-architecture` | `mattpocock-skills` | Scan a codebase for deepening opportunities, present them as a visual HTML report, then grill through whichever one you pick. |
| `/pr` | `mattpocock-skills` | Use when writing a PR body. |
| `/prototype` | `mattpocock-skills` | Build a throwaway prototype to answer a design question. Use when the user wants to sanity-check whether a state model or logic feels right, or explore what a UI should look like. |
| `/research` | `mattpocock-skills` | Investigate a question against high-trust primary sources and capture the findings as a Markdown file in the repo. Use when the user wants a topic researched, docs or API facts gathered, or reading legwork delegated to a background agent. |
| `/retro` | `mattpocock-skills` | Conduct a retrospective on a coding session. |
| `/setup-matt-pocock-skills` | `mattpocock-skills` | Configure this repo for the engineering skills: set up its issue tracker, triage label vocabulary, and domain doc layout. Run once before first use of the other engineering skills. |
| `/tdd` | `mattpocock-skills` | Test-driven development. Use when the user wants to build features or fix bugs test-first, mentions "red-green-refactor", or wants integration tests. |
| `/teach` | `mattpocock-skills` | Teach the user a new skill or concept, within this workspace. |
| `/to-questionnaire` | `mattpocock-skills` | Turn a decision you can't fully answer into a questionnaire for someone else to fill in. |
| `/to-spec` | `mattpocock-skills` | Turn the current conversation into a spec and publish it to the project issue tracker: no interview, just synthesis of what you've already discussed. |
| `/to-tickets` | `mattpocock-skills` | Break a plan, spec, or the current conversation into a set of tracer-bullet tickets, each declaring its blocking edges, published to the configured tracker (edges as text in one file per ticket locally, or native blocking links on a real tracker). |
| `/triage` | `mattpocock-skills` | Move issues and external PRs through a state machine of triage roles, categorise, verify, grill if needed, and write agent-ready briefs. |
| `/wait-what` | `mattpocock-skills` | Stop. That last message did not land: re-pitch it. |
| `/wayfinder` | `mattpocock-skills` | Plan a huge chunk of work (more than one agent session can hold) as a shared map of decision tickets on your issue tracker, and resolve them one at a time until the way to the destination is clear. |
| `/wizard` | `mattpocock-skills` | Generate an interactive bash wizard that walks a human through steps only they can perform. Use when provisioning infrastructure, setting up credentials or CI secrets, walking an unfamiliar third-party dashboard, or running a one-off migration or cutover. Don't invoke this for steps the agent can perform itself. |
| `/writing-for-agents` | `mattpocock-skills` | Writing documents for agents. Use when creating or editing skills, or modifying AGENTS.md or CLAUDE.md. |
| `/karpathy-guidelines` | `andrej-karpathy-skills` | Behavioral guidelines to reduce common LLM coding mistakes. Use when writing, reviewing, or refactoring code to avoid overcomplication, make surgical changes, surface assumptions, and define verifiable success criteria. |

### 2.4 Spec-Driven Development & Milestone Lifecycle (Open GSD)
> Source: `open-gsd/gsd-core` (144+ skills)

| Skill / Command | Category | Operational Focus |
| :--- | :--- | :--- |
| `/gsd-add-tests` | `gsd-core` | Generate tests for a completed phase based on UAT criteria and implementation |
| `/gsd-ai-integration-phase` | `gsd-core` | Generate an AI-SPEC.md design contract for phases that involve building AI systems. |
| `/gsd-audit-fix` | `gsd-core` | Autonomous audit-to-fix pipeline — find issues, classify, fix, test, commit |
| `/gsd-audit-milestone` | `gsd-core` | Audit milestone completion against original intent before archiving |
| `/gsd-audit-uat` | `gsd-core` | Cross-phase audit of all outstanding UAT and verification items |
| `/gsd-autonomous` | `gsd-core` | Run all remaining phases autonomously — discuss→plan→execute per phase |
| `/gsd-capture` | `gsd-core` | Capture ideas, tasks, notes, and seeds to their destination |
| `/gsd-cleanup` | `gsd-core` | Archive accumulated phase directories from completed milestones |
| `/gsd-code-review` | `gsd-core` | Review source files changed during a phase for bugs, security issues, and code quality problems |
| `/gsd-complete-milestone` | `gsd-core` | Archive completed milestone and prepare for next version |
| `/gsd-config` | `gsd-core` | Configure GSD settings — workflow toggles, advanced knobs, integrations, and model profile |
| `/gsd-debug` | `gsd-core` | Systematic debugging with persistent state across context resets |
| `/gsd-discuss-phase` | `gsd-core` | Gather phase context through adaptive questioning before planning. |
| `/gsd-docs-update` | `gsd-core` | Generate or update project documentation verified against the codebase |
| `/gsd-eval-review` | `gsd-core` | Audit an executed AI phase's evaluation coverage and produce an EVAL-REVIEW.md remediation plan. |
| `/gsd-execute-phase` | `gsd-core` | SDD phase execution — execute all plans in a phase with dependency-aware wave parallelization |
| `/gsd-explore` | `gsd-core` | Socratic ideation and idea routing — think through ideas before committing to plans |
| `/gsd-extract-learnings` | `gsd-core` | Extract decisions, lessons, patterns, and surprises from completed phase artifacts |
| `/gsd-fast` | `gsd-core` | Execute a trivial task inline — no subagents, no planning overhead |
| `/gsd-forensics` | `gsd-core` | Post-mortem investigation for failed GSD workflows — diagnoses what went wrong. |
| `/gsd-graphify` | `gsd-core` | Build, query, and inspect the project knowledge graph in .planning/graphs/ |
| `/gsd-health` | `gsd-core` | Diagnose planning directory health and optionally repair issues |
| `/gsd-help` | `gsd-core` | Show available GSD commands and usage guide |
| `/gsd-import` | `gsd-core` | Ingest external plans with conflict detection against project decisions before writing anything. |
| `/gsd-inbox` | `gsd-core` | Triage and review open GitHub issues and PRs against project templates and contribution guidelines. |
| `/gsd-ingest-docs` | `gsd-core` | Bootstrap or merge a .planning/ setup from existing ADRs, PRDs, SPECs, and docs in a repo. |
| `/gsd-manager` | `gsd-core` | Interactive command center for managing multiple phases from one terminal |
| `/gsd-map-codebase` | `gsd-core` | Analyze codebase with parallel mapper agents to produce .planning/codebase/ documents |
| `/gsd-mempalace-capture` | `gsd-core` | File a phase artifact into MemPalace; mirror decision facts into its temporal KG |
| `/gsd-mempalace-recall` | `gsd-core` | Recall decisions, patterns, and surprises from MemPalace before planning |
| `/gsd-milestone-summary` | `gsd-core` | Generate a comprehensive project summary from milestone artifacts for team onboarding and review |
| `/gsd-mvp-phase` | `gsd-core` | Plan a phase as a vertical MVP slice — user story, SPIDR splitting, then plan-phase |
| `/gsd-new-milestone` | `gsd-core` | Start a new milestone cycle — update PROJECT.md and route to requirements |
| `/gsd-new-project` | `gsd-core` | Initialize a new project with deep context gathering and PROJECT.md |
| `/gsd-next` | `gsd-core` | Smart entry — detect project state and route to the right next GSD action. |
| `/gsd-ns-context` | `gsd-core` | codebase intel | map graphify docs learnings mempalace |
| `/gsd-ns-ideate` | `gsd-core` | exploration capture | explore sketch spike spec capture |
| `/gsd-ns-manage` | `gsd-core` | config workspace | workstreams thread update ship inbox |
| `/gsd-ns-project` | `gsd-core` | project lifecycle | milestones audits summary |
| `/gsd-ns-review` | `gsd-core` | quality gates | code review debug audit security eval ui |
| `/gsd-ns-workflow` | `gsd-core` | workflow | discuss plan execute verify phase progress |
| `/gsd-onboard` | `gsd-core` | Guide existing codebase onboarding through mapping, doc ingest, and planning setup |
| `/gsd-pause-work` | `gsd-core` | Create context handoff when pausing work mid-phase |
| `/gsd-phase` | `gsd-core` | Multi-phase management — add, insert, remove, or edit phases in ROADMAP.md (roadmap phase CRUD) |
| `/gsd-plan-phase` | `gsd-core` | Create detailed phase plan (PLAN.md) with verification loop |
| `/gsd-plan-review-convergence` | `gsd-core` | Cross-AI plan convergence - replan until review concerns are resolved. |
| `/gsd-pr-branch` | `gsd-core` | Create a clean PR branch by filtering out .planning/ commits — ready for code review |
| `/gsd-profile-user` | `gsd-core` | Generate developer behavioral profile and create Claude-discoverable artifacts |
| `/gsd-progress` | `gsd-core` | Check progress, advance workflow, or dispatch freeform intent — the unified GSD situational command |
| `/gsd-quick` | `gsd-core` | Execute a quick task with GSD guarantees (atomic commits, state tracking) but skip optional agents |
| `/gsd-quick-batch` | `gsd-core` | Batch several `/skill:gsd-quick`-shaped tasks together — planned, dispatched, and merged as one run |
| `/gsd-resume-work` | `gsd-core` | Resume work from previous session with full context restoration |
| `/gsd-review` | `gsd-core` | Request cross-AI peer review of phase plans from external AI CLIs |
| `/gsd-review-backlog` | `gsd-core` | Review and promote backlog items to active milestone |
| `/gsd-secure-phase` | `gsd-core` | Retroactively verify threat mitigations for a completed phase |
| `/gsd-settings` | `gsd-core` | Configure GSD workflow toggles and model profile |
| `/gsd-ship` | `gsd-core` | Create PR, run review, and prepare for merge after verification passes |
| `/gsd-sketch` | `gsd-core` | Sketch UI/design ideas with throwaway HTML mockups, or propose what to sketch next (frontier mode) |
| `/gsd-spec-phase` | `gsd-core` | Clarify WHAT a phase delivers with ambiguity scoring; produces a SPEC.md before discuss-phase. |
| `/gsd-spike` | `gsd-core` | Spike an idea through experiential exploration, or propose what to spike next (frontier mode) |
| `/gsd-stats` | `gsd-core` | Display project statistics — phases, plans, requirements, git metrics, and timeline |
| `/gsd-surface` | `gsd-core` | Toggle which skills are surfaced — apply a profile, list, or disable a cluster without reinstall |
| `/gsd-thread` | `gsd-core` | Manage persistent context threads for cross-session work |
| `/gsd-ui-phase` | `gsd-core` | Generate UI design contract (UI-SPEC.md) for frontend phases |
| `/gsd-ui-review` | `gsd-core` | Retroactive 6-pillar visual audit of implemented frontend code |
| `/gsd-ultraplan-phase` | `gsd-core` | [BETA] Offload plan phase to Claude Code's ultraplan cloud; review in browser and import back. |
| `/gsd-undo` | `gsd-core` | Safe git revert. Roll back phase or plan commits using the phase manifest with dependency checks. |
| `/gsd-update` | `gsd-core` | Update GSD to latest version with changelog display |
| `/gsd-validate-phase` | `gsd-core` | Retroactively audit and fill Nyquist validation gaps for a completed phase |
| `/gsd-verify-work` | `gsd-core` | Validate built features through conversational UAT |
| `/gsd-workspace` | `gsd-core` | Manage GSD workspaces — create, list, or remove isolated workspace environments |
| `/gsd-workstreams` | `gsd-core` | Manage parallel workstreams — list, create, switch, status, progress, complete, and resume |

### 2.6 Google Elephant-Goldfish Model Governance (EGM)
> Source: `google/elephant-goldfish-model` (14 skills)

| Skill / Command | Category | Operational Focus |
| :--- | :--- | :--- |
| `/egm-bootstrap` | `egm` | Bootstrap directory README.md files across a repository subtree. |
| `/egm-brainstorm` | `egm` | >- |
| `/egm-draft` | `egm` | >- |
| `/egm-drift-report` | `egm` | >- |
| `/egm-resolve` | `egm` | >- |
| `/egm-review` | `egm` | >- |
| `/egm-review-loop` | `egm` | >- |

### 2.7 Cloud Firebase & Security Engineering
> Source: `firebase/firebase-skills` (13 skills)

| Skill / Command | Category | Operational Focus |
| :--- | :--- | :--- |
| `/extension-to-functions-codebase` | `firebase` | Skill for converting an installed Firebase Extension (or extension source) into a standalone Cloud Functions for Firebase codebase or publishable npm package, including V1 to V2 trigger upgrades, lifecycle hooks, and declarative security |
| `/firebase-ai-logic-basics` | `firebase` | Official skill for integrating Firebase AI Logic (Gemini API) into web applications. Covers setup, multimodal inference, structured output, and security. |
| `/firebase-app-hosting-basics` | `firebase` | >- |
| `/firebase-auth-basics` | `firebase` | Guide for setting up and using Firebase Authentication. Use this skill when the user's app requires user sign-in, user management, or secure data access using auth rules. |
| `/firebase-basics` | `firebase` | >- |
| `/firebase-crashlytics` | `firebase` | Comprehensive guide for Firebase Crashlytics, including provisioning and SDK usage. Use this skill when the user needs help setting up Crashlytics, adding crash reporting, or using the Crashlytics SDK in their application. |
| `/firebase-data-connect` | `firebase` | Builds and deploys Firebase SQL Connect (aka Firebase Data Connect) backends with PostgreSQL securely. Use when designing schemas with tables and relations, writing authorized queries and mutations, configuring real-time data updates, or generating type-safe SDKs. Use when you need a relational database with Firebase, or when the user mentions SQL Connect or Data Connect. |
| `/firebase-firestore` | `firebase` | >- |
| `/firebase-hosting-basics` | `firebase` | >- |
| `/firebase-remote-config-basics` | `firebase` | >- |
| `/firebase-security-rules-auditor` | `firebase` | >- |
| `/firestore-rules-creation` | `firebase` | >- |
| `/xcode-project-setup` | `firebase` | Safely modifies Xcode projects (.pbxproj) to add Swift Packages and link files. Use this skill whenever an iOS project needs dependencies installed (e.g. Firebase, Alamofire). |

### 2.8 Stitch UI Generative Suite & Cross-Platform Porting
> Source: `~/.agents/skills/` (18 skills)

| Skill / Command | Category | Operational Focus |
| :--- | :--- | :--- |
| `/archify` | `stitch` | Create validated architecture, workflow, sequence, data-flow, lifecycle/state, ERD, class, tree, timeline, and waterfall diagrams as explorable standalone HTML with dark/light themes, motion and image/SVG/WebM export. Accept requirements or Mermaid; inspect repository evidence for code-backed diagrams. Use when visualizing systems, API calls, pipelines, schemas, histories, timings, or everyday steps, parts, relationships and states. Also turn an answer into a one-page explainer, with the conclusion first, structured panels, native diagrams and charts, when the user asks for a page, an explainer or an answer to share (做成一页, 用页面讲), or proactively when the user's rules opt in with an [archify explainer: ...] line. Not for dashboards. |
| `/design-md` | `stitch` | Analyze Stitch projects and synthesize a semantic design system into DESIGN.md files |
| `/enhance-prompt` | `stitch` | Transforms vague UI ideas into polished, Stitch-optimized prompts. Enhances specificity, adds UI/UX keywords, injects design system context, and structures output for better generation results. |
| `/react-vite-dashboard` | `stitch` | Convert Stitch designs into production React + Vite dashboards with TanStack Query, accessible tokens from DESIGN.md, and Web3-ready patterns (ethers/viem). |
| `/remotion` | `stitch` | Generate walkthrough videos from Stitch projects using Remotion with smooth transitions, zooming, and text overlays |
| `/reverse-engineer-anything` | `stitch` | Reverse engineer native, managed, Electron/JavaScript, packaged, firmware, and browser targets with REA, and build agent environments (AgentEnv bundles) of web applications from a live instance or a capture. Use shipped-artifact or requested runtime evidence to explain features, compare versions, decompile code, or guide a reconstruction. Skip REA for ordinary source-repository architecture analysis. |
| `/shadcn-ui` | `stitch` | Expert guidance for integrating and building applications with shadcn/ui components, including component discovery, installation, customization, and best practices. |
| `/site-md` | `stitch` | Analyze a project description or site requirements and synthesize a project constitution into SITE.md for the Stitch Build Loop |
| `/stitch-loop` | `stitch` | Teaches agents to iteratively build websites using Stitch with an autonomous baton-passing loop pattern |
| `/stitch::code-to-design` | `stitch` | >- |
| `/stitch::extract-design-md` | `stitch` | >- |
| `/stitch::extract-static-html` | `stitch` | >- |
| `/stitch::generate-design` | `stitch` | >- |
| `/stitch::manage-design-system` | `stitch` | >- |
| `/stitch::react-components` | `stitch` | >- |
| `/stitch::react-native` | `stitch` | >- |
| `/stitch::upload-to-stitch` | `stitch` | >- |
| `/taste-design` | `stitch` | Semantic Design System Skill for Google Stitch. Generates agent-friendly DESIGN.md files that enforce premium, anti-generic UI standards — strict typography, calibrated color, asymmetric layouts, perpetual micro-motion, and hardware-accelerated performance. |

### 2.9 Mobile App Research & Patterns (Mobbin)
> Source: `mobbin/skills` (4 skills)

| Skill / Command | Category | Operational Focus |
| :--- | :--- | :--- |
| `/best-practices` | `mobbin` | Tells your agent what Mobbin can and can't do and how the Mobbin skills work together, so it picks the right one or says when Mobbin isn't the right tool. Read it first, before any other Mobbin skill or tools, when the user asks about Mobbin or its connection, or starts a design task Mobbin could help with, including one that's too big or outside what Mobbin does. |
| `/plain-writing` | `mobbin` | Makes every report a Mobbin skill writes read like a person wrote it, in plain interface terms. Use only when a Mobbin skill writes prose for the user or asks for this skill by name; not for writing outside Mobbin skills. Removes the tells of AI-generated writing (em dashes, puffery, design buzzwords, rule-of-three, sign-offs) without changing a fact or adding one. |
| `/redesign-screen` | `mobbin` | Redesign any screen from patterns real apps ship. Share a screenshot, your code or a design file, and the skill finds how apps on Mobbin handle the same screen. Explore a few directions or fix specific problems, and see the evidence behind each change. You get a before and after image and a short report. The redesign stays in your own design system, and anything that already works is kept. Use whenever the user points at a screen, section, or component they have, or at a reference, a Mobbin result, one or more Mobbin collections or the screens they saved, and asks to redesign, improve, restyle, or rebuild it, or to see a few directions for it. |
| `/search` | `mobbin` | Gets better results from every Mobbin search, so the references match what you're working on. Use before every call to search_screens, search_flows or search_sections. Covers the tool, writing an effective query, the mode, pulling more results, and what to do when a search falls short. |

### 2.10 Garry Tan gstack Founder Suite
> Source: `garrytan/gstack` (58 skills)

| Skill / Command | Category | Operational Focus |
| :--- | :--- | :--- |
| `/gstack` | `gstack` | | |
| `/gstack-autoplan` | `gstack` | | |
| `/gstack-benchmark` | `gstack` | | |
| `/gstack-benchmark-models` | `gstack` | | |
| `/gstack-browse` | `gstack` | | |
| `/gstack-canary` | `gstack` | | |
| `/gstack-careful` | `gstack` | | |
| `/gstack-claude-code` | `gstack` | | |
| `/gstack-codex` | `gstack` | | |
| `/gstack-context-restore` | `gstack` | | |
| `/gstack-context-save` | `gstack` | | |
| `/gstack-cso` | `gstack` | | |
| `/gstack-design-consultation` | `gstack` | | |
| `/gstack-design-html` | `gstack` | | |
| `/gstack-design-review` | `gstack` | | |
| `/gstack-design-shotgun` | `gstack` | | |
| `/gstack-deslop-shared-libs` | `gstack` | | |
| `/gstack-devex-review` | `gstack` | | |
| `/gstack-diagram` | `gstack` | | |
| `/gstack-document-generate` | `gstack` | | |
| `/gstack-document-release` | `gstack` | | |
| `/gstack-eval-plan` | `gstack` | | |
| `/gstack-freeze` | `gstack` | | |
| `/gstack-guard` | `gstack` | | |
| `/gstack-health` | `gstack` | | |
| `/gstack-investigate` | `gstack` | | |
| `/gstack-ios-clean` | `gstack` | | |
| `/gstack-ios-design-review` | `gstack` | | |
| `/gstack-ios-fix` | `gstack` | | |
| `/gstack-ios-qa` | `gstack` | | |
| `/gstack-ios-sync` | `gstack` | | |
| `/gstack-land-and-deploy` | `gstack` | | |
| `/gstack-landing-report` | `gstack` | | |
| `/gstack-learn` | `gstack` | | |
| `/gstack-make-pdf` | `gstack` | | |
| `/gstack-office-hours` | `gstack` | | |
| `/gstack-open-gstack-browser` | `gstack` | | |
| `/gstack-pair-agent` | `gstack` | | |
| `/gstack-plan-ceo-review` | `gstack` | | |
| `/gstack-plan-design-review` | `gstack` | | |
| `/gstack-plan-devex-review` | `gstack` | | |
| `/gstack-plan-eng-review` | `gstack` | | |
| `/gstack-plan-tune` | `gstack` | | |
| `/gstack-qa` | `gstack` | | |
| `/gstack-qa-only` | `gstack` | | |
| `/gstack-retro` | `gstack` | | |
| `/gstack-review` | `gstack` | | |
| `/gstack-scrape` | `gstack` | | |
| `/gstack-setup-browser-cookies` | `gstack` | | |
| `/gstack-setup-deploy` | `gstack` | | |
| `/gstack-setup-gbrain` | `gstack` | | |
| `/gstack-ship` | `gstack` | | |
| `/gstack-skillify` | `gstack` | | |
| `/gstack-spec` | `gstack` | | |
| `/gstack-sync-gbrain` | `gstack` | | |
| `/gstack-test-audit` | `gstack` | | |
| `/gstack-unfreeze` | `gstack` | | |
| `/gstack-upgrade` | `gstack` | | |

### 2.11 Modular UX & Design Ops Library (Designer Skills)
> Source: `owl-listener/designer-skills` (111 skills)

| Skill / Command | Category | Operational Focus |
| :--- | :--- | :--- |
| `/a-b-test-design` | `designer-skills` | Design an A/B experiment — hypothesis, variants, primary metric, and sample size. Use when a change can be measured quantitatively at scale. For observing behaviour qualitatively, use `test-scenario`. |
| `/accessibility-audit` | `designer-skills` | Audit an existing interface against WCAG, producing findings with severity ratings and remediation steps. Use when you have a design or build to assess now. Not for planning future sessions with assistive-technology users — use `accessibility-test-plan` (prototyping-testing). |
| `/accessibility-test-plan` | `designer-skills` | Plan accessibility testing — assistive technologies, participant criteria, WCAG coverage, and session protocol. Use when scheduling testing with real AT users. Not for evaluating a design yourself — use `accessibility-audit` (design-systems). |
| `/aesthetic-usability` | `designer-skills` | Apply the Aesthetic-Usability Effect — polished, consistent interfaces are perceived as more usable and forgive minor friction. Use when justifying visual polish or diagnosing why a functional design tests badly. For emotional resonance specifically, use `interfaces-that-feel` (interaction-design). |
| `/affinity-diagram` | `designer-skills` | Cluster many qualitative data points into themes and insight statements. Use when synthesising across multiple sessions or sources. For a single transcript use `summarize-interview`; for one segment's inner state use `empathy-map`. |
| `/animation-principles` | `designer-skills` | Apply animation principles — easing, staging, follow-through — to one specific UI motion. Use when tuning how an animation feels. For product-wide duration and easing tokens use `motion-system` (design-systems); for a full interaction spec use `micro-interaction-spec`. |
| `/behavioural-analytics` | `designer-skills` | Read funnels, retention curves, and event data as a designer — separating a design problem from a tracking artefact. Use when handed product data you did not design and asked why people drop off. For choosing what to measure, use `metrics-definition` (ux-strategy); for running a controlled test, use `a-b-test-design` (prototyping-testing). |
| `/business-design` | `designer-skills` | Read financials, map competitive landscapes, and argue design decisions in the language of value. Use when defending design to commercial stakeholders. For the live negotiation itself, use `design-negotiation` (designer-toolkit). |
| `/card-sort-analysis` | `designer-skills` | Analyse open or closed card sort results into a proposed grouping and label set. Use after running a sort study. For turning that evidence into a full structure, use `information-architecture` (ux-strategy). |
| `/case-study` | `designer-skills` | Craft a portfolio case study with narrative arc, process evidence, and outcomes. Use when telling a project's story to an external audience. For an internal stakeholder deck, use `presentation-deck`. |
| `/click-test-plan` | `designer-skills` | Design first-click and click tests for findability and navigation. Use when testing whether people can locate something. For full task-based observation, use `test-scenario`. |
| `/color-system` | `designer-skills` | Build a product colour system — tonal scales, semantic roles, and contrast compliance. Use when defining or rebuilding colour from scratch. For dark-mode adaptation use `dark-mode-design`; for chart palettes use `data-visualization`; for multi-brand token architecture use `theming-system` (design-systems). |
| `/competitive-analysis` | `designer-skills` | Compare UX patterns, features, strengths, and gaps across rival products. Use when you need to know what others actually do. For deliberately adopting their conventions, use `jakobs-law` (interaction-design). |
| `/component-spec` | `designer-skills` | Specify one component — props, states, variants, accessibility, and usage rules. Use when defining a library component. For the reusable doc scaffold use `documentation-template`; for a problem-solution pattern use `pattern-library`. |
| `/concept-selection` | `designer-skills` | Choose between competing concepts against criteria fixed in advance, and record what each rejected concept was testing. Use when several directions are alive and one has to win. For picking which problem to work on, use `opportunity-framework` (ux-strategy); for deciding by production traffic, use `a-b-test-design`. |
| `/content-strategy` | `designer-skills` | Define what content a product needs, how it is structured, and who owns it. Use when content itself is the problem. For the words in the interface use `ux-writing` (designer-toolkit); for structural hierarchy use `information-architecture`. |
| `/conversational-ux` | `designer-skills` | Design voice and conversational interfaces — dialog flows, error recovery, and persona. Use when the interface speaks and listens rather than being tapped. For graphical input collection, use `form-design`. |
| `/critique-affordance` | `designer-skills` | Critique a rendered screen's affordances — what looks clickable, state visibility, CTA clarity, and action discoverability. Use when reviewing an existing screen. For sizing and positioning targets in new work, use `fitts-law` (interaction-design). |
| `/critique-brand-consistency` | `designer-skills` | Critique a rendered screen against mood.md, voice.md, and tokens.md. Use when those brand files exist and you are checking compliance. For defining the visual language itself, use `illustration-style` (ui-design). |
| `/critique-color` | `designer-skills` | Critique a rendered screen's colour — contrast ratios, palette coherence, and semantic meaning. Use when reviewing one screen. For a product-wide WCAG audit use `accessibility-audit` (design-systems); for building the palette use `color-system` (ui-design). |
| `/critique-composition` | `designer-skills` | Critique a rendered screen's composition — balance, whitespace, rhythm, and gestalt grouping. Use when a layout feels off but hierarchy is fine. For emphasis and eye flow specifically, use `critique-visual-hierarchy`. |
| `/critique-information-density` | `designer-skills` | Critique a rendered screen's density — cognitive load, content prioritisation, scanning patterns, and progressive disclosure. Use when a screen feels overwhelming. For the underlying choice-count principle, use `hicks-law` (interaction-design). |
| `/critique-typography` | `designer-skills` | Critique a rendered screen's typography — scale usage, readability, consistency, and token compliance. Use when reviewing type on a screen. For defining the scale itself, use `typography-scale` (ui-design). |
| `/critique-visual-hierarchy` | `designer-skills` | Critique a rendered screen's hierarchy — entry point, eye flow, weight distribution, and emphasis. Use when attention lands in the wrong place. For establishing hierarchy in new work, use `visual-hierarchy` (ui-design). |
| `/dark-mode-design` | `designer-skills` | Adapt an existing palette to dark mode — surface elevation, contrast rebalancing, and desaturation rules. Use when you already have a light palette to translate. For building the base palette first, use `color-system`. |
| `/data-visualization` | `designer-skills` | Select chart types and design data encodings — marks, axes, labels, and accessible chart styling. Use when presenting data graphically. Owns chart selection and encoding only; the categorical colour ramp itself belongs to `color-system`. |
| `/design-brief` | `designer-skills` | Write a project brief — problem space, constraints, audience, and success criteria. Use at kickoff for one specific project. For long-horizon aspiration use `north-star-vision`; for reusable decision rules use `design-principles`. |
| `/design-critique` | `designer-skills` | Facilitate a structured team critique — framing, feedback rules, and actionable outcomes. Use when running a session with people in the room. For a solo expert review, use `heuristic-evaluation` (prototyping-testing). |
| `/design-debt-audit` | `designer-skills` | Inventory and prioritise accumulated design inconsistencies across a product. Use when drift has built up over time. For token coverage specifically use `design-token-audit` (designer-toolkit); for WCAG gaps use `accessibility-audit` (design-systems). |
| `/design-impact-reporting` | `designer-skills` | Communicate design's contribution to business and user outcomes in stakeholder language. Use when reporting results upward. For choosing the metrics in the first place, use `metrics-definition` (ux-strategy). |
| `/design-negotiation` | `designer-skills` | Advocate for design quality, scope, and timeline with partners and leadership using evidence and shared goals. Use in the conversation itself. For the commercial vocabulary behind it, use `business-design` (ux-strategy). |
| `/design-principles` | `designer-skills` | Define actionable principles that resolve trade-offs when the team disagrees. Use when the same decisions keep getting relitigated. For a single project's framing, use `design-brief`. |
| `/design-qa-checklist` | `designer-skills` | Build a QA checklist for verifying that a build matches the design. Use at implementation review. For the spec engineers build from, use `handoff-spec`. |
| `/design-rationale` | `designer-skills` | Write rationale connecting decisions to user needs, business goals, and principles. Use when a decision needs defending in writing. For a live conversation, use `design-negotiation`. |
| `/design-review-process` | `designer-skills` | Establish review gates — criteria, checkpoints, and approval flow. Use when work ships without consistent review. For running one individual session, use `design-critique`. |
| `/design-sprint-plan` | `designer-skills` | Plan and facilitate a design sprint from challenge framing through prototype testing. Use when compressing discovery into days. For ongoing team cadence, use `team-workflow`. |
| `/design-system-adoption` | `designer-skills` | Create adoption strategy and enablement materials to drive design system usage. Use when the system exists but teams ignore it. For contribution and versioning rules, use `design-system-governance` (design-systems). |
| `/design-system-governance` | `designer-skills` | Define how the system evolves — contribution model, versioning, deprecation, and change management. Use when multiple teams contribute. For driving uptake use `design-system-adoption` (designer-toolkit); for design file history use `version-control-strategy` (design-ops). |
| `/design-token` | `designer-skills` | Define and organise tokens for colour, spacing, type, and elevation with naming and usage rules. Use when establishing the token layer. For auditing existing usage use `design-token-audit` (designer-toolkit); for multi-brand mapping use `theming-system`. |
| `/design-token-audit` | `designer-skills` | Audit token usage across a product for coverage, drift, and hard-coded values. Use when tokens exist and you suspect they are being bypassed. For defining tokens in the first place, use `design-token` (design-systems). |
| `/diary-study-plan` | `designer-skills` | Design a diary study — prompts, cadence, duration, participant criteria, and analysis frame. Use when behaviour unfolds over days or weeks. For a single-session study, use `usability-test-plan`. |
| `/documentation-template` | `designer-skills` | Generate a reusable documentation scaffold for components, patterns, or guidelines. Use when standardising how the system is documented. For the content of one component's spec, use `component-spec`. |
| `/doherty-threshold` | `designer-skills` | Apply the Doherty Threshold — keep system response under 400ms to preserve user flow. Use when diagnosing perceived slowness or setting a performance budget. For what to show during unavoidable waits, use `loading-states`. |
| `/empathy-map` | `designer-skills` | Build a Says, Thinks, Does, Feels map for one user or segment. Use when sharing user understanding quickly. For a composite archetype with goals and behaviours use `user-persona`; for cross-session themes use `affinity-diagram`. |
| `/error-handling-ux` | `designer-skills` | Design error prevention, detection, and recovery across a product — message content, placement, and escape routes. Use when errors span multiple flows. For validation inside a single form, use `form-design`. |
| `/experience-map` | `designer-skills` | Map the full ecosystem of touchpoints, channels, and relationships across a service. Use when the experience spans more than one product. For one persona's linear journey use `journey-map` (design-research); for backstage operations use `service-blueprint`. |
| `/feedback-patterns` | `designer-skills` | Design confirmations, status updates, and notifications that tell users an action registered. Use when the system must acknowledge success or change. For waiting states use `loading-states`; for failures use `error-handling-ux`. |
| `/fitts-law` | `designer-skills` | Apply Fitts's Law — target acquisition time depends on size and distance. Use when sizing and positioning controls, especially for touch. For how many controls to show at once, use `hicks-law`. |
| `/form-design` | `designer-skills` | Design a form end to end — field order, grouping, validation, and completion. Use when the artifact is a form. For product-wide error strategy use `error-handling-ux`; for first-run signup use `onboarding-design`. |
| `/gesture-patterns` | `designer-skills` | Design gesture interactions for touch and pointer — swipe, drag, long-press, and their discoverability. Use when input is gestural. For OS-standard gestures on iOS and Android, use `platform-conventions` (ui-design). |
| `/handoff-spec` | `designer-skills` | Write the implementation handoff — measurements, behaviours, assets, states, and edge cases. Use when engineering picks up the work. For verifying the result afterwards use `design-qa-checklist`; for reusable library components use `component-spec` (design-systems). |
| `/heuristic-evaluation` | `designer-skills` | Run an expert review against Nielsen's heuristics and domain criteria, with severity ratings. Use when you need findings without recruiting participants. For a facilitated team feedback session, use `design-critique` (design-ops). |
| `/hicks-law` | `designer-skills` | Apply Hick's Law — decision time grows with the number of simultaneous choices. Use when a screen offers too many options at once. For how many items survive in memory afterwards, use `millers-law`. |
| `/icon-system` | `designer-skills` | Specify an icon system — grid, sizing, stroke weight, naming, categories, and implementation. Use when standardising iconography. For broader illustration, use `illustration-style` (ui-design). |
| `/illustration-style` | `designer-skills` | Define an illustration style guide — visual language, colour usage, and application rules. Use when commissioning or standardising illustration. For icons, use `icon-system` (design-systems). |
| `/information-architecture` | `designer-skills` | Design content structure, hierarchy, labelling, and the navigation model. Use when organising what exists. For the UI that exposes it use `navigation-patterns` (interaction-design); for user-generated grouping evidence use `card-sort-analysis` (design-research). |
| `/interfaces-that-feel` | `designer-skills` | Apply an emotional resonance lens to a UI that is technically correct but flat, prescribing changes at the copy, motion, and interaction layer. Use when a design tests fine but lands cold. For the polish-perception argument, use `aesthetic-usability` (ui-design). |
| `/interview-script` | `designer-skills` | Write a structured interview guide — warm-up, core exploration, and wrap-up. Use before running interviews. For analysing what comes back, use `summarize-interview`. |
| `/jakobs-law` | `designer-skills` | Apply Jakob's Law — users expect your product to work like the others they already use. Use when deciding whether to innovate on a familiar pattern. For OS-mandated conventions specifically, use `platform-conventions` (ui-design). |
| `/jobs-to-be-done` | `designer-skills` | Map functional, emotional, and social jobs with outcome expectations. Use when reframing decisions around motivation rather than features. For who the user is, use `user-persona`. |
| `/journey-map` | `designer-skills` | Map one persona's end-to-end experience with stages, touchpoints, emotions, and pain points. Use when improving an existing experience. For the multi-channel ecosystem use `experience-map` (ux-strategy); for screen-level paths use `user-flow-diagram` (prototyping-testing). |
| `/law-of-closure` | `designer-skills` | Apply the Law of Closure — the eye completes implied shapes from partial forms. Use when reducing visual weight by dropping borders or letting negative space suggest structure. For explicit containers, use `law-of-common-region`. |
| `/law-of-common-region` | `designer-skills` | Apply the Law of Common Region — a shared container, background, or border groups elements regardless of spacing. Use when grouping must survive a tight layout. For grouping by spacing alone, use `law-of-proximity`. |
| `/law-of-continuity` | `designer-skills` | Apply the Law of Continuity — the eye follows alignment and unbroken paths. Use when sequencing steps, aligning content, or designing carousels and timelines. For grouping rather than sequencing, use `law-of-proximity`. |
| `/law-of-figure-ground` | `designer-skills` | Apply the Law of Figure-Ground — establish which layer is foreground and actionable versus background. Use when designing modals, overlays, and depth. For emphasising one element among peers, use `von-restorff-effect`. |
| `/law-of-proximity` | `designer-skills` | Apply the Law of Proximity — spatial closeness groups elements more strongly than any other cue. Use when spacing alone must carry grouping. For grouping via containers use `law-of-common-region`; via shared appearance use `law-of-similarity`. |
| `/law-of-similarity` | `designer-skills` | Apply the Law of Similarity — shared colour, shape, or size signals that elements belong to one category. Use when signalling relationships across distance. For grouping by position, use `law-of-proximity`. |
| `/layout-grid` | `designer-skills` | Define a responsive grid — columns, gutters, margins, and breakpoint behaviour. Use when establishing page structure. For the spacing scale inside components use `spacing-system`; for cross-device behaviour use `responsive-design`. |
| `/loading-states` | `designer-skills` | Design waiting experiences — spinners, skeletons, optimistic updates, and progressive reveal. Use when content takes time to arrive. For the latency budget itself use `doherty-threshold`; for success confirmation use `feedback-patterns`. |
| `/localization-design` | `designer-skills` | Design for multiple languages, writing directions, and cultural contexts — text expansion, RTL mirroring, and locale formats. Use when shipping beyond one locale. For the words themselves, use `ux-writing` (designer-toolkit). |
| `/metrics-definition` | `designer-skills` | Define UX metrics and KPIs that connect design decisions to measurable outcomes. Use when choosing what to measure. For presenting the results afterwards, use `design-impact-reporting` (design-ops). |
| `/micro-interaction-spec` | `designer-skills` | Specify one micro-interaction completely — trigger, rules, feedback, loops, and modes. Use when handing a single interaction to engineering. For motion craft alone use `animation-principles`; for multi-state components use `state-machine`. |
| `/millers-law` | `designer-skills` | Apply Miller's Law — chunk information into groups of about four to fit working memory. Use when grouping fields, menu items, or steps. For reducing the number of choices offered, use `hicks-law`. |
| `/motion-system` | `designer-skills` | Define motion tokens — durations, easing vocabulary, and reduced-motion handling — for consistency product-wide. Use when standardising motion across a system. For crafting one specific animation, use `animation-principles` (interaction-design). |
| `/naming-convention` | `designer-skills` | Establish naming rules for components, tokens, and layers with patterns and worked examples. Use when names are inconsistent or being set. For what the tokens actually contain, use `design-token`. |
| `/navigation-patterns` | `designer-skills` | Select and design a navigation pattern — tabs, drawer, hierarchy, or hub — matched to product structure and user tasks. Use when choosing how users move between sections. For the underlying content structure, use `information-architecture` (ux-strategy). |
| `/north-star-vision` | `designer-skills` | Articulate a long-horizon product vision that aligns teams and anchors strategy. Use when direction is contested or absent. For near-term project scope, use `design-brief`. |
| `/onboarding-design` | `designer-skills` | Design the first-run experience — activation path, progressive disclosure, and time to first value. Use for a user's very first session. For the mechanics of the signup form itself, use `form-design`. |
| `/opportunity-framework` | `designer-skills` | Identify, score, and prioritise design opportunities against impact and effort. Use when there are more ideas than capacity. For framing the one you choose, use `design-brief`. |
| `/parallel-concepts` | `designer-skills` | Build several genuinely different solutions to the same problem at once, spread across what the user does rather than how it looks. Use when one direction is on the table and the team is about to refine it by default. For choosing between the concepts afterwards, use `concept-selection`. |
| `/pattern-library` | `designer-skills` | Structure a pattern entry — problem context, solution, usage examples, and related patterns. Use when documenting a recurring solution rather than a component. For a single component's API, use `component-spec`. |
| `/peak-end-rule` | `designer-skills` | Apply the Peak-End Rule — a flow is remembered by its most intense moment and its last. Use when designing completion, celebration, or cancellation moments. For sustaining engagement mid-flow, use `zeigarnik-effect`. |
| `/platform-conventions` | `designer-skills` | Design to iOS and Android conventions — what each OS mandates, where they diverge, and when to unify. Use when shipping native apps. For breakpoint adaptation use `responsive-design`; for matching competitor patterns use `jakobs-law` (interaction-design). |
| `/presentation-deck` | `designer-skills` | Structure a design presentation for a specific audience and decision. Use when presenting internally. For a portfolio narrative use `case-study`; for the written argument use `design-rationale`. |
| `/prototype-strategy` | `designer-skills` | Choose prototype fidelity and method to match the design question and the decision at stake. Use before building a prototype. For what to test once it exists, use `test-scenario`. |
| `/qual-quant-triangulation` | `designer-skills` | Reconcile what the numbers say with what users say, and design the study that settles it rather than restates it. Use when behavioural data and research findings point different ways. For reading the data on its own, use `behavioural-analytics`; for synthesising interviews on their own, use `affinity-diagram`. |
| `/readable-measure` | `designer-skills` | Set line length and measure for comfortable reading across type sizes and breakpoints. Use when tuning body text. Covers measure only — for the full size and weight scale, use `typography-scale`. |
| `/research-repository` | `designer-skills` | Build a repository that makes findings findable, reusable, and cumulative across teams. Use when the same research keeps getting redone. For synthesising one study, use `affinity-diagram`. |
| `/responsive-design` | `designer-skills` | Design layouts and interactions that adapt across screen sizes and input methods. Use when one design must serve many viewports. For the underlying column grid use `layout-grid`; for OS-specific patterns use `platform-conventions`. |
| `/search-ux` | `designer-skills` | Design search — query input, zero results, refinement, and result presentation. Use when users retrieve rather than browse. For browse structure, use `navigation-patterns`. |
| `/serial-position-effect` | `designer-skills` | Apply the Serial Position Effect — first and last items in a sequence are recalled best. Use when ordering menus, lists, and steps. For emphasising one item regardless of its position, use `von-restorff-effect` (ui-design). |
| `/service-blueprint` | `designer-skills` | Map service delivery across frontstage actions, backstage processes, and supporting systems. Use when staff and operations are part of the experience. For the customer-visible layer only, use `experience-map`. |
| `/spacing-system` | `designer-skills` | Create a spacing scale from a base unit with rules for when each step applies. Use when standardising padding and margins. For page-level columns and gutters, use `layout-grid`. |
| `/stakeholder-alignment` | `designer-skills` | Build alignment artifacts — responsibility matrices, decision rights, and communication plans. Use when unclear ownership stalls decisions. For persuading in the moment, use `design-negotiation` (designer-toolkit). |
| `/state-machine` | `designer-skills` | Model component behaviour as explicit states, events, and transitions. Use when a component has many interacting states that must be exhaustive. For the feel and feedback of a single interaction, use `micro-interaction-spec`. |
| `/summarize-interview` | `designer-skills` | Turn one interview transcript into themes, supporting quotes, and action items. Use immediately after a session. For synthesising many sessions at once, use `affinity-diagram`. |
| `/survey-design` | `designer-skills` | Design unbiased survey instruments — question wording, scales, and sampling — to measure attitudes at scale. Use when you need quantitative breadth. For behavioural experiments, use `a-b-test-design` (prototyping-testing). |
| `/team-workflow` | `designer-skills` | Design the team's operating rhythm — task management, collaboration rituals, and tooling. Use when the day-to-day cadence needs structure. For a time-boxed sprint, use `design-sprint-plan`. |
| `/teslers-law` | `designer-skills` | Apply Tesler's Law — every process has irreducible complexity that someone must absorb. Use when deciding whether the product or the user carries it. For reducing apparent choice, use `hicks-law`. |
| `/test-scenario` | `designer-skills` | Write realistic usability task scenarios with success criteria and facilitation notes. Use when you have a study and need the tasks. For the surrounding study design, use `usability-test-plan` (design-research). |
| `/theming-system` | `designer-skills` | Design theming architecture — brand variants, dark mode, and high-contrast — mapped through token layers. Use when one system must serve multiple themes. For a single palette use `color-system` (ui-design); for dark mode craft use `dark-mode-design` (ui-design). |
| `/typography-scale` | `designer-skills` | Create a modular type scale with size, weight, and line-height relationships. Use when establishing typographic structure. For line length only use `readable-measure`; for judging type on an existing screen use `critique-typography` (visual-critique). |
| `/usability-test-plan` | `designer-skills` | Design a usability study — research questions, methodology, participant criteria, metrics, and facilitation guide. Use when planning the study as a whole. For writing the task scenarios inside it, use `test-scenario` (prototyping-testing). |
| `/user-flow-diagram` | `designer-skills` | Diagram screen-level paths, decision points, and branch logic. Use when specifying how a feature is traversed. For the emotional end-to-end arc, use `journey-map` (design-research). |
| `/user-persona` | `designer-skills` | Build research-grounded personas with goals, frustrations, and behavioural patterns. Use when decisions need a consistent user reference. For one session's emotional snapshot use `empathy-map`; for motivation framing use `jobs-to-be-done`. |
| `/ux-writing` | `designer-skills` | Write interface copy — microcopy, error messages, empty states, and CTAs. Use when the words are the deliverable. For content structure and ownership, use `content-strategy` (ux-strategy). |
| `/version-control-strategy` | `designer-skills` | Define version control for design files, components, and libraries — branching, naming, and release. Use when file history is chaotic. For design system contribution rules, use `design-system-governance` (design-systems). |
| `/visual-hierarchy` | `designer-skills` | Establish hierarchy through size, weight, colour, spacing, and position so the eye lands in the intended order. Use when composing new work. For judging an existing screen, use `critique-visual-hierarchy` (visual-critique). |
| `/von-restorff-effect` | `designer-skills` | Apply the Von Restorff Effect — the element that differs from its neighbours is the one remembered. Use when a single action must dominate. For overall ordering rather than single-element emphasis, use `visual-hierarchy`. |
| `/wireframe-spec` | `designer-skills` | Specify wireframe layout — content priority, component placement, and annotation. Use when defining structure before visual design. For grid mechanics, use `layout-grid` (ui-design). |
| `/zeigarnik-effect` | `designer-skills` | Apply the Zeigarnik Effect — incomplete tasks stay mentally active. Use when designing progress indicators, saved drafts, and return hooks. For the emotional shape of the ending, use `peak-end-rule`. |

### 2.12 Documents, Media & Tooling (Anthropics)
> Source: `anthropics/skills` (19 skills)

| Skill / Command | Category | Operational Focus |
| :--- | :--- | :--- |
| `/academy-guide` | `anthropics-skills` | > |
| `/algorithmic-art` | `anthropics-skills` | Creating algorithmic art using p5.js with seeded randomness and interactive parameter exploration. Use this when users request creating art using code, generative art, algorithmic art, flow fields, or particle systems. Create original algorithmic art rather than copying existing artists' work to avoid copyright violations. |
| `/brand-guidelines` | `anthropics-skills` | Applies Anthropic's official brand colors and typography to any sort of artifact that may benefit from having Anthropic's look-and-feel. Use it when brand colors or style guidelines, visual formatting, or company design standards apply. |
| `/canvas-design` | `anthropics-skills` | Create beautiful visual art in .png and .pdf documents using design philosophy. You should use this skill when the user asks to create a poster, piece of art, design, or other static piece. Create original visual designs, never copying existing artists' work to avoid copyright violations. |
| `/claude-api` | `anthropics-skills` | |- |
| `/discernment-nudge` | `anthropics-skills` | > |
| `/doc-coauthoring` | `anthropics-skills` | Guide users through a structured workflow for co-authoring documentation. Use when user wants to write documentation, proposals, technical specs, decision docs, or similar structured content. This workflow helps users efficiently transfer context, refine content through iteration, and verify the doc works for readers. Trigger when user mentions writing docs, creating proposals, drafting specs, or similar documentation tasks. |
| `/docx` | `anthropics-skills` | Use this skill whenever the user wants to create, read, edit, or manipulate Word documents (.docx files) or Word templates (.dotx files). Triggers include: any mention of 'Word doc', 'word document', '.docx', '.dotx', or requests to produce professional documents with formatting like tables of contents, headings, page numbers, or letterheads. Also use when extracting or reorganizing content from .docx or .dotx files, inserting or replacing images in documents, performing find-and-replace in Word files, working with tracked changes or comments, or converting content into a polished Word document. If the user asks for a 'report', 'memo', 'letter', 'template', or similar deliverable as a Word or .docx file, use this skill. Do NOT use for PDFs, spreadsheets, Google Docs, or general coding tasks unrelated to document generation. |
| `/frontend-design` | `anthropics-skills` | Guidance for distinctive, intentional visual design when building new UI or reshaping an existing one. Helps with aesthetic direction, typography, and making choices that don't read as templated defaults. |
| `/internal-comms` | `anthropics-skills` | A set of resources to help me write all kinds of internal communications, using the formats that my company likes to use. Claude should use this skill whenever asked to write some sort of internal communications (status reports, leadership updates, 3P updates, company newsletters, FAQs, incident reports, project updates, etc.). |
| `/mcp-builder` | `anthropics-skills` | Guide for creating high-quality MCP (Model Context Protocol) servers that enable LLMs to interact with external services through well-designed tools. Use when building MCP servers to integrate external APIs or services, whether in Python (FastMCP) or Node/TypeScript (MCP SDK). |
| `/pdf` | `anthropics-skills` | Use this skill whenever the user wants to do anything with PDF files. This includes reading or extracting text/tables from PDFs, combining or merging multiple PDFs into one, splitting PDFs apart, rotating pages, adding watermarks, creating new PDFs, filling PDF forms, encrypting/decrypting PDFs, extracting images, and OCR on scanned PDFs to make them searchable. If the user mentions a .pdf file or asks to produce one, use this skill. |
| `/pptx` | `anthropics-skills` | Use this skill any time a .pptx or .potx file is involved in any way — as input, output, or both. This includes: creating slide decks, pitch decks, or presentations; reading, parsing, or extracting text from any .pptx or .potx file (even if the extracted content will be used elsewhere, like in an email or summary); editing, modifying, or updating existing presentations; combining or splitting slide files; working with templates (.potx), layouts, speaker notes, or comments. Trigger whenever the user mentions \"deck,\" \"slides,\" \"presentation,\" or references a .pptx or .potx filename, regardless of what they plan to do with the content afterward. If a .pptx or .potx file needs to be opened, created, or touched, use this skill. |
| `/skill-creator` | `anthropics-skills` | Create new skills, modify and improve existing skills, and measure skill performance. Use when users want to create a skill from scratch, edit, or optimize an existing skill, run evals to test a skill, benchmark skill performance with variance analysis, or optimize a skill's description for better triggering accuracy. |
| `/slack-gif-creator` | `anthropics-skills` | Knowledge and utilities for creating animated GIFs optimized for Slack. Provides constraints, validation tools, and animation concepts. Use when users request animated GIFs for Slack like "make me a GIF of X doing Y for Slack. |
| `/theme-factory` | `anthropics-skills` | Toolkit for styling artifacts with a theme. These artifacts can be slides, docs, reportings, HTML landing pages, etc. There are 10 pre-set themes with colors/fonts that you can apply to any artifact that has been creating, or can generate a new theme on-the-fly. |
| `/web-artifacts-builder` | `anthropics-skills` | Suite of tools for creating elaborate, multi-component claude.ai HTML artifacts using modern frontend web technologies (React, Tailwind CSS, shadcn/ui). Use for complex artifacts requiring state management, routing, or shadcn/ui components - not for simple single-file HTML/JSX artifacts. |
| `/webapp-testing` | `anthropics-skills` | Toolkit for interacting with and testing local web applications using Playwright. Supports verifying frontend functionality, debugging UI behavior, capturing browser screenshots, and viewing browser logs. |
| `/xlsx` | `anthropics-skills` | Use this skill any time a spreadsheet file is the primary input or output. This means any task where the user wants to: open, read, edit, or fix an existing .xlsx, .xlsm, .xltx, .csv, or .tsv file (e.g., adding columns, computing formulas, formatting, charting, cleaning messy data); create a new spreadsheet from scratch or from other data sources; or convert between tabular file formats. Trigger especially when the user references a spreadsheet file by name or path — even casually (like \"the xlsx in my downloads\") — and wants something done to it or produced from it. Also trigger for cleaning or restructuring messy tabular data files (malformed rows, misplaced headers, junk data) into proper spreadsheets. The deliverable must be a spreadsheet file. Do NOT trigger when the primary deliverable is a Word document, HTML report, standalone Python script, database pipeline, or Google Sheets API integration, even if tabular data is involved. |

### 2.5 Enterprise Engineering, Multi-Agent Loops & Evals (ECC)
> Source: `affaan-m/ecc` (1,165+ Modular Skills & Orchestration Commands)

| Skill / Command | Category | Operational Focus |
| :--- | :--- | :--- |
| `/agent-harness-construction` | `ecc` | AI エージェントのアクション空間、ツール定義、観測フォーマットを設計・最適化して完了率を向上させます。 |
| `/agent-security-hardening` | `ecc` | Security hardening guidance for AI agent frameworks that process untrusted content, invoke tools, write workspace files, manage runtime identifiers, or handle credentials. Use when building or reviewing an agent runtime, autonomous worker, tool gateway, memory service, or multi-tenant agent deployment. Do not use for general web application security or offensive testing. |
| `/cost-aware-llm-pipeline` | `ecc` | LLM APIの使用量のコスト最適化パターン — タスクの複雑さによるモデルルーティング、予算追跡、リトライロジック、プロンプトキャッシング。 |
| `/cost-tracking` | `ecc` | ローカルのコスト追跡データベースからClaude Codeのトークン使用量、支出、予算を追跡・レポートします。コスト、支出、使用量、トークン、予算、またはプロジェクト、ツール、セッション、日付によるコスト内訳について質問する場合に使用します。 |
| `/council` | `ecc` | 曖昧な決定、トレードオフ、ゴー/ノーゴーの判断のために4つの声のカウンシルを召集します。複数の有効なパスが存在し、選択前に構造化された異議が必要な場合に使用します。 |
| `/council-multi-model` | `ecc` | Add one optional external Codex critique after the existing council has produced a decision draft. Use when an ambiguous, high-consequence decision would benefit from a separate model invocation's attempt to break the synthesis. Requires explicit consent before sending the compact draft and disagreement to OpenAI, labels same-provider reviews honestly, and marks the review absent when the adapter is unavailable. |
| `/deepseek-harness-setup` | `ecc` | Install and operate Everything Claude Code (ECC) on the DeepSeek Harness (DSH): native skill roots (~/.dsh/skills, .agents/skills), the @deepseek-ai/dsh-hooks-claude-code bridge for command hooks, bare-insert patch mounting, generator usage, event-support limits, and update workflow. Use when setting up ECC on DeepSeek Harness, wiring ECC hooks into a DSH profile, or debugging why ECC skills or hooks do not appear in a DSH session. |
| `/ecc-cmd-aside` | `ecc` | Answer a quick side question without interrupting or losing context from the current task. Resume work automatically after answering. |
| `/ecc-cmd-astra-review` | `ecc` | Send Claude-written code to GPT-6-Astra (ChatGPT, via Codex CLI) for an independent cross-provider review, then fix what it finds. |
| `/ecc-cmd-auto-update` | `ecc` | Pull the latest ECC repo changes and reinstall the current managed targets. |
| `/ecc-cmd-build-fix` | `ecc` | Detect the project build system and incrementally fix build/type errors with minimal safe changes. |
| `/ecc-cmd-checkpoint` | `ecc` | Create, verify, or list workflow checkpoints after running verification checks. |
| `/ecc-cmd-code-review` | `ecc` | Code review — local uncommitted changes or GitHub PR (pass PR number/URL for PR mode). Use for a step-by-step PRP-style checklist review; for a multi-agent pass use /review-pr, and for the adversarially-verified Workflow pass use /orch-review. |
| `/ecc-cmd-cost-report` | `ecc` | Generate a local Claude Code cost report from the ECC cost-tracker metrics log. Use for a terminal summary or CSV export of tracked spend; the cost-tracking skill covers the same metrics log for on-demand cost/budget questions asked in conversation. |
| `/ecc-cmd-cpp-build` | `ecc` | Fix C++ build errors, CMake issues, and linker problems incrementally. Invokes the cpp-build-resolver agent for minimal, surgical fixes. |
| `/ecc-cmd-cpp-review` | `ecc` | Comprehensive C++ code review for memory safety, modern C++ idioms, concurrency, and security. Invokes the cpp-reviewer agent. |
| `/ecc-cmd-cpp-test` | `ecc` | Enforce TDD workflow for C++. Write GoogleTest tests first, then implement. Verify coverage with gcov/lcov. |
| `/ecc-cmd-ecc-guide` | `ecc` | Navigate ECC's current agents, skills, commands, hooks, install profiles, and docs from the live repository surface. This is the slash-command entrypoint for that navigation; the ecc-guide skill covers the same map for on-demand use in conversation. |
| `/ecc-cmd-epic-claim` | `ecc` | Claim an epic issue, stamp coordination state, and sync local ownership. |
| `/ecc-cmd-epic-decompose` | `ecc` | Break an epic into task children without creating task branches. |
| `/ecc-cmd-epic-publish` | `ecc` | Publish a validated epic update back to the issue and local cache. |
| `/ecc-cmd-epic-review` | `ecc` | Mark epic review requested, approved, or changes requested. |
| `/ecc-cmd-epic-sync` | `ecc` | Sync epic issue bodies, labels, and local coordination snapshots from GitHub. |
| `/ecc-cmd-epic-unblock` | `ecc` | Sweep blocked epic issues and reopen anything whose dependencies are closed. |
| `/ecc-cmd-epic-validate` | `ecc` | Validate epic readiness, dependencies, and coordination policy. |
| `/ecc-cmd-fastapi-review` | `ecc` | Review a FastAPI application for architecture, async correctness, dependency injection, Pydantic schemas, security, performance, and testability. |
| `/ecc-cmd-feature-dev` | `ecc` | Guided feature development with codebase understanding and architecture focus |
| `/ecc-cmd-flutter-build` | `ecc` | Fix Dart analyzer errors and Flutter build failures incrementally. Invokes the dart-build-resolver agent for minimal, surgical fixes. |
| `/ecc-cmd-flutter-review` | `ecc` | Review Flutter/Dart code for idiomatic patterns, widget best practices, state management, performance, accessibility, and security. Invokes the flutter-reviewer agent. |
| `/ecc-cmd-flutter-test` | `ecc` | Run Flutter/Dart tests, report failures, and incrementally fix test issues. Covers unit, widget, golden, and integration tests. |
| `/ecc-cmd-gan-build` | `ecc` | Run a generator/evaluator build loop for implementation tasks with bounded iterations and scoring. |
| `/ecc-cmd-gan-design` | `ecc` | Run a generator/evaluator design loop for frontend or visual work with bounded iterations and scoring. |
| `/ecc-cmd-go-build` | `ecc` | Fix Go build errors, go vet warnings, and linter issues incrementally. Invokes the go-build-resolver agent for minimal, surgical fixes. |
| `/ecc-cmd-go-review` | `ecc` | Comprehensive Go code review for idiomatic patterns, concurrency safety, error handling, and security. Invokes the go-reviewer agent. |
| `/ecc-cmd-go-test` | `ecc` | Enforce TDD workflow for Go. Write table-driven tests first, then implement. Verify 80%+ coverage with go test -cover. |
| `/ecc-cmd-gradle-build` | `ecc` | Fix Gradle build errors for Android and KMP projects |
| `/ecc-cmd-harness-audit` | `ecc` | Run a deterministic repository harness audit and return a prioritized scorecard. Use for a deterministic overall repo-readiness scorecard; for a security-specific audit use the security-scan command. |
| `/ecc-cmd-hookify` | `ecc` | Create hooks to prevent unwanted behaviors from conversation analysis or explicit instructions. Use to generate a new hook rule file from conversation analysis or a described behavior; the hookify-rules skill covers hookify rule syntax and patterns for authoring or editing rules directly. |
| `/ecc-cmd-hookify-configure` | `ecc` | Enable or disable hookify rules interactively |
| `/ecc-cmd-hookify-help` | `ecc` | Get help with the hookify system |
| `/ecc-cmd-hookify-list` | `ecc` | List all configured hookify rules |
| `/ecc-cmd-jira` | `ecc` | Retrieve a Jira ticket, analyze requirements, update status, or add comments. Uses the jira-integration skill and MCP or REST API. |
| `/ecc-cmd-kotlin-build` | `ecc` | Fix Kotlin/Gradle build errors, compiler warnings, and dependency issues incrementally. Invokes the kotlin-build-resolver agent for minimal, surgical fixes. |
| `/ecc-cmd-kotlin-review` | `ecc` | Comprehensive Kotlin code review for idiomatic patterns, null safety, coroutine safety, and security. Invokes the kotlin-reviewer agent. |
| `/ecc-cmd-kotlin-test` | `ecc` | Enforce TDD workflow for Kotlin. Write Kotest tests first, then implement. Verify 80%+ coverage with Kover. |
| `/ecc-cmd-learn` | `ecc` | Extract reusable patterns from the current session and save them as candidate skills or guidance. Use to review a session on demand and persist an approved skill file; continuous-learning-v2 is a separate, configurable Stop/PreToolUse/PostToolUse hook-observation and instinct-evolution system (background observer disabled by default) and is not an automatic equivalent of this command. |
| `/ecc-cmd-learn-eval` | `ecc` | Extract reusable patterns from the session, self-evaluate quality before saving, and determine the right save location (Global vs Project). |
| `/ecc-cmd-loop-start` | `ecc` | Prepare a managed autonomous loop pattern with safety defaults and explicit stop conditions, then print the commands to launch and monitor it. Use to set up sequential, continuous-pr, rfc-dag, or infinite loop patterns with safety gates before starting one; the continuous-agent-loop skill covers the same pattern selection and quality-gate guidance for in-conversation use (supersedes the deprecated autonomous-loops skill). |
| `/ecc-cmd-loop-status` | `ecc` | Inspect active loop state, progress, failure signals, and recommended intervention. |
| `/ecc-cmd-marketing-campaign` | `ecc` | Plan and execute a full marketing campaign. Accepts a product brief and returns positioning, landing page copy, email sequence, social posts, ad variants, video scripts, and a content calendar. Can also review existing copy for conversion quality. This is the slash-command entrypoint that delegates to the marketing-agent; prefer the marketing-campaign skill for the same end-to-end workflow in conversation. |
| `/ecc-cmd-model-route` | `ecc` | Recommend the best model tier for the current task based on complexity, risk, and budget. |
| `/ecc-cmd-multi-backend` | `ecc` | Run a backend-focused multi-model workflow for APIs, algorithms, data, and business logic. |
| `/ecc-cmd-multi-execute` | `ecc` | Execute a multi-model implementation plan while preserving Claude as the only filesystem writer. |
| `/ecc-cmd-multi-frontend` | `ecc` | Run a frontend-focused multi-model workflow for components, layouts, animation, and UI polish. |
| `/ecc-cmd-multi-plan` | `ecc` | Create a multi-model (Codex + Antigravity) implementation plan without modifying production code. Requires the external ccg-workflow runtime, not part of the base ECC install (see Prerequisite below). Use when the user explicitly wants dual-model plan drafts; for a single-model plan with no extra runtime use /plan. |
| `/ecc-cmd-multi-workflow` | `ecc` | Run a full multi-model development workflow with research, planning, execution, optimization, and review. |
| `/ecc-cmd-orch-add-feature` | `ecc` | Orchestrate building a brand-new feature end to end — research, plan, TDD, review, gated commit. Wrapper that kicks off the orch-add-feature skill. |
| `/ecc-cmd-orch-build-mvp` | `ecc` | Orchestrate bootstrapping a working MVP from a design/spec doc — ingest, slice, scaffold, TDD, review, gated commit (reuses the GAN harness). Wrapper for the orch-build-mvp skill. |
| `/ecc-cmd-orch-change-feature` | `ecc` | Orchestrate altering an existing, working feature to new desired behavior — update tests to the new spec, change impl, review, gated commit. Wrapper for the orch-change-feature skill. |
| `/ecc-cmd-orch-fix-defect` | `ecc` | Orchestrate fixing a bug — reproduce it as a failing regression test, fix to green, review, gated commit. Wrapper for the orch-fix-defect skill. |
| `/ecc-cmd-orch-refine-code` | `ecc` | Orchestrate a behavior-preserving refactor — confirm tests green, restructure without changing behavior, keep green, review, gated commit. Wrapper for the orch-refine-code skill. |
| `/ecc-cmd-orch-review` | `ecc` | Run the orch-review native Workflow over a diff (local changes or a GitHub PR) and report blocking vs advisory findings. Surface for the orch-review workflow. Use for a native-Workflow, adversarially-verified multi-dimension review with fan-out and dedup; for the step-by-step checklist pass use /code-review, and for the multi-agent pass use /review-pr. |
| `/ecc-cmd-plan` | `ecc` | Restate requirements, assess risks, and create step-by-step implementation plan. WAIT for user CONFIRM before touching any code. Use for a single-model inline or PRD-driven implementation plan; for a dual-model (Codex/Antigravity) plan use /multi-plan, and for visual annotate-and-approve review of the resulting plan use /plan-canvas. |
| `/ecc-cmd-plan-canvas` | `ecc` | Open a plan or HTML artifact in the browser Plan Canvas for annotate-and-approve review. This is a thin slash-command entrypoint over the plan-canvas skill, which covers the full workflow and rules. |
| `/ecc-cmd-plan-prd` | `ecc` | Generate a lean, problem-first PRD and hand off to /plan for implementation planning. |
| `/ecc-cmd-pm2` | `ecc` | Analyze a project and generate PM2 service commands for detected frontend, backend, or database services. |
| `/ecc-cmd-pr` | `ecc` | Create a GitHub PR from current branch with unpushed commits — discovers templates, analyzes changes, pushes |
| `/ecc-cmd-project-init` | `ecc` | Detect a project's stack and produce a dry-run ECC onboarding plan using the repository's install manifests and stack mappings. Use to onboard ECC into a target project via a reviewable dry-run plan; for general feature discovery and navigation use the ecc-guide skill or /ecc-guide command instead. |
| `/ecc-cmd-prp-commit` | `ecc` | Quick commit with natural language file targeting — describe what to commit in plain English |
| `/ecc-cmd-prp-implement` | `ecc` | Execute an implementation plan with rigorous validation loops |
| `/ecc-cmd-prp-plan` | `ecc` | Create comprehensive feature implementation plan with codebase analysis and pattern extraction |
| `/ecc-cmd-prp-pr` | `ecc` | Alias of /pr for the PRP workflow series. Use when creating a pull request mid-PRP workflow; otherwise use /pr. |
| `/ecc-cmd-prp-prd` | `ecc` | Interactive PRD generator - problem-first, hypothesis-driven product spec with back-and-forth questioning |
| `/ecc-cmd-python-review` | `ecc` | Comprehensive Python code review for PEP 8 compliance, type hints, security, and Pythonic idioms. Invokes the python-reviewer agent. |
| `/ecc-cmd-quality-gate` | `ecc` | Run the ECC formatter quality gate for a single file and report remediation steps. |
| `/ecc-cmd-react-build` | `ecc` | Fix React build failures (Vite, webpack, Next.js, CRA, Parcel, esbuild, Bun) incrementally — JSX/TSX compile errors, hydration mismatches, server/client component boundary failures, missing types. Invokes the react-build-resolver agent for minimal, surgical fixes. |
| `/ecc-cmd-react-review` | `ecc` | Comprehensive React/JSX code review for hook correctness, render performance, server/client component boundaries, accessibility, and React-specific security. Invokes the react-reviewer agent (and typescript-reviewer alongside on TSX/JSX changes). |
| `/ecc-cmd-react-test` | `ecc` | Enforce TDD workflow for React. Write React Testing Library tests first (behavior-focused, accessibility-first), then implement components. Detects Vitest or Jest and verifies coverage targets. |
| `/ecc-cmd-refactor-clean` | `ecc` | Safely identify and remove dead code with verification after each change. |
| `/ecc-cmd-resume-session` | `ecc` | Load the most recent session file from ~/.claude/session-data/ and resume work with full context from where the last session ended. |
| `/ecc-cmd-review-pr` | `ecc` | Comprehensive PR review using specialized agents (code-reviewer, comment-analyzer, pr-test-analyzer, silent-failure-hunter, type-design-analyzer, code-simplifier). Use for a multi-agent PR review pass; for the adversarially-verified Workflow pass use /orch-review, and for the standalone step-by-step checklist review use /code-review. |
| `/ecc-cmd-rust-build` | `ecc` | Fix Rust build errors, borrow checker issues, and dependency problems incrementally. Invokes the rust-build-resolver agent for minimal, surgical fixes. |
| `/ecc-cmd-rust-review` | `ecc` | Comprehensive Rust code review for ownership, lifetimes, error handling, unsafe usage, and idiomatic patterns. Invokes the rust-reviewer agent. |
| `/ecc-cmd-rust-test` | `ecc` | Enforce TDD workflow for Rust. Write tests first, then implement. Verify 80%+ coverage with cargo-llvm-cov. |
| `/ecc-cmd-santa-loop` | `ecc` | Adversarial dual-review convergence loop — two independent model reviewers must both approve before code ships. Use for the CLI-driven version of this loop; wraps the santa-method skill. |
| `/ecc-cmd-save-session` | `ecc` | Save current session state to a dated file in ~/.claude/session-data/ so work can be resumed in a future session with full context. |
| `/ecc-cmd-security-scan` | `ecc` | Run AgentShield against agent, hook, MCP, permission, and secret surfaces. This is the slash-command entrypoint for that audit; prefer the security-scan skill for the same AgentShield audit in conversation. |
| `/ecc-cmd-sessions` | `ecc` | Manage Claude Code session history, aliases, and session metadata. |
| `/ecc-cmd-setup-pm` | `ecc` | Configure your preferred package manager (npm/pnpm/yarn/bun) |
| `/ecc-cmd-test-coverage` | `ecc` | Analyze coverage, identify gaps, and generate missing tests toward the target threshold. |
| `/ecc-cmd-token-card` | `ecc` | Render a token-usage stat card from local agent session logs and commit it to the repo. Invokes the token-card skill. |
| `/ecc-cmd-update-codemaps` | `ecc` | Scan project structure and generate token-lean architecture codemaps. |
| `/ecc-cmd-update-docs` | `ecc` | Sync documentation from source-of-truth files such as scripts, schemas, routes, and exports. |
| `/ecc-cmd-vue-review` | `ecc` | Comprehensive Vue.js code review for Composition API correctness, reactivity, composable patterns, template security, accessibility, and Vue-specific performance. Invokes the vue-reviewer agent (and typescript-reviewer alongside on .vue/.ts changes). |
| `/ecc-council` | `ecc` | Convene a four-voice council for ambiguous decisions, tradeoffs, and go/no-go calls. Use when multiple valid paths exist and you need structured disagreement before choosing. |
| `/ecc-tools-cost-audit` | `ecc` | ECC ツール、エージェント、スキル、および実装のコスト監査を実施します。プロンプト入力トークンを分析して、計算効率を定量化します。 |
| `/instinct-export` | `ecc` | Export instincts from project/global scope to a file |
| `/instinct-import` | `ecc` | Import instincts from file or URL into project/global scope |
| `/instinct-status` | `ecc` | Show learned instincts (project + global) with confidence |
| `/loop-design-check` | `ecc` | Design a goal-oriented agent loop or review one for failure modes: spinning, Goodhart-gaming the verifier, or running a wrong answer to completion. Covers machine-decidable goals, loop types, plan/build/judge skeletons, and runaway prevention; mechanism wiring lives in autonomous-loops. Use when designing, writing, or checking an agent loop. 中文触发：写 loop、设计 loop、做一个 loop、检查 loop 对不对、loop 体检、loop 会不会跑飞、可判定目标、五个崩法、plan build judge。 |
| `/orch-add-feature` | `ecc` | Orchestrate building a brand-new feature end to end — research, plan, TDD implementation, review, and gated commit — by delegating each phase to the matching ECC agent. Use when adding a capability that does not exist yet. |
| `/orch-build-mvp` | `ecc` | Orchestrate bootstrapping a working MVP from a design or spec document — ingest the SDD/PRD, plan thin vertical slices, scaffold the first end-to-end slice, then drive a generator-evaluator build loop with review and gated feat commits. Use when a design or spec document must become a running MVP through planned vertical slices. |
| `/orch-change-feature` | `ecc` | Orchestrate altering an existing, working feature to new desired behavior — update its tests to the new spec, change the implementation to match, review, and gated commit. Use when behavior is not broken but should be different. |
| `/orch-fix-defect` | `ecc` | Orchestrate fixing a bug — reproduce it as a failing regression test, fix to green, review, and gated commit — by delegating each phase to the matching ECC agent. Use when existing behavior is broken or wrong. |
| `/orch-pipeline` | `ecc` | Shared orchestration engine behind the orch-* skill family — the gated Research-Plan-TDD-Review-Commit pipeline, size classifier, agent and command map, and two human gates (plan approval, commit confirmation) that orch-* operation skills delegate to. Use indirectly via orch-add-feature, orch-fix-defect, orch-change-feature, orch-refine-code, or orch-build-mvp; read directly only when adding an orch operation or tuning shared phases. |
| `/orch-refine-code` | `ecc` | Orchestrate a behavior-preserving refactor — confirm tests are green, restructure without changing behavior, keep tests green, review, and gated commit. Use when the structure should improve but behavior must not change. |
| `/pytorch-patterns` | `ecc` | PyTorch deep learning patterns and best practices for building robust, efficient, and reproducible training pipelines, model architectures, and data loading. |
| `/security-bounty-hunter` | `ecc` | リポジトリ内の悪用可能なバウンティ対象のセキュリティ問題を発見します。ノイズの多いローカルのみの発見ではなく、実際のレポートに適格なリモートから到達可能な脆弱性に焦点を当てます。 |
| `/security-review` | `ecc` | Use this skill when adding authentication, handling user input, working with secrets, creating API endpoints, or implementing payment/sensitive features. Provides comprehensive security checklist and patterns. |
| `/security-scan` | `ecc` | AgentShield を使用して、Claude Code の設定（.claude/ ディレクトリ）のセキュリティ脆弱性、設定ミス、インジェクションリスクをスキャンします。CLAUDE.md、settings.json、MCP サーバー、フック、エージェント定義をチェックします。 |
| `/test-collaboration` | `ecc` | Inventory and govern a project's test assets. Connect requirements, business rules, risks, bugs, and cross-boundary contracts to stable TEST-IDs and executable evidence in the repository's existing test registry or TESTS.md. Use for test inventory, test gap analysis, unit/integration/contract/E2E/smoke classification, bug-to-test regression protection, consumer/provider contract testing, test-necessity review, and pre-delivery evidence review. |
*(And 302+ framework, language, and micro-optimization skills in `~/.gemini/config/plugins/ecc/skills/`)*

---

## 3. Model Context Protocol (MCP) Architecture

> Model Context Protocol connects LLM reasoning directly to external tools, local emulators, live browser runtimes, reverse engineering engines, and cloud databases.

### 3.1 Active System MCP Servers & Tool Suites

| Server Name | Primary Domain | Core Capabilities & Tool Highlights |
| :--- | :--- | :--- |
| **`artemis`** | Native Android Mobile Automation | `mobile_get_device_state` (UI hierarchy & bounds), `mobile_diagnose` (ADB self-healing), `mobile_run_task` (Flash/Pro automated agent exploration), `mobile_inspect_trace`, `mobile_manage_task`. |
| **`ecc_chrome-devtools`** | Chrome Browser Runtime & DOM | `click`, `fill`, `hover`, `press_key`, `take_screenshot`, `take_snapshot`, `list_console_messages`, `list_network_requests`, `lighthouse_audit`, `performance_start_trace`. |
| **`motion`** | Motion.dev Animation Platform | `search-motion-docs` (semantic search across Motion/Framer docs), `insert-motion-component` (scaffolding 60fps accessible animation components). |
| **`paper`** | Canvas Design & Artboard Ops | `open_file`, `create_artboard`, `get_node_info`, `get_children`, `get_screenshot`, `get_jsx`, `get_computed_styles`, `update_styles`, `get_tokens`, `create_tokens`. |
| **`rea`** | Reverse Engineer Anything | Binary disassembler (`procedure_assembly`, `batch_decompile`), Mach-O/ELF/PE inspection, Frida hooks (`start_frida_session`, `load_frida_script`), Cutter/GDB debugging, ADB control (`inspect_android_package`, `read_adb_logcat`), Web runtime reconciliation. |
| **`expo`** | React Native Expo Development | `read_documentation`, `search_documentation`, `workflow_run`, `build_run`, `sandbox_create`, `simulator_start`, `automation_tap`, `automation_take_screenshot`. |
| **`mobbin_mobbin`** | Real-World Mobile Design Gallery | `search_screens`, `search_flows`, `search_sections`, `list_collections`, `get_collection_contents`, `create_collection`. |

### 3.2 Extended Ecosystem MCP Servers Registry

| Server Name | Source Plugin | Runtime Command | Functional Description |
| :--- | :--- | :--- | :--- |
| **`nexus`** | `ecc` | `nexus mcp` | Local cost and privacy proxy: routes prompts to cheapest capable model and masks sensitive PII before egress. |
| **`ito-compute`** | `ecc` | `node .../ito-mcp.js` | Opt-in local compute marketplace integration for distributed inference and tasks. |
| **`jira`** | `ecc` | `uvx mcp-atlassian==0.21.0` | Atlassian Jira issue management: search, create, update, comment, and transition tickets. |
| **`github`** | `ecc` | `npx -y @modelcontextprotocol/server-github` | Full GitHub repository operations: PR creation, review comments, issue triage, branch management. |
| **`firecrawl`** | `ecc` | `npx -y firecrawl-mcp` | Web scraping, deep crawling, and LLM-ready markdown extraction from any URL. |
| **`supabase`** | `ecc` | `npx -y @supabase/mcp-server` | PostgreSQL cloud database management, SQL execution, migrations, and auth policy verification. |
| **`context7`** | `ecc` | `uvx context7-mcp` | Real-time library documentation lookup and up-to-date framework API reference retriever. |
| **`gsd-mcp-server`** | `gsd-core` | `node .../gsd-mcp.js` | Companion server for GSD milestone state queries, plan verification, and graph traversal. |

---

## 4. Master Intent Router & Invocation Guide

| "I want to..." | Recommended Slash Command | Specialist Subagent | Key MCP Server |
| :--- | :--- | :--- | :--- |
| **Test features on a live connected Android device** | — | — | `artemis` (`mobile_run_task`, `mobile_get_device_state`) |
| **Inspect live DOM, network frames & console logs** | `/browser-testing-with-devtools` | `e2e-runner` | `ecc_chrome-devtools` |
| **Author bulletproof Firestore security rules** | `/firestore-rules-creation` | `firestore-rules-author` | — |
| **Audit animation frame drops & MotionScore** | `/animation-performance` | `motion-reviewer` | `motion` |
| **Review Kotlin coroutines, Room & Compose architecture** | `/gsd-code-review` | `kotlin-reviewer` | — |
| **Audit accessibility touch targets & screen reader semantics**| `/accessibility-audit` | `a11y-architect` | `artemis` (view_type="hierarchy") |
| **Audit app security, GDPR compliance & zero-write privacy** | `/security-and-hardening` | `security-reviewer` | — |
| **Resolve compilation or Gradle build errors** | `/gsd-code-review --fix` | `build-error-resolver` | — |
| **Review documentation with stateless multi-persona swarm** | `/egm-review` | `egm-review` / `egm-synthesizer` | — |
| **Decompose codebase into documentation contracts** | `/egm-bootstrap` | `egm-bootstrap-orchestrator`| — |
| **Synthesize semantic design tokens into DESIGN.md** | `/design-md` | `code-architect` | `paper` |
| **Port Stitch web designs to React Native or Compose** | `/stitch-react-native` | `react-reviewer` | `paper` |
| **Reverse engineer a binary, APK or web bundle** | `/reverse-engineer-anything` | `code-explorer` | `rea` |
| **Search real mobile UI flows and inspiration** | `/search` | — | `mobbin_mobbin` |
| **Orchestrate full milestone lifecycle (SDD)** | `/gsd-next` -> `/gsd-plan-phase` | `planner` / `gsd-planner` | `gsd-mcp-server` |
| **Execute milestone wave with strict quality gates** | `/gsd-execute-phase` | `gsd-executor` | — |
| **Verify completed work against acceptance criteria** | `/gsd-verify-work` | `gsd-verifier` | `artemis` |
| **Clean unused files, stubs and dead code safely** | `/gsd-cleanup` | `refactor-cleaner` | — |
| **Build interactive local knowledge graph** | `/gsd-graphify` | `gsd-intel-updater` | — |

---

*Generated automatically for the CoupleFit ecosystem. Single Source of Truth for Antigravity Agents, Skills & MCP Servers.*