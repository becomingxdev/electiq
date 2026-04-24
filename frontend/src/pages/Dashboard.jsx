import React from 'react';
import { Link } from 'react-router-dom';

const Dashboard = () => {
  return (
    <div className="flex flex-col items-center justify-center space-y-16 py-12">
      <div className="text-center space-y-4 animate-fade-in-up">
        <h1 className="text-6xl font-extrabold text-gray-900 tracking-tight">ElectIQ</h1>
        <p className="text-2xl text-gray-600 font-medium max-w-2xl mx-auto">AI Powered Election Assistant</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-8 w-full max-w-5xl">
        <Link to="/eligibility" className="bg-white rounded-2xl shadow-sm hover:shadow-xl transition-all duration-300 p-8 flex flex-col items-center text-center group border border-gray-100 hover:-translate-y-1">
          <div className="w-16 h-16 bg-blue-50 text-blue-600 rounded-full flex items-center justify-center mb-6 group-hover:bg-blue-600 group-hover:text-white transition-colors duration-300">
            <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
          </div>
          <h2 className="text-xl font-bold mb-3 text-gray-900">Eligibility Checker</h2>
          <p className="text-gray-500 leading-relaxed">Find out if you meet the requirements to vote in your area quickly and easily.</p>
        </Link>

        <Link to="/timeline" className="bg-white rounded-2xl shadow-sm hover:shadow-xl transition-all duration-300 p-8 flex flex-col items-center text-center group border border-gray-100 hover:-translate-y-1">
          <div className="w-16 h-16 bg-green-50 text-green-600 rounded-full flex items-center justify-center mb-6 group-hover:bg-green-600 group-hover:text-white transition-colors duration-300">
             <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
          </div>
          <h2 className="text-xl font-bold mb-3 text-gray-900">Election Timeline</h2>
          <p className="text-gray-500 leading-relaxed">Stay up to date with important election dates, registration deadlines, and polling days.</p>
        </Link>

        <Link to="/assistant" className="bg-white rounded-2xl shadow-sm hover:shadow-xl transition-all duration-300 p-8 flex flex-col items-center text-center group border border-gray-100 hover:-translate-y-1">
          <div className="w-16 h-16 bg-purple-50 text-purple-600 rounded-full flex items-center justify-center mb-6 group-hover:bg-purple-600 group-hover:text-white transition-colors duration-300">
             <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"></path></svg>
          </div>
          <h2 className="text-xl font-bold mb-3 text-gray-900">AI Assistant</h2>
          <p className="text-gray-500 leading-relaxed">Ask any questions about the voting process, candidates, or laws and get instant answers.</p>
        </Link>
      </div>
    </div>
  );
};

export default Dashboard;
