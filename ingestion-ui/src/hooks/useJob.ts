"use client";

import { useCallback, useEffect, useState } from "react";
import { getJobById, cancelJob, retryFailedItems } from "@/lib/mock-store";
import type { JobStatusResponse } from "@/types/job";

export function useJob(jobId: string) {
  const [job, setJob] = useState<JobStatusResponse | null>(null);

  useEffect(() => {
    setJob(getJobById(jobId) ?? null);
    const t = setInterval(() => setJob(getJobById(jobId) ?? null), 2000);
    return () => clearInterval(t);
  }, [jobId]);

  const refresh = useCallback(() => {
    setJob(getJobById(jobId) ?? null);
  }, [jobId]);

  const handleCancel = useCallback(() => {
    if (!job || (job.status !== "PENDING" && job.status !== "PROCESSING")) return;
    cancelJob(job.jobId);
    setJob(getJobById(job.jobId) ?? null);
  }, [job]);

  const handleRetry = useCallback(() => {
    if (!job) return;
    const failedItems = job.items.filter((i) => i.status === "FAILED");
    if (failedItems.length === 0) return;
    retryFailedItems(job.jobId);
    setJob(getJobById(job.jobId) ?? null);
  }, [job]);

  return { job, refresh, handleCancel, handleRetry };
}
