q() { curl -s -X POST https://api.ftcscout.org/graphql -H 'content-type: application/json' -d "$(python3 -c 'import json,sys;print(json.dumps({"query":sys.argv[1]}))' "$1")"; }
