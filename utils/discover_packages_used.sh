#!/bin/bash
echo "Package used in this project"
grep -rn "import" ../src/main/java | cut -d ":" -f 3 | grep -v "br.ufba.dcc" | grep -v "java." | cut -d " " -f 2 | sed 's/[A-Z].*//' | sed 's/\.*$/,/' | sort | uniq
