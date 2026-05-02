import { useState, useRef, useEffect, useCallback } from 'react';
import { askAssistant } from '../api/electiqService';
import { saveFeedback, incrementQueryCount } from '../services/firebaseChatService';
import ChatMessage from '../components/ChatMessage';
import TypingIndicator from '../components/TypingIndicator';
import { SendIcon } from '../components/icons';

const INITIAL_MESSAGES = [
  { role: 'assistant', content: 'Hello! I am ElectIQ, your AI election assistant. How can I help you today?' },
];

const AIAssistant = () => {
  const [question, setQuestion]   = useState('');
  const [messages, setMessages]   = useState(INITIAL_MESSAGES);
  const [loading, setLoading]     = useState(false);
  const messagesEndRef             = useRef(null);

  const scrollToBottom = useCallback(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages, loading, scrollToBottom]);

  const handleFeedback = useCallback(async (content, isHelpful) => {
    await saveFeedback(content, isHelpful);
  }, []);

  const handleSubmit = useCallback(async (e) => {
    e.preventDefault();
    const trimmed = question.trim();
    if (!trimmed || loading) return;

    setMessages((prev) => [...prev, { role: 'user', content: trimmed }]);
    setQuestion('');
    setLoading(true);

    incrementQueryCount();

    try {
      const answer = await askAssistant(trimmed);
      setMessages((prev) => [...prev, { role: 'assistant', content: answer }]);
    } catch {
      setMessages((prev) => [
        ...prev,
        { role: 'assistant', content: 'Sorry, I encountered an error. Please try again.', error: true },
      ]);
    } finally {
      setLoading(false);
    }
  }, [question, loading]);

  return (
    <div className="max-w-3xl mx-auto w-full h-[80vh] flex flex-col">
      <div className="mb-6 text-center shrink-0">
        <h1 className="text-4xl font-extrabold text-gray-900 tracking-tight mb-2">AI Assistant</h1>
        <p className="text-gray-500">Ask me anything about voting, elections, or your rights.</p>
      </div>

      <div className="flex-grow bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden flex flex-col">
        <div
          className="flex-grow overflow-y-auto p-6 space-y-6 bg-gray-50/50"
          role="list"
          aria-label="Conversation history"
          aria-live="polite"
        >
          {messages.map((msg, idx) => (
            <ChatMessage key={idx} msg={msg} onFeedback={handleFeedback} />
          ))}
          {loading && <TypingIndicator />}
          <div ref={messagesEndRef} />
        </div>

        <div className="p-4 bg-white border-t border-gray-100 shrink-0">
          <form onSubmit={handleSubmit} className="flex gap-2">
            <label htmlFor="assistant-input" className="sr-only">Ask an election question</label>
            <input
              id="assistant-input"
              type="text"
              value={question}
              onChange={(e) => setQuestion(e.target.value)}
              placeholder="Type your question..."
              className="flex-grow px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-purple-500 outline-none transition-all"
              disabled={loading}
              maxLength={500}
              aria-label="Your question"
            />
            <button
              type="submit"
              disabled={loading || !question.trim()}
              className="bg-purple-600 text-white p-3 px-6 rounded-xl hover:bg-purple-700 focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 transition-all disabled:opacity-70 disabled:cursor-not-allowed flex items-center justify-center font-semibold outline-none"
              aria-label="Send message"
            >
              <span>Send</span>
              <SendIcon />
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default AIAssistant;
