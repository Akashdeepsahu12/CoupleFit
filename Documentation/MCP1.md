# Antigravity Ecosystem Model Context Protocol (MCP) Catalog (MCP1.md)

> Comprehensive specification and configuration guide for all MCP servers and tools across the installed ecosystem:
> - **Active & Available System MCP Servers**: `artemis`, `motion`
> - **ECC MCP Ecosystem** (`https://github.com/affaan-m/ecc`): `chrome-devtools`, `nexus`, `ito-compute`, and canonical registry
> - **GSD-Core MCP Ecosystem** (`https://github.com/open-gsd/gsd-core`): `gsd-mcp-server` companion server & tools

---

## 1. Antigravity System Active MCP Servers

### 1.1 Artemis Mobile Testing MCP Server (`artemis`)
Provides native Android and device automation, screen inspection, touch actions, and diagnostics.

**Available Tools:**
- `mobile_get_device_state`: Captures current UI element hierarchy, bounds, and screen state.
- `mobile_diagnose`: Deep diagnostic check on ADB connections, RSA authorization, emulators, and runners.
- `mobile_inspect_trace`: Inspects execution trace, logs, screenshots, and step histories.
- `mobile_manage_task`: Manages running tasks on devices (`list`, `status`, `stop`).
- `mobile_run_task`: Dispatches exploratory or test automation tasks using Flash or Pro models.

### 1.2 Motion Animation MCP Server (`motion`)
Provides live access to Motion and Motion+ documentation, animation curves, and frame-rate diagnostics.

**Configuration (`mcp.json`):**
```json
{
  "mcpServers": {
    "Motion": { "url": "https://mcp.motion.dev" },
    "Motion+": { "url": "https://mcp.motion.dev/plus" }
  }
}
```

**Available Tools:**
- `search-motion-docs`: Real-time semantic search over Motion official API and animation guides.

---

## 2. ECC MCP Architecture (`affaan-m/ecc`)

### 2.1 Chrome DevTools MCP Server (`chrome-devtools`)
Enables full DOM tree inspection, console error logging, performance profiling, and network frame capture.

**Configuration (`mcp_config.json`):**
```json
{
  "mcpServers": {
    "chrome-devtools": {
      "command": "npx",
      "args": ["-y", "chrome-devtools-mcp@1.10.1"],
      "cwd": "",
      "env": null
    }
  }
}
```

### 2.2 ECC Canonical MCP Servers Inventory (`mcp-servers.json`)

