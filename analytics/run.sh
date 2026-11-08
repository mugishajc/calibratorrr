#!/usr/bin/env bash
# Run the Spark lab: create the venv on first use, generate data if missing, run the lessons.
# Extra arguments go to case_analytics.py, e.g.  ./run.sh --pause
set -euo pipefail
cd "$(dirname "$0")"

# Spark 4 runs on Java 17 or 21. Use Java 17 if it is installed, whatever the default is.
if JH=$(/usr/libexec/java_home -v 17 2>/dev/null); then export JAVA_HOME="$JH"; fi

if [ ! -x .venv/bin/python ]; then
  python3.12 -m venv .venv
  .venv/bin/pip install -r requirements.txt
fi

[ -f data/raw/cases.csv ] || .venv/bin/python generate_data.py

.venv/bin/python case_analytics.py "$@"
