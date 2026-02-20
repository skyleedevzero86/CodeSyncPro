"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Legend,
} from "recharts";
import { fetchStatistics, downloadStatisticsExcel, triggerDownload } from "@/lib/api";
import type { StatisticsResponse } from "@/types/statistics";

const STATUS_LABELS: Record<string, string> = {
  PENDING: "대기",
  PROCESSING: "처리중",
  SUCCESS: "완료",
  PARTIAL_SUCCESS: "일부완료",
  FAILED: "실패",
  CANCELLED: "취소됨",
  RETRYING: "재시도중",
};

const CHART_COLORS = [
  "#3b82f6",
  "#22c55e",
  "#eab308",
  "#ef4444",
  "#8b5cf6",
  "#6b7280",
  "#ec4899",
];

function formatIsoDate(d: Date) {
  return d.toISOString().slice(0, 19) + "Z";
}

function lastMonth(): { from: string; to: string } {
  const to = new Date();
  const from = new Date(to);
  from.setDate(from.getDate() - 30);
  return { from: formatIsoDate(from), to: formatIsoDate(to) };
}

export default function StatisticsPage() {
  const [range, setRange] = useState(lastMonth());
  const [data, setData] = useState<StatisticsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [downloading, setDownloading] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetchStatistics(range.from, range.to);
      setData(res);
    } catch (e) {
      setError(e instanceof Error ? e.message : "통계 조회 실패");
      setData(null);
    } finally {
      setLoading(false);
    }
  }, [range.from, range.to]);

  useEffect(() => {
    load();
  }, [load]);

  const handleExport = async () => {
    setDownloading(true);
    try {
      const blob = await downloadStatisticsExcel(range.from, range.to);
      const fromLabel = range.from.slice(0, 10);
      const toLabel = range.to.slice(0, 10);
      triggerDownload(blob, `ingestion-statistics-${fromLabel}-${toLabel}.xlsx`);
    } catch (e) {
      setError(e instanceof Error ? e.message : "엑셀 다운로드 실패");
    } finally {
      setDownloading(false);
    }
  };

  const statusChartData = data
    ? Object.entries(data.countByStatus)
        .filter(([, v]) => v > 0)
        .map(([name, count]) => ({ name: STATUS_LABELS[name] ?? name, value: count }))
    : [];

  const errorChartData = data
    ? Object.entries(data.errorCodeCounts)
        .filter(([, v]) => v > 0)
        .map(([name, count]) => ({ name, count }))
    : [];

  return (
    <div className="min-h-screen bg-neutral-50 dark:bg-neutral-950">
      <header className="border-b border-neutral-200 bg-white dark:border-neutral-800 dark:bg-neutral-900">
        <div className="mx-auto max-w-5xl px-4 py-4">
          <Link
            href="/"
            className="text-sm text-neutral-500 hover:text-neutral-700 dark:text-neutral-400 dark:hover:text-neutral-300"
          >
            ← 목록
          </Link>
          <div className="mt-2 flex flex-wrap items-center justify-between gap-4">
            <h1 className="text-xl font-semibold text-neutral-900 dark:text-white">
              수집 통계
            </h1>
            <div className="flex flex-wrap items-center gap-3">
              <label className="flex items-center gap-2 text-sm text-neutral-600 dark:text-neutral-400">
                From
                <input
                  type="datetime-local"
                  value={range.from.slice(0, 16)}
                  onChange={(e) =>
                    setRange((r) => ({ ...r, from: formatIsoDate(new Date(e.target.value)) }))
                  }
                  className="rounded border border-neutral-300 bg-white px-2 py-1 dark:border-neutral-600 dark:bg-neutral-800 dark:text-white"
                />
              </label>
              <label className="flex items-center gap-2 text-sm text-neutral-600 dark:text-neutral-400">
                To
                <input
                  type="datetime-local"
                  value={range.to.slice(0, 16)}
                  onChange={(e) =>
                    setRange((r) => ({ ...r, to: formatIsoDate(new Date(e.target.value)) }))
                  }
                  className="rounded border border-neutral-300 bg-white px-2 py-1 dark:border-neutral-600 dark:bg-neutral-800 dark:text-white"
                />
              </label>
              <button
                type="button"
                onClick={load}
                disabled={loading}
                className="rounded-lg bg-neutral-200 px-3 py-1.5 text-sm font-medium text-neutral-800 hover:bg-neutral-300 disabled:opacity-50 dark:bg-neutral-700 dark:text-neutral-200 dark:hover:bg-neutral-600"
              >
                {loading ? "조회 중…" : "조회"}
              </button>
              <button
                type="button"
                onClick={handleExport}
                disabled={downloading || !data}
                className="rounded-lg bg-emerald-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
              >
                {downloading ? "다운로드 중…" : "엑셀 다운로드"}
              </button>
            </div>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-5xl px-4 py-8">
        {error && (
          <div className="mb-4 rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-800 dark:border-red-800 dark:bg-red-900/20 dark:text-red-300">
            {error}
          </div>
        )}

        {loading && !data && (
          <p className="text-center text-neutral-500 dark:text-neutral-400">통계를 불러오는 중…</p>
        )}

        {data && (
          <div className="space-y-8">
            <section className="grid grid-cols-2 gap-4 sm:grid-cols-4">
              <div className="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900">
                <p className="text-xs font-medium uppercase text-neutral-500 dark:text-neutral-400">
                  총 작업 수
                </p>
                <p className="mt-1 text-2xl font-semibold text-neutral-900 dark:text-white">
                  {data.totalJobs}
                </p>
              </div>
              <div className="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900">
                <p className="text-xs font-medium uppercase text-neutral-500 dark:text-neutral-400">
                  처리 파일
                </p>
                <p className="mt-1 text-2xl font-semibold text-neutral-900 dark:text-white">
                  {data.totalProcessedFiles}
                </p>
              </div>
              <div className="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900">
                <p className="text-xs font-medium uppercase text-neutral-500 dark:text-neutral-400">
                  실패 파일
                </p>
                <p className="mt-1 text-2xl font-semibold text-red-600 dark:text-red-400">
                  {data.totalFailedFiles}
                </p>
              </div>
              <div className="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900">
                <p className="text-xs font-medium uppercase text-neutral-500 dark:text-neutral-400">
                  프로젝트
                </p>
                <p className="mt-1 text-2xl font-semibold text-neutral-900 dark:text-white">
                  {data.totalProjects}
                </p>
              </div>
            </section>

            <section className="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900">
              <h2 className="mb-4 text-sm font-medium text-neutral-500 dark:text-neutral-400">
                상태별 작업 수
              </h2>
              {statusChartData.length > 0 ? (
                <div className="h-64">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={statusChartData}
                        dataKey="value"
                        nameKey="name"
                        cx="50%"
                        cy="50%"
                        outerRadius={80}
                        label={({ name, value }) => `${name} ${value}`}
                      >
                        {statusChartData.map((_, i) => (
                          <Cell key={i} fill={CHART_COLORS[i % CHART_COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip />
                      <Legend />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              ) : (
                <p className="text-sm text-neutral-500 dark:text-neutral-400">
                  해당 기간 데이터가 없습니다.
                </p>
              )}
            </section>

            <section className="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900">
              <h2 className="mb-4 text-sm font-medium text-neutral-500 dark:text-neutral-400">
                일별 작업 수
              </h2>
              {data.jobsByDay.length > 0 ? (
                <div className="h-64">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={data.jobsByDay}>
                      <XAxis dataKey="date" tick={{ fontSize: 12 }} />
                      <YAxis allowDecimals={false} />
                      <Tooltip />
                      <Bar dataKey="count" fill="#3b82f6" name="작업 수" radius={[4, 4, 0, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              ) : (
                <p className="text-sm text-neutral-500 dark:text-neutral-400">
                  해당 기간 일별 데이터가 없습니다.
                </p>
              )}
            </section>

            {errorChartData.length > 0 && (
              <section className="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900">
                <h2 className="mb-4 text-sm font-medium text-neutral-500 dark:text-neutral-400">
                  에러 코드별 발생 수 (피드백 참고)
                </h2>
                <div className="h-64">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={errorChartData} layout="vertical" margin={{ left: 80 }}>
                      <XAxis type="number" allowDecimals={false} />
                      <YAxis type="category" dataKey="name" width={70} tick={{ fontSize: 11 }} />
                      <Tooltip />
                      <Bar dataKey="count" fill="#ef4444" name="건수" radius={[0, 4, 4, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </section>
            )}

            <section className="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900">
              <h2 className="mb-4 text-sm font-medium text-neutral-500 dark:text-neutral-400">
                작업 목차 (요약)
              </h2>
              {data.jobSummaries.length > 0 ? (
                <div className="overflow-x-auto">
                  <table className="w-full text-left text-sm">
                    <thead>
                      <tr className="border-b border-neutral-200 dark:border-neutral-700">
                        <th className="py-2 pr-2 font-medium">작업 ID</th>
                        <th className="py-2 pr-2 font-medium">상태</th>
                        <th className="py-2 pr-2 font-medium">생성일시</th>
                        <th className="py-2 pr-2 font-medium">처리/총 파일</th>
                        <th className="py-2 font-medium">실패</th>
                      </tr>
                    </thead>
                    <tbody>
                      {data.jobSummaries.slice(0, 50).map((row) => (
                        <tr
                          key={row.jobId}
                          className="border-b border-neutral-100 dark:border-neutral-800"
                        >
                          <td className="py-2 pr-2 font-mono text-xs">{row.jobId.slice(0, 8)}…</td>
                          <td className="py-2 pr-2">{STATUS_LABELS[row.status] ?? row.status}</td>
                          <td className="py-2 pr-2 text-neutral-600 dark:text-neutral-400">
                            {row.createdAt.slice(0, 19)}
                          </td>
                          <td className="py-2 pr-2">
                            {row.processedFiles} / {row.totalFiles}
                          </td>
                          <td className="py-2 text-red-600 dark:text-red-400">
                            {row.failedFiles}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                  {data.jobSummaries.length > 50 && (
                    <p className="mt-2 text-xs text-neutral-500 dark:text-neutral-400">
                      상위 50건만 표시. 전체는 엑셀 다운로드에서 확인하세요.
                    </p>
                  )}
                </div>
              ) : (
                <p className="text-sm text-neutral-500 dark:text-neutral-400">
                  해당 기간 작업이 없습니다.
                </p>
              )}
            </section>
          </div>
        )}
      </main>
    </div>
  );
}
