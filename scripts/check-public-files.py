#!/usr/bin/env python3
"""Check Git content without printing potentially sensitive values."""

import argparse
import re
import subprocess
import sys
from pathlib import PurePosixPath


PATTERNS = {
    "personal absolute path": re.compile(
        rb"/(?:Users|home)/[A-Za-z0-9_-]+(?:/|\b)|/var/folders/[A-Za-z0-9/]+|[A-Za-z]:\\Users\\[A-Za-z0-9_-]+"
    ),
    "private key": re.compile(rb"-----BEGIN (?:RSA |EC |DSA |OPENSSH |ENCRYPTED )?PRIVATE KEY-----"),
    "GitHub token": re.compile(rb"\b(?:gh[pousr]_[A-Za-z0-9]{30,}|github_pat_[A-Za-z0-9_]{40,})\b"),
    "AWS access key": re.compile(rb"\b(?:AKIA|ASIA)[A-Z0-9]{16}\b"),
    "Google API key": re.compile(rb"\bAIza[0-9A-Za-z_-]{35}\b"),
}


def forbidden(name):
    path = PurePosixPath(name)
    parts = path.parts
    return (
        any(part in {"private", "signing", "REMOVE", "artifacts", ".gradle", ".idea", "build", "Knight"} for part in parts)
        or path.name in {".DS_Store", "local.properties", "keystore.properties", "google-services.json"}
        or (path.name.startswith(".env") and path.name not in {".env.example", ".env.sample"})
        or path.suffix.lower() in {".jks", ".keystore", ".pem", ".p12", ".pfx", ".key", ".apk", ".aab", ".apks", ".hprof", ".log"}
        or "SIGNING_BACKUP" in path.name.upper()
    )


def check(entries):
    failures = []
    for name, data in entries:
        if forbidden(name):
            failures.append(f"{name}: private/generated file must not be tracked")
        if b"\0" in data[:8192]:
            continue
        for line, value in enumerate(data.splitlines(), 1):
            for label, pattern in PATTERNS.items():
                if pattern.search(value):
                    failures.append(f"{name}:{line}: {label}")
    return failures


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--staged", action="store_true", help="Check index content for changed files only")
    args = parser.parse_args()
    command = ["git", "diff", "--cached", "--name-only", "--diff-filter=ACMR", "-z"] if args.staged else ["git", "ls-files", "-z"]
    names = subprocess.check_output(command).decode().rstrip("\0").split("\0")
    entries = []
    for name in filter(None, names):
        # Read the index, not working files; unstaged fixes cannot hide a staged leak.
        data = subprocess.check_output(["git", "show", ":" + name])
        entries.append((name, data))
    failures = check(entries)
    if failures:
        print("Public-file check failed (values redacted):", file=sys.stderr)
        print("\n".join(failures), file=sys.stderr)
        return 1
    print(f"Public-file check passed: {len(entries)} files.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
