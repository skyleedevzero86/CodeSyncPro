"use client";

import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import Link from "next/link";
import { getJobById, cancelJob, retryFailedItems } from "@/lib/mock-store";
import type { JobStatusResponse, JobItemResponse, ItemStatus } from "@/types/job";
import { JobStatusBadge } from "@/components/JobStatusBadge";

const itemStatusLabel: Record<ItemStatus, string> = {
  PENDING: "대기",
  PROCESSING: "처리중",
  SUCCESS: "성공",
  FAILED: "실패",
  RETRYING: "재시도중",
  SKIPPED: "건너뜀",
};

export default function JobDetailPage() {
  const params = useParams();
  const router = useRouter();
  const jobId = params.id as string;
  const [job, setJob] = useState<JobStatusResponse | null>(null);

  useEffect(() => {
    setJob(getJobById(jobId) ?? null);
    const t = setInterval(() => setJob(getJobById(jobId) ?? null), 2000);
    return () => clearInterval(t);
  }, [jobId]);

  if (!job) {
    return (
      <div className="min-h-screen bg-neutral-50 dark:bg-neutral-950 flex items-center justify-center">
        <div className="text-center">
          <p className="text-neutral-500 dark:text-neutral-400">작업을 찾을 수 없습니다.</p>
          <Link href="/" className="mt-2 inline-block text-sm text-blue-600 dark:text-blue-400">
            목록으로
          </Link>
        </div>
      </div>
    );
  }

  const pct =
    job.progress.totalFiles > 0
      ? Math.round((job.progress.processedFiles / job.progress.totalFiles) * 100)
      : 0;
  const failedItems = job.items.filter((i) => i.status === "FAILED");
  const canCancel = job.status === "PENDING" || job.status === "PROCESSING";
  const canRetry = failedItems.length > 0;

  const handleCancel = () => {
    if (!canCancel) return;
    cancelJob(job.jobId);
    setJob(getJobById(job.jobId) ?? null);
  };

  const handleRetry = () => {
    if (!canRetry) return;
    retryFailedItems(job.jobId);
    setJob(getJobById(job.jobId) ?? null);
  };

  return (
    <div className="min-h-screen bg-neutral-50 dark:bg-neutral-950">
      <header className="border-b border-neutral-200 bg-white dark:border-neutral-800 dark:bg-neutral-900">
        <div className="mx-auto max-w-4xl px-4 py-4">
          <Link
            href="/"
            className="text-sm text-neutral-500 hover:text-neutral-700 dark:text-neutral-400 dark:hover:text-neutral-300"
          >
            ← 목록
          </Link>
          <div className="mt-2 flex items-center gap-3">
            <h1 className="font-mono text-lg font-semibold text-neutral-900 dark:text-white">
              {job.jobId}
            </h1>
            <JobStatusBadge status={job.status} />
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-4xl px-4 py-8 space-y-6">
        <section className="rounded-xl border border-neutral-200 bg-white p-6 dark:border-neutral-700 dark:bg-neutral-900">
          <h2 className="text-sm font-medium text-neutral-500 dark:text-neutral-400">진행률</h2>
          <div className="mt-2 grid grid-cols-2 gap-4 sm:grid-cols-4">
            <div>
              <p className="text-2xl font-semibold text-neutral-900 dark:text-white">
                {job.progress.processedProjects}/{job.progress.totalProjects || "—"}
              </p>
              <p className="text-xs text-neutral-500 dark:text-neutral-400">프로젝트</p>
            </div>
            <div>
              <p className="text-2xl font-semibold text-neutral-900 dark:text-white">
                {job.progress.processedFiles}/{job.progress.totalFiles || "—"}
              </p>
              <p className="text-xs text-neutral-500 dark:text-neutral-400">파일</p>
            </div>
            <div>
              <p className="text-2xl font-semibold text-red-600 dark:text-red-400">
                {job.progress.failedFiles}
              </p>
              <p className="text-xs text-neutral-500 dark:text-neutral-400">실패</p>
            </div>
            <div>
              <p className="text-2xl font-semibold text-neutral-900 dark:text-white">{pct}%</p>
              <p className="text-xs text-neutral-500 dark:text-neutral-400">완료율</p>
            </div>
          </div>
          {(job.status === "PROCESSING" || job.status === "RETRYING") && (
            <div className="mt-3 h-2 w-full overflow-hidden rounded-full bg-neutral-100 dark:bg-neutral-800">
              <div
                className="h-full rounded-full bg-blue-500 transition-all"
                style={{ width: `${pct}%` }}
              />
            </div>
          )}
        </section>

        <section className="rounded-xl border border-neutral-200 bg-white p-6 dark:border-neutral-700 dark:bg-neutral-900">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-medium text-neutral-500 dark:text-neutral-400">
              작업 항목 ({job.items.length})
            </h2>
            <div className="flex gap-2">
              {canCancel && (
                <button
                  type="button"
                  onClick={handleCancel}
                  className="rounded-lg border border-red-200 bg-white px-3 py-1.5 text-sm font-medium text-red-700 hover:bg-red-50 dark:border-red-800 dark:bg-neutral-900 dark:text-red-400 dark:hover:bg-red-900/20"
                >
                  작업 취소
                </button>
              )}
              {canRetry && (
                <button
                  type="button"
                  onClick={handleRetry}
                  className="rounded-lg border border-blue-200 bg-white px-3 py-1.5 text-sm font-medium text-blue-700 hover:bg-blue-50 dark:border-blue-800 dark:bg-neutral-900 dark:text-blue-400 dark:hover:bg-blue-900/20"
                >
                  실패 항목 재시도
                </button>
              )}
            </div>
          </div>
          {job.items.length === 0 ? (
            <p className="mt-4 text-sm text-neutral-500 dark:text-neutral-400">
              항목이 없거나 아직 스캔되지 않았습니다.
            </p>
          ) : (
            <div className="mt-4 overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-neutral-200 dark:border-neutral-700">
                    <th className="py-2 text-left font-medium text-neutral-700 dark:text-neutral-300">
                      프로젝트 / 파일
                    </th>
                    <th className="py-2 text-left font-medium text-neutral-700 dark:text-neutral-300">
                      상태
                    </th>
                    <th className="py-2 text-left font-medium text-neutral-700 dark:text-neutral-300">
                      에러
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {job.items.map((item: JobItemResponse) => (
                    <tr
                      key={item.itemId}
                      className="border-b border-neutral-100 dark:border-neutral-800"
                    >
                      <td className="py-2">
                        <span className="font-medium text-neutral-900 dark:text-white">
                          {item.projectPath}
                        </span>
                        <span className="text-neutral-500 dark:text-neutral-400"> / </span>
                        <span className="text-neutral-600 dark:text-neutral-300">
                          {item.filePath}
                        </span>
                      </td>
                      <td className="py-2">
                        <span
                          className={
                            item.status === "FAILED"
                              ? "text-red-600 dark:text-red-400"
                              : item.status === "SUCCESS"
                                ? "text-emerald-600 dark:text-emerald-400"
                                : "text-neutral-600 dark:text-neutral-400"
                          }
                        >
                          {itemStatusLabel[item.status]}
                        </span>
                        {item.retryCount > 0 && (
                          <span className="ml-1 text-xs text-neutral-400">
                            (재시도 {item.retryCount})
                          </span>
                        )}
                      </td>
                      <td className="py-2 text-neutral-500 dark:text-neutral-400">
                        {item.error ? (
                          <span className="text-red-600 dark:text-red-400" title={item.error.message}>
                            {item.error.code}: {item.error.message.slice(0, 40)}…
                          </span>
                        ) : (
                          "—"
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </main>
    </div>
  );
}
