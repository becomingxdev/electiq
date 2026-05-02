

/**
 * Static Tailwind class maps — required for purge-safety.
 * Dynamic interpolation (bg-${color}) is stripped by the Tailwind compiler
 * because it cannot scan partial strings. All variants must appear in full.
 */
const ACCENT_CLASSES = {
  blue: {
    topBorder:  'border-t-blue-500',
    circle:     'bg-blue-50',
    heading:    'text-blue-600',
  },
  green: {
    topBorder:  'border-t-green-500',
    circle:     'bg-green-50',
    heading:    'text-green-600',
  },
  purple: {
    topBorder:  'border-t-purple-500',
    circle:     'bg-purple-50',
    heading:    'text-purple-600',
  },
};

/**
 * Displays a single election date metric (e.g. Registration Deadline).
 *
 * @param {'blue'|'green'|'purple'} accentColor - Theme colour key.
 * @param {string} label    - Card heading (e.g. "Registration").
 * @param {string} sublabel - Secondary label (e.g. "Deadline").
 * @param {string} value    - The date value to display.
 */
const TimelineCard = ({ accentColor, label, sublabel, value }) => {
  const classes = ACCENT_CLASSES[accentColor] ?? ACCENT_CLASSES.blue;

  return (
    <div
      className={`bg-white p-6 rounded-xl shadow-sm border border-gray-100 border-t-4 ${classes.topBorder} relative overflow-hidden group`}
    >
      <div
        className={`absolute top-0 right-0 -mt-4 -mr-4 w-16 h-16 ${classes.circle} rounded-full opacity-50 group-hover:scale-150 transition-transform duration-500`}
        aria-hidden="true"
      />
      <h3 className={`text-sm font-bold ${classes.heading} uppercase tracking-wider mb-2`}>{label}</h3>
      <p className="text-gray-500 text-sm mb-1">{sublabel}</p>
      <p className="text-2xl font-bold text-gray-900">{value || 'N/A'}</p>
    </div>
  );
};

export default TimelineCard;
