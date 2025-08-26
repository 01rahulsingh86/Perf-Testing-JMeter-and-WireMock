import json, sys, os

if len(sys.argv) < 2:
    print("usage: extract_stats.py <statistics.json>", file=sys.stderr)
    sys.exit(2)

path = sys.argv[1]
if not os.path.exists(path):
    print("statistics.json not found", file=sys.stderr)
    sys.exit(2)

stats = json.load(open(path))

# JMeter dashboard statistics.json: often has "content" with entries
def find_p95_for(name):
    # try exact match first
    for k, v in stats.get("content", {}).items():
        if v.get("name") == name:
            # JMeter dashboard labels p95 as 'pct3' (p90=pct2, p99=pct4)
            return float(v.get("percentiles", {}).get("pct3", 0.0))
    # try fuzzy for POST /orders
    for k, v in stats.get("content", {}).items():
        nm = v.get("name","")
        if "POST /orders" in nm:
            return float(v.get("percentiles", {}).get("pct3", 0.0))
    return 0.0

menu_p95 = 0.0
# Try resolve menu either with promo or without
menu_candidates = ["GET /menu?loc=sea", "GET /menu?loc=sea&promo=true"]
for cand in menu_candidates:
    p = find_p95_for(cand)
    if p > 0: menu_p95 = p; break

orders_p95 = find_p95_for("POST /orders")

out = {"menu_p95_ms": round(menu_p95,2), "orders_p95_ms": round(orders_p95,2)}
print(json.dumps(out))