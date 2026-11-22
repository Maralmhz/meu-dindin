import { get, put } from "@vercel/blob";
import { randomBytes, createHash } from "node:crypto";

const CLIENT_ID = "272708465865-f6shmohodvh6c27mgp34hh3apvrl4tae.apps.googleusercontent.com";
const ALLOWED_EMAIL = "maralmhz@gmail.com";
const ALLOWED_ORIGINS = new Set([
  "https://meu-dindin.pages.dev",
  "https://meu-dindin-lake.vercel.app",
  "https://meu-dindin.vercel.app"
]);

function send(res, status, body, origin) {
  res.statusCode = status;
  res.setHeader("Content-Type", "application/json; charset=utf-8");
  res.setHeader("Cache-Control", "no-store");
  res.setHeader("Vary", "Origin");
  if (origin && ALLOWED_ORIGINS.has(origin)) {
    res.setHeader("Access-Control-Allow-Origin", origin);
    res.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
    res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
  }
  res.end(JSON.stringify(body));
}

async function verifyGoogleToken(req) {
  const auth = req.headers.authorization || "";
  const match = auth.match(/^Bearer\s+(.+)$/i);
  if (!match) throw new Error("Faça login com sua conta Google.");
  const response = await fetch("https://oauth2.googleapis.com/tokeninfo?id_token=" + encodeURIComponent(match[1]));
  if (!response.ok) throw new Error("Sessão Google inválida. Entre novamente.");
  const claims = await response.json();
  if (claims.aud !== CLIENT_ID || claims.email_verified !== "true" ||
      String(claims.email || "").toLowerCase() !== ALLOWED_EMAIL ||
      !claims.sub || Number(claims.exp) * 1000 <= Date.now()) {
    throw new Error("Esta conta Google não está autorizada para acessar o Meu Dindin.");
  }
  return claims.sub;
}

export default async function handler(req, res) {
  const origin = req.headers.origin || "";
  if (req.method === "OPTIONS") return send(res, 204, {}, origin);
  if (origin && !ALLOWED_ORIGINS.has(origin)) return send(res, 403, { error: "Origem não autorizada." }, origin);
  if (!["GET", "POST"].includes(req.method)) return send(res, 405, { error: "Método não permitido." }, origin);

  try {
    const subject = await verifyGoogleToken(req);
    const pathname = "meu-dindin/users/" + subject + ".json";

    if (req.method === "POST" && req.body && req.body.action === "create-widget-token") {
      const token = randomBytes(32).toString("base64url");
      const tokenHash = createHash("sha256").update(token).digest("hex");
      await put("meu-dindin/widget-tokens/" + tokenHash + ".json", JSON.stringify({ subject, createdAt: new Date().toISOString() }), {
        access: "private", addRandomSuffix: false, allowOverwrite: true, contentType: "application/json", cacheControlMaxAge: 0
      });
      return send(res, 200, { ok: true, token }, origin);
    }

    if (req.method === "GET") {
      const file = await get(pathname, { access: "private", useCache: false });
      if (!file || file.statusCode !== 200) return send(res, 200, { exists: false, transactions: [] }, origin);
      const raw = await new Response(file.stream).text();
      const saved = JSON.parse(raw);
      return send(res, 200, { exists: true, transactions: Array.isArray(saved.transactions) ? saved.transactions : [], updatedAt: saved.updatedAt || null }, origin);
    }

    const body = req.body && typeof req.body === "object" ? req.body : {};
    if (body.action !== "save" || !Array.isArray(body.transactions)) {
      return send(res, 400, { error: "Dados inválidos." }, origin);
    }
    if (body.transactions.length > 10000) return send(res, 413, { error: "Limite de lançamentos excedido." }, origin);
    const payload = JSON.stringify({ transactions: body.transactions, updatedAt: new Date().toISOString() });
    await put(pathname, payload, {
      access: "private",
      addRandomSuffix: false,
      allowOverwrite: true,
      contentType: "application/json",
      cacheControlMaxAge: 0
    });
    return send(res, 200, { ok: true, updatedAt: new Date().toISOString() }, origin);
  } catch (error) {
    return send(res, 401, { error: error.message || "Não foi possível sincronizar." }, origin);
  }
}
