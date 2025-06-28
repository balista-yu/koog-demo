import React, { useState, useEffect } from 'react';
import { Play, Loader2, CheckCircle, AlertCircle } from 'lucide-react';
import { useChat } from '../hooks/useChat';
import { clsx } from 'clsx';

interface WorkflowResult {
  id: string;
  task: string;
  result: string;
  timestamp: number;
  status: 'success' | 'error';
}

const WORKFLOW_EXAMPLES = [
  '今日の東京の天気を調べて、温度に基づいて服装のアドバイスをください',
  '売上データ [1200, 1500, 900, 1800, 2100] を分析して、レポートを生成してください',
  '来週の月曜日の日付を計算して、3日後の日付も教えてください',
  '125 * 8 + 50 / 2 を計算して、結果をレポート形式で出力してください',
];

export const WorkflowInterface: React.FC = () => {
  const [task, setTask] = useState('');
  const [results, setResults] = useState<WorkflowResult[]>([]);
  
  const { processWorkflow, isWorkflowLoading, workflowError, workflowData } = useChat();

  useEffect(() => {
    if (workflowData) {
      const newResult: WorkflowResult = {
        id: Date.now().toString(),
        task: task,
        result: workflowData.response || 'エラーが発生しました',
        timestamp: workflowData.timestamp,
        status: 'success',
      };
      setResults(prev => [newResult, ...prev]);
      setTask('');
    }
  }, [workflowData, task]);

  useEffect(() => {
    if (workflowError) {
      const errorResult: WorkflowResult = {
        id: Date.now().toString(),
        task: task,
        result: `エラー: ${workflowError.message}`,
        timestamp: Date.now(),
        status: 'error',
      };
      setResults(prev => [errorResult, ...prev]);
    }
  }, [workflowError, task]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!task.trim() || isWorkflowLoading) return;

    processWorkflow({ task: task.trim() });
  };

  const handleExampleClick = (example: string) => {
    if (!isWorkflowLoading) {
      setTask(example);
    }
  };

  return (
    <div className="flex flex-col h-full bg-white rounded-lg shadow-sm border border-gray-200">
      {/* Header */}
      <div className="px-6 py-4 border-b border-gray-200">
        <h2 className="text-lg font-semibold text-gray-900">🔧 ワークフロー実行</h2>
        <p className="text-sm text-gray-500">複雑なタスクを段階的に実行します</p>
      </div>

      {/* Input Form */}
      <div className="px-6 py-4 border-b border-gray-200">
        <form onSubmit={handleSubmit} className="space-y-3">
          <textarea
            value={task}
            onChange={(e) => setTask(e.target.value)}
            placeholder="実行したいタスクを詳細に記述してください..."
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent resize-none"
            rows={3}
            disabled={isWorkflowLoading}
          />
          <button
            type="submit"
            disabled={!task.trim() || isWorkflowLoading}
            className={clsx(
              'w-full px-4 py-2 bg-primary-500 text-white rounded-md hover:bg-primary-600 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2 transition-colors',
              'disabled:opacity-50 disabled:cursor-not-allowed',
              'flex items-center justify-center space-x-2'
            )}
          >
            {isWorkflowLoading ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin" />
                <span>実行中...</span>
              </>
            ) : (
              <>
                <Play className="w-4 h-4" />
                <span>ワークフロー実行</span>
              </>
            )}
          </button>
        </form>

        {/* Examples */}
        <div className="mt-4">
          <p className="text-sm font-medium text-gray-700 mb-2">サンプルタスク:</p>
          <div className="space-y-2">
            {WORKFLOW_EXAMPLES.map((example, index) => (
              <button
                key={index}
                onClick={() => handleExampleClick(example)}
                disabled={isWorkflowLoading}
                className="w-full text-left px-3 py-2 text-sm bg-gray-50 hover:bg-gray-100 rounded-md transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {example}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Results */}
      <div className="flex-1 overflow-y-auto px-6 py-4">
        {results.length === 0 && !isWorkflowLoading && (
          <div className="text-center py-8 text-gray-500">
            <p>まだワークフローを実行していません</p>
            <p className="text-sm mt-1">上のサンプルタスクを試してみてください</p>
          </div>
        )}

        <div className="space-y-4">
          {results.map((result) => (
            <div
              key={result.id}
              className={clsx(
                'p-4 rounded-lg border',
                result.status === 'success' && 'bg-green-50 border-green-200',
                result.status === 'error' && 'bg-red-50 border-red-200'
              )}
            >
              <div className="flex items-start space-x-2">
                {result.status === 'success' ? (
                  <CheckCircle className="w-5 h-5 text-green-600 mt-0.5 flex-shrink-0" />
                ) : (
                  <AlertCircle className="w-5 h-5 text-red-600 mt-0.5 flex-shrink-0" />
                )}
                <div className="flex-1 min-w-0">
                  <h4 className="text-sm font-medium text-gray-900 mb-1">
                    タスク: {result.task}
                  </h4>
                  <div className="text-sm text-gray-700 whitespace-pre-wrap">
                    {result.result}
                  </div>
                  <div className="text-xs text-gray-500 mt-2">
                    {new Date(result.timestamp).toLocaleString('ja-JP')}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};