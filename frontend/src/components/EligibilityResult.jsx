
import { CheckCircleIcon, XCircleIcon } from './icons';

/**
 * Displays the eligibility check result with an appropriate colour scheme.
 *
 * @param {{ eligible: boolean, message?: string }} result - Backend EligibilityResponse DTO.
 */
const EligibilityResult = ({ result }) => {
  const isEligible = result.eligible;

  return (
    <div
      className={`mt-8 p-6 rounded-2xl border ${isEligible ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200'}`}
      aria-live="polite"
    >
      <div className="flex items-center mb-3">
        {isEligible ? <CheckCircleIcon /> : <XCircleIcon />}
        <h3 className={`text-2xl font-bold ${isEligible ? 'text-green-800' : 'text-red-800'}`}>
          {isEligible ? 'Eligible to Vote' : 'Not Eligible'}
        </h3>
      </div>
      <p className={`text-lg ml-11 ${isEligible ? 'text-green-700' : 'text-red-700'}`}>
        {result.message || (isEligible ? 'You meet all the requirements.' : 'Based on the details provided.')}
      </p>
    </div>
  );
};

export default EligibilityResult;
