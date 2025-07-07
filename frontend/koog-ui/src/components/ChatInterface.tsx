import React, { useState, useRef, useEffect } from 'react';
import { Send, Loader2, AlertCircle, Cable } from 'lucide-react';
import { useChat } from '../hooks/useChat';
import { mcpApi } from '../services/api';
import { clsx } from 'clsx';

interface Message {
  id: string;
  type: 'user' | 'assistant' | 'error';
  content: string;
  timestamp: number;
}

export const ChatInterface: React.FC = () => {
  const [messages, setMessages] = useState<Message[]>([
    {
      id: '1',
      type: 'assistant',
      content: 'こんにちは！Koog AIエージェントです。何かお手伝いできることはありますか？\n\n💡 MCP機能を試したい場合:\n• 「天気 東京」でMCP経由の天気取得\n• 「計算 2+2」でMCP経由の計算\n• 「MCP status」でMCPサーバー状態確認',
      timestamp: Date.now(),
    },
  ]);
  const [inputValue, setInputValue] = useState('');
  const [useMcp, setUseMcp] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  
  const { sendMessage, isChatLoading, chatError, chatData } = useChat();

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  useEffect(() => {
    if (chatData) {
      const newMessage: Message = {
        id: Date.now().toString(),
        type: 'assistant',
        content: chatData.response || 'エラーが発生しました',
        timestamp: chatData.timestamp,
      };
      setMessages(prev => [...prev, newMessage]);
    }
  }, [chatData]);

  useEffect(() => {
    if (chatError) {
      const errorMessage: Message = {
        id: Date.now().toString(),
        type: 'error',
        content: `エラー: ${chatError.message}`,
        timestamp: Date.now(),
      };
      setMessages(prev => [...prev, errorMessage]);
    }
  }, [chatError]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputValue.trim() || isChatLoading) return;

    const userMessage: Message = {
      id: Date.now().toString(),
      type: 'user',
      content: inputValue.trim(),
      timestamp: Date.now(),
    };

    setMessages(prev => [...prev, userMessage]);
    
    // Check for MCP status command
    if (inputValue.toLowerCase().includes('mcp status')) {
      try {
        const mcpStatus = await mcpApi.getMcpStatus();
        const statusMessage: Message = {
          id: Date.now().toString(),
          type: 'assistant',
          content: `🔌 MCP Server Status: ${mcpStatus.status}\n\nTools: ${mcpStatus.tools?.length || 0}\nResources: ${mcpStatus.resources?.length || 0}`,
          timestamp: Date.now(),
        };
        setMessages(prev => [...prev, statusMessage]);
      } catch (error) {
        const errorMessage: Message = {
          id: Date.now().toString(),
          type: 'error',
          content: `MCP status check failed: ${error instanceof Error ? error.message : 'Unknown error'}`,
          timestamp: Date.now(),
        };
        setMessages(prev => [...prev, errorMessage]);
      }
      setInputValue('');
      return;
    }

    // Use MCP chat if enabled or if message contains weather/calculation keywords
    if (useMcp || inputValue.toLowerCase().includes('天気') || inputValue.toLowerCase().includes('計算') || inputValue.toLowerCase().includes('weather') || inputValue.toLowerCase().includes('calculate')) {
      try {
        const response = await mcpApi.chatWithMcp({ message: inputValue.trim() });
        const mcpMessage: Message = {
          id: Date.now().toString(),
          type: 'assistant',
          content: `🔌 [MCP] ${response.response}`,
          timestamp: Date.now(),
        };
        setMessages(prev => [...prev, mcpMessage]);
      } catch (error) {
        const errorMessage: Message = {
          id: Date.now().toString(),
          type: 'error',
          content: `MCP error: ${error instanceof Error ? error.message : 'Unknown error'}`,
          timestamp: Date.now(),
        };
        setMessages(prev => [...prev, errorMessage]);
      }
    } else {
      // Use regular chat
      sendMessage({ message: inputValue.trim() });
    }
    
    setInputValue('');
  };

  return (
    <div className="flex flex-col h-full bg-white rounded-lg shadow-sm border border-gray-200">
      {/* Header */}
      <div className="px-6 py-4 border-b border-gray-200">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-lg font-semibold text-gray-900">💬 AIチャット</h2>
            <p className="text-sm text-gray-500">天気、計算、データ分析などができます</p>
          </div>
          <div className="flex items-center space-x-2">
            <label className="flex items-center space-x-2 text-sm">
              <input
                type="checkbox"
                checked={useMcp}
                onChange={(e) => setUseMcp(e.target.checked)}
                className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
              />
              <Cable className="w-4 h-4" />
              <span>MCP Mode</span>
            </label>
          </div>
        </div>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto px-6 py-4 space-y-4">
        {messages.map((message) => (
          <div
            key={message.id}
            className={clsx(
              'flex',
              message.type === 'user' ? 'justify-end' : 'justify-start'
            )}
          >
            <div
              className={clsx(
                'max-w-[80%] px-4 py-2 rounded-lg text-sm',
                message.type === 'user' && 'bg-primary-500 text-white',
                message.type === 'assistant' && 'bg-gray-100 text-gray-900',
                message.type === 'error' && 'bg-red-100 text-red-900 border border-red-200'
              )}
            >
              {message.type === 'error' && (
                <AlertCircle className="inline-block w-4 h-4 mr-2" />
              )}
              <span className="whitespace-pre-wrap">{message.content}</span>
              <div className="text-xs opacity-70 mt-1">
                {new Date(message.timestamp).toLocaleTimeString('ja-JP')}
              </div>
            </div>
          </div>
        ))}
        
        {isChatLoading && (
          <div className="flex justify-start">
            <div className="bg-gray-100 px-4 py-2 rounded-lg">
              <Loader2 className="w-4 h-4 animate-spin" />
              <span className="ml-2 text-sm text-gray-600">考え中...</span>
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Input */}
      <form onSubmit={handleSubmit} className="px-6 py-4 border-t border-gray-200">
        <div className="flex space-x-2">
          <input
            type="text"
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            placeholder="メッセージを入力してください..."
            className="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            disabled={isChatLoading}
          />
          <button
            type="submit"
            disabled={!inputValue.trim() || isChatLoading}
            className={clsx(
              'px-4 py-2 bg-primary-500 text-white rounded-md hover:bg-primary-600 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2 transition-colors',
              'disabled:opacity-50 disabled:cursor-not-allowed'
            )}
          >
            <Send className="w-4 h-4" />
          </button>
        </div>
      </form>
    </div>
  );
};