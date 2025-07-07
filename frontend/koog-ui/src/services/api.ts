import axios from 'axios';
import { ChatRequest, ChatResponse, WorkflowRequest } from '../types/api';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/koog';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000, // 30 seconds
});

// Request interceptor for logging
apiClient.interceptors.request.use(
  (config) => {
    console.log(`🚀 API Request: ${config.method?.toUpperCase()} ${config.url}`, config.data);
    return config;
  },
  (error) => {
    console.error('❌ API Request Error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor for logging and error handling
apiClient.interceptors.response.use(
  (response) => {
    console.log(`✅ API Response: ${response.status}`, response.data);
    return response;
  },
  (error) => {
    console.error('❌ API Response Error:', error.response?.data || error.message);
    return Promise.reject(error);
  }
);

export const chatApi = {
  sendMessage: async (request: ChatRequest): Promise<ChatResponse> => {
    const response = await apiClient.post<ChatResponse>('/chat', request);
    return response.data;
  },
};

export const workflowApi = {
  processWorkflow: async (request: WorkflowRequest): Promise<ChatResponse> => {
    const response = await apiClient.post<ChatResponse>('/workflow', request);
    return response.data;
  },
};

export const mcpApi = {
  chatWithMcp: async (request: ChatRequest): Promise<ChatResponse> => {
    const response = await apiClient.post<ChatResponse>('/chat-with-mcp', request);
    return response.data;
  },
  
  getMcpStatus: async () => {
    const response = await apiClient.get('/mcp-status');
    return response.data;
  },
  
  getTools: async () => {
    const response = await apiClient.get('/api/mcp/tools');
    return response.data;
  },
  
  getResources: async () => {
    const response = await apiClient.get('/api/mcp/resources');
    return response.data;
  },
  
  callTool: async (toolName: string, toolArgs: Record<string, any>) => {
    const response = await apiClient.post(`/api/mcp/tools/${toolName}`, toolArgs);
    return response.data;
  },
};

export default apiClient;