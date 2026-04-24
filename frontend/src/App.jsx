import React, { Suspense, lazy } from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';

const Dashboard = lazy(() => import('./pages/Dashboard'));
const EligibilityChecker = lazy(() => import('./pages/EligibilityChecker'));
const ElectionTimeline = lazy(() => import('./pages/ElectionTimeline'));
const AIAssistant = lazy(() => import('./pages/AIAssistant'));

function App() {
  return (
    <Router>
      <div className="min-h-screen bg-gray-50 text-gray-900 font-sans">
        <header className="bg-white shadow-sm p-4 sticky top-0 z-10">
          <nav className="max-w-6xl mx-auto flex justify-between items-center" aria-label="Main Navigation">
            <Link to="/" className="text-2xl font-extrabold text-blue-600 tracking-tight focus:ring-2 focus:ring-blue-500 rounded-lg outline-none">ElectIQ</Link>
            <div className="space-x-6 font-medium text-gray-600 flex">
              <Link to="/eligibility" className="hover:text-blue-600 transition-colors focus:ring-2 focus:ring-blue-500 rounded-lg outline-none p-1">Eligibility</Link>
              <Link to="/timeline" className="hover:text-blue-600 transition-colors focus:ring-2 focus:ring-blue-500 rounded-lg outline-none p-1">Timeline</Link>
              <Link to="/assistant" className="hover:text-blue-600 transition-colors focus:ring-2 focus:ring-blue-500 rounded-lg outline-none p-1">AI Assistant</Link>
            </div>
          </nav>
        </header>
        <main className="max-w-6xl mx-auto p-4 py-10">
          <Suspense fallback={
            <div className="flex items-center justify-center h-64">
              <div className="w-12 h-12 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
            </div>
          }>
            <Routes>
              <Route path="/" element={<Dashboard />} />
              <Route path="/eligibility" element={<EligibilityChecker />} />
              <Route path="/timeline" element={<ElectionTimeline />} />
              <Route path="/assistant" element={<AIAssistant />} />
            </Routes>
          </Suspense>
        </main>
      </div>
    </Router>
  );
}

export default App;