import { db } from '../firebase';
import { collection, addDoc, serverTimestamp, increment, doc, setDoc } from 'firebase/firestore';

/**
 * Persists a user feedback rating for an assistant reply to Firestore.
 *
 * @param {string} content   - The assistant reply text being rated.
 * @param {boolean} isHelpful - Whether the user rated the reply as helpful.
 */
export const saveFeedback = async (content, isHelpful) => {
  await addDoc(collection(db, 'feedback'), {
    content,
    isHelpful,
    timestamp: serverTimestamp(),
    type: 'assistant_reply',
  });
};

/**
 * Increments the assistant query counter in Firestore.
 * Failures are intentionally non-blocking; stats loss is acceptable.
 */
export const incrementQueryCount = () => {
  const statsRef = doc(db, 'usage_stats', 'assistant');
  setDoc(statsRef, { total_queries: increment(1) }, { merge: true }).catch(() => {
    if (import.meta.env.DEV) {
      console.error('[ElectIQ] Failed to update usage stats.');
    }
  });
};
