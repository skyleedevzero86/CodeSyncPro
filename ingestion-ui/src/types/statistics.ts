export interface DayCount {
  date: string;
  count: number;
}

export interface JobSummaryRow {
  jobId: string;
  status: string;
  sourceType: string;
  mode: string;
  createdAt: string;
  startedAt: string | null;
  completedAt: string | null;
  totalProjects: number;
  processedProjects: number;
  totalFiles: number;
  processedFiles: number;
  failedFiles: number;
  skippedFiles: number;
  itemCount: number;
}

export interface StatisticsResponse {
  from: string;
  to: string;
  totalJobs: number;
  countByStatus: Record<string, number>;
  jobsByDay: DayCount[];
  totalProcessedFiles: number;
  totalFailedFiles: number;
  totalSkippedFiles: number;
  totalProjects: number;
  jobSummaries: JobSummaryRow[];
  errorCodeCounts: Record<string, number>;
}
