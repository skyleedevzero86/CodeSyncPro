import type { StatisticsResponse } from "@/types/statistics";
import type {
  JobStatusResponse,
  CreateJobRequest,
  CreateJobResponse,
  RetryJobResponse,
} from "@/types/job";

const API_BASE =
  typeof window !== "undefined"
    ? (process.env.NEXT_PUBLIC_API_URL || "http://localhost:9080")
    : "";

const defaultFetchOptions: RequestInit = {
  mode: "cors",
  credentials: "omit",
  headers: { "Content-Type": "application/json" },
};

async function handleResponse<T>(res: Response, parse: (r: Response) => Promise<T>): Promise<T> {
  if (!res.ok) {
    const text = await res.text();
    throw new Error(res.status === 404 ? "Not found" : res.statusText + (text ? ": " + text : ""));
  }
  return parse(res);
}

export async function fetchStatistics(
  from: string,
  to: string,
  limit = 10000
): Promise<StatisticsResponse> {
  const url = `${API_BASE}/api/v1/statistics?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&limit=${limit}`;
  const res = await fetch(url, { ...defaultFetchOptions, method: "GET" });
  return handleResponse(res, (r) => r.json());
}

export async function downloadStatisticsExcel(
  from: string,
  to: string,
  limit = 10000
): Promise<Blob> {
  const url = `${API_BASE}/api/v1/statistics/export?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&limit=${limit}`;
  const res = await fetch(url, { ...defaultFetchOptions, method: "GET" });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(res.statusText + (text ? ": " + text : ""));
  }
  return res.blob();
}

export async function fetchJobs(limit = 100, offset = 0): Promise<JobStatusResponse[]> {
  const url = `${API_BASE}/api/v1/jobs?limit=${limit}&offset=${offset}`;
  const res = await fetch(url, { ...defaultFetchOptions, method: "GET" });
  return handleResponse(res, (r) => r.json());
}

export async function fetchJob(jobId: string): Promise<JobStatusResponse> {
  const url = `${API_BASE}/api/v1/jobs/${encodeURIComponent(jobId)}`;
  const res = await fetch(url, { ...defaultFetchOptions, method: "GET" });
  return handleResponse(res, (r) => r.json());
}

export async function createJob(request: CreateJobRequest): Promise<CreateJobResponse> {
  const url = `${API_BASE}/api/v1/jobs`;
  const res = await fetch(url, {
    ...defaultFetchOptions,
    method: "POST",
    body: JSON.stringify(request),
  });
  return handleResponse(res, (r) => r.json());
}

export async function cancelJob(jobId: string): Promise<void> {
  const url = `${API_BASE}/api/v1/jobs/${encodeURIComponent(jobId)}`;
  const res = await fetch(url, { ...defaultFetchOptions, method: "DELETE" });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(res.statusText + (text ? ": " + text : ""));
  }
}

export async function retryFailedItems(
  jobId: string,
  itemIds?: string[] | null
): Promise<RetryJobResponse> {
  const url = `${API_BASE}/api/v1/jobs/${encodeURIComponent(jobId)}/retry`;
  const res = await fetch(url, {
    ...defaultFetchOptions,
    method: "POST",
    body: JSON.stringify({ itemIds: itemIds ?? null }),
  });
  return handleResponse(res, (r) => r.json());
}

export function triggerDownload(blob: Blob, filename: string) {
  const a = document.createElement("a");
  a.href = URL.createObjectURL(blob);
  a.download = filename;
  a.click();
  URL.revokeObjectURL(a.href);
}
