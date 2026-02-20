"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { getJobs } from "@/lib/mock-store";
import type { JobStatusResponse } from "@/types/job";
import { JobCard } from "./JobCard";

export function JobList() {
  const [jobs, setJobs] = useState<JobStatusResponse[]>([]);

  useEffect(() => {
    setJobs(getJobs());
    const t = setInterval(() => setJobs(getJobs()), 2000);
    return () => clearInterval(t);
  }, []);

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold text-neutral-900 dark:text-white">
          수집 작업 목록
        </h2>
        <Link
          href="/jobs/new"
          className="rounded-lg bg-neutral-900 px-4 py-2 text-sm font-medium text-white hover:bg-neutral-800 dark:bg-neutral-100 dark:text-neutral-900 dark:hover:bg-neutral-200"
        >
          새 작업 생성
        </Link>
      </div>
      {jobs.length === 0 ? (
        <p className="rounded-xl border border-dashed border-neutral-300 py-8 text-center text-neutral-500 dark:border-neutral-600 dark:text-neutral-400">
          작업이 없습니다. 새 작업을 생성하세요.
        </p>
      ) : (
        <ul className="space-y-3">
          {jobs.map((job) => (
            <li key={job.jobId}>
              <JobCard job={job} />
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
