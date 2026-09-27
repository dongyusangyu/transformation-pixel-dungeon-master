# Private Files

Local-only material belongs in `.private/`, never in Android or desktop assets:

- `server-admin/`: administrator source and server-only service definitions.
- `player-data/`: real player exports, databases, and snapshots.
- `operations/`: private deployment and analysis scripts and reports.
- `documents/`: internal designs and maintenance notes.

The entire directory is ignored, including its documentation. It is not a
Gradle source or asset directory and is not distributed in repository archives.
Deployed administrator code stays on the server; this local directory is only
for private maintenance. Public backend code and synthetic tests remain in `server/`.

Install the checks in each new clone:

```powershell
./scripts/install_privacy_hooks.ps1
```

Pre-commit checks the staged files, including force-added files and common
credential patterns. Pre-push rejects known private paths in history. GitHub
Actions runs the same checks. Ignoring a previously tracked file does not remove
it from existing history; history cleanup is a separate operation.

These are accidental-disclosure safeguards, not access controls: hooks can be
bypassed and Actions cannot undo a push. Do not use `git add -f`, `--no-verify`,
disable the hooks, or rename private files into public directories. Require the
privacy check in branch protection for stronger enforcement. New private file
types should be added to both `.gitignore` and `scripts/privacy_guard.py`.

Rotate previously published passwords or tokens even after history cleanup.
Other clones, forks, and GitHub cached commit views require separate cleanup.
