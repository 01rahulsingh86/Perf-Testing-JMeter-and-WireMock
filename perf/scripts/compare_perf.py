import json, sys, os

if len(sys.argv) < 2:
    print("usage: compare_perf.py <current.json>", file=sys.stderr)
    sys.exit(2)

cur = json.load(open(sys.argv[1]))

if not os.path.exists("baseline.json"):
    print("No baseline.json found. Skipping comparison (first run?).")
    sys.exit(0)

base = json.load(open("baseline.json"))
fails = []

def reg(key):
    if key not in base or key not in cur:
        return False
    med = float(base[key])
    val = float(cur[key])
    return val > med * 1.05 and (val - med) > 10.0

for k in cur.keys():
    if reg(k):
        fails.append(f"{k}: baseline {base[k]} -> current {cur[k]}")

if fails:
    print("REGRESSIONS:")
    for f in fails:
        print("- " + f)
    sys.exit(1)
else:
    print("OK: within guardband (≤5% or ≤10ms)")