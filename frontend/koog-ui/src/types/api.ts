export interface ChatRequest {
  message: string;
  context?: string;
}

export interface ChatResponse {
  response: string | null;
  timestamp: number;
}

export interface WorkflowRequest {
  task: string;
}

export interface ApiError {
  message: string;
  status?: number;
}