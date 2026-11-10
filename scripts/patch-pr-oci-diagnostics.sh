#!/usr/bin/env bash
set -euo pipefail

# Run from the Ghost-Os repository root after initializing third_party/pr.
file="third_party/pr/src/pr-cli/src/oci.rs"

if [[ ! -f "$file" ]]; then
  echo "Missing $file. Initialize the third_party/pr submodule first." >&2
  exit 1
fi

python3 - "$file" <<'PY'
from pathlib import Path
import sys

path = Path(sys.argv[1])
source = path.read_text()
replacements = (
    ('format!("download blob {}: {}", url, e)',
     'format!("download blob {}: {:#}", url, e)'),
    ('format!("read blob stream {}: {}", url, e)',
     'format!("read blob stream {}: {:#}", url, e)'),
)

for old, new in replacements:
    if old in source:
        source = source.replace(old, new)
    elif new not in source:
        raise SystemExit(f"Expected diagnostic expression not found: {old}")

path.write_text(source)
print(f"Updated detailed OCI download diagnostics in {path}")
PY
