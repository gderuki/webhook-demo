# AGENTS.md

Enterprise-grade direction. Humble beginnings. One step at a time.

- Build a distributed app in a clear, incremental way.
- Keep it simple and understandable until it truly needs more.
- No fancy infrastructure unless the prompt explicitly asks for it.
- Keep changes transparent and easy to verify.

## Rule for future assistants

- After each change, print a short release-note style update.
- Keep it brief: what changed, how to reproduce, and what to expect.
- No long explanations unless asked.

## Repro format

Repro:
- docker compose up --build -d
- <trigger action>
- expect: <short result>

Keep it tight. No extra narrative.
