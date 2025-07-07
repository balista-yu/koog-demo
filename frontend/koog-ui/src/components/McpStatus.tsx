import React, { useState, useEffect } from 'react';
import { CheckCircle, XCircle, AlertCircle, Loader2, Wrench, Database } from 'lucide-react';
import { mcpApi } from '../services/api';
import { clsx } from 'clsx';

interface McpTool {
  name: string;
  description: string;
}

interface McpResource {
  uri: string;
  name: string;
  description?: string;
}

interface McpStatusData {
  status: 'active' | 'error' | 'loading';
  tools?: McpTool[];
  resources?: McpResource[];
  message?: string;
}

export const McpStatus: React.FC = () => {
  const [statusData, setStatusData] = useState<McpStatusData>({ status: 'loading' });
  const [lastChecked, setLastChecked] = useState<Date | null>(null);

  const checkMcpStatus = async () => {
    try {
      setStatusData({ status: 'loading' });
      const response = await mcpApi.getMcpStatus();
      setStatusData(response);
      setLastChecked(new Date());
    } catch (error) {
      setStatusData({
        status: 'error',
        message: error instanceof Error ? error.message : 'Failed to connect to MCP server'
      });
      setLastChecked(new Date());
    }
  };

  useEffect(() => {
    checkMcpStatus();
    // Check status every 30 seconds
    const interval = setInterval(checkMcpStatus, 30000);
    return () => clearInterval(interval);
  }, []);

  const getStatusIcon = () => {
    switch (statusData.status) {
      case 'active':
        return <CheckCircle className="w-5 h-5 text-green-500" />;
      case 'error':
        return <XCircle className="w-5 h-5 text-red-500" />;
      case 'loading':
        return <Loader2 className="w-5 h-5 text-blue-500 animate-spin" />;
      default:
        return <AlertCircle className="w-5 h-5 text-yellow-500" />;
    }
  };

  const getStatusText = () => {
    switch (statusData.status) {
      case 'active':
        return 'MCP Server Active';
      case 'error':
        return 'MCP Server Error';
      case 'loading':
        return 'Checking MCP Server...';
      default:
        return 'MCP Server Unknown';
    }
  };

  const getStatusColor = () => {
    switch (statusData.status) {
      case 'active':
        return 'bg-green-50 border-green-200';
      case 'error':
        return 'bg-red-50 border-red-200';
      case 'loading':
        return 'bg-blue-50 border-blue-200';
      default:
        return 'bg-yellow-50 border-yellow-200';
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200">
      {/* Header */}
      <div className="px-6 py-4 border-b border-gray-200">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <h3 className="text-lg font-semibold text-gray-900">🔌 MCP Status</h3>
            {getStatusIcon()}
          </div>
          <button
            onClick={checkMcpStatus}
            disabled={statusData.status === 'loading'}
            className="px-3 py-1 text-xs bg-gray-100 hover:bg-gray-200 rounded-md transition-colors disabled:opacity-50"
          >
            Refresh
          </button>
        </div>
        <p className="text-sm text-gray-500 mt-1">Model Context Protocol integration status</p>
      </div>

      {/* Status Display */}
      <div className="px-6 py-4">
        <div className={clsx('p-4 rounded-lg border', getStatusColor())}>
          <div className="flex items-center space-x-2">
            {getStatusIcon()}
            <span className="font-medium">{getStatusText()}</span>
          </div>
          
          {statusData.message && (
            <p className="text-sm text-gray-600 mt-2">{statusData.message}</p>
          )}
          
          {lastChecked && (
            <p className="text-xs text-gray-500 mt-2">
              Last checked: {lastChecked.toLocaleTimeString('ja-JP')}
            </p>
          )}
        </div>

        {/* Tools Section */}
        {statusData.tools && statusData.tools.length > 0 && (
          <div className="mt-6">
            <div className="flex items-center space-x-2 mb-3">
              <Wrench className="w-4 h-4 text-gray-600" />
              <h4 className="font-medium text-gray-900">Available Tools</h4>
              <span className="text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded-full">
                {statusData.tools.length}
              </span>
            </div>
            <div className="space-y-2">
              {statusData.tools.map((tool, index) => (
                <div key={index} className="p-3 bg-gray-50 rounded-md">
                  <div className="font-medium text-sm text-gray-900">{tool.name}</div>
                  <div className="text-xs text-gray-600 mt-1">{tool.description}</div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Resources Section */}
        {statusData.resources && statusData.resources.length > 0 && (
          <div className="mt-6">
            <div className="flex items-center space-x-2 mb-3">
              <Database className="w-4 h-4 text-gray-600" />
              <h4 className="font-medium text-gray-900">Available Resources</h4>
              <span className="text-xs bg-green-100 text-green-800 px-2 py-1 rounded-full">
                {statusData.resources.length}
              </span>
            </div>
            <div className="space-y-2">
              {statusData.resources.map((resource, index) => (
                <div key={index} className="p-3 bg-gray-50 rounded-md">
                  <div className="font-medium text-sm text-gray-900">{resource.name}</div>
                  <div className="text-xs text-gray-600 mt-1">{resource.uri}</div>
                  {resource.description && (
                    <div className="text-xs text-gray-500 mt-1">{resource.description}</div>
                  )}
                </div>
              ))}
            </div>
          </div>
        )}

        {/* MCP Features */}
        <div className="mt-6 p-4 bg-blue-50 rounded-lg">
          <h4 className="font-medium text-blue-900 mb-2">💡 MCP Features</h4>
          <ul className="text-sm text-blue-800 space-y-1">
            <li>• チャットで「天気 東京」と入力すると MCP 経由で天気を取得</li>
            <li>• チャットで「計算 2+2」と入力すると MCP 経由で計算を実行</li>
            <li>• `/chat-with-mcp` エンドポイントで高度な MCP 統合機能を利用</li>
          </ul>
        </div>
      </div>
    </div>
  );
};