import "../_runtime.mjs";
import { n as require_react } from "../_libs/@radix-ui/react-compose-refs+[...].mjs";
import { S as require_jsx_runtime } from "../_libs/@tanstack/react-router+[...].mjs";
import { n as TSS_SERVER_FUNCTION, r as getServerFnById, t as createServerFn } from "./ssr.mjs";
import { n as cn } from "./router-n8Gg9loh.mjs";
require_react();
var import_jsx_runtime = require_jsx_runtime();
function Label({ className, ...props }) {
	return /* @__PURE__ */ (0, import_jsx_runtime.jsx)("label", {
		className: cn("text-xs font-medium tracking-wide text-muted-foreground", className),
		...props
	});
}
function Textarea({ className, ...props }) {
	return /* @__PURE__ */ (0, import_jsx_runtime.jsx)("textarea", {
		className: cn("flex min-h-28 w-full rounded-lg border border-border bg-card px-3 py-2.5 text-sm leading-normal text-foreground placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring", className),
		...props
	});
}
var createSsrRpc = (functionId) => {
	const url = "/_serverFn/" + functionId;
	const serverFnMeta = { id: functionId };
	const fn = async (...args) => {
		return (await getServerFnById(functionId, { origin: "server" }))(...args);
	};
	return Object.assign(fn, {
		url,
		serverFnMeta,
		[TSS_SERVER_FUNCTION]: true
	});
};
var runPrompt = createServerFn({ method: "POST" }).validator((input) => input).handler(createSsrRpc("1049bad283d038fd5dcfd5c0770c49d53be8936991122557ded91c7c7ede0ea1"));
var improvePrompt = createServerFn({ method: "POST" }).validator((input) => input).handler(createSsrRpc("27e95d3e09f8192da2d32713d7f8ce87bf53d53933fb4757a1eda86c3e2194f3"));
var runEval = createServerFn({ method: "POST" }).validator((input) => input).handler(createSsrRpc("09e676400a8352789b4a992a23c8be3bc225fd7138253dd211713e807e081f72"));
//#endregion
export { runPrompt as a, runEval as i, Textarea as n, improvePrompt as r, Label as t };
