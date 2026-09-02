import { S as require_jsx_runtime, y as Link } from "../_libs/@tanstack/react-router+[...].mjs";
import { h as ArrowRight } from "../_libs/lucide-react.mjs";
import { n as Card, t as Button } from "./card-BfEqT_9M.mjs";
//#region node_modules/.nitro/vite/services/ssr/assets/stack-BGayP8NR.js
var import_jsx_runtime = require_jsx_runtime();
var STEPS = [
	{
		n: "01",
		title: "Playground first",
		body: "Start in a live model, not a document. Change one variable at a time: system, few-shot, temperature. Promptforge’s Playground is that loop, running on Grok.",
		to: "/playground",
		cta: "Open playground"
	},
	{
		n: "02",
		title: "Structure multi-step work",
		body: "When a task has stages, stop hand-tuning a single string. Frameworks (CO-STAR, RISEN) keep slots explicit. In production, DSPy compiles those slots against a metric instead of hope.",
		to: "/",
		cta: "Open composer"
	},
	{
		n: "03",
		title: "Test before you ship",
		body: "Two variants, a tiny dataset, a judge. Eval Lab is the in-browser version of Promptfoo: regression you can feel. Production teams keep that suite in CI.",
		to: "/eval",
		cta: "Open eval lab"
	},
	{
		n: "04",
		title: "Version what actually ran",
		body: "The prompt in git is often not the prompt in production. Save drafts here. Later, wire PromptLayer, Langfuse, or Braintrust so traces, versions, and evals share a spine.",
		to: "/library",
		cta: "Open library"
	}
];
function Stack() {
	return /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
		className: "space-y-8",
		children: [/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("header", {
			className: "max-w-2xl space-y-3",
			children: [
				/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
					className: "text-xs font-medium uppercase tracking-[0.14em] text-muted-foreground",
					children: "Starter stack"
				}),
				/* @__PURE__ */ (0, import_jsx_runtime.jsx)("h1", {
					className: "text-3xl md:text-4xl",
					children: "Four moves, in order"
				}),
				/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
					className: "text-sm text-muted-foreground md:text-base",
					children: "Experiment, structure, evaluate, then observe. Skip a step and the prompt becomes folklore."
				})
			]
		}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)("ol", {
			className: "grid gap-4 md:grid-cols-2",
			children: STEPS.map((step) => /* @__PURE__ */ (0, import_jsx_runtime.jsx)("li", { children: /* @__PURE__ */ (0, import_jsx_runtime.jsxs)(Card, {
				className: "flex h-full flex-col p-5",
				children: [
					/* @__PURE__ */ (0, import_jsx_runtime.jsx)("span", {
						className: "font-mono text-xs text-muted-foreground",
						children: step.n
					}),
					/* @__PURE__ */ (0, import_jsx_runtime.jsx)("h2", {
						className: "mt-2 text-xl",
						children: step.title
					}),
					/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
						className: "mt-2 flex-1 text-sm leading-relaxed text-muted-foreground",
						children: step.body
					}),
					/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Button, {
						asChild: true,
						variant: "secondary",
						className: "mt-5 w-fit",
						children: /* @__PURE__ */ (0, import_jsx_runtime.jsxs)(Link, {
							to: step.to,
							children: [step.cta, /* @__PURE__ */ (0, import_jsx_runtime.jsx)(ArrowRight, {})]
						})
					})
				]
			}) }, step.n))
		})]
	});
}
//#endregion
export { Stack as component };
