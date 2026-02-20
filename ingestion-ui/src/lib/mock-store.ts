"use client";

import type {
  JobStatusResponse,
  CreateJobRequest,
  CreateJobResponse,
  JobStatus,
  JobProgress,
  JobItemResponse,
  ItemStatus,
} from "@/types/job";

const initialJobs: JobStatusResponse[] = [
  {
    jobId: "a1b2c3d4-0001-4000-8000-000000000001",
    status: "SUCCESS",
    progress: {
      totalProjects: 3,
      processedProjects: 3,
      totalFiles: 42,
      processedFiles: 42,
      failedFiles: 0,
      skippedFiles: 2,
    },
    startedAt: "2025-02-09T10:00:00Z",
    completedAt: "2025-02-09T10:05:30Z",
    cancelledAt: null,
    items: [],
  },
  {
    jobId: "a1b2c3d4-0002-4000-8000-000000000002",
    status: "PROCESSING",
    progress: {
      totalProjects: 5,
      processedProjects: 2,
      totalFiles: 80,
      processedFiles: 28,
      failedFiles: 1,
      skippedFiles: 0,
    },
    startedAt: "2025-02-09T11:00:00Z",
    completedAt: null,
    cancelledAt: null,
    items: [
      {
        itemId: "item-1",
        projectPath: "group/repo-a",
        filePath: "src/main.kt",
        status: "SUCCESS",
        error: null,
        retryCount: 0,
        nextRetryAt: null,
        processedAt: "2025-02-09T11:01:00Z",
      },
      {
        itemId: "item-2",
        projectPath: "group/repo-b",
        filePath: "docs/readme.md",
        status: "FAILED",
        error: {
          code: "NETWORK_ERROR",
          message: "Connection timeout",
          retryable: true,
        },
        retryCount: 1,
        nextRetryAt: "2025-02-09T11:10:00Z",
        processedAt: null,
      },
    ],
  },
  {
    jobId: "a1b2c3d4-0003-4000-8000-000000000003",
    status: "PENDING",
    progress: {
      totalProjects: 0,
      processedProjects: 0,
      totalFiles: 0,
      processedFiles: 0,
      failedFiles: 0,
      skippedFiles: 0,
    },
    startedAt: null,
    completedAt: null,
    cancelledAt: null,
    items: [],
  },
];

let jobs: JobStatusResponse[] = [...initialJobs];

export function getJobs(): JobStatusResponse[] {
  return [...jobs];
}

export function getJobById(jobId: string): JobStatusResponse | undefined {
  return jobs.find((j) => j.jobId === jobId);
}

export function createJob(request: CreateJobRequest): CreateJobResponse {
  const jobId = crypto.randomUUID();
  const now = new Date().toISOString();
  const newJob: JobStatusResponse = {
    jobId,
    status: "PENDING",
    progress: {
      totalProjects: 0,
      processedProjects: 0,
      totalFiles: 0,
      processedFiles: 0,
      failedFiles: 0,
      skippedFiles: 0,
    },
    startedAt: null,
    completedAt: null,
    cancelledAt: null,
    items: [],
  };
  jobs = [newJob, ...jobs];
  return {
    jobId,
    status: "PENDING",
    createdAt: now,
    statusUrl: `/jobs/${jobId}`,
  };
}

export function cancelJob(jobId: string): boolean {
  const idx = jobs.findIndex((j) => j.jobId === jobId);
  if (idx === -1) return false;
  const job = jobs[idx];
  if (job.status !== "PENDING" && job.status !== "PROCESSING") return false;
  const now = new Date().toISOString();
  jobs = jobs.map((j) =>
    j.jobId === jobId
      ? { ...j, status: "CANCELLED" as JobStatus, cancelledAt: now }
      : j
  );
  return true;
}

export function retryFailedItems(
  jobId: string,
  itemIds?: string[] | null
): { jobId: string; itemsToRetry: number } | null {
  const idx = jobs.findIndex((j) => j.jobId === jobId);
  if (idx === -1) return null;
  const job = jobs[idx];
  const failed = job.items.filter((i) => i.status === "FAILED");
  const toRetry =
    itemIds && itemIds.length > 0
      ? failed.filter((i) => itemIds.includes(i.itemId))
      : failed;
  if (toRetry.length === 0) return null;
  const retryingItems: JobItemResponse[] = job.items.map((item) => {
    const match = toRetry.find((r) => r.itemId === item.itemId);
    if (!match) return item;
    return {
      ...item,
      status: "RETRYING" as ItemStatus,
      nextRetryAt: new Date(Date.now() + 60_000).toISOString(),
    };
  });
  jobs = jobs.map((j) =>
    j.jobId === jobId ? { ...j, status: "RETRYING" as JobStatus, items: retryingItems } : j
  );
  return { jobId, itemsToRetry: toRetry.length };
}

export function resetMockStore(): void {
  jobs = [...initialJobs];
}
