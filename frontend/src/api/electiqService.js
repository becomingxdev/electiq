import api from '../api/axios';

// --- Eligibility API ---
export const checkEligibility = async (age, citizen, hasIdProof) => {
  const response = await api.post('/eligibility/check', {
    age: parseInt(age, 10),
    citizen,
    hasIdProof,
  });
  return response.data;
};

// --- Election Timeline API ---
export const fetchElectionTimeline = async (state) => {
  const params = state ? `?state=${encodeURIComponent(state)}` : '';
  const response = await api.get(`/elections/timeline${params}`);
  return response.data;
};

// --- AI Assistant API ---
export const askAssistant = async (question) => {
  const response = await api.post('/assistant/ask', { question });
  const { data } = response;
  // Handle all possible response shapes from the backend
  if (typeof data === 'string') return data;
  if (data.answer) return data.answer;
  if (data.response) return data.response;
  if (data.message) return data.message;
  return 'I received a response in an unexpected format.';
};

