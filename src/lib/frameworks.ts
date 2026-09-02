export type Field = {
  key: string;
  label: string;
  hint: string;
  placeholder: string;
  multiline?: boolean;
};

export type Framework = {
  id: string;
  name: string;
  tag: string;
  summary: string;
  assemble: (values: Record<string, string>) => string;
  fields: Field[];
};

const line = (label: string, value?: string) => {
  const v = (value ?? "").trim();
  return v ? `${label}: ${v}` : "";
};

export const FRAMEWORKS: Framework[] = [
  {
    id: "costar",
    name: "CO-STAR",
    tag: "General",
    summary: "Context, Objective, Style, Tone, Audience, Response.",
    fields: [
      {
        key: "context",
        label: "Context",
        hint: "Background the model needs",
        placeholder: "You are advising a Series A SaaS team shipping its first AI feature.",
        multiline: true,
      },
      {
        key: "objective",
        label: "Objective",
        hint: "The job to be done",
        placeholder: "Draft a launch email that explains the feature without jargon.",
        multiline: true,
      },
      {
        key: "style",
        label: "Style",
        hint: "Form and structure",
        placeholder: "Plain language, short paragraphs, one CTA.",
      },
      {
        key: "tone",
        label: "Tone",
        hint: "Voice",
        placeholder: "Confident, calm, specific.",
      },
      {
        key: "audience",
        label: "Audience",
        hint: "Who reads it",
        placeholder: "Founders who are skeptical of AI hype.",
      },
      {
        key: "response",
        label: "Response",
        hint: "Output contract",
        placeholder: "Subject line + 120-word body + 3 subject alternatives.",
        multiline: true,
      },
    ],
    assemble: (v) =>
      [
        "Follow this brief exactly.",
        line("Context", v.context),
        line("Objective", v.objective),
        line("Style", v.style),
        line("Tone", v.tone),
        line("Audience", v.audience),
        line("Response format", v.response),
      ]
        .filter(Boolean)
        .join("\n\n"),
  },
  {
    id: "craft",
    name: "CRAFT",
    tag: "Creative",
    summary: "Context, Role, Action, Format, Tone.",
    fields: [
      {
        key: "context",
        label: "Context",
        hint: "Situation",
        placeholder: "A design studio pitching a museum exhibition website.",
        multiline: true,
      },
      {
        key: "role",
        label: "Role",
        hint: "Who the model is",
        placeholder: "Senior copywriter with a modernist restraint.",
      },
      {
        key: "action",
        label: "Action",
        hint: "What to do",
        placeholder: "Write homepage hero copy and three supporting lines.",
        multiline: true,
      },
      {
        key: "format",
        label: "Format",
        hint: "Shape of the answer",
        placeholder: "Markdown. Headline, subhead, three bullets.",
      },
      {
        key: "tone",
        label: "Tone",
        hint: "Voice",
        placeholder: "Quiet, precise, never breathless.",
      },
    ],
    assemble: (v) =>
      [
        line("Role", v.role),
        line("Context", v.context),
        line("Action", v.action),
        line("Format", v.format),
        line("Tone", v.tone),
      ]
        .filter(Boolean)
        .join("\n\n"),
  },
  {
    id: "rtf",
    name: "RTF",
    tag: "Fast",
    summary: "Role, Task, Format — the smallest reliable scaffold.",
    fields: [
      {
        key: "role",
        label: "Role",
        hint: "Persona",
        placeholder: "Staff engineer reviewing a pull request.",
      },
      {
        key: "task",
        label: "Task",
        hint: "The work",
        placeholder: "List the three highest-risk issues in this diff summary.",
        multiline: true,
      },
      {
        key: "format",
        label: "Format",
        hint: "Shape",
        placeholder: "Numbered list. Each item: risk, why, suggested fix.",
      },
    ],
    assemble: (v) =>
      [line("Role", v.role), line("Task", v.task), line("Format", v.format)]
        .filter(Boolean)
        .join("\n\n"),
  },
  {
    id: "risen",
    name: "RISEN",
    tag: "Process",
    summary: "Role, Instructions, Steps, End goal, Narrowing.",
    fields: [
      {
        key: "role",
        label: "Role",
        hint: "Persona",
        placeholder: "Research analyst synthesizing primary sources.",
      },
      {
        key: "instructions",
        label: "Instructions",
        hint: "Rules",
        placeholder: "Cite only what is given. Flag uncertainty. No filler.",
        multiline: true,
      },
      {
        key: "steps",
        label: "Steps",
        hint: "Procedure",
        placeholder: "1. Extract claims. 2. Group them. 3. Rank by evidence.",
        multiline: true,
      },
      {
        key: "endGoal",
        label: "End goal",
        hint: "Done when",
        placeholder: "A one-page brief a PM can act on this afternoon.",
      },
      {
        key: "narrowing",
        label: "Narrowing",
        hint: "Constraints",
        placeholder: "No more than 400 words. No bullet dump of raw notes.",
        multiline: true,
      },
    ],
    assemble: (v) =>
      [
        line("Role", v.role),
        line("Instructions", v.instructions),
        line("Steps", v.steps),
        line("End goal", v.endGoal),
        line("Constraints", v.narrowing),
      ]
        .filter(Boolean)
        .join("\n\n"),
  },
  {
    id: "ape",
    name: "APE",
    tag: "Direct",
    summary: "Action, Purpose, Expectation.",
    fields: [
      {
        key: "action",
        label: "Action",
        hint: "Verb the model should do",
        placeholder: "Rewrite this prompt so it is unambiguous and testable.",
        multiline: true,
      },
      {
        key: "purpose",
        label: "Purpose",
        hint: "Why it matters",
        placeholder: "So evals stop drifting when the model changes.",
      },
      {
        key: "expectation",
        label: "Expectation",
        hint: "What good looks like",
        placeholder: "Return only the improved prompt. No preamble.",
        multiline: true,
      },
    ],
    assemble: (v) =>
      [
        line("Action", v.action),
        line("Purpose", v.purpose),
        line("Expectation", v.expectation),
      ]
        .filter(Boolean)
        .join("\n\n"),
  },
  {
    id: "freeform",
    name: "Freeform",
    tag: "Blank",
    summary: "No scaffold. Write the prompt as you would in a playground.",
    fields: [
      {
        key: "body",
        label: "Prompt",
        hint: "Full instruction",
        placeholder: "Write the prompt here…",
        multiline: true,
      },
    ],
    assemble: (v) => (v.body ?? "").trim(),
  },
];

export function getFramework(id: string) {
  return FRAMEWORKS.find((f) => f.id === id) ?? FRAMEWORKS[0];
}
