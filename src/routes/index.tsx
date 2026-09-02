import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { ArrowRight, Copy, Sparkles, Save } from "lucide-react";
import { useMemo, useState } from "react";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { improvePrompt } from "@/lib/ai";
import { FRAMEWORKS, getFramework } from "@/lib/frameworks";
import { useForge } from "@/lib/store";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/")({ component: Compose });

function Compose() {
  const navigate = useNavigate();
  const {
    frameworkId,
    fields,
    setFramework,
    setField,
    assembled,
    saveCurrent,
  } = useForge();
  const fw = getFramework(frameworkId);
  const prompt = useMemo(() => assembled(), [frameworkId, fields, assembled]);
  const [busy, setBusy] = useState(false);

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
    void navigate({ to: "/playground" });
  }

  return (
    <div className="grid min-w-0 gap-8 lg:grid-cols-[minmax(0,1fr)_22rem]">
      <div className="space-y-6">
        <header className="space-y-2">
          <p className="text-xs font-medium uppercase tracking-[0.14em] text-muted-foreground">
            Composer
          </p>
          <h1 className="text-3xl md:text-4xl">Build a prompt that holds still</h1>
          <p className="max-w-xl text-sm text-muted-foreground md:text-base">
            Pick a framework, fill the slots, then run it. Structure beats
            adjectives.
          </p>
        </header>

        <div className="grid grid-cols-3 gap-2 sm:flex sm:flex-wrap">
          {FRAMEWORKS.map((item) => {
            const active = item.id === frameworkId;
            return (
              <button
                key={item.id}
                type="button"
                onClick={() => setFramework(item.id)}
                className={cn(
                  "flex min-h-11 flex-col rounded-xl border px-3 py-2.5 text-left transition-colors duration-150",
                  active
                    ? "border-primary/40 bg-muted"
                    : "border-border bg-card hover:bg-muted",
                )}
              >
                <span className="text-sm font-medium">{item.name}</span>
                <span className="text-[0.7rem] text-muted-foreground">
                  {item.tag}
                </span>
              </button>
            );
          })}
        </div>

        <p className="text-sm text-muted-foreground">{fw.summary}</p>

        <div className="space-y-4">
          {fw.fields.map((field) => (
            <div key={field.key} className="space-y-1.5">
              <div className="flex items-baseline justify-between gap-3">
                <Label htmlFor={field.key}>{field.label}</Label>
                <span className="text-[0.7rem] text-muted-foreground">
                  {field.hint}
                </span>
              </div>
              {field.multiline ? (
                <Textarea
                  id={field.key}
                  value={fields[field.key] ?? ""}
                  onChange={(e) => setField(field.key, e.target.value)}
                  placeholder={field.placeholder}
                  rows={3}
                />
              ) : (
                <Input
                  id={field.key}
                  value={fields[field.key] ?? ""}
                  onChange={(e) => setField(field.key, e.target.value)}
                  placeholder={field.placeholder}
                />
              )}
            </div>
          ))}
        </div>
      </div>

      <aside className="space-y-4 lg:sticky lg:top-20 lg:self-start">
        <Card className="p-4 md:p-5">
          <div className="mb-3 flex items-center justify-between">
            <span className="text-xs font-medium uppercase tracking-[0.12em] text-muted-foreground">
              Assembled
            </span>
            <Badge>{fw.name}</Badge>
          </div>
          <pre className="max-h-[28rem] overflow-auto whitespace-pre-wrap font-mono text-[0.8rem] leading-relaxed text-foreground/90">
            {prompt || "Fields will compile here."}
          </pre>
          <div className="mt-4 grid grid-cols-2 gap-2">
            <Button variant="secondary" size="sm" onClick={onCopy} disabled={!prompt}>
              <Copy />
              Copy
            </Button>
            <Button variant="secondary" size="sm" onClick={onSave} disabled={!prompt}>
              <Save />
              Save
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={onImprove}
              disabled={busy || !prompt}
            >
              <Sparkles />
              {busy ? "Rewriting…" : "Improve"}
            </Button>
            <Button size="sm" onClick={onRun} disabled={!prompt}>
              Run
              <ArrowRight />
            </Button>
          </div>
        </Card>
      </aside>
    </div>
  );
}
