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
  const response = await api.post('/api/v1/assistant/ask', { query: question });
  return response.data.response;
};
