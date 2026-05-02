
import { AlertCircleIcon } from './icons';

/**
 * Displays a dismissible error banner with an alert icon.
 *
 * @param {string} message - The error message to display.
 */
const ErrorAlert = ({ message }) => (
  <div
    className="mt-6 p-4 bg-red-50 text-red-700 rounded-xl border border-red-100 flex items-start"
    role="alert"
    aria-live="assertive"
  >
    <AlertCircleIcon />
    <p className="font-medium">{message}</p>
  </div>
);

export default ErrorAlert;