```json
{
  "mcpServers": {
    "nexus": {
      "command": "nexus",
      "args": ["mcp"],
      "description": "Local cost/privacy proxy - query your own usage & savings, route to the cheapest capable model, and mask secrets/PII before egress (nexus_stats, nexus_savings, nexus_recent, nexus_providers, nexus_cost_breakdown)"
    },
    "ito-compute": {
      "command": "node",
      "args": ["/absolute/path/to/ito-cloud-runtime/cli/ito-compute-cli/dist/bin/ito-mcp.js"],
      "description": "Opt-in local Itô compute MCP. The canonical package is unpublished and must be built from Ito-Markets/ito-cloud-runtime/cli/ito-compute-cli. Exposes ito_auth, ito_find, ito_status, and ito_accept. ito_auth validates existing credentials; it does not start device login. Use ecc ito login [--no-browser] for device authorization, which stores tokens in macOS Keychain by default; explicit file fallback must retain owner-only settings. ECC itself performs no browser automation. ITO_API_KEY is forwarded directly to auth, find, status, and accept when configured; ITO_AUTH_MODE=legacy is not required."
    },
    "jira": {
      "command": "uvx",
      "args": ["mcp-atlassian==0.21.0"],
      "env": {
        "JIRA_URL": "YOUR_JIRA_URL_HERE",
        "JIRA_EMAIL": "YOUR_JIRA_EMAIL_HERE",
        "JIRA_API_TOKEN": "YOUR_JIRA_API_TOKEN_HERE"
      },
      "description": "Jira issue tracking — search, create, update, comment, transition issues"
    },
    "github": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "env": {
        "GITHUB_PERSONAL_ACCESS_TOKEN": "YOUR_GITHUB_PAT_HERE"
      },
      "description": "GitHub operations - PRs, issues, repos"
    },
    "firecrawl": {
      "command": "npx",
      "args": ["-y", "firecrawl-mcp"],
      "env": {
        "FIRECRAWL_API_KEY": "YOUR_FIRECRAWL_KEY_HERE"
      },
      "description": "Web scraping and crawling"
    },
    "supabase": {
      "command": "npx",
      "args": ["-y", "@supabase/mcp-server-supabase@latest", "--project-ref=YOUR_PROJECT_REF"],
      "description": "Supabase database operations"
    },
    "ecc-memory-vault": {
      "command": "ecc-memory-mcp",
      "env": {
        "ECC_MEMORY_HARNESS": "YOUR_LOWERCASE_HARNESS_SLUG_HERE"
      },
      "description": "Opt-in local ECC Memory Vault shared by Claude, Codex, Hermes, Cursor, OpenCode, and other MCP clients. Replace ECC_MEMORY_HARNESS with this server's lowercase identity; callers cannot override it. Normal search recall is active project+team memory. To permit explicitly requested user scope, the operator may also set ECC_MEMORY_ALLOW_USER_SCOPE=1. Writes are create-only and always unreviewed. Install ECC globally or make its bin available on PATH. Not enabled by default."
    },
    "memory": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-memory"],
      "description": "Persistent memory across sessions"
    },
    "omega-memory": {
      "command": "uvx",
      "args": ["omega-memory", "serve"],
      "description": "Persistent agent memory with semantic search, multi-agent coordination, and knowledge graphs — run via uvx (richer than the basic memory store)"
    },
    "longhand": {
      "command": "longhand",
      "args": ["mcp-server"],
      "description": "Lossless Claude Code session history — indexes raw tool calls, file edits, and thinking blocks from ~/.claude/projects/*.jsonl into local SQLite + ChromaDB before Claude Code rotates them. Complements memory/omega-memory (synthesized) with verbatim recall. Install: pip install longhand && longhand setup"
    },
    "sequential-thinking": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-sequential-thinking"],
      "description": "Chain-of-thought reasoning"
    },
    "vercel": {
      "type": "http",
      "url": "https://mcp.vercel.com",
      "description": "Vercel deployments and projects"
    },
    "railway": {
      "command": "npx",
      "args": ["-y", "@railway/mcp-server"],
      "description": "Railway deployments"
    },
    "cloudflare-docs": {
      "type": "http",
      "url": "https://docs.mcp.cloudflare.com/mcp",
      "description": "Cloudflare documentation search"
    },
    "cloudflare-workers-builds": {
      "type": "http",
      "url": "https://builds.mcp.cloudflare.com/mcp",
      "description": "Cloudflare Workers builds"
    },
    "cloudflare-workers-bindings": {
      "type": "http",
      "url": "https://bindings.mcp.cloudflare.com/mcp",
      "description": "Cloudflare Workers bindings"
    },
    "cloudflare-observability": {
      "type": "http",
      "url": "https://observability.mcp.cloudflare.com/mcp",
      "description": "Cloudflare observability/logs"
    },
    "clickhouse": {
      "type": "http",
      "url": "https://mcp.clickhouse.cloud/mcp",
      "description": "ClickHouse analytics queries"
    },
    "exa-web-search": {
      "command": "npx",
      "args": ["-y", "exa-mcp-server"],
      "env": {
        "EXA_API_KEY": "YOUR_EXA_API_KEY_HERE"
      },
      "description": "Web search, research, and data ingestion via Exa API — prefer task-scoped use for broader research after GitHub search and primary docs"
    },
    "parallel-search": {
      "type": "http",
      "url": "https://search.parallel.ai/mcp",
      "description": "Parallel Web Search — LLM-optimized web_search and web_fetch tools that take an objective + queries and return citation-backed excerpts in a single call (replaces multiple keyword searches). Works key-free for anonymous use. For higher rate limits, add a headers block: { \"Authorization\": \"Bearer YOUR_PARALLEL_API_KEY_HERE\" } using a key from platform.parallel.ai."
    },
    "context7": {
      "command": "npx",
      "args": ["-y", "@upstash/context7-mcp@latest"],
      "description": "Live documentation lookup — use with /docs command and documentation-lookup skill (resolve-library-id, query-docs)."
    },
    "codescene": {
      "command": "npx",
      "args": ["-y", "@codescene/codehealth-mcp"],
      "env": {
        "CS_ACCESS_TOKEN": "YOUR_CS_ACCESS_TOKEN_HERE"
      },
      "description": "Optional. CodeScene Code Health MCP (community skill: codehealth-mcp). Not enabled by default — copy only if you opt in. Requires your own CS_ACCESS_TOKEN (no bundled credentials). See skills/codehealth-mcp/SKILL.md for data boundaries and failure behavior."
    },
    "magic": {
      "command": "npx",
      "args": ["-y", "@magicuidesign/mcp@latest"],
      "description": "Magic UI components"
    },
    "memxus": {
      "type": "http",
      "url": "https://mcp.memxus.com/mcp",
      "headers": {
        "Authorization": "Bearer YOUR_MEMXUS_API_KEY_HERE"
      },
      "description": "Universal persistent memory across Claude Code, Cursor, Gemini CLI and any AI tool — save context once, auto-recalled in every session. Note: review stored memories before use in production agents to avoid prompt-injection via memory-poisoning. Free at memxus.com"
    },
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem", "/path/to/your/projects"],
      "description": "Filesystem operations (set your path)"
    },
    "playwright": {
      "command": "npx",
      "args": ["-y", "@playwright/mcp", "--browser", "chrome"],
      "description": "Browser automation and testing via Playwright"
    },
    "fal-ai": {
      "command": "npx",
      "args": ["-y", "fal-ai-mcp-server"],
      "env": {
        "FAL_KEY": "YOUR_FAL_KEY_HERE"
      },
      "description": "AI image/video/audio generation via fal.ai models"
    },
    "browserbase": {
      "command": "npx",
      "args": ["-y", "@browserbasehq/mcp-server-browserbase"],
      "env": {
        "BROWSERBASE_API_KEY": "YOUR_BROWSERBASE_KEY_HERE"
      },
      "description": "Cloud browser sessions via Browserbase"
    },
    "browser-use": {
      "type": "http",
      "url": "https://api.browser-use.com/mcp",
      "headers": {
        "x-browser-use-api-key": "YOUR_BROWSER_USE_KEY_HERE"
      },
      "description": "AI browser agent for web tasks"
    },
    "devfleet": {
      "type": "http",
      "url": "http://localhost:18801/mcp",
      "description": "Multi-agent orchestration — dispatch parallel Claude Code agents in isolated worktrees. Plan projects, auto-chain missions, read structured reports. Repo: https://github.com/LEC-AI/claude-devfleet"
    },
    "token-optimizer": {
      "command": "npx",
      "args": ["-y", "token-optimizer-mcp"],
      "description": "Token optimization for 95%+ context reduction via content deduplication and compression"
    },
    "laraplugins": {
      "type": "http",
      "url": "https://laraplugins.io/mcp/plugins",
      "description": "Laravel plugin discovery — search packages by keyword, health score, Laravel/PHP version compatibility. Use with laravel-plugin-discovery skill."
    },
    "confluence": {
      "command": "npx",
      "args": ["-y", "confluence-mcp-server"],
      "env": {
        "CONFLUENCE_BASE_URL": "YOUR_CONFLUENCE_URL_HERE",
        "CONFLUENCE_EMAIL": "YOUR_EMAIL_HERE",
        "CONFLUENCE_API_TOKEN": "YOUR_CONFLUENCE_TOKEN_HERE"
      },
      "description": "Confluence Cloud integration — search pages, retrieve content, explore spaces"
    },
    "evalview": {
      "command": "python3",
      "args": ["-m", "evalview", "mcp", "serve"],
      "env": {
        "OPENAI_API_KEY": "YOUR_OPENAI_API_KEY_HERE"
      },
      "description": "AI agent regression testing — snapshot behavior, detect regressions in tool calls and output quality. 8 tools: create_test, run_snapshot, run_check, list_tests, validate_skill, generate_skill_tests, run_skill_test, generate_visual_report. API key optional — deterministic checks (tool diff, output hash) work without it. Install: pip install \"evalview>=0.5,<1\""
    }
  },
  "_comments": {
    "usage": "Copy the servers you need to your ~/.claude.json mcpServers section",
    "env_vars": "Replace YOUR_*_HERE placeholders with actual values",
    "disabling": "Use ECC_DISABLED_MCPS=github,context7,... to disable bundled ECC MCPs during install/sync, or use disabledMcpServers in project config for per-project overrides",
    "context_warning": "Keep under 10 MCPs enabled to preserve context window"
  }
}

```

