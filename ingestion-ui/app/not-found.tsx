import Link from "next/link";

export default function NotFound() {
  return (
    <div className="min-h-screen bg-neutral-50 dark:bg-neutral-950 flex items-center justify-center px-4">
      <div className="text-center">
        <h1 className="text-2xl font-semibold text-neutral-900 dark:text-white">
          페이지를 찾을 수 없습니다
        </h1>
        <p className="mt-2 text-neutral-500 dark:text-neutral-400">
          요청하신 경로가 존재하지 않습니다.
        </p>
        <Link
          href="/"
          className="mt-6 inline-block rounded-lg bg-neutral-900 px-4 py-2 text-sm font-medium text-white hover:bg-neutral-800 dark:bg-neutral-100 dark:text-neutral-900 dark:hover:bg-neutral-200"
        >
          홈으로
        </Link>
      </div>
    </div>
  );
}
