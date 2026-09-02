import { n as TSS_SERVER_FUNCTION, t as createServerFn } from "./ssr.mjs";
//#region node_modules/.nitro/vite/services/ssr/assets/ai-De5e3eC9.js
var createServerRpc = (serverFnMeta, splitImportFn) => {
	const url = "/_serverFn/" + serverFnMeta.id;
	return Object.assign(splitImportFn, {
		url,
		serverFnMeta,
		[TSS_SERVER_FUNCTION]: true
	});
};
async function complete(opts) {
	const apiKey = process.env.XAI_API_KEY;
	if (!apiKey) return {
		ok: false,
		error: "AI is not available in this environment."
	};
	const messages = [];
	if (opts.system?.trim()) messages.push({
		role: "system",
		content: opts.system.trim()
	});
	messages.push({
		role: "user",
		content: opts.user
	});
	const res = await fetch("https://api.x.ai/v1/chat/completions", {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
			Authorization: `Bearer ${apiKey}`
		},
		body: JSON.stringify({
			model: "grok-4.5",
			messages,
			temperature: opts.temperature ?? .4,
			max_tokens: opts.maxTokens
		})
	});
	if (!res.ok) return {
		ok: false,
		error: `Model request failed (${res.status}).`
	};
	return {
		ok: true,
		text: (await res.json()).choices?.[0]?.message?.content?.trim() ?? ""
	};
}
var runPrompt_createServerFn_handler = createServerRpc({
	id: "1049bad283d038fd5dcfd5c0770c49d53be8936991122557ded91c7c7ede0ea1",
	name: "runPrompt",
	filename: "src/lib/ai.ts"
}, (opts) => runPrompt.__executeServer(opts));
var runPrompt = createServerFn({ method: "POST" }).validator((input) => input).handler(runPrompt_createServerFn_handler, async ({ data }) => {
	const user = data.user.trim();
	if (!user) return {
		ok: false,
		error: "Write a prompt first."
	};
	if (user.length > 8e3) return {
		ok: false,
		error: "Prompt is too long."
	};
	return complete({
		system: data.system.slice(0, 4e3),
		user,
		temperature: Math.min(1.2, Math.max(0, data.temperature)),
		maxTokens: 700
	});
});
var improvePrompt_createServerFn_handler = createServerRpc({
	id: "27e95d3e09f8192da2d32713d7f8ce87bf53d53933fb4757a1eda86c3e2194f3",
	name: "improvePrompt",
	filename: "src/lib/ai.ts"
}, (opts) => improvePrompt.__executeServer(opts));
var improvePrompt = createServerFn({ method: "POST" }).validator((input) => input).handler(improvePrompt_createServerFn_handler, async ({ data }) => {
	const prompt = data.prompt.trim();
	if (!prompt) return {
		ok: false,
		error: "Nothing to improve."
	};
	return complete({
		system: "You rewrite prompts. Keep the author's intent. Make the instruction unambiguous, add an output contract, remove fluff. Return ONLY the improved prompt.",
		user: prompt.slice(0, 6e3),
		temperature: .3,
		maxTokens: 500
	});
});
var runEval_createServerFn_handler = createServerRpc({
	id: "09e676400a8352789b4a992a23c8be3bc225fd7138253dd211713e807e081f72",
	name: "runEval",
	filename: "src/lib/ai.ts"
}, (opts) => runEval.__executeServer(opts));
var runEval = createServerFn({ method: "POST" }).validator((input) => input).handler(runEval_createServerFn_handler, async ({ data }) => {
	const cases = data.cases.map((c) => ({
		input: c.input.trim(),
		expected: c.expected.trim()
	})).filter((c) => c.input).slice(0, 4);
	if (!data.promptA.trim() || !data.promptB.trim()) return {
		ok: false,
		error: "Both prompt variants are required."
	};
	if (cases.length === 0) return {
		ok: false,
		error: "Add at least one test case."
	};
	const fill = (tpl, input) => tpl.replaceAll("{{input}}", input);
	const rows = [];
	for (const c of cases) {
		const [a, b] = await Promise.all([complete({
			user: fill(data.promptA, c.input).slice(0, 6e3),
			temperature: .3,
			maxTokens: 280
		}), complete({
			user: fill(data.promptB, c.input).slice(0, 6e3),
			temperature: .3,
			maxTokens: 280
		})]);
		rows.push({
			input: c.input,
			expected: c.expected,
			a: a.ok ? a.text : "",
			b: b.ok ? b.text : "",
			aError: a.ok ? void 0 : a.error,
			bError: b.ok ? void 0 : b.error
		});
	}
	const judged = await complete({
		system: "Score two prompt variants. For each case give A and B a 0-5 integer and one short reason. Then pick a winner. Format:\nCase N: A=x B=y — reason\nWinner: A|B|tie — reason",
		user: rows.map((r, i) => `CASE ${i + 1}\nTask: ${r.input}\nCriteria: ${r.expected || "clarity, completeness, constraint following"}\nA:\n${r.a || r.aError}\nB:\n${r.b || r.bError}`).join("\n\n---\n\n").slice(0, 1e4),
		temperature: .2,
		maxTokens: 420
	});
	return {
		ok: true,
		rows,
		verdict: judged.ok ? judged.text : "Judge unavailable: " + judged.error
	};
});
//#endregion
export { improvePrompt_createServerFn_handler, runEval_createServerFn_handler, runPrompt_createServerFn_handler };
