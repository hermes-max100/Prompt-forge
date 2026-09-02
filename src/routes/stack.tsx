import { createFileRoute, Link } from "@tanstack/react-router";
import { ArrowRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";

export const Route = createFileRoute("/stack")({ component: Stack });

const STEPS = [
  {
    n: "01",
    title: "Playground first",
    body: "Start in a live model, not a document. Change one variable at a time: system, few-shot, temperature. Promptforge’s Playground is that loop, running on Grok.",
    to: "/playground",
    cta: "Open playground",
  },
  {
    n: "02",
    title: "Structure multi-step work",
    body: "When a task has stages, stop hand-tuning a single string. Frameworks (CO-STAR, RISEN) keep slots explicit. In production, DSPy compiles those slots against a metric instead of hope.",
    to: "/",
    cta: "Open composer",
  },
  {
    n: "03",
    title: "Test before you ship",
    body: "Two variants, a tiny dataset, a judge. Eval Lab is the in-browser version of Promptfoo: regression you can feel. Production teams keep that suite in CI.",
    to: "/eval",
    cta: "Open eval lab",
  },
  {
    n: "04",
    title: "Version what actually ran",
    body: "The prompt in git is often not the prompt in production. Save drafts here. Later, wire PromptLayer, Langfuse, or Braintrust so traces, versions, and evals share a spine.",
    to: "/library",
    cta: "Open library",
  },
];

function Stack() {
  return (
    <div className="space-y-8">
      <header className="max-w-2xl space-y-3">
        <p className="text-xs font-medium uppercase tracking-[0.14em] text-muted-foreground">
          Starter stack
        </p>
        <h1 className="text-3xl md:text-4xl">Four moves, in order</h1>
        <p className="text-sm text-muted-foreground md:text-base">
          Experiment, structure, evaluate, then observe. Skip a step and the
          prompt becomes folklore.
        </p>
      </header>

      <ol className="grid gap-4 md:grid-cols-2">
        {STEPS.map((step) => (
          <li key={step.n}>
            <Card className="flex h-full flex-col p-5">
              <span className="font-mono text-xs text-muted-foreground">
                {step.n}
              </span>
              <h2 className="mt-2 text-xl">{step.title}</h2>
              <p className="mt-2 flex-1 text-sm leading-relaxed text-muted-foreground">
                {step.body}
              </p>
              <Button asChild variant="secondary" className="mt-5 w-fit">
                <Link to={step.to}>
                  {step.cta}
                  <ArrowRight />
                </Link>
              </Button>
            </Card>
          </li>
        ))}
      </ol>
    </div>
  );
}
