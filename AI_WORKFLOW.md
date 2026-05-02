# AI WORKFLOW - SINGLE SOURCE OF TRUTH

This document defines the strict operational workflow for any AI assistant (Antigravity, Codex, Cursor, Copilot) operating within the SeShop codebase. 

**CRITICAL RULE**: Do NOT hallucinate architectures, requirements, or progress. Always derive truth from the documents below and the active codebase.

## 1. Initialization Phase
Upon starting a new session or receiving a prompt to "continue work", the AI MUST perform the following steps:
1. **Read Progress**: Read `IMPLEMENTATION_PLAN.md` to identify the current Phase, active tasks, and recent completions.
2. **Audit Codebase**: Reread the codebase related to the active tasks to verify the actual state of implementation. Use GitNexus tools (see section 4) if available.
3. **Consult Documentation**: Read the specific documents in `docs/` relevant to the active task (e.g., `docs/3.Design/SESHOP SAD.md` for architecture, `docs/3.Design/SESHOP API Spec.md` for endpoints). **Do not rely on memory.**

## 2. Execution Rules
When generating code or making changes:
- **No Redundancy**: Do not duplicate documentation contents. Use Markdown references and links to point to the `docs/` folder.
- **No Contradictions**: Ensure code aligns perfectly with `SESHOP SAD.md`, `SESHOP ADD.md`, and the `API Spec`.
- **Single Source of Truth**: The `docs/` directory is the immutable truth for design. `IMPLEMENTATION_PLAN.md` is the immutable truth for project progress.

## 3. Continuation & Progress Tracking
Every time the AI finishes a chunk of work:
1. **Update `IMPLEMENTATION_PLAN.md`**: Mark completed tasks as `[x]` and add new technical sub-tasks if discovered.
2. **Auto-Continue**: Always end your response with a clear summary of what was just done, and state the exact next task from `IMPLEMENTATION_PLAN.md`. Ask the user "Should I proceed with [Next Task]?" to create a self-sustaining loop.

---

## 4. GitNexus — Code Intelligence

This project is indexed by GitNexus as **SeShop**. Use the GitNexus MCP tools to understand code, assess impact, and navigate safely.
> If any GitNexus tool warns the index is stale, run `npx gitnexus analyze` in terminal first.

### Always Do
- **MUST run impact analysis before editing any symbol.** Before modifying a function, class, or method, run `gitnexus_impact({target: "symbolName", direction: "upstream"})` and report the blast radius (direct callers, affected processes, risk level) to the user.
- **MUST run `gitnexus_detect_changes()` before committing** to verify your changes only affect expected symbols and execution flows.
- **MUST warn the user** if impact analysis returns HIGH or CRITICAL risk before proceeding with edits.
- When exploring unfamiliar code, use `gitnexus_query({query: "concept"})` to find execution flows instead of grepping.
- When you need full context on a specific symbol, use `gitnexus_context({name: "symbolName"})`.

### Never Do
- NEVER edit a function, class, or method without first running `gitnexus_impact` on it.
- NEVER ignore HIGH or CRITICAL risk warnings from impact analysis.
- NEVER rename symbols with find-and-replace — use `gitnexus_rename` which understands the call graph.
- NEVER commit changes without running `gitnexus_detect_changes()` to check affected scope.
