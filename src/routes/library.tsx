import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { ArrowUpRight, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { getFramework } from "@/lib/frameworks";
import { useForge } from "@/lib/store";

export const Route = createFileRoute("/library")({ component: Library });

function Library() {
  const { saved, loadSaved, deleteSaved } = useForge();
  const navigate = useNavigate();

  if (saved.length === 0) {
    return (
      <div className="space-y-3">
        <h1 className="text-3xl">Library</h1>
        <p className="max-w-md text-sm text-muted-foreground">
          Saved prompts live here, on this device. Compose one, then hit Save.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <header className="space-y-2">
        <p className="text-xs font-medium uppercase tracking-[0.14em] text-muted-foreground">
          Library
        </p>
        <h1 className="text-3xl md:text-4xl">Kept versions</h1>
      </header>
      <ul className="grid gap-3 sm:grid-cols-2">
        {saved.map((item) => (
          <li key={item.id}>
            <Card className="flex h-full flex-col p-4">
              <div className="mb-2 flex items-start justify-between gap-2">
                <h2 className="text-lg leading-snug">{item.title}</h2>
                <Badge>{getFramework(item.frameworkId).name}</Badge>
              </div>
              <p className="line-clamp-4 flex-1 font-mono text-[0.75rem] text-muted-foreground">
                {item.assembled}
              </p>
              <div className="mt-4 flex gap-2">
                <Button
                  size="sm"
                  onClick={() => {
                    loadSaved(item.id);
                    void navigate({ to: "/" });
                    toast.success("Loaded into composer");
                  }}
                >
                  Open
                  <ArrowUpRight />
                </Button>
                <Button
                  size="sm"
                  variant="ghost"
                  onClick={() => deleteSaved(item.id)}
                >
                  <Trash2 />
                </Button>
              </div>
            </Card>
          </li>
        ))}
      </ul>
    </div>
  );
}
