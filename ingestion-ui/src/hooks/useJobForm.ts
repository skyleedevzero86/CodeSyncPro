"use client";

import { useState, useCallback } from "react";
import { useRouter } from "next/navigation";
import { createJob } from "@/lib/api";
import type { CreateJobRequest, SourceType, IngestMode } from "@/types/job";

const defaultRequest: CreateJobRequest = {
  sourceType: "GITLAB",
  sourceConfig: {
    baseUrl: "https://gitlab.com",
    accessToken: "",
    targetBranch: "main",
    shouldIncludeSubgroups: true,
    shouldIncludeArchived: false,
    shouldUseMembershipOnly: true,
    pageSize: 100,
  },
  options: {
    mode: "FULL",
    fileFilters: {
      includeGlobs: [],
      excludeDirs: [],
      excludeFiles: [],
      maxFileSizeBytes: 5_000_000,
      skipBinary: true,
    },
    concurrency: { projects: 2, files: 8 },
    cleanupAfterIngest: true,
  },
};

export function useJobForm() {
  const router = useRouter();
  const [req, setReq] = useState<CreateJobRequest>(defaultRequest);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const setSourceType = useCallback((sourceType: SourceType) => {
    setReq((r) => ({ ...r, sourceType }));
  }, []);
  const setBaseUrl = useCallback((baseUrl: string) => {
    setReq((r) => ({ ...r, sourceConfig: { ...r.sourceConfig, baseUrl } }));
  }, []);
  const setAccessToken = useCallback((accessToken: string) => {
    setReq((r) => ({ ...r, sourceConfig: { ...r.sourceConfig, accessToken } }));
  }, []);
  const setMode = useCallback((mode: IngestMode) => {
    setReq((r) => ({ ...r, options: { ...r.options, mode } }));
  }, []);

  const handleSubmit = useCallback(
    async (e: React.FormEvent) => {
      e.preventDefault();
      setSubmitting(true);
      setError(null);
      try {
        const res = await createJob(req);
        router.push(`/jobs/${res.jobId}`);
      } catch (err) {
        setError(err instanceof Error ? err.message : "작업 생성에 실패했습니다.");
      } finally {
        setSubmitting(false);
      }
    },
    [req, router]
  );

  return {
    req,
    submitting,
    error,
    setSourceType,
    setBaseUrl,
    setAccessToken,
    setMode,
    handleSubmit,
  };
}
