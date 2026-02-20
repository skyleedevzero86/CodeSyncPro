"use client";

import type { JobStatus } from "@/types/job";

const statusStyles: Record<
  JobStatus,
  { bg: string; text: string; label: string }
> = {
  PENDING: { bg: "bg-slate-100 dark:bg-slate-800", text: "text-slate-700 dark:text-slate-300", label: "대기" },
  PROCESSING: { bg: "bg-blue-100 dark:bg-blue-900/40", text: "text-blue-800 dark:text-blue-200", label: "처리중" },
  SUCCESS: { bg: "bg-emerald-100 dark:bg-emerald-900/40", text: "text-emerald-800 dark:text-emerald-200", label: "완료" },
  PARTIAL_SUCCESS: { bg: "bg-amber-100 dark:bg-amber-900/40", text: "text-amber-800 dark:text-amber-200", label: "일부완료" },
  FAILED: { bg: "bg-red-100 dark:bg-red-900/40", text: "text-red-800 dark:text-red-200", label: "실패" },
  CANCELLED: { bg: "bg-neutral-100 dark:bg-neutral-800", text: "text-neutral-600 dark:text-neutral-400", label: "취소됨" },
  RETRYING: { bg: "bg-violet-100 dark:bg-violet-900/40", text: "text-violet-800 dark:text-violet-200", label: "재시도중" },
};

export function JobStatusBadge({ status }: { status: JobStatus }) {
  const s = statusStyles[status] ?? statusStyles.PENDING;
  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${s.bg} ${s.text}`}
    >
      {s.label}
    </span>
  );
}
