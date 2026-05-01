import axios from 'axios';

const baseURL = import.meta.env.REACT_APP_API_BASE_URL;

if (!baseURL) {
  console.error(
    '[ElectIQ] REACT_APP_API_BASE_URL is not set! ' +
    'Create a .env file with REACT_APP_API_BASE_URL=http://localhost:8080/api/v1 for local dev.'
  );
}

const api = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
    'x-api-key': import.meta.env.VITE_API_KEY,
  },
  // withCredentials is intentionally omitted:
  // The backend is a stateless REST API that does NOT use cookies or sessions.
  // Setting this to true would require the server to echo a specific origin header
  // and adds no security benefit for this use case.
  timeout: 15000, // 15s — matches Cloud Run's default request timeout
});

// Dev-mode request logging
if (import.meta.env.DEV) {
  api.interceptors.request.use((config) => {
    console.log("API KEY:", import.meta.env.VITE_API_KEY);
    console.debug(`[API] ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`, config.data ?? '');
    return config;
  });
}

// Normalize error responses so callers always get a consistent error shape
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const message =
      error.response?.data?.message ||
      error.response?.data?.error ||
      error.message ||
      'An unexpected error occurred.';

    if (import.meta.env.DEV) {
      console.error(`[API Error] ${status ?? 'Network'}: ${message}`, error);
    }

    // Re-throw with a clean message attached
    error.userMessage = message;
    return Promise.reject(error);
  }
);

export default api;
