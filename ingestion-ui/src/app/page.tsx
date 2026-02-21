import Link from "next/link";
import { JobList } from "@/components/JobList";

export const dynamic = "force-static";

export default function Home() {
  return (
    <div className="min-h-screen bg-neutral-50 dark:bg-neutral-950">
      <header className="border-b border-neutral-200 bg-white dark:border-neutral-800 dark:bg-neutral-900">
        <div className="mx-auto max-w-4xl px-4 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-xl font-semibold text-neutral-900 dark:text-white">
                Code Ingestion Service
              </h1>
              <p className="mt-0.5 text-sm text-neutral-500 dark:text-neutral-400">
                수집 작업 관리 (연동 전 목업)
              </p>
            </div>
            <Link
              href="/statistics"
              className="rounded-lg border border-neutral-300 px-3 py-1.5 text-sm font-medium text-neutral-700 hover:bg-neutral-50 dark:border-neutral-600 dark:text-neutral-300 dark:hover:bg-neutral-800"
            >
              통계
            </Link>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-4xl px-4 py-8">
        <JobList />
      </main>
    </div>
  );
}
