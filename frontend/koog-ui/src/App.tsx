import React, { useState } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MessageSquare, Settings, Bot, Cable } from 'lucide-react';
import { ChatInterface } from './components/ChatInterface';
import { WorkflowInterface } from './components/WorkflowInterface';
import { McpStatus } from './components/McpStatus';
import { clsx } from 'clsx';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5 minutes
      refetchOnWindowFocus: false,
    },
  },
});

type Tab = 'chat' | 'workflow' | 'mcp';

function App() {
  const [activeTab, setActiveTab] = useState<Tab>('chat');

  const tabs = [
    { id: 'chat' as Tab, label: 'チャット', icon: MessageSquare },
    { id: 'workflow' as Tab, label: 'ワークフロー', icon: Settings },
    { id: 'mcp' as Tab, label: 'MCP Status', icon: Cable },
  ];

  return (
    <QueryClientProvider client={queryClient}>
      <div className="min-h-screen bg-gray-50">
        {/* Header */}
        <header className="bg-white shadow-sm border-b border-gray-200">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex items-center justify-between h-16">
              <div className="flex items-center space-x-3">
                <Bot className="w-8 h-8 text-primary-600" />
                <div>
                  <h1 className="text-xl font-semibold text-gray-900">
                    Koog Demo Dashboard
                  </h1>
                  <p className="text-sm text-gray-500">AI エージェント管理ダッシュボード</p>
                </div>
              </div>
              <div className="flex items-center space-x-2 text-sm text-gray-500">
                <div className="w-2 h-2 bg-green-400 rounded-full"></div>
                <span>APIサーバー接続中</span>
              </div>
            </div>
          </div>
        </header>

        {/* Navigation Tabs */}
        <nav className="bg-white border-b border-gray-200">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex space-x-8">
              {tabs.map((tab) => {
                const Icon = tab.icon;
                return (
                  <button
                    key={tab.id}
                    onClick={() => setActiveTab(tab.id)}
                    className={clsx(
                      'flex items-center space-x-2 py-4 px-1 border-b-2 font-medium text-sm transition-colors',
                      activeTab === tab.id
                        ? 'border-primary-500 text-primary-600'
                        : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                    )}
                  >
                    <Icon className="w-4 h-4" />
                    <span>{tab.label}</span>
                  </button>
                );
              })}
            </div>
          </div>
        </nav>

        {/* Main Content */}
        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="h-[calc(100vh-12rem)]">
            {activeTab === 'chat' && <ChatInterface />}
            {activeTab === 'workflow' && <WorkflowInterface />}
            {activeTab === 'mcp' && <McpStatus />}
          </div>
        </main>
      </div>
    </QueryClientProvider>
  );
}

export default App;
