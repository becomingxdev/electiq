import FeatureCard from '../components/FeatureCard';

const FEATURE_CARDS = [
  {
    to: '/eligibility',
    color: 'blue',
    title: 'Eligibility Checker',
    description: 'Find out if you meet the requirements to vote in your area quickly and easily.',
    icon: (
      <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    ),
  },
  {
    to: '/timeline',
    color: 'green',
    title: 'Election Timeline',
    description: 'Stay up to date with important election dates, registration deadlines, and polling days.',
    icon: (
      <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
      </svg>
    ),
  },
  {
    to: '/assistant',
    color: 'purple',
    title: 'AI Assistant',
    description: 'Ask any questions about the voting process, candidates, or laws and get instant answers.',
    icon: (
      <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
      </svg>
    ),
  },
];

const Dashboard = () => (
  <div className="flex flex-col items-center justify-center space-y-16 py-12">
    <div className="text-center space-y-4 animate-fade-in-up">
      <h1 className="text-6xl font-extrabold text-gray-900 tracking-tight">ElectIQ</h1>
      <p className="text-2xl text-gray-600 font-medium max-w-2xl mx-auto">AI Powered Election Assistant</p>
    </div>

    <div className="grid grid-cols-1 md:grid-cols-3 gap-8 w-full max-w-5xl">
      {FEATURE_CARDS.map((card) => (
        <FeatureCard key={card.to} {...card} />
      ))}
    </div>
  </div>
);

export default Dashboard;
