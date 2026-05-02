import { useState, useCallback } from 'react';
import { checkEligibility } from '../api/electiqService';
import CheckboxRow from '../components/CheckboxRow';
import ErrorAlert from '../components/ErrorAlert';
import EligibilityResult from '../components/EligibilityResult';

const INITIAL_FORM = { age: '', citizen: false, hasIdProof: false };

const EligibilityChecker = () => {
  const [formData, setFormData] = useState(INITIAL_FORM);
  const [result, setResult]     = useState(null);
  const [loading, setLoading]   = useState(false);
  const [error, setError]       = useState('');

  const handleAgeChange = useCallback((e) => {
    setFormData((prev) => ({ ...prev, age: e.target.value }));
  }, []);

  const handleCheckboxToggle = useCallback((name) => {
    setFormData((prev) => ({ ...prev, [name]: !prev[name] }));
  }, []);

  const handleSubmit = useCallback(async (e) => {
    e.preventDefault();
    if (loading) return;
    setLoading(true);
    setError('');
    setResult(null);
    try {
      const data = await checkEligibility(formData.age, formData.citizen, formData.hasIdProof);
      setResult(data);
    } catch (err) {
      setError(err.userMessage || 'Failed to check eligibility. Please try again.');
    } finally {
      setLoading(false);
    }
  }, [formData, loading]);

  return (
    <div className="max-w-2xl mx-auto w-full">
      <div className="mb-8 text-center">
        <h1 className="text-4xl font-extrabold text-gray-900 tracking-tight mb-3">Eligibility Checker</h1>
        <p className="text-gray-500">Verify your eligibility to participate in upcoming elections.</p>
      </div>

      <div className="bg-white p-8 rounded-2xl shadow-sm border border-gray-100">
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label htmlFor="age-input" className="block text-sm font-semibold text-gray-700 mb-2">Age</label>
            <input
              id="age-input"
              type="number"
              name="age"
              value={formData.age}
              onChange={handleAgeChange}
              required
              min="1"
              max="150"
              className="w-full px-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all bg-gray-50 focus:bg-white"
              placeholder="e.g. 25"
              aria-required="true"
            />
          </div>

          <CheckboxRow
            id="citizen"
            name="citizen"
            checked={formData.citizen}
            label="I am a citizen"
            onChange={handleCheckboxToggle}
          />

          <CheckboxRow
            id="hasIdProof"
            name="hasIdProof"
            checked={formData.hasIdProof}
            label="I have valid ID proof"
            onChange={handleCheckboxToggle}
          />

          <button
            type="submit"
            disabled={loading || !formData.age}
            className="w-full bg-blue-600 text-white font-bold py-4 px-6 rounded-xl hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-all disabled:opacity-70 disabled:cursor-not-allowed shadow-md hover:shadow-lg outline-none"
            aria-label={loading ? 'Checking eligibility' : 'Check Eligibility'}
          >
            {loading ? 'Checking...' : 'Check Eligibility'}
          </button>
        </form>

        {error  && <ErrorAlert message={error} />}
        {result && <EligibilityResult result={result} />}
      </div>
    </div>
  );
};

export default EligibilityChecker;
