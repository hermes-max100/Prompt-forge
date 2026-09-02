import { S as require_jsx_runtime, b as useNavigate } from "../_libs/@tanstack/react-router+[...].mjs";
import { m as ArrowUpRight, n as Trash2 } from "../_libs/lucide-react.mjs";
import { n as toast } from "../_libs/sonner.mjs";
import { n as Card, t as Button } from "./card-BfEqT_9M.mjs";
import { n as getFramework, r as useForge } from "./store-BbFtGqI7.mjs";
import { t as Badge } from "./badge-DW7d1CIs.mjs";
//#region node_modules/.nitro/vite/services/ssr/assets/library-Cj4XEC0s.js
var import_jsx_runtime = require_jsx_runtime();
function Library() {
	const { saved, loadSaved, deleteSaved } = useForge();
	const navigate = useNavigate();
	if (saved.length === 0) return /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
		className: "space-y-3",
		children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)("h1", {
			className: "text-3xl",
			children: "Library"
		}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
			className: "max-w-md text-sm text-muted-foreground",
			children: "Saved prompts live here, on this device. Compose one, then hit Save."
		})]
	});
	return /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
		className: "space-y-6",
		children: [/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("header", {
			className: "space-y-2",
			children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
				className: "text-xs font-medium uppercase tracking-[0.14em] text-muted-foreground",
				children: "Library"
			}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)("h1", {
				className: "text-3xl md:text-4xl",
				children: "Kept versions"
			})]
		}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)("ul", {
			className: "grid gap-3 sm:grid-cols-2",
			children: saved.map((item) => /* @__PURE__ */ (0, import_jsx_runtime.jsx)("li", { children: /* @__PURE__ */ (0, import_jsx_runtime.jsxs)(Card, {
				className: "flex h-full flex-col p-4",
				children: [
					/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
						className: "mb-2 flex items-start justify-between gap-2",
						children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)("h2", {
							className: "text-lg leading-snug",
							children: item.title
						}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)(Badge, { children: getFramework(item.frameworkId).name })]
					}),
					/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
						className: "line-clamp-4 flex-1 font-mono text-[0.75rem] text-muted-foreground",
						children: item.assembled
					}),
					/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
						className: "mt-4 flex gap-2",
						children: [/* @__PURE__ */ (0, import_jsx_runtime.jsxs)(Button, {
							size: "sm",
							onClick: () => {
								loadSaved(item.id);
								navigate({ to: "/" });
								toast.success("Loaded into composer");
							},
							children: ["Open", /* @__PURE__ */ (0, import_jsx_runtime.jsx)(ArrowUpRight, {})]
						}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)(Button, {
							size: "sm",
							variant: "ghost",
							onClick: () => deleteSaved(item.id),
							children: /* @__PURE__ */ (0, import_jsx_runtime.jsx)(Trash2, {})
						})]
					})
				]
			}) }, item.id))
		})]
	});
}
//#endregion
export { Library as component };
