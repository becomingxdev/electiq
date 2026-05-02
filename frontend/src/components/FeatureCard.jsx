import { Link } from 'react-router-dom';

/**
 * Static Tailwind class maps — required for purge-safety.
 * Dynamic interpolation (bg-${color}) is stripped by the Tailwind compiler.
 */
const COLOR_CLASSES = {
  blue: {
    icon:      'bg-blue-50 text-blue-600',
    iconHover: 'group-hover:bg-blue-600 group-hover:text-white',
  },
  green: {
    icon:      'bg-green-50 text-green-600',
    iconHover: 'group-hover:bg-green-600 group-hover:text-white',
  },
  purple: {
    icon:      'bg-purple-50 text-purple-600',
    iconHover: 'group-hover:bg-purple-600 group-hover:text-white',
  },
};

/**
 * Navigation card linking to a primary feature of the app.
 *
 * @param {string}    to          - React Router destination path.
 * @param {'blue'|'green'|'purple'} color - Theme colour key.
 * @param {string}    title       - Card heading.
 * @param {string}    description - Short feature description.
 * @param {React.ReactNode} icon  - SVG icon element.
 */
const FeatureCard = ({ to, color, title, description, icon }) => {
  const classes = COLOR_CLASSES[color] ?? COLOR_CLASSES.blue;

  return (
    <Link
      to={to}
      className="bg-white rounded-2xl shadow-sm hover:shadow-xl transition-all duration-300 p-8 flex flex-col items-center text-center group border border-gray-100 hover:-translate-y-1"
    >
      <div
        className={`w-16 h-16 rounded-full flex items-center justify-center mb-6 transition-colors duration-300 ${classes.icon} ${classes.iconHover}`}
      >
        {icon}
      </div>
      <h2 className="text-xl font-bold mb-3 text-gray-900">{title}</h2>
      <p className="text-gray-500 leading-relaxed">{description}</p>
    </Link>
  );
};

export default FeatureCard;
