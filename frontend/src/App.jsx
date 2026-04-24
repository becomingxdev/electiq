import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import Dashboard from './pages/Dashboard';
import EligibilityChecker from './pages/EligibilityChecker';
import ElectionTimeline from './pages/ElectionTimeline';
import AIAssistant from './pages/AIAssistant';

function App() {
  return (
    <Router>
      <div className="min-h-screen bg-gray-50 text-gray-900 font-sans">
        <nav className="bg-white shadow-sm p-4 sticky top-0 z-10">
          <div className="max-w-6xl mx-auto flex justify-between items-center">
            <Link to="/" className="text-2xl font-extrabold text-blue-600 tracking-tight">ElectIQ</Link>
            <div className="space-x-6 font-medium text-gray-600">
              <Link to="/eligibility" className="hover:text-blue-600 transition-colors">Eligibility</Link>
              <Link to="/timeline" className="hover:text-blue-600 transition-colors">Timeline</Link>
              <Link to="/assistant" className="hover:text-blue-600 transition-colors">AI Assistant</Link>
            </div>
          </div>
        </nav>
        <main className="max-w-6xl mx-auto p-4 py-10">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/eligibility" element={<EligibilityChecker />} />
            <Route path="/timeline" element={<ElectionTimeline />} />
            <Route path="/assistant" element={<AIAssistant />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;