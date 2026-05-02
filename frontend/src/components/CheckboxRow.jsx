

/**
 * Reusable checkbox row with a click-anywhere-to-toggle interaction.
 *
 * @param {string}   id       - Input element ID, also used by the label's htmlFor.
 * @param {string}   name     - Field name forwarded to the onChange handler.
 * @param {boolean}  checked  - Controlled checked state.
 * @param {string}   label    - Human-readable label text.
 * @param {Function} onChange - Callback receiving the field name when toggled.
 */
const CheckboxRow = ({ id, name, checked, label, onChange }) => (
  <div
    className="flex items-center space-x-4 p-4 rounded-xl border border-gray-100 bg-gray-50 cursor-pointer hover:bg-gray-100 transition-colors focus-within:ring-2 focus-within:ring-blue-500"
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
        className="w-5 h-5 text-blue-600 border-gray-300 rounded focus:ring-blue-500 cursor-pointer outline-none"
        aria-checked={checked}
      />
    </div>
    <label htmlFor={id} className="text-gray-800 font-medium cursor-pointer flex-grow select-none">
      {label}
    </label>
  </div>
);

export default CheckboxRow;
