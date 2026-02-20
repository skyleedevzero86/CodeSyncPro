export type SourceType = "GITLAB" | "GITHUB" | "BITBUCKET";

export type JobStatus =
  | "PENDING"
  | "PROCESSING"
  | "SUCCESS"
  | "PARTIAL_SUCCESS"
  | "FAILED"
  | "CANCELLED"
  | "RETRYING";

export type ItemStatus =
  | "PENDING"
  | "PROCESSING"
  | "SUCCESS"
  | "FAILED"
  | "RETRYING"
  | "SKIPPED";

export type IngestMode = "FULL" | "INCREMENTAL";

export interface SourceConfigDto {
  baseUrl: string;
  accessToken: string;
  projectIds?: number[];
  groupIds?: number[];
  targetBranch?: string;
  shouldIncludeSubgroups?: boolean;
  shouldIncludeArchived?: boolean;
  shouldUseMembershipOnly?: boolean;
  pageSize?: number;
}

export interface FileFiltersDto {
  includeGlobs?: string[];
  excludeDirs?: string[];
  excludeFiles?: string[];
  maxFileSizeBytes?: number;
  skipBinary?: boolean;
}

export interface ConcurrencyConfigDto {
  projects?: number;
  files?: number;
}

export interface JobOptionsDto {
  mode: IngestMode;
  fileFilters: FileFiltersDto;
  concurrency: ConcurrencyConfigDto;
  cleanupAfterIngest?: boolean;
  since?: string | null;
}

export interface CreateJobRequest {
  sourceType: SourceType;
  sourceConfig: SourceConfigDto;
  options: JobOptionsDto;
  callbackUrl?: string | null;
}

export interface CreateJobResponse {
  jobId: string;
  status: JobStatus;
  createdAt: string;
  statusUrl: string;
}

export interface JobProgress {
  totalProjects: number;
  processedProjects: number;
  totalFiles: number;
  processedFiles: number;
  failedFiles: number;
  skippedFiles?: number;
}

export interface ErrorResponse {
  code: string;
  message: string;
  retryable: boolean;
}

export interface JobItemResponse {
  itemId: string;
  projectPath: string;
  filePath: string;
  status: ItemStatus;
  error: ErrorResponse | null;
  retryCount: number;
  nextRetryAt: string | null;
  processedAt: string | null;
}

export interface JobStatusResponse {
  jobId: string;
  status: JobStatus;
  progress: JobProgress;
  startedAt: string | null;
  completedAt: string | null;
  cancelledAt: string | null;
  items: JobItemResponse[];
}

export interface RetryJobResponse {
  jobId: string;
  itemsToRetry: number;
}
