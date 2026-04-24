import React, { useState, useRef, useEffect, useCallback, memo } from 'react';
import { askAssistant } from '../api/electiqService';
import { db } from '../firebase';
import { collection, addDoc, serverTimestamp, increment, doc, setDoc } from 'firebase/firestore';

const INITIAL_MESSAGES = [
  { role: 'assistant', content: 'Hello! I am ElectIQ, your AI election assistant. How can I help you today?' },
];

const UserIcon = () => (
  <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
  </svg>
);

const BotIcon = () => (
  <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
  </svg>
);

const TypingIndicator = () => (
  <div className="flex justify-start" aria-live="polite" aria-label="Assistant is typing">
    <div className="flex flex-row">
      <div className="w-8 h-8 rounded-full bg-purple-600 mr-3 flex items-center justify-center flex-shrink-0">
        <BotIcon />
      </div>
      <div className="p-4 rounded-2xl bg-white border border-gray-100 rounded-tl-none flex items-center space-x-2">
        <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" />
        <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }} />
        <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.4s' }} />
      </div>
    </div>
  </div>
);

const ChatMessage = memo(({ msg, onFeedback }) => {
  const isUser = msg.role === 'user';
  const [voted, setVoted] = useState(false);

  const handleFeedback = async (isHelpful) => {
    if (voted) return;
    setVoted(true);
    await onFeedback(msg.content, isHelpful);
  };

  return (
    <div className={`flex ${isUser ? 'justify-end' : 'justify-start'}`} role="listitem">
      <div className={`flex max-w-[80%] ${isUser ? 'flex-row-reverse' : 'flex-row'}`}>
        <div className={`w-8 h-8 rounded-full flex-shrink-0 flex items-center justify-center mt-1 ${isUser ? 'bg-blue-600 ml-3' : 'bg-purple-600 mr-3'}`} aria-hidden="true">
          {isUser ? <UserIcon /> : <BotIcon />}
        </div>
        <div>
          <div className={`p-4 rounded-2xl shadow-sm ${
            isUser
              ? 'bg-blue-600 text-white rounded-tr-none'
              : msg.error
                ? 'bg-red-50 text-red-700 border border-red-100 rounded-tl-none'
                : 'bg-white text-gray-800 border border-gray-100 rounded-tl-none'
          }`}>
            <span className="sr-only">{isUser ? 'You said:' : 'Assistant said:'}</span>
            <p className="whitespace-pre-wrap">{msg.content}</p>
          </div>
          
          {!isUser && !msg.error && !voted && (
            <div className="flex gap-2 mt-2 ml-1">
              <button 
                onClick={() => handleFeedback(true)}
                className="text-xs text-gray-400 hover:text-green-600 transition-colors flex items-center"
                aria-label="Helpful"
              >
                <svg className="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M14 10h4.708C19.75 10 21 11.25 21 12.5c0 .5-.25 1-.5 1.5l-4.5 7c-.5.5-1.25 1-2 1h-7c-.75 0-1.5-.5-2-1l-2-2c-.5-.5-.5-1.25 0-1.75L6 14.5V10c0-1.1.9-2 2-2h4V3.5c0-.8.7-1.5 1.5-1.5s1.5.7 1.5 1.5V10z" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/></svg>
                Helpful
              </button>
              <button 
                onClick={() => handleFeedback(false)}
                className="text-xs text-gray-400 hover:text-red-600 transition-colors flex items-center"
                aria-label="Not helpful"
              >
                <svg className="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M10 14H5.292C4.25 14 3 12.75 3 11.5c0-.5.25-1 .5-1.5l4.5-7c.5-.5 1.25-1 2-1h7c.75 0 1.5.5 2 1l2 2c.5.5.5 1.25 0 1.75L18 9.5V14c0 1.1-.9 2-2 2h-4v6.5c0 .8-.7 1.5-1.5 1.5S9 23.3 9 22.5V14z" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/></svg>
                Not helpful
              </button>
            </div>
          )}
          {voted && !isUser && (
            <span className="text-[10px] text-gray-400 mt-1 ml-1">Thanks for feedback!</span>
          )}
        </div>
      </div>
    </div>
  );
});

const AIAssistant = () => {
  const [question, setQuestion] = useState('');
  const [messages, setMessages] = useState(INITIAL_MESSAGES);
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef(null);

  const scrollToBottom = useCallback(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages, loading, scrollToBottom]);

  const handleFeedback = useCallback(async (content, isHelpful) => {
    try {
      await addDoc(collection(db, "feedback"), {
        content,
        isHelpful,
        timestamp: serverTimestamp(),
        type: 'assistant_reply'
      });
    } catch (err) {
      console.error("Feedback error:", err);
    }
  }, []);

  const handleSubmit = useCallback(async (e) => {
    e.preventDefault();
    const trimmed = question.trim();
    if (!trimmed || loading) return;

    setMessages((prev) => [...prev, { role: 'user', content: trimmed }]);
    setQuestion('');
    setLoading(true);

    try {
      // Efficiency: Track usage in Firestore
      const statsRef = doc(db, "usage_stats", "assistant");
      setDoc(statsRef, { total_queries: increment(1) }, { merge: true }).catch(console.error);

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
        {/* Chat Messages */}
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

        {/* Input Area */}
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
              <svg className="w-5 h-5 ml-2 -mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
              </svg>
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default AIAssistant;
