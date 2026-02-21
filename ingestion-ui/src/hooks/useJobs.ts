"use client";

import { useEffect, useState } from "react";
import { fetchJobs } from "@/lib/api";
import type { JobStatusResponse } from "@/types/job";

const POLL_INTERVAL_MS = 3000;

export function useJobs(): JobStatusResponse[] {
  const [jobs, setJobs] = useState<JobStatusResponse[]>([]);

  useEffect(() => {
    let cancelled = false;

    const load = () => {
      fetchJobs(100, 0)
        .then((list) => {
          if (!cancelled) setJobs(list);
        })
        .catch(() => {
          if (!cancelled) setJobs([]);
        });
    };

    load();
    const t = setInterval(load, POLL_INTERVAL_MS);
    return () => {
      cancelled = true;
      clearInterval(t);
    };
  }, []);

  return jobs;
}
