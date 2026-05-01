import api from '../api/axios';

// --- Eligibility API ---
export const checkEligibility = async (age, citizen, hasIdProof) => {
  const response = await api.post('/api/v1/eligibility/check', {
    age: parseInt(age, 10),
    citizen,
    hasIdProof,
  });
  return response.data;
};

// --- Election Timeline API ---
export const fetchElectionTimeline = async (state) => {
  const params = state ? `?state=${encodeURIComponent(state)}` : '';
  const response = await api.get(`/api/v1/elections/timeline${params}`);
  return response.data;
};

// --- AI Assistant API ---
export const askAssistant = async (question) => {
  // Task 5: Use "query" instead of "question" to match standardized backend DTO
  const response = await api.post('/api/v1/assistant/ask', { query: question });
  const { data } = response;
  
  // Handle all possible response shapes from the backend
  if (typeof data === 'string') return data;
  if (data.response) return data.response; // Standardized field
  if (data.answer) return data.answer; // Legacy/Fallback support
  if (data.message) return data.message;
  
  return 'I received a response in an unexpected format.';
};
