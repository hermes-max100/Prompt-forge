import { i as __toESM } from "../_runtime.mjs";
import { n as require_react } from "../_libs/@radix-ui/react-compose-refs+[...].mjs";
import { S as require_jsx_runtime } from "../_libs/@tanstack/react-router+[...].mjs";
import { l as LoaderCircle, n as Trash2, s as Play } from "../_libs/lucide-react.mjs";
import { n as toast } from "../_libs/sonner.mjs";
import { n as Card, t as Button } from "./card-BfEqT_9M.mjs";
import { a as runPrompt, n as Textarea, t as Label } from "./ai-BJT6c9Jr.mjs";
import { r as useForge } from "./store-BbFtGqI7.mjs";
//#region node_modules/.nitro/vite/services/ssr/assets/playground-4gI6Ux4y.js
var import_react = /* @__PURE__ */ __toESM(require_react());
var import_jsx_runtime = require_jsx_runtime();
function Playground() {
	const { system, setSystem, temperature, setTemperature, assembled, fields, frameworkId, pushRun, runs, clearRuns } = useForge();
	const prompt = (0, import_react.useMemo)(() => assembled(), [
		frameworkId,
		fields,
		assembled
	]);
	const [user, setUser] = (0, import_react.useState)(prompt);
	const [output, setOutput] = (0, import_react.useState)("");
	const [busy, setBusy] = (0, import_react.useState)(false);
	async function onRun() {
		const body = user.trim() || prompt;
		if (!body) {
			toast.error("Write a prompt first");
			return;
		}
		setBusy(true);
		setOutput("");
		try {
			const res = await runPrompt({ data: {
				system,
				user: body,
				temperature
			} });
			if (!res.ok) {
				toast.error(res.error);
				return;
			}
			setOutput(res.text);
			pushRun(body, res.text);
		} finally {
			setBusy(false);
		}
	}
	return /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
		className: "space-y-6",
		children: [/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("header", {
			className: "space-y-2",
			children: [
				/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
					className: "text-xs font-medium uppercase tracking-[0.14em] text-muted-foreground",
					children: "Playground"
				}),
				/* @__PURE__ */ (0, import_jsx_runtime.jsx)("h1", {
					className: "text-3xl md:text-4xl",
					children: "Run it against Grok"
				}),
				/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
					className: "max-w-xl text-sm text-muted-foreground",
					children: "System message, user prompt, temperature. One click, one completion."
				})
			]
		}), /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
			className: "grid gap-4 lg:grid-cols-2",
			children: [/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
				className: "space-y-4",
				children: [
					/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
						className: "space-y-1.5",
						children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Label, {
							htmlFor: "system",
							children: "System"
						}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)(Textarea, {
							id: "system",
							value: system,
							onChange: (e) => setSystem(e.target.value),
							rows: 4
						})]
					}),
					/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
						className: "space-y-1.5",
						children: [/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
							className: "flex items-center justify-between",
							children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Label, {
								htmlFor: "user",
								children: "User prompt"
							}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)("button", {
								type: "button",
								className: "text-xs text-muted-foreground hover:text-foreground",
								onClick: () => setUser(prompt),
								children: "Load from composer"
							})]
						}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)(Textarea, {
							id: "user",
							value: user,
							onChange: (e) => setUser(e.target.value),
							rows: 10,
							className: "min-h-48 font-mono text-[0.8rem]"
						})]
					}),
					/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
						className: "flex items-center gap-4",
						children: [/* @__PURE__ */ (0, import_jsx_runtime.jsxs)(Label, {
							htmlFor: "temp",
							className: "shrink-0",
							children: ["Temperature ", temperature.toFixed(2)]
						}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)("input", {
							id: "temp",
							type: "range",
							min: 0,
							max: 1.2,
							step: .05,
							value: temperature,
							onChange: (e) => setTemperature(Number(e.target.value)),
							className: "h-11 w-full accent-primary"
						})]
					}),
					/* @__PURE__ */ (0, import_jsx_runtime.jsxs)(Button, {
						onClick: onRun,
						disabled: busy,
						className: "w-full sm:w-auto",
						children: [busy ? /* @__PURE__ */ (0, import_jsx_runtime.jsx)(LoaderCircle, { className: "animate-spin" }) : /* @__PURE__ */ (0, import_jsx_runtime.jsx)(Play, {}), busy ? "Running…" : "Run prompt"]
					})
				]
			}), /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
				className: "space-y-4",
				children: [
					/* @__PURE__ */ (0, import_jsx_runtime.jsxs)(Card, {
						className: "min-h-64 p-4 md:p-5",
						children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
							className: "mb-2 text-xs font-medium uppercase tracking-[0.12em] text-muted-foreground",
							children: "Output"
						}), busy && !output ? /* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
							className: "text-sm text-muted-foreground",
							children: "Waiting on the model…"
						}) : /* @__PURE__ */ (0, import_jsx_runtime.jsx)("pre", {
							className: "whitespace-pre-wrap font-sans text-sm leading-relaxed",
							children: output || "The completion will land here."
						})]
					}),
					/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
						className: "flex items-center justify-between",
						children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)("h2", {
							className: "text-lg",
							children: "Recent runs"
						}), runs.length > 0 && /* @__PURE__ */ (0, import_jsx_runtime.jsxs)(Button, {
							variant: "ghost",
							size: "sm",
							onClick: clearRuns,
							children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Trash2, {}), "Clear"]
						})]
					}),
					runs.length === 0 ? /* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
						className: "text-sm text-muted-foreground",
						children: "No runs yet."
					}) : /* @__PURE__ */ (0, import_jsx_runtime.jsx)("ul", {
						className: "space-y-3",
						children: runs.slice(0, 6).map((run) => /* @__PURE__ */ (0, import_jsx_runtime.jsx)("li", { children: /* @__PURE__ */ (0, import_jsx_runtime.jsxs)(Card, {
							className: "p-4",
							children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
								className: "line-clamp-2 font-mono text-[0.75rem] text-muted-foreground",
								children: run.input
							}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
								className: "mt-2 line-clamp-4 text-sm",
								children: run.output
							})]
						}) }, run.id))
					})
				]
			})]
		})]
	});
}
//#endregion
export { Playground as component };
