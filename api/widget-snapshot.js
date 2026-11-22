import { get } from "@vercel/blob";
import { createHash } from "node:crypto";

function send(res, status, body) {
  res.statusCode = status;
  res.setHeader("Content-Type", "application/json; charset=utf-8");
  res.setHeader("Cache-Control", "no-store, private");
  res.end(JSON.stringify(body));
}

export default async function handler(req, res) {
  if (req.method !== "GET") return send(res, 405, { error: "Método não permitido." });
  try {
    const auth = req.headers.authorization || "";
    const match = auth.match(/^Bearer\s+(.+)$/i);
    if (!match) return send(res, 401, { error: "Widget não vinculado. Abra o app Meu Dindin e conecte a conta." });
    const tokenHash = createHash("sha256").update(match[1]).digest("hex");
    const tokenFile = await get("meu-dindin/widget-tokens/" + tokenHash + ".json", { access: "private", useCache: false });
    if (!tokenFile || tokenFile.statusCode !== 200) return send(res, 401, { error: "Vínculo do widget inválido. Conecte novamente pelo Meu Dindin." });
    const tokenData = JSON.parse(await new Response(tokenFile.stream).text());
    const dataFile = await get("meu-dindin/users/" + tokenData.subject + ".json", { access: "private", useCache: false });
    if (!dataFile || dataFile.statusCode !== 200) return send(res, 200, { income: 0, expense: 0, updatedAt: null });
    const saved = JSON.parse(await new Response(dataFile.stream).text());
    const transactions = Array.isArray(saved.transactions) ? saved.transactions : [];
    let income = 0, expense = 0;
    for (const item of transactions) {
      const amount = Number(item.amount) || 0;
      if (String(item.category || "").toUpperCase() === "RECEITA") income += amount;
      else expense += amount;
    }
    return send(res, 200, { income, expense, updatedAt: saved.updatedAt || null });
  } catch (error) {
    return send(res, 500, { error: "Não foi possível atualizar o widget." });
  }
}
