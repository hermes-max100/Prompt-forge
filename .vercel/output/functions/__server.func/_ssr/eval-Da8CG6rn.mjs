import { i as __toESM } from "../_runtime.mjs";
import { n as require_react } from "../_libs/@radix-ui/react-compose-refs+[...].mjs";
import { S as require_jsx_runtime } from "../_libs/@tanstack/react-router+[...].mjs";
import { l as LoaderCircle, n as Trash2, o as Plus, s as Play } from "../_libs/lucide-react.mjs";
import { n as toast } from "../_libs/sonner.mjs";
import { n as Card, t as Button } from "./card-BfEqT_9M.mjs";
import { i as runEval, n as Textarea, t as Label } from "./ai-BJT6c9Jr.mjs";
import { t as Input } from "./input-DmpDz7hq.mjs";
import { r as useForge } from "./store-BbFtGqI7.mjs";
//#region node_modules/.nitro/vite/services/ssr/assets/eval-Da8CG6rn.js
var import_react = /* @__PURE__ */ __toESM(require_react());
var import_jsx_runtime = require_jsx_runtime();
function EvalLab() {
	const { evalPromptA, evalPromptB, evalCases, setEvalPromptA, setEvalPromptB, addEvalCase, updateEvalCase, removeEvalCase } = useForge();
	const [busy, setBusy] = (0, import_react.useState)(false);
	const [rows, setRows] = (0, import_react.useState)(null);
	const [verdict, setVerdict] = (0, import_react.useState)("");
	async function onRun() {
		setBusy(true);
		setRows(null);
		setVerdict("");
		try {
			const res = await runEval({ data: {
				promptA: evalPromptA,
				promptB: evalPromptB,
				cases: evalCases
			} });
			if (!res.ok) {
				toast.error(res.error);
				return;
			}
			setRows(res.rows);
			setVerdict(res.verdict);
		} finally {
			setBusy(false);
		}
	}
	return /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
		className: "space-y-6",
		children: [
			/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("header", {
				className: "space-y-2",
				children: [
					/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
						className: "text-xs font-medium uppercase tracking-[0.14em] text-muted-foreground",
						children: "Eval lab"
					}),
					/* @__PURE__ */ (0, import_jsx_runtime.jsx)("h1", {
						className: "text-3xl md:text-4xl",
						children: "A versus B, with a judge"
					}),
					/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("p", {
						className: "max-w-xl text-sm text-muted-foreground",
						children: [
							"Use ",
							"{{input}}",
							" in both prompts. Up to four cases. Each run scores both variants, then Grok writes a verdict."
						]
					})
				]
			}),
			/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
				className: "grid gap-4 md:grid-cols-2",
				children: [/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
					className: "space-y-1.5",
					children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Label, {
						htmlFor: "pa",
						children: "Prompt A"
					}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)(Textarea, {
						id: "pa",
						value: evalPromptA,
						onChange: (e) => setEvalPromptA(e.target.value),
						rows: 7,
						className: "font-mono text-[0.8rem]"
					})]
				}), /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
					className: "space-y-1.5",
					children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Label, {
						htmlFor: "pb",
						children: "Prompt B"
					}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)(Textarea, {
						id: "pb",
						value: evalPromptB,
						onChange: (e) => setEvalPromptB(e.target.value),
						rows: 7,
						className: "font-mono text-[0.8rem]"
					})]
				})]
			}),
			/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
				className: "flex items-center justify-between",
				children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)("h2", {
					className: "text-lg",
					children: "Cases"
				}), /* @__PURE__ */ (0, import_jsx_runtime.jsxs)(Button, {
					variant: "secondary",
					size: "sm",
					onClick: addEvalCase,
					disabled: evalCases.length >= 4,
					children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Plus, {}), "Add case"]
				})]
			}),
			/* @__PURE__ */ (0, import_jsx_runtime.jsx)("ul", {
				className: "space-y-3",
				children: evalCases.map((c, i) => /* @__PURE__ */ (0, import_jsx_runtime.jsx)("li", { children: /* @__PURE__ */ (0, import_jsx_runtime.jsxs)(Card, {
					className: "space-y-3 p-4",
					children: [
						/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
							className: "flex items-center justify-between",
							children: [/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("span", {
								className: "text-xs uppercase tracking-[0.12em] text-muted-foreground",
								children: ["Case ", i + 1]
							}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)(Button, {
								variant: "ghost",
								size: "sm",
								onClick: () => removeEvalCase(c.id),
								disabled: evalCases.length <= 1,
								children: /* @__PURE__ */ (0, import_jsx_runtime.jsx)(Trash2, {})
							})]
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Input, {
							value: c.input,
							onChange: (e) => updateEvalCase(c.id, { input: e.target.value }),
							placeholder: "Task / input"
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Input, {
							value: c.expected,
							onChange: (e) => updateEvalCase(c.id, { expected: e.target.value }),
							placeholder: "What good looks like (used by the judge)"
						})
					]
				}) }, c.id))
			}),
			/* @__PURE__ */ (0, import_jsx_runtime.jsxs)(Button, {
				onClick: onRun,
				disabled: busy,
				children: [busy ? /* @__PURE__ */ (0, import_jsx_runtime.jsx)(LoaderCircle, { className: "animate-spin" }) : /* @__PURE__ */ (0, import_jsx_runtime.jsx)(Play, {}), busy ? "Evaluating…" : "Run evaluation"]
			}),
			rows && /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
				className: "space-y-4",
				children: [rows.map((row, i) => /* @__PURE__ */ (0, import_jsx_runtime.jsxs)(Card, {
					className: "grid gap-4 p-4 md:grid-cols-2",
					children: [
						/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
							className: "md:col-span-2",
							children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
								className: "text-xs uppercase tracking-[0.12em] text-muted-foreground",
								children: row.input
							}), row.expected ? /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("p", {
								className: "mt-1 text-xs text-muted-foreground",
								children: ["Criteria: ", row.expected]
							}) : null]
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", { children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
							className: "mb-1 text-xs font-medium",
							children: "A"
						}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)("pre", {
							className: "whitespace-pre-wrap text-sm leading-relaxed",
							children: row.aError ?? row.a
						})] }),
						/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", { children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
							className: "mb-1 text-xs font-medium",
							children: "B"
						}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)("pre", {
							className: "whitespace-pre-wrap text-sm leading-relaxed",
							children: row.bError ?? row.b
						})] })
					]
				}, i)), /* @__PURE__ */ (0, import_jsx_runtime.jsxs)(Card, {
					className: "p-4 md:p-5",
					children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
						className: "mb-2 text-xs uppercase tracking-[0.12em] text-muted-foreground",
						children: "Judge"
					}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)("pre", {
						className: "whitespace-pre-wrap font-sans text-sm leading-relaxed",
						children: verdict
					})]
				})]
			})
		]
	});
}
//#endregion
export { EvalLab as component };
