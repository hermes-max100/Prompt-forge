import { i as __toESM } from "../_runtime.mjs";
import { n as require_react } from "../_libs/@radix-ui/react-compose-refs+[...].mjs";
import { S as require_jsx_runtime, b as useNavigate } from "../_libs/@tanstack/react-router+[...].mjs";
import { a as Save, h as ArrowRight, i as Sparkles, p as Copy } from "../_libs/lucide-react.mjs";
import { n as toast } from "../_libs/sonner.mjs";
import { n as cn } from "./router-n8Gg9loh.mjs";
import { n as Card, t as Button } from "./card-BfEqT_9M.mjs";
import { n as Textarea, r as improvePrompt, t as Label } from "./ai-BJT6c9Jr.mjs";
import { t as Input } from "./input-DmpDz7hq.mjs";
import { n as getFramework, r as useForge, t as FRAMEWORKS } from "./store-BbFtGqI7.mjs";
import { t as Badge } from "./badge-DW7d1CIs.mjs";
//#region node_modules/.nitro/vite/services/ssr/assets/routes-BP46wV_q.js
var import_react = /* @__PURE__ */ __toESM(require_react());
var import_jsx_runtime = require_jsx_runtime();
function Compose() {
	const navigate = useNavigate();
	const { frameworkId, fields, setFramework, setField, assembled, saveCurrent } = useForge();
	const fw = getFramework(frameworkId);
	const prompt = (0, import_react.useMemo)(() => assembled(), [
		frameworkId,
		fields,
		assembled
	]);
	const [busy, setBusy] = (0, import_react.useState)(false);
	async function onCopy() {
		if (!prompt) return;
		await navigator.clipboard.writeText(prompt);
		toast.success("Copied assembled prompt");
	}
	function onSave() {
		if (!prompt) {
			toast.error("Fill a few fields first");
			return;
		}
		saveCurrent(fw.name + " draft");
		toast.success("Saved to library");
	}
	async function onImprove() {
		if (!prompt) {
			toast.error("Fill a few fields first");
			return;
		}
		setBusy(true);
		try {
			const res = await improvePrompt({ data: { prompt } });
			if (!res.ok) {
				toast.error(res.error);
				return;
			}
			setFramework("freeform");
			useForge.getState().setField("body", res.text);
			toast.success("Rewritten into Freeform");
		} finally {
			setBusy(false);
		}
	}
	function onRun() {
		if (!prompt) {
			toast.error("Fill a few fields first");
			return;
		}
		navigate({ to: "/playground" });
	}
	return /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
		className: "grid min-w-0 gap-8 lg:grid-cols-[minmax(0,1fr)_22rem]",
		children: [/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
			className: "space-y-6",
			children: [
				/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("header", {
					className: "space-y-2",
					children: [
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
							className: "text-xs font-medium uppercase tracking-[0.14em] text-muted-foreground",
							children: "Composer"
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)("h1", {
							className: "text-3xl md:text-4xl",
							children: "Build a prompt that holds still"
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
							className: "max-w-xl text-sm text-muted-foreground md:text-base",
							children: "Pick a framework, fill the slots, then run it. Structure beats adjectives."
						})
					]
				}),
				/* @__PURE__ */ (0, import_jsx_runtime.jsx)("div", {
					className: "grid grid-cols-3 gap-2 sm:flex sm:flex-wrap",
					children: FRAMEWORKS.map((item) => {
						const active = item.id === frameworkId;
						return /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("button", {
							type: "button",
							onClick: () => setFramework(item.id),
							className: cn("flex min-h-11 flex-col rounded-xl border px-3 py-2.5 text-left transition-colors duration-150", active ? "border-primary/40 bg-muted" : "border-border bg-card hover:bg-muted"),
							children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)("span", {
								className: "text-sm font-medium",
								children: item.name
							}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)("span", {
								className: "text-[0.7rem] text-muted-foreground",
								children: item.tag
							})]
						}, item.id);
					})
				}),
				/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
					className: "text-sm text-muted-foreground",
					children: fw.summary
				}),
				/* @__PURE__ */ (0, import_jsx_runtime.jsx)("div", {
					className: "space-y-4",
					children: fw.fields.map((field) => /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
						className: "space-y-1.5",
						children: [/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
							className: "flex items-baseline justify-between gap-3",
							children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Label, {
								htmlFor: field.key,
								children: field.label
							}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)("span", {
								className: "text-[0.7rem] text-muted-foreground",
								children: field.hint
							})]
						}), field.multiline ? /* @__PURE__ */ (0, import_jsx_runtime.jsx)(Textarea, {
							id: field.key,
							value: fields[field.key] ?? "",
							onChange: (e) => setField(field.key, e.target.value),
							placeholder: field.placeholder,
							rows: 3
						}) : /* @__PURE__ */ (0, import_jsx_runtime.jsx)(Input, {
							id: field.key,
							value: fields[field.key] ?? "",
							onChange: (e) => setField(field.key, e.target.value),
							placeholder: field.placeholder
						})]
					}, field.key))
				})
			]
		}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)("aside", {
			className: "space-y-4 lg:sticky lg:top-20 lg:self-start",
			children: /* @__PURE__ */ (0, import_jsx_runtime.jsxs)(Card, {
				className: "p-4 md:p-5",
				children: [
					/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
						className: "mb-3 flex items-center justify-between",
						children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)("span", {
							className: "text-xs font-medium uppercase tracking-[0.12em] text-muted-foreground",
							children: "Assembled"
						}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)(Badge, { children: fw.name })]
					}),
					/* @__PURE__ */ (0, import_jsx_runtime.jsx)("pre", {
						className: "max-h-[28rem] overflow-auto whitespace-pre-wrap font-mono text-[0.8rem] leading-relaxed text-foreground/90",
						children: prompt || "Fields will compile here."
					}),
					/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
						className: "mt-4 grid grid-cols-2 gap-2",
						children: [
							/* @__PURE__ */ (0, import_jsx_runtime.jsxs)(Button, {
								variant: "secondary",
								size: "sm",
								onClick: onCopy,
								disabled: !prompt,
								children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Copy, {}), "Copy"]
							}),
							/* @__PURE__ */ (0, import_jsx_runtime.jsxs)(Button, {
								variant: "secondary",
								size: "sm",
								onClick: onSave,
								disabled: !prompt,
								children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Save, {}), "Save"]
							}),
							/* @__PURE__ */ (0, import_jsx_runtime.jsxs)(Button, {
								variant: "outline",
								size: "sm",
								onClick: onImprove,
								disabled: busy || !prompt,
								children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Sparkles, {}), busy ? "Rewriting…" : "Improve"]
							}),
							/* @__PURE__ */ (0, import_jsx_runtime.jsxs)(Button, {
								size: "sm",
								onClick: onRun,
								disabled: !prompt,
								children: ["Run", /* @__PURE__ */ (0, import_jsx_runtime.jsx)(ArrowRight, {})]
							})
						]
					})
				]
			})
		})]
	});
}
//#endregion
export { Compose as component };
