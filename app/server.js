const express = require("express");
const axios = require("axios").default;
const bodyParser = require("body-parser");

const app = express();
app.use(bodyParser.json());

const PORT = process.env.PORT || 8080;
const PAYMENTS_URL = process.env.PAYMENTS_URL || "http://localhost:8089/payments";
const REWARDS_URL  = process.env.REWARDS_URL  || "http://localhost:8089/rewards/earn";

app.get("/menu", async (req, res) => {
  res.json([{ sku: "latte-grande", price: 5.25 }, { sku: "americano-tall", price: 3.10 }]);
});

app.post("/orders", async (req, res) => {
  const runId = req.header("x-run-id") || "local";
  const { sku = "latte-grande", qty = 1, channel = "web", coupon } = req.body || {};
  try {
    const pay = await axios.post(PAYMENTS_URL, { amount: 4.99, sku, qty, channel }, { headers: { "x-run-id": runId } });
    if (!pay.data || pay.data.status !== "APPROVED") {
      return res.status(402).json({ error: "payment_failed" });
    }
    await axios.post(REWARDS_URL, { points: 30, reason: "purchase" }, { headers: { "x-run-id": runId } });
    return res.status(201).json({ id: `${Date.now()}-${Math.random()}`, status: "CONFIRMED", coupon: coupon || null });
  } catch (e) {
    const code = e?.response?.status || 500;
    return res.status(code).json({ error: "downstream_error", detail: e.message });
  }
});

app.get("/health", (_, res) => res.send("ok"));

app.listen(PORT, () => console.log(`Service A listening on ${PORT}`));