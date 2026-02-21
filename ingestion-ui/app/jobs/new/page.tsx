"use client";

import Link from "next/link";
import { useJobForm } from "@/hooks/useJobForm";

export default function NewJobPage() {
  const {
    req,
    submitting,
    error,
    setSourceType,
    setBaseUrl,
    setAccessToken,
    setMode,
    handleSubmit,
  } = useJobForm();

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
          <h1 className="mt-2 text-xl font-semibold text-neutral-900 dark:text-white">
            새 수집 작업 생성
          </h1>
        </div>
      </header>
      <main className="mx-auto max-w-4xl px-4 py-8">
        <form
          onSubmit={handleSubmit}
          className="space-y-6 rounded-xl border border-neutral-200 bg-white p-6 dark:border-neutral-700 dark:bg-neutral-900"
        >
          <div>
            <label className="block text-sm font-medium text-neutral-700 dark:text-neutral-300">
              소스 타입
            </label>
            <select
              value={req.sourceType}
              onChange={(e) => setSourceType(e.target.value as "GITLAB" | "GITHUB" | "BITBUCKET")}
              className="mt-1 w-full rounded-lg border border-neutral-300 bg-white px-3 py-2 dark:border-neutral-600 dark:bg-neutral-800 dark:text-white"
            >
              <option value="GITLAB">GitLab</option>
              <option value="GITHUB">GitHub</option>
              <option value="BITBUCKET">Bitbucket</option>
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-neutral-700 dark:text-neutral-300">
              Base URL
            </label>
            <input
              type="url"
              value={req.sourceConfig.baseUrl}
              onChange={(e) => setBaseUrl(e.target.value)}
              className="mt-1 w-full rounded-lg border border-neutral-300 bg-white px-3 py-2 dark:border-neutral-600 dark:bg-neutral-800 dark:text-white"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-neutral-700 dark:text-neutral-300">
              Access Token
            </label>
            <input
              type="password"
              value={req.sourceConfig.accessToken}
              onChange={(e) => setAccessToken(e.target.value)}
              placeholder="(목업에서는 미연동)"
              className="mt-1 w-full rounded-lg border border-neutral-300 bg-white px-3 py-2 dark:border-neutral-600 dark:bg-neutral-800 dark:text-white"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-neutral-700 dark:text-neutral-300">
              수집 모드
            </label>
            <select
              value={req.options.mode}
              onChange={(e) => setMode(e.target.value as "FULL" | "INCREMENTAL")}
              className="mt-1 w-full rounded-lg border border-neutral-300 bg-white px-3 py-2 dark:border-neutral-600 dark:bg-neutral-800 dark:text-white"
            >
              <option value="FULL">FULL</option>
              <option value="INCREMENTAL">INCREMENTAL</option>
            </select>
          </div>
          {error && (
            <div className="rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-800 dark:border-red-800 dark:bg-red-900/20 dark:text-red-300">
              {error}
            </div>
          )}
          <div className="flex gap-3">
            <button
              type="submit"
              disabled={submitting}
              className="rounded-lg bg-neutral-900 px-4 py-2 text-sm font-medium text-white hover:bg-neutral-800 disabled:opacity-50 dark:bg-neutral-100 dark:text-neutral-900 dark:hover:bg-neutral-200"
            >
              {submitting ? "생성 중…" : "작업 생성"}
            </button>
            <Link
              href="/"
              className="rounded-lg border border-neutral-300 px-4 py-2 text-sm font-medium text-neutral-700 hover:bg-neutral-50 dark:border-neutral-600 dark:text-neutral-300 dark:hover:bg-neutral-800"
            >
              취소
            </Link>
          </div>
        </form>
      </main>
    </div>
  );
}
