"use client";

import { useCallback, useEffect, useState } from "react";
import { fetchJob, cancelJob as apiCancelJob, retryFailedItems as apiRetryFailedItems } from "@/lib/api";
import type { JobStatusResponse } from "@/types/job";

const POLL_INTERVAL_MS = 2000;

export function useJob(jobId: string) {
  const [job, setJob] = useState<JobStatusResponse | null>(null);

  useEffect(() => {
    if (!jobId) return;
    let cancelled = false;

    const load = () => {
      fetchJob(jobId)
        .then((data) => {
          if (!cancelled) setJob(data);
        })
        .catch(() => {
          if (!cancelled) setJob(null);
        });
    };

    load();
    const t = setInterval(load, POLL_INTERVAL_MS);
    return () => {
      cancelled = true;
      clearInterval(t);
    };
  }, [jobId]);

  const refresh = useCallback(() => {
    if (!jobId) return;
    fetchJob(jobId).then(setJob).catch(() => setJob(null));
  }, [jobId]);

  const handleCancel = useCallback(async () => {
    if (!job || (job.status !== "PENDING" && job.status !== "PROCESSING")) return;
    try {
      await apiCancelJob(job.jobId);
      const updated = await fetchJob(job.jobId);
      setJob(updated);
    } catch {
      refresh();
    }
  }, [job, refresh]);

  const handleRetry = useCallback(async () => {
    if (!job) return;
    const failedItems = job.items.filter((i) => i.status === "FAILED");
    if (failedItems.length === 0) return;
    try {
      await apiRetryFailedItems(job.jobId);
      const updated = await fetchJob(job.jobId);
      setJob(updated);
    } catch {
      refresh();
    }
  }, [job, refresh]);

  return { job, refresh, handleCancel, handleRetry };
}
