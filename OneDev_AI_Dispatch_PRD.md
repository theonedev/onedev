# OneDev AI Dispatch — PRD
**Version:** 2.1 | **Status:** Draft | **Date:** March 2026  
**Author:** Beacon Co / Ian | **Replaces:** AI Dispatch PRD v2.0

---

## 1. Executive Summary

OneDev AI Dispatch adds `@claude` and `@copilot` mention-based dispatch from PR comments, backed by a persistent interactive session UI inside the PR page. The developer posts a comment, a session starts, and a live terminal-style panel opens on the PR where they can watch the agent work and steer it mid-run — exactly like GitHub Copilot's session model.

The implementation is deliberately thin. OneDev already has everything needed:

- **The repo** is already on disk as a bare repo
- **The branch** is already known from the PR
- **The WebSocket + terminal UI** already exists for CI/CD job terminals
- **The comment system** already fires events on new comments

This project wires those existing primitives together with `git worktree`, `claude`, and a chat input bar.

---

## 2. Core Concept

```
PR Comment:  @claude the null check in UserService is wrong, fix it and add a test

                        ↓ comment event fires

OneDev:      creates git worktree from PR branch
             starts: claude -p "{prompt}" (interactive mode)
             opens WebSocket to AI Run tab

AI Run Tab:  ┌─────────────────────────────────────────┐
             │ [claude] Reading UserService.java...     │
             │ [claude] Found issue on line 47          │
             │ [claude] Writing fix...                  │
             │ [claude] Running tests...                │
             │ [claude] Tests passed. Committing.       │
             │                                         │
             │ > _                    [End Session]     │
             └─────────────────────────────────────────┘

User types:  > also check the integration test for this method

             [claude] Looking at integration tests...
             [claude] Found UserServiceIT.java, updating...
```

The session stays alive. The user can guide, correct, or expand scope. When done, they click **End Session** or Claude Code exits naturally. The worktree is cleaned up, commits are already on the branch.

---

## 3. What Gets Built

### 3.1 Comment Parser
Detects `@mention` at the start of a PR comment line.

```
@claude <prompt>              → dispatch to Claude Code
@claude --think <prompt>      → Claude Code with extended thinking
@claude --no-commit <prompt>  → read-only, output posted as comment reply
@copilot <prompt>             → dispatch to Copilot CLI
```

Rules:
- Line-leading only — mid-sentence `@claude` does nothing
- Comments from the AI system user excluded (no loops)
- Case-insensitive

### 3.2 Worktree Manager
Thin wrapper around `git worktree`. No clone needed — the repo is already on disk.

```bash
# On session start
git -C /opt/onedev/repos/{project}.git \
    worktree add /tmp/ai-run-{runId} {pr-branch}

# On session end
git -C /opt/onedev/repos/{project}.git \
    worktree remove /tmp/ai-run-{runId} --force
```

That's the entire "repo setup" step.

### 3.3 Session Runner
Starts the CLI tool as a **long-lived process** with stdin kept open.

```bash
# Working dir: /tmp/ai-run-{runId}
claude --dangerously-skip-permissions \
       --output-format stream-json \
       -p "{initial_prompt}"
```

- `stdout/stderr` → piped to WebSocket broadcaster
- `stdin` → accepts follow-up messages from the session UI input bar
- Process stays alive until natural exit or user clicks End Session (SIGTERM → SIGKILL after 5s)

### 3.4 WebSocket Pipe
Reuses OneDev's existing CI/CD job terminal WebSocket infrastructure. Endpoint: `/ws/ai-run/{runId}`.

Message types:
```json
{ "type": "output",  "data": "[claude] Writing UserService.java..." }
{ "type": "commit",  "data": "abc1234" }
{ "type": "status",  "data": "completed" }
{ "type": "input",   "data": "also check the integration test" }
```

### 3.5 AI Run Tab (Session UI)
New tab on the PR page: **AI Run**, alongside Files Changed / Commits / Comments.

```
┌── AI Run ──────────────────────────────────────────────────────┐
│                                                                 │
│  Session #3 · @claude · started 2m ago · 1 commit              │
│  triggered by: "fix the null check in UserService"            │
│                                                         [End]  │
│ ┌─────────────────────────────────────────────────────────────┐│
│ │ [claude] Reading UserService.java...                        ││
│ │ [claude] Found null dereference on line 47                  ││
│ │ [claude] Writing UserService.java                           ││
│ │ [claude] Writing UserServiceTest.java                       ││
│ │ [claude] Running: mvn test -Dtest=UserServiceTest           ││
│ │ [claude] Tests passed (4/4)                                 ││
│ │ [claude] Committing: fix: null check in UserService         ││
│ │                                                             ││
│ │ > also check the integration test for this method           ││
│ │                                                             ││
│ │ [claude] Reading UserServiceIT.java...                      ││
│ │ [claude] Integration test missing coverage for null case    ││
│ │ [claude] Writing UserServiceIT.java                         ││
│ └─────────────────────────────────────────────────────────────┘│
│  ┌──────────────────────────────────────────────┐ [Send]       │
│  │ > _                                          │              │
│  └──────────────────────────────────────────────┘              │
└────────────────────────────────────────────────────────────────┘
```

Components:
- **Output pane** — scrollable, auto-scroll with "Jump to latest" on manual scroll-up
- **Input bar** — sends text to process stdin over WebSocket
- **End Session** button — SIGTERM flow, worktree cleanup
- **Session header** — agent, status, duration, trigger comment link, commit count

