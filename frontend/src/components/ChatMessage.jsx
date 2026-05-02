import { useState, memo } from 'react';
import { BotIcon, UserIcon, ThumbUpIcon, ThumbDownIcon } from './icons';

/**
 * A single chat bubble for either the user or the assistant.
 * Includes a thumbs-up / thumbs-down feedback row for assistant replies.
 *
 * @param {{ role: string, content: string, error?: boolean }} msg
 * @param {(content: string, isHelpful: boolean) => Promise<void>} onFeedback
 */
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
        <div
          className={`w-8 h-8 rounded-full flex-shrink-0 flex items-center justify-center mt-1 ${isUser ? 'bg-blue-600 ml-3' : 'bg-purple-600 mr-3'}`}
          aria-hidden="true"
        >
          {isUser ? <UserIcon /> : <BotIcon />}
        </div>

        <div>
          <div
            className={`p-4 rounded-2xl shadow-sm ${
              isUser
                ? 'bg-blue-600 text-white rounded-tr-none'
                : msg.error
                  ? 'bg-red-50 text-red-700 border border-red-100 rounded-tl-none'
                  : 'bg-white text-gray-800 border border-gray-100 rounded-tl-none'
            }`}
          >
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
                <ThumbUpIcon />
                Helpful
              </button>
              <button
                onClick={() => handleFeedback(false)}
                className="text-xs text-gray-400 hover:text-red-600 transition-colors flex items-center"
                aria-label="Not helpful"
              >
                <ThumbDownIcon />
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

ChatMessage.displayName = 'ChatMessage';

export default ChatMessage;
