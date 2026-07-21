#!/usr/bin/env bash
#
# Launch Chrome with a remote debugging port on a dedicated profile so the test
# can ATTACH to it (chrome.debugger.address=127.0.0.1:<port>) and reuse your
# real, logged-in session instead of a fresh/incognito one.
#
# Usage:
#   ./scripts/launch-chrome-debug.sh [port]      # default port 9222
#
# In the window that opens, sign in to Facebook ONCE by hand (a human login is
# not flagged as a bot). Leave this Chrome running, then in another terminal:
#   mvn test -Dchrome.debugger.address=127.0.0.1:<port>
#
# A DEDICATED profile directory is used because Chrome 136+ will not expose the
# debugging port on your normal/default profile. Override the Chrome binary with
# CHROME_BIN, or the profile directory with CHROME_DEBUG_PROFILE.
set -euo pipefail

PORT="${1:-9222}"
PROFILE_DIR="${CHROME_DEBUG_PROFILE:-$HOME/.wallethub-selenium/debug-profile}"

if [[ -n "${CHROME_BIN:-}" ]]; then
  CHROME="$CHROME_BIN"
elif [[ "$(uname)" == "Darwin" ]]; then
  CHROME="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
else
  CHROME="$(command -v google-chrome || command -v google-chrome-stable || echo google-chrome)"
fi

if [[ "$(uname)" == "Darwin" && ! -x "$CHROME" ]]; then
  echo "Chrome not found at: $CHROME" >&2
  echo "Set CHROME_BIN to your Chrome executable path and retry." >&2
  exit 1
fi

mkdir -p "$PROFILE_DIR"
echo "Launching Chrome on debug port $PORT (profile: $PROFILE_DIR)"
echo "Sign in to Facebook in the window that opens, then run:"
echo "  mvn test -Dchrome.debugger.address=127.0.0.1:$PORT"
exec "$CHROME" \
  --remote-debugging-port="$PORT" \
  --remote-debugging-address=127.0.0.1 \
  --user-data-dir="$PROFILE_DIR" \
  --no-first-run \
  --no-default-browser-check
