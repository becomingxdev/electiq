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
  const response = await api.get(`/elections/timeline?state=${encodeURIComponent(state)}`);
  return response.data;
};

// --- AI Assistant API ---
export const askAssistant = async (question) => {
  const response = await api.post('/assistant/ask', { question });
  const { data } = response;
  return data.answer || data.response || (typeof data === 'string' ? data : 'I received a response in an unexpected format.');
};
