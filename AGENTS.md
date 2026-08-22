# AGENTS.md

Enterprise-grade direction. Humble beginnings. One step at a time.

- Build a distributed app in a clear, incremental way.
- Keep it simple and understandable until it truly needs more.
- No fancy infrastructure unless the prompt explicitly asks for it.
- Keep changes transparent and easy to verify.

## Rule for future assistants

- After each change, print a short release-note style update.
- Keep README.md as the quick reference; avoid churn during implementation.
- Update README.md only when the user asks to commit/finalize or when behavior/features materially change.
- Do not verify build whatsoever.

## Repro format

Repro:
- docker compose up --build -d
- <trigger action>
- expect: <short result>

Keep it tight. No extra narrative.
