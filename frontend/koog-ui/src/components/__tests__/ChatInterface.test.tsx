import { render, screen, fireEvent } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ChatInterface } from '../ChatInterface';
import { vi } from 'vitest';

// Mock the useChat hook
vi.mock('../../hooks/useChat', () => ({
  useChat: () => ({
    sendMessage: vi.fn(),
    isChatLoading: false,
    chatError: null,
    chatData: null,
  }),
}));

const createTestQueryClient = () => new QueryClient({
  defaultOptions: {
    queries: { retry: false },
    mutations: { retry: false },
  },
});

const renderWithProviders = (ui: React.ReactElement) => {
  const queryClient = createTestQueryClient();
  return render(
    <QueryClientProvider client={queryClient}>
      {ui}
    </QueryClientProvider>
  );
};

describe('ChatInterface', () => {
  test('renders chat interface with initial message', () => {
    renderWithProviders(<ChatInterface />);
    
    expect(screen.getByText('💬 AIチャット')).toBeInTheDocument();
    expect(screen.getByText('こんにちは！Koog AIエージェントです。何かお手伝いできることはありますか？')).toBeInTheDocument();
  });

  test('allows user to type and submit message', () => {
    renderWithProviders(<ChatInterface />);
    
    const input = screen.getByPlaceholderText('メッセージを入力してください...');
    const submitButton = screen.getByRole('button', { name: /送信/i });
    
    fireEvent.change(input, { target: { value: 'テストメッセージ' } });
    expect(input).toHaveValue('テストメッセージ');
    
    fireEvent.click(submitButton);
    expect(input).toHaveValue('');
  });

  test('disables input when loading', () => {
    vi.doMock('../../hooks/useChat', () => ({
      useChat: () => ({
        sendMessage: vi.fn(),
        isChatLoading: true,
        chatError: null,
        chatData: null,
      }),
    }));

    renderWithProviders(<ChatInterface />);
    
    const input = screen.getByPlaceholderText('メッセージを入力してください...');
    expect(input).toBeDisabled();
  });
});