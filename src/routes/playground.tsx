import { createFileRoute } from "@tanstack/react-router";
import { Loader2, Play, Trash2 } from "lucide-react";
import { useMemo, useState } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { runPrompt } from "@/lib/ai";
import { useForge } from "@/lib/store";

export const Route = createFileRoute("/playground")({ component: Playground });

function Playground() {
  const {
    system,
    setSystem,
    temperature,
    setTemperature,
    assembled,
    fields,
    frameworkId,
    pushRun,
    runs,
    clearRuns,
  } = useForge();
  const prompt = useMemo(() => assembled(), [frameworkId, fields, assembled]);
  const [user, setUser] = useState(prompt);
  const [output, setOutput] = useState("");
  const [busy, setBusy] = useState(false);

  async function onRun() {
    const body = user.trim() || prompt;
    if (!body) {
      toast.error("Write a prompt first");
      return;
    }
    setBusy(true);
    setOutput("");
    try {
      const res = await runPrompt({
        data: { system, user: body, temperature },
      });
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

  return (
    <div className="space-y-6">
      <header className="space-y-2">
        <p className="text-xs font-medium uppercase tracking-[0.14em] text-muted-foreground">
          Playground
        </p>
        <h1 className="text-3xl md:text-4xl">Run it against Grok</h1>
        <p className="max-w-xl text-sm text-muted-foreground">
          System message, user prompt, temperature. One click, one completion.
        </p>
      </header>

      <div className="grid gap-4 lg:grid-cols-2">
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="system">System</Label>
            <Textarea
              id="system"
              value={system}
              onChange={(e) => setSystem(e.target.value)}
              rows={4}
            />
          </div>
          <div className="space-y-1.5">
            <div className="flex items-center justify-between">
              <Label htmlFor="user">User prompt</Label>
              <button
                type="button"
                className="text-xs text-muted-foreground hover:text-foreground"
                onClick={() => setUser(prompt)}
              >
                Load from composer
              </button>
            </div>
            <Textarea
              id="user"
              value={user}
              onChange={(e) => setUser(e.target.value)}
              rows={10}
              className="min-h-48 font-mono text-[0.8rem]"
            />
          </div>
          <div className="flex items-center gap-4">
            <Label htmlFor="temp" className="shrink-0">
              Temperature {temperature.toFixed(2)}
            </Label>
            <input
              id="temp"
              type="range"
              min={0}
              max={1.2}
              step={0.05}
              value={temperature}
              onChange={(e) => setTemperature(Number(e.target.value))}
              className="h-11 w-full accent-primary"
            />
          </div>
          <Button onClick={onRun} disabled={busy} className="w-full sm:w-auto">
            {busy ? <Loader2 className="animate-spin" /> : <Play />}
            {busy ? "Running…" : "Run prompt"}
          </Button>
        </div>

        <div className="space-y-4">
          <Card className="min-h-64 p-4 md:p-5">
            <p className="mb-2 text-xs font-medium uppercase tracking-[0.12em] text-muted-foreground">
              Output
            </p>
            {busy && !output ? (
              <p className="text-sm text-muted-foreground">Waiting on the model…</p>
            ) : (
              <pre className="whitespace-pre-wrap font-sans text-sm leading-relaxed">
                {output || "The completion will land here."}
              </pre>
            )}
          </Card>

          <div className="flex items-center justify-between">
            <h2 className="text-lg">Recent runs</h2>
            {runs.length > 0 && (
              <Button variant="ghost" size="sm" onClick={clearRuns}>
                <Trash2 />
                Clear
              </Button>
            )}
          </div>
          {runs.length === 0 ? (
            <p className="text-sm text-muted-foreground">No runs yet.</p>
          ) : (
            <ul className="space-y-3">
              {runs.slice(0, 6).map((run) => (
                <li key={run.id}>
                  <Card className="p-4">
                    <p className="line-clamp-2 font-mono text-[0.75rem] text-muted-foreground">
                      {run.input}
                    </p>
                    <p className="mt-2 line-clamp-4 text-sm">{run.output}</p>
                  </Card>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}
