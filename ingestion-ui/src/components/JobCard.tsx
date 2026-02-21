"use client";

import Link from "next/link";
import type { JobStatusResponse } from "@/types/job";
import { JobStatusBadge } from "./JobStatusBadge";

export function JobCard({ job }: { job: JobStatusResponse }) {
  const pct =
    job.progress.totalFiles > 0
      ? Math.round((job.progress.processedFiles / job.progress.totalFiles) * 100)
      : 0;

  return (
    <Link
      href={`/jobs/${job.jobId}`}
      className="block rounded-xl border border-neutral-200 bg-white p-4 shadow-sm transition hover:border-neutral-300 hover:shadow dark:border-neutral-700 dark:bg-neutral-900 dark:hover:border-neutral-600"
    >
      <div className="flex items-center justify-between gap-2">
        <span className="font-mono text-sm text-neutral-500 dark:text-neutral-400">
          {job.jobId.slice(0, 8)}…
        </span>
        <JobStatusBadge status={job.status} />
      </div>
      <div className="mt-2 text-sm text-neutral-600 dark:text-neutral-300">
        프로젝트 {job.progress.processedProjects}/{job.progress.totalProjects || "—"} · 파일{" "}
        {job.progress.processedFiles}/{job.progress.totalFiles || "—"}
        {job.progress.failedFiles > 0 && (
          <span className="text-red-600 dark:text-red-400"> · 실패 {job.progress.failedFiles}</span>
        )}
      </div>
      {job.status === "PROCESSING" || job.status === "RETRYING" ? (
        <div className="mt-2 h-1.5 w-full overflow-hidden rounded-full bg-neutral-100 dark:bg-neutral-800">
          <div
            className="h-full rounded-full bg-blue-500 transition-all"
            style={{ width: `${pct}%` }}
          />
        </div>
      ) : null}
    </Link>
  );
}
