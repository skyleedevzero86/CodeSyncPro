"use client";

import { useEffect, useState } from "react";
import { getJobs } from "@/lib/mock-store";
import type { JobStatusResponse } from "@/types/job";

export function useJobs(): JobStatusResponse[] {
  const [jobs, setJobs] = useState<JobStatusResponse[]>([]);

  useEffect(() => {
    setJobs(getJobs());
    const t = setInterval(() => setJobs(getJobs()), 2000);
    return () => clearInterval(t);
  }, []);

  return jobs;
}
