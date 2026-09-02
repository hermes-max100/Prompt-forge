import { create } from "zustand";
import { persist } from "zustand/middleware";
import { uid } from "./utils";
import { getFramework } from "./frameworks";

export type SavedPrompt = {
  id: string;
  title: string;
  frameworkId: string;
  fields: Record<string, string>;
  assembled: string;
  system: string;
  createdAt: number;
  updatedAt: number;
};

export type PlaygroundRun = {
  id: string;
  input: string;
  output: string;
  at: number;
};

export type EvalCase = { id: string; input: string; expected: string };

type State = {
  frameworkId: string;
  fields: Record<string, string>;
  system: string;
  temperature: number;
  saved: SavedPrompt[];
  runs: PlaygroundRun[];
  evalPromptA: string;
  evalPromptB: string;
  evalCases: EvalCase[];
  setFramework: (id: string) => void;
  setField: (key: string, value: string) => void;
  setSystem: (value: string) => void;
  setTemperature: (value: number) => void;
  assembled: () => string;
  saveCurrent: (title?: string) => string;
  loadSaved: (id: string) => void;
  deleteSaved: (id: string) => void;
  pushRun: (input: string, output: string) => void;
  clearRuns: () => void;
  setEvalPromptA: (v: string) => void;
  setEvalPromptB: (v: string) => void;
  addEvalCase: () => void;
  updateEvalCase: (id: string, patch: Partial<EvalCase>) => void;
  removeEvalCase: (id: string) => void;
};

const starterCases: EvalCase[] = [
  {
    id: "c1",
    input: "Summarize a 12-page privacy policy for a consumer app.",
    expected: "plain language, under 120 words, names the data collected",
  },
  {
    id: "c2",
    input: "Explain vector embeddings to a product manager.",
    expected: "no jargon without a definition, one analogy, one caveat",
  },
  {
    id: "c3",
    input: "Write a rejection note for a late-stage candidate.",
    expected: "warm, specific, no false hope, under 90 words",
  },
];

export const useForge = create<State>()(
  persist(
    (set, get) => ({
      frameworkId: "costar",
      fields: {},
      system: "You are a precise prompt engineer. Follow the brief. Do not pad.",
      temperature: 0.4,
      saved: [],
      runs: [],
      evalPromptA:
        "You are a senior editor. Complete the task below in under 120 words. Task: {{input}}",
      evalPromptB:
        "Role: staff writer.\nTask: {{input}}\nFormat: 3 short paragraphs.\nTone: plain, specific.",
      evalCases: starterCases,
      setFramework: (id) => set({ frameworkId: id, fields: {} }),
      setField: (key, value) =>
        set({ fields: { ...get().fields, [key]: value } }),
      setSystem: (system) => set({ system }),
      setTemperature: (temperature) => set({ temperature }),
      assembled: () => getFramework(get().frameworkId).assemble(get().fields),
      saveCurrent: (title) => {
        const assembled = get().assembled();
        const id = uid();
        const now = Date.now();
        const item: SavedPrompt = {
          id,
          title: title?.trim() || assembled.slice(0, 48) || "Untitled prompt",
          frameworkId: get().frameworkId,
          fields: { ...get().fields },
          assembled,
          system: get().system,
          createdAt: now,
          updatedAt: now,
        };
        set({ saved: [item, ...get().saved] });
        return id;
      },
      loadSaved: (id) => {
        const item = get().saved.find((s) => s.id === id);
        if (!item) return;
        set({
          frameworkId: item.frameworkId,
          fields: { ...item.fields },
          system: item.system,
        });
      },
      deleteSaved: (id) =>
        set({ saved: get().saved.filter((s) => s.id !== id) }),
      pushRun: (input, output) =>
        set({
          runs: [
            { id: uid(), input, output, at: Date.now() },
            ...get().runs,
          ].slice(0, 20),
        }),
      clearRuns: () => set({ runs: [] }),
      setEvalPromptA: (evalPromptA) => set({ evalPromptA }),
      setEvalPromptB: (evalPromptB) => set({ evalPromptB }),
      addEvalCase: () => {
        if (get().evalCases.length >= 4) return;
        set({
          evalCases: [
            ...get().evalCases,
            { id: uid(), input: "", expected: "" },
          ],
        });
      },
      updateEvalCase: (id, patch) =>
        set({
          evalCases: get().evalCases.map((c) =>
            c.id === id ? { ...c, ...patch } : c,
          ),
        }),
      removeEvalCase: (id) =>
        set({ evalCases: get().evalCases.filter((c) => c.id !== id) }),
    }),
    { name: "promptforge-v1" },
  ),
);
