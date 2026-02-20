import type { StatisticsResponse } from "@/types/statistics";

const API_BASE =
  typeof window !== "undefined"
    ? (process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080")
    : "";

export async function fetchStatistics(
  from: string,
  to: string,
  limit = 10000
): Promise<StatisticsResponse> {
  const url = `${API_BASE}/api/v1/statistics?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&limit=${limit}`;
  const res = await fetch(url);
  if (!res.ok) {
    const text = await res.text();
    throw new Error(res.statusText + (text ? ": " + text : ""));
  }
  return res.json();
}

export async function downloadStatisticsExcel(
  from: string,
  to: string,
  limit = 10000
): Promise<Blob> {
  const url = `${API_BASE}/api/v1/statistics/export?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&limit=${limit}`;
  const res = await fetch(url);
  if (!res.ok) {
    const text = await res.text();
    throw new Error(res.statusText + (text ? ": " + text : ""));
  }
  return res.blob();
}

export function triggerDownload(blob: Blob, filename: string) {
  const a = document.createElement("a");
  a.href = URL.createObjectURL(blob);
  a.download = filename;
  a.click();
  URL.revokeObjectURL(a.href);
}
