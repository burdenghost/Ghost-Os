#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
RUNTIME="$ROOT/third_party/pr"

git -C "$ROOT" config --global url."https://github.com/".insteadOf "git@github.com:"
git -C "$ROOT" submodule update --init --recursive

EXPECTED="7029b1fe8fa84d64339cabe38f9c5be2ea2ae7ac"
ACTUAL="$(git -C "$RUNTIME" rev-parse HEAD)"
if [[ "$ACTUAL" != "$EXPECTED" ]]; then
  echo "Runtime source mismatch: expected $EXPECTED, got $ACTUAL" >&2
  exit 1
fi

if [[ "$(uname -s)" != "Linux" || "$(uname -m)" != "x86_64" ]]; then
  echo "The upstream native build script currently requires a Linux x86_64 build host." >&2
  echo "Use the GitHub Actions Android build workflow or a Linux x86_64 workstation." >&2
  exit 2
fi

command -v rustc >/dev/null || { echo 'Rust is required.' >&2; exit 1; }
rustup target add aarch64-linux-android

cd "$RUNTIME"
scripts/build.sh --arch=arm64
scripts/build-pr-cli.sh

JNI="$RUNTIME/android/proot-engine/src/main/jniLibs/arm64-v8a"
install -Dm755 build/out/arm64/proot "$JNI/libproot.so"
install -Dm755 build/out/arm64/loader "$JNI/libproot-loader.so"

echo "Runtime binaries built and staged in $JNI"
