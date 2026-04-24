import React, { useState } from 'react';
import { fetchElectionTimeline } from '../api/electiqService';

const TimelineCard = ({ accentColor, label, sublabel, value }) => (
  <div className={`bg-white p-6 rounded-xl shadow-sm border border-gray-100 border-t-4 border-t-${accentColor}-500 relative overflow-hidden group`}>
    <div className={`absolute top-0 right-0 -mt-4 -mr-4 w-16 h-16 bg-${accentColor}-50 rounded-full opacity-50 group-hover:scale-150 transition-transform duration-500`} />
    <h3 className={`text-sm font-bold text-${accentColor}-600 uppercase tracking-wider mb-2`}>{label}</h3>
    <p className="text-gray-500 text-sm mb-1">{sublabel}</p>
    <p className="text-2xl font-bold text-gray-900">{value || 'N/A'}</p>
  </div>
);

const ElectionTimeline = () => {
  const [stateName, setStateName] = useState('');
  const [timeline, setTimeline] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!stateName.trim()) return;

    setLoading(true);
    setError('');
    setTimeline(null);
    try {
      const data = await fetchElectionTimeline(stateName.trim());
      setTimeline(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch election timeline. Please check the state name and try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto w-full">
      <div className="mb-8 text-center">
        <h1 className="text-4xl font-extrabold text-gray-900 tracking-tight mb-3">Election Timeline</h1>
        <p className="text-gray-500">Find important election dates and deadlines for your state.</p>
      </div>

      <div className="bg-white p-8 rounded-2xl shadow-sm border border-gray-100 mb-8">
        <form onSubmit={handleSubmit} className="flex gap-4 flex-col sm:flex-row">
          <input
            type="text"
            value={stateName}
            onChange={(e) => setStateName(e.target.value)}
            placeholder="Enter state name (e.g., California)"
            className="flex-grow px-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all bg-gray-50 focus:bg-white"
            required
          />
          <button
            type="submit"
            disabled={loading || !stateName.trim()}
            className="bg-blue-600 text-white font-bold py-3 px-8 rounded-xl hover:bg-blue-700 focus:ring-4 focus:ring-blue-200 transition-all disabled:opacity-70 disabled:cursor-not-allowed whitespace-nowrap shadow-md hover:shadow-lg"
          >
            {loading ? 'Searching...' : 'View Timeline'}
          </button>
        </form>
      </div>

      {error && (
        <div className="p-4 bg-red-50 text-red-700 rounded-xl border border-red-100 flex items-start mb-8">
          <svg className="w-5 h-5 mr-3 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <p className="font-medium">{error}</p>
        </div>
      )}

      {timeline && (
        <div className="space-y-6 animate-fade-in-up">
          <h2 className="text-2xl font-bold text-gray-800 border-b pb-2">
            Timeline for {timeline.state || stateName}
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <TimelineCard accentColor="blue" label="Registration" sublabel="Deadline" value={timeline.registrationDeadline} />
            <TimelineCard accentColor="green" label="Polling" sublabel="Election Date" value={timeline.pollingDate} />
            <TimelineCard accentColor="purple" label="Results" sublabel="Expected Date" value={timeline.resultDate} />
          </div>
        </div>
      )}
    </div>
  );
};

export default ElectionTimeline;