---

## 3. GSD-Core Companion MCP Server (`open-gsd/gsd-core`)

### 3.1 Overview & Architecture
The GSD companion MCP server (`gsd-mcp-server`) runs as a stdio JSON-RPC 2.0 process that exposes GSD spec-driven lifecycle tools directly to host IDEs and agents.

**Exposed Tools:**
1. `gsd_invoke_command`: Allows programmatic execution of any GSD command (`/gsd-plan-phase`, `/gsd-execute-phase`, `/gsd-verify-work`, etc.).
2. `gsd_read_state`: Reads project milestone state, active wave, completed plans, and `.planning/` artifacts.
3. `gsd_write_state`: Updates milestone phase status, records verification checkpoints, and manages `.planning/` state atomically.

**Setup Configuration (`mcp_config.json`):**
```json
{
  "mcpServers": {
    "gsd": {
      "command": "npx",
      "args": ["-y", "@opengsd/gsd-core", "mcp"]
    }
  }
}
```

---

## 4. Mobbin Design Reference MCP Server (`mobbin/skills`)

### 4.1 Overview & Configuration
Connects directly to the Mobbin Design Reference API to query real-world iOS, Android, and Web design patterns, flows, and production screens from top apps.

**Configuration (`mcp_config.json`):**
```json
{
  "mcpServers": {
    "mobbin": {
      "serverUrl": "https://api.mobbin.com/mcp"
    }
  }
}
```

**Exposed Tools & Capabilities:**
- `search_screens`: Search millions of production iOS/Android screens by UI component, pattern, or tag.
- `search_flows`: Search complete user flows (onboarding, checkout, auth, settings, paywalls).
- `search_sections`: Find atomic sections and micro-interactions across production mobile apps.
