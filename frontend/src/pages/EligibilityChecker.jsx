import React, { useState } from 'react';
import { checkEligibility } from '../api/electiqService';

const INITIAL_FORM = { age: '', citizen: false, hasIdProof: false };

const ErrorAlert = ({ message }) => (
  <div className="mt-6 p-4 bg-red-50 text-red-700 rounded-xl border border-red-100 flex items-start">
    <svg className="w-5 h-5 mr-3 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
    </svg>
    <p className="font-medium">{message}</p>
  </div>
);

const EligibilityResult = ({ result }) => {
  const isEligible = result.eligible;
  return (
    <div className={`mt-8 p-6 rounded-2xl border ${isEligible ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200'}`}>
      <div className="flex items-center mb-3">
        {isEligible ? (
          <svg className="w-8 h-8 text-green-600 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        ) : (
          <svg className="w-8 h-8 text-red-600 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        )}
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

const CheckboxRow = ({ id, name, checked, label, onChange }) => (
  <div
    className="flex items-center space-x-4 p-4 rounded-xl border border-gray-100 bg-gray-50 cursor-pointer hover:bg-gray-100 transition-colors"
    onClick={() => onChange(name)}
  >
    <div className="flex-shrink-0">
      <input
        type="checkbox"
        id={id}
        name={name}
        checked={checked}
        onChange={() => onChange(name)}
        onClick={(e) => e.stopPropagation()}
        className="w-5 h-5 text-blue-600 border-gray-300 rounded focus:ring-blue-500 cursor-pointer"
      />
    </div>
    <label htmlFor={id} className="text-gray-800 font-medium cursor-pointer flex-grow">
      {label}
    </label>
  </div>
);

const EligibilityChecker = () => {
  const [formData, setFormData] = useState(INITIAL_FORM);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleAgeChange = (e) => {
    setFormData((prev) => ({ ...prev, age: e.target.value }));
  };

  const handleCheckboxToggle = (name) => {
    setFormData((prev) => ({ ...prev, [name]: !prev[name] }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setResult(null);
    try {
      const data = await checkEligibility(formData.age, formData.citizen, formData.hasIdProof);
      setResult(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to check eligibility. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto w-full">
      <div className="mb-8 text-center">
        <h1 className="text-4xl font-extrabold text-gray-900 tracking-tight mb-3">Eligibility Checker</h1>
        <p className="text-gray-500">Verify your eligibility to participate in upcoming elections.</p>
      </div>

      <div className="bg-white p-8 rounded-2xl shadow-sm border border-gray-100">
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">Age</label>
            <input
              type="number"
              name="age"
              value={formData.age}
              onChange={handleAgeChange}
              required
              min="0"
              className="w-full px-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all bg-gray-50 focus:bg-white"
              placeholder="Enter your age"
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
            className="w-full bg-blue-600 text-white font-bold py-4 px-6 rounded-xl hover:bg-blue-700 focus:ring-4 focus:ring-blue-200 transition-all disabled:opacity-70 disabled:cursor-not-allowed shadow-md hover:shadow-lg"
          >
            {loading ? 'Checking...' : 'Check Eligibility'}
          </button>
        </form>

        {error && <ErrorAlert message={error} />}
        {result && <EligibilityResult result={result} />}
      </div>
    </div>
  );
};

export default EligibilityChecker;
