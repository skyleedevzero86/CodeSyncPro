"use client";

import { useCallback, useEffect, useState } from "react";
import { fetchStatistics, downloadStatisticsExcel, triggerDownload } from "@/lib/api";
import type { StatisticsResponse } from "@/types/statistics";

function formatIsoDate(d: Date) {
  return d.toISOString().slice(0, 19) + "Z";
}

function lastMonth(): { from: string; to: string } {
  const to = new Date();
  const from = new Date(to);
  from.setDate(from.getDate() - 30);
  return { from: formatIsoDate(from), to: formatIsoDate(to) };
}

export function useStatistics() {
  const [range, setRange] = useState(lastMonth);
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

  const setRangeFrom = useCallback((from: string) => {
    setRange((r) => ({ ...r, from }));
  }, []);
  const setRangeTo = useCallback((to: string) => {
    setRange((r) => ({ ...r, to }));
  }, []);

  const handleExport = useCallback(async () => {
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
  }, [range.from, range.to]);

  return {
    range,
    setRangeFrom,
    setRangeTo,
    data,
    loading,
    error,
    downloading,
    load,
    handleExport,
    formatIsoDate,
  };
}
