# Public repository privacy

The privacy cleanup on 31 October 2026 covers the published `main`, `KMP` and
`android-apk-handoff` branches, the local `codex/layout-refresh` branch, and tags
`v0` and `v1`.

## Findings and changes

- Historical build outputs, Gradle caches and machine metadata contained local
  file paths. Remove these generated files from history rather than publishing
  redacted build reports.
- Android handoff documents contained personal absolute paths. Use paths relative
  to the repository, or generic environment variables, in public documentation.
- An older signing keystore and its usable password were committed together.
  Remove the entire historical signing directory. Its certificate differs from
  the current Play upload certificate; the current key was not found in tracked
  history. Removing history cannot revoke an exposed key: retire the old key if
  it is still accepted by any service or release process.
- Older documents and commit metadata contained personal email addresses.
  Redact private document addresses and use the account's GitHub noreply address
  for commit metadata. Preserve licensing, attribution and intentional public
  product links.
- Ignore signing files, private directories, local configuration, environment
  files, generated app packages, memory dumps, logs and design reference folders.
- Disable public Gradle Build Scan publishing. Previously published scans can
  contain machine/environment details and require separate removal through the
  hosting service; deleting a repository link does not delete a scan.

## Preventing another exposure

Run `python3 scripts/check-public-files.py` to inspect all indexed files. The
checker reports filenames, line numbers and finding categories without printing
matched values. It rejects private/generated filenames and common credential or
personal-path patterns. It is a guardrail, not a guarantee that arbitrary secrets
can be recognized automatically.

Enable the local commit check with:

```sh
git config core.hooksPath .githooks
```

The hook checks staged bytes, so an unstaged correction cannot hide a staged
secret. The same check runs in the public repository privacy GitHub workflow.
An ignore rule does not untrack an already committed file or remove old history.

## Published history

History cleanup changes commit IDs. Keep the recovery bundle private. After
publication, collaborators should make a fresh clone and reapply only reviewed
changes; merging an old clone can restore removed history.

Old clones, forks, downloaded artifacts, cached commit pages, and GitHub pull
request references may retain removed content. GitHub Support may need to purge
cached sensitive objects after rewritten branches and tags are published. Do not
claim that a force push erases all copies or makes an exposed credential safe.

A local path reveals a machine username and directory structure; publishing the
text alone does not grant remote access to that filesystem.
