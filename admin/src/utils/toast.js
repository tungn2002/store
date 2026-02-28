import React, { createContext, useContext, useState, useCallback, useMemo, useEffect } from 'react';
import { TransitionGroup, CSSTransition } from 'react-transition-group';

// Toast Context
const ToastContext = createContext();

export const useToast = () => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within ToastProvider');
  }
  return context;
};

// Toast Item Component
const ToastItem = ({ message, type, onClose }) => {
  const isSuccess = type === 'success';
  const bgColor = isSuccess
    ? 'bg-green-100 dark:bg-green-800'
    : 'bg-red-100 dark:bg-red-800';
  const textColor = isSuccess
    ? 'text-green-700 dark:text-green-100'
    : 'text-red-700 dark:text-red-100';
  const borderColor = isSuccess
    ? 'border-green-500'
    : 'border-red-500';

  return (
    <div
      className={`${bgColor} ${textColor} border-l-4 ${borderColor} px-4 py-3 rounded shadow-lg mb-2 flex justify-between items-center min-w-[280px]`}
    >
      <span className="text-sm">{message}</span>
      <button
        onClick={onClose}
        className="ml-4 text-lg font-bold hover:opacity-70 focus:outline-none"
      >
        ×
      </button>
    </div>
  );
};

// Toast Provider Component
export const ToastProvider = ({ children }) => {
  const [toasts, setToasts] = useState([]);

  const removeToast = useCallback((id) => {
    setToasts((prev) => prev.filter((toast) => toast.id !== id));
  }, []);

  const addToast = useCallback((message, type = 'success', duration = 3000) => {
    const id = Date.now();
    setToasts((prev) => [...prev, { id, message, type }]);

    setTimeout(() => {
      setToasts((prev) => prev.filter((toast) => toast.id !== id));
    }, duration);
  }, [removeToast]);

  const showToast = useMemo(() => ({
    success: (message, duration) => addToast(message, 'success', duration),
    error: (message, duration) => addToast(message, 'error', duration),
  }), [addToast]);

  return (
    <ToastContext.Provider value={showToast}>
      {children}
      {/* Toast Container - fixed at bottom right */}
      <div
        style={{
          position: 'fixed',
          bottom: '20px',
          right: '20px',
          zIndex: 9999,
        }}
      >
        <TransitionGroup>
          {toasts.map((toast) => (
            <CSSTransition key={toast.id} timeout={300} classNames="toast" appear>
              <ToastItem
                message={toast.message}
                type={toast.type}
                onClose={() => removeToast(toast.id)}
              />
            </CSSTransition>
          ))}
        </TransitionGroup>
      </div>
    </ToastContext.Provider>
  );
};

export default ToastProvider;
