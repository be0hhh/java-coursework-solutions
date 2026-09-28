#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "${BASH_SOURCE[0]}")"
if [[ $# -eq 0 ]]; then
    echo 'Использование: bash run.sh T02 [аргументы задания]'
    echo 'Доступные задания:'
    for source in T[0-9][0-9].java; do
        [[ -f "$source" ]] && basename "$source" .java
    done
    exit 0
fi
task="${1%.java}"
shift
if [[ ! "$task" =~ ^T[0-9]{2}$ || ! -f "$task.java" ]]; then
    echo "Нет задания: $task" >&2
    exit 1
fi
if ! command -v javac >/dev/null || ! command -v java >/dev/null; then
    echo 'Нужен установленный JDK: команды javac и java должны быть доступны в PATH.' >&2
    exit 1
fi
mkdir -p "build/$task"
if [[ "$(javac -version 2>&1)" == javac\ 1.8* ]]; then
    javac -encoding UTF-8 -source 8 -target 8 -cp 'lib/*' -d "build/$task" "$task.java"
else
    javac -encoding UTF-8 --release 8 -cp 'lib/*' -d "build/$task" "$task.java"
fi
exec java -cp "build/$task:lib/*" "$task" "$@"
