import { createServerFn } from "@tanstack/react-start";

type ChatOk = { ok: true; text: string };
type ChatErr = { ok: false; error: string };
export type ChatResult = ChatOk | ChatErr;

async function complete(opts: {
  system?: string;
  user: string;
  temperature?: number;
  maxTokens: number;
}): Promise<ChatResult> {
  const apiKey = process.env.XAI_API_KEY;
  if (!apiKey) {
    return { ok: false, error: "AI is not available in this environment." };
  }

  const messages: { role: "system" | "user"; content: string }[] = [];
  if (opts.system?.trim()) {
    messages.push({ role: "system", content: opts.system.trim() });
  }
  messages.push({ role: "user", content: opts.user });

  const res = await fetch("https://api.x.ai/v1/chat/completions", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${apiKey}`,
    },
    body: JSON.stringify({
      model: "grok-4.5",
      messages,
      temperature: opts.temperature ?? 0.4,
      max_tokens: opts.maxTokens,
    }),
  });

  if (!res.ok) {
    return { ok: false, error: `Model request failed (${res.status}).` };
  }

  const body = (await res.json()) as {
    choices?: { message?: { content?: string } }[];
  };
  return { ok: true, text: body.choices?.[0]?.message?.content?.trim() ?? "" };
}

export const runPrompt = createServerFn({ method: "POST" })
  .validator((input: { system: string; user: string; temperature: number }) => input)
  .handler(async ({ data }) => {
    const user = data.user.trim();
    if (!user) return { ok: false as const, error: "Write a prompt first." };
    if (user.length > 8000) return { ok: false as const, error: "Prompt is too long." };
    return complete({
      system: data.system.slice(0, 4000),
      user,
      temperature: Math.min(1.2, Math.max(0, data.temperature)),
      maxTokens: 700,
    });
  });

export const improvePrompt = createServerFn({ method: "POST" })
  .validator((input: { prompt: string }) => input)
  .handler(async ({ data }) => {
    const prompt = data.prompt.trim();
    if (!prompt) return { ok: false as const, error: "Nothing to improve." };
    return complete({
      system:
        "You rewrite prompts. Keep the author's intent. Make the instruction unambiguous, add an output contract, remove fluff. Return ONLY the improved prompt.",
      user: prompt.slice(0, 6000),
      temperature: 0.3,
      maxTokens: 500,
    });
  });

export type EvalRow = {
  input: string;
  expected: string;
  a: string;
  b: string;
  aError?: string;
  bError?: string;
};

export const runEval = createServerFn({ method: "POST" })
  .validator(
    (input: {
      promptA: string;
      promptB: string;
      cases: { input: string; expected: string }[];
    }) => input,
  )
  .handler(async ({ data }) => {
    const cases = data.cases
      .map((c) => ({
        input: c.input.trim(),
        expected: c.expected.trim(),
      }))
      .filter((c) => c.input)
      .slice(0, 4);

    if (!data.promptA.trim() || !data.promptB.trim()) {
      return { ok: false as const, error: "Both prompt variants are required." };
    }
    if (cases.length === 0) {
      return { ok: false as const, error: "Add at least one test case." };
    }

    const fill = (tpl: string, input: string) =>
      tpl.replaceAll("{{input}}", input);

    const rows: EvalRow[] = [];
    for (const c of cases) {
      const [a, b] = await Promise.all([
        complete({
          user: fill(data.promptA, c.input).slice(0, 6000),
          temperature: 0.3,
          maxTokens: 280,
        }),
        complete({
          user: fill(data.promptB, c.input).slice(0, 6000),
          temperature: 0.3,
          maxTokens: 280,
        }),
      ]);
      rows.push({
        input: c.input,
        expected: c.expected,
        a: a.ok ? a.text : "",
        b: b.ok ? b.text : "",
        aError: a.ok ? undefined : a.error,
        bError: b.ok ? undefined : b.error,
      });
    }

    const judgeInput = rows
      .map(
        (r, i) =>
          `CASE ${i + 1}\nTask: ${r.input}\nCriteria: ${r.expected || "clarity, completeness, constraint following"}\nA:\n${r.a || r.aError}\nB:\n${r.b || r.bError}`,
      )
      .join("\n\n---\n\n");

    const judged = await complete({
      system:
        "Score two prompt variants. For each case give A and B a 0-5 integer and one short reason. Then pick a winner. Format:\nCase N: A=x B=y — reason\nWinner: A|B|tie — reason",
      user: judgeInput.slice(0, 10000),
      temperature: 0.2,
      maxTokens: 420,
    });

    return {
      ok: true as const,
      rows,
      verdict: judged.ok ? judged.text : "Judge unavailable: " + judged.error,
    };
  });
