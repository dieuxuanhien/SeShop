export type ApiMeta = {
  traceId: string;
  timestamp?: string;
};

export type ApiResponse<T> = {
  data: T;
  meta: ApiMeta;
};

export type ApiErrorDetail = {
  field?: string;
  reason: string;
};

export type ApiError = {
  code: string;
  message: string;
  details?: ApiErrorDetail[];
  traceId?: string;
};

export type PageResponse<T> = {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};
