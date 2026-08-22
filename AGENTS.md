# AGENTS.md

Enterprise-grade direction. Humble beginnings. One step at a time.

- Build a distributed app in a clear, incremental way.
- Keep it simple and understandable until it truly needs more.
- No fancy infrastructure unless the prompt explicitly asks for it.
- Keep changes transparent and easy to verify.

## Rule for future assistants

- After each change, print a short release-note style update.
- Keep README.md as the quick reference; avoid churn during implementation.
- Do not update README.md while the user is still iterating on the implementation unless behavior/features materially change.
- When the user explicitly asks to commit or finalize the work, update README.md before the commit so it reflects the current state of the project.
- If the user asks to commit after manual testing, do the README update at that point and then commit everything together.
- Never add a Co-authored-by trailer or any GitHub co-author metadata to commits.
- Do not verify build whatsoever.

## Repro format

Repro:
- docker compose up --build -d
- <trigger action>
- expect: <short result>

Keep it tight. No extra narrative.
