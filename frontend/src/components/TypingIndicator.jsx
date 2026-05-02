import { BotIcon } from './icons';

/**
 * Animated three-dot indicator shown while the assistant is generating a reply.
 */
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

export default TypingIndicator;
