import React, { useState, useRef, useEffect } from 'react';
import { Send, Loader2, AlertCircle } from 'lucide-react';
import { useChat } from '../hooks/useChat';
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
      content: 'こんにちは！Koog AIエージェントです。何かお手伝いできることはありますか？',
      timestamp: Date.now(),
    },
  ]);
  const [inputValue, setInputValue] = useState('');
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

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputValue.trim() || isChatLoading) return;

    const userMessage: Message = {
      id: Date.now().toString(),
      type: 'user',
      content: inputValue.trim(),
      timestamp: Date.now(),
    };

    setMessages(prev => [...prev, userMessage]);
    sendMessage({ message: inputValue.trim() });
    setInputValue('');
  };

  return (
    <div className="flex flex-col h-full bg-white rounded-lg shadow-sm border border-gray-200">
      {/* Header */}
      <div className="px-6 py-4 border-b border-gray-200">
        <h2 className="text-lg font-semibold text-gray-900">💬 AIチャット</h2>
        <p className="text-sm text-gray-500">天気、計算、データ分析などができます</p>
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