Past sessions collapse into history cards below the active session.

---

## 4. Data Model

```sql
ai_dispatch_run
  id               BIGINT PK
  pull_request_id  BIGINT FK
  comment_id       BIGINT FK
  triggered_by     BIGINT FK → onedev_user
  agent            VARCHAR(32)    -- 'claude' | 'copilot'
  flags            VARCHAR(256)   -- '--think', '--no-commit'
  prompt           TEXT
  state            VARCHAR(32)    -- Queued|Running|Completed|Failed|Cancelled
  worktree_path    VARCHAR(512)
  log              TEXT
  commit_shas      VARCHAR(1024)
  created_at       TIMESTAMP
  started_at       TIMESTAMP
  completed_at     TIMESTAMP
  exit_code        INT
```

---

## 5. Component Map

| Component | ~Size | Reuses |
|---|---|---|
| `CommentDispatchListener` | 40 lines | OneDev comment event system |
| `CommentParser` | 50 lines | — |
| `WorktreeManager` | 30 lines | OneDev repo path resolution |
| `SessionRunner` | 80 lines | — |
| `RunStreamBroadcaster` | 60 lines | — |
| `AiRunWebSocketResource` | 80 lines | OneDev WS infra |
| `AiRunTab` (Wicket) | 150 lines | OneDev CI terminal panel |
| `CommentStatusBadge` (Wicket) | 40 lines | OneDev Ajax components |
| **Total** | **~530 lines** | |

---

## 6. Implementation Phases

### Phase 1 — Fire and Forget (MVP)
Get `@claude` working end-to-end with streaming output. Session input deferred.

- [ ] Comment parser + dispatch listener
- [ ] WorktreeManager (create/destroy)
- [ ] SessionRunner — process start, stdout pipe, commit detection, worktree cleanup
- [ ] WebSocket broadcaster wired to existing OneDev WS infra
- [ ] AiRunTab — output pane + status header (no input bar yet)
- [ ] CommentStatusBadge on triggering comment
- [ ] AI Settings: API key, enable toggle, max concurrent sessions (default 3), timeout (default 30 min)

### Phase 2 — Interactive Session
- [ ] Input bar in AiRunTab → writes to process stdin via WebSocket
- [ ] End Session button → SIGTERM flow
- [ ] Session history cards for past runs on the same PR
- [ ] `@copilot` dispatch path
- [ ] `--think` flag passthrough
- [ ] `--no-commit` → output posted as comment reply

### Phase 3 — Hardening
- [ ] Worktree orphan cleanup on server restart
- [ ] Log replay on WebSocket reconnect
- [ ] Rate limiting per user (N sessions/hour)
- [ ] Audit log entries per session
- [ ] Dockerfile patch for OneDev container (add `claude` + `gh` to image)

---

## 7. Risks

| Risk | Mitigation |
|---|---|
| Prompt injection via comment body | Pass via `ProcessBuilder` array — never shell-interpolated |
| Claude Code makes unintended writes | Standard git commits, revertible via OneDev UI |
| Worktree left behind on crash | Startup cleanup scans `/tmp/ai-run-*` and removes orphans |
| OneDev Docker container missing CLI tools | Dockerfile patch provided; setup check in Admin > AI Dispatch |
| Large repo worktree creation slow | `git worktree add` on a local bare repo is near-instant regardless of repo size |

---

## 8. Non-Goals

- Running agents inside CI/CD agents rather than the server — future
- Ollama/local model backend for Claude Code — monitor upstream
- Mobile UI — desktop-first
- Auto-merge after AI fix — always requires human
- Support for Aider, Continue, etc. — architecture supports it, defer

---

## 9. Success Metrics

| Metric | Target |
|---|---|
| Trigger-to-first-output latency | < 10s |
| Session success rate (exit 0) | > 85% |
| AI commit revert rate | < 15% |
| PR cycle time delta | -30% |
| Context switches to external editor | Near zero |

---

## Appendix: Example Sessions

### Simple fix with mid-session steering
```
Comment:  @claude the findByEmail query is case sensitive, fix it

  [claude] Reading UserRepository.java
  [claude] Issue on line 83: raw string comparison
  [claude] Writing UserRepository.java
  [claude] Running tests... passed
  [claude] Committing: fix: case-insensitive email lookup

User:  > also add an index on the email column while you're in there

  [claude] Reading schema migration files...
  [claude] Writing V012__add_email_index.sql
  [claude] Committing: feat: add index on users.email
```

### Review only (--no-commit)
```
Comment:  @claude --no-commit review this PR for SQL injection risks

  → streams analysis to AI Run tab
  → on exit, posts reply to triggering comment:

  "Found 2 potential issues:
   1. UserController.java:34 — unsanitized input in query
   2. ReportService.java:91 — string concatenation in WHERE clause
   [Full log → AI Run #7]"
```

### Extended thinking
```
Comment:  @claude --think the payment module is tightly coupled to Stripe,
          refactor it behind an interface

  [claude] <thinking>
  [claude]   Analyzing 6 files with direct Stripe SDK calls...
  [claude]   Designing PaymentProvider interface...
  [claude] </thinking>
  [claude] Writing PaymentProvider.java
  [claude] Writing StripePaymentProvider.java
  [claude] Updating PaymentService.java
  [claude] ... (4 more files)
  [claude] Committing: refactor: extract PaymentProvider interface
```
