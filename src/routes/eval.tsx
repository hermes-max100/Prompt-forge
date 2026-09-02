import { createFileRoute } from "@tanstack/react-router";
import { Loader2, Plus, Play, Trash2 } from "lucide-react";
import { useState } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Input } from "@/components/ui/input";
import { runEval, type EvalRow } from "@/lib/ai";
import { useForge } from "@/lib/store";

export const Route = createFileRoute("/eval")({ component: EvalLab });

function EvalLab() {
  const {
    evalPromptA,
    evalPromptB,
    evalCases,
    setEvalPromptA,
    setEvalPromptB,
    addEvalCase,
    updateEvalCase,
    removeEvalCase,
  } = useForge();
  const [busy, setBusy] = useState(false);
  const [rows, setRows] = useState<EvalRow[] | null>(null);
  const [verdict, setVerdict] = useState("");

  async function onRun() {
    setBusy(true);
    setRows(null);
    setVerdict("");
    try {
      const res = await runEval({
        data: {
          promptA: evalPromptA,
          promptB: evalPromptB,
          cases: evalCases,
        },
      });
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

  return (
    <div className="space-y-6">
      <header className="space-y-2">
        <p className="text-xs font-medium uppercase tracking-[0.14em] text-muted-foreground">
          Eval lab
        </p>
        <h1 className="text-3xl md:text-4xl">A versus B, with a judge</h1>
        <p className="max-w-xl text-sm text-muted-foreground">
          Use {"{{input}}"} in both prompts. Up to four cases. Each run scores
          both variants, then Grok writes a verdict.
        </p>
      </header>

      <div className="grid gap-4 md:grid-cols-2">
        <div className="space-y-1.5">
          <Label htmlFor="pa">Prompt A</Label>
          <Textarea
            id="pa"
            value={evalPromptA}
            onChange={(e) => setEvalPromptA(e.target.value)}
            rows={7}
            className="font-mono text-[0.8rem]"
          />
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="pb">Prompt B</Label>
          <Textarea
            id="pb"
            value={evalPromptB}
            onChange={(e) => setEvalPromptB(e.target.value)}
            rows={7}
            className="font-mono text-[0.8rem]"
          />
        </div>
      </div>

      <div className="flex items-center justify-between">
        <h2 className="text-lg">Cases</h2>
        <Button
          variant="secondary"
          size="sm"
          onClick={addEvalCase}
          disabled={evalCases.length >= 4}
        >
          <Plus />
          Add case
        </Button>
      </div>

      <ul className="space-y-3">
        {evalCases.map((c, i) => (
          <li key={c.id}>
            <Card className="space-y-3 p-4">
              <div className="flex items-center justify-between">
                <span className="text-xs uppercase tracking-[0.12em] text-muted-foreground">
                  Case {i + 1}
                </span>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => removeEvalCase(c.id)}
                  disabled={evalCases.length <= 1}
                >
                  <Trash2 />
                </Button>
              </div>
              <Input
                value={c.input}
                onChange={(e) =>
                  updateEvalCase(c.id, { input: e.target.value })
                }
                placeholder="Task / input"
              />
              <Input
                value={c.expected}
                onChange={(e) =>
                  updateEvalCase(c.id, { expected: e.target.value })
                }
                placeholder="What good looks like (used by the judge)"
              />
            </Card>
          </li>
        ))}
      </ul>

      <Button onClick={onRun} disabled={busy}>
        {busy ? <Loader2 className="animate-spin" /> : <Play />}
        {busy ? "Evaluating…" : "Run evaluation"}
      </Button>

      {rows && (
        <div className="space-y-4">
          {rows.map((row, i) => (
            <Card key={i} className="grid gap-4 p-4 md:grid-cols-2">
              <div className="md:col-span-2">
                <p className="text-xs uppercase tracking-[0.12em] text-muted-foreground">
                  {row.input}
                </p>
                {row.expected ? (
                  <p className="mt-1 text-xs text-muted-foreground">
                    Criteria: {row.expected}
                  </p>
                ) : null}
              </div>
              <div>
                <p className="mb-1 text-xs font-medium">A</p>
                <pre className="whitespace-pre-wrap text-sm leading-relaxed">
                  {row.aError ?? row.a}
                </pre>
              </div>
              <div>
                <p className="mb-1 text-xs font-medium">B</p>
                <pre className="whitespace-pre-wrap text-sm leading-relaxed">
                  {row.bError ?? row.b}
                </pre>
              </div>
            </Card>
          ))}
          <Card className="p-4 md:p-5">
            <p className="mb-2 text-xs uppercase tracking-[0.12em] text-muted-foreground">
              Judge
            </p>
            <pre className="whitespace-pre-wrap font-sans text-sm leading-relaxed">
              {verdict}
            </pre>
          </Card>
        </div>
      )}
    </div>
  );
}
