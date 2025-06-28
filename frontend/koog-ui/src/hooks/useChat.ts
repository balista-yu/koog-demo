import { useMutation, useQueryClient } from '@tanstack/react-query';
import { chatApi, workflowApi } from '../services/api';
import { ChatRequest, WorkflowRequest } from '../types/api';

export const useChat = () => {
  const queryClient = useQueryClient();

  const chatMutation = useMutation({
    mutationFn: chatApi.sendMessage,
    onSuccess: () => {
      // Invalidate and refetch any related queries
      queryClient.invalidateQueries({ queryKey: ['chat-history'] });
    },
    onError: (error) => {
      console.error('Chat error:', error);
    },
  });

  const workflowMutation = useMutation({
    mutationFn: workflowApi.processWorkflow,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workflow-history'] });
    },
    onError: (error) => {
      console.error('Workflow error:', error);
    },
  });

  return {
    sendMessage: (request: ChatRequest) => chatMutation.mutate(request),
    processWorkflow: (request: WorkflowRequest) => workflowMutation.mutate(request),
    isChatLoading: chatMutation.isPending,
    isWorkflowLoading: workflowMutation.isPending,
    chatError: chatMutation.error,
    workflowError: workflowMutation.error,
    chatData: chatMutation.data,
    workflowData: workflowMutation.data,
  };
};