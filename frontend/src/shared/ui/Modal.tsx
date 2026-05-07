import { useEffect, useRef } from 'react';
import { X } from 'lucide-react';

type ModalProps = {
  open?: boolean;
  isOpen?: boolean;
  onClose: () => void;
  children: React.ReactNode;
  className?: string;
  title?: string;
};

export function Modal({ open, isOpen, onClose, children, className = '', title }: ModalProps) {
  const resolvedOpen = open ?? isOpen ?? false;
  const overlayRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!resolvedOpen) return;
    const handleEsc = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', handleEsc);
    document.body.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', handleEsc);
      document.body.style.overflow = '';
    };
  }, [resolvedOpen, onClose]);

  if (!resolvedOpen) return null;

  return (
    <div
      ref={overlayRef}
      className="fixed inset-0 z-[100] flex items-center justify-center px-4 py-8"
      onClick={(e) => {
        if (e.target === overlayRef.current) onClose();
      }}
    >
      {/* Backdrop */}
      <div className="absolute inset-0 bg-ink/80 backdrop-blur-sm animate-fade-in" />

      {/* Content */}
      <div
        className={`relative z-10 max-h-[90vh] overflow-auto rounded-lg border border-primary/10 bg-ink shadow-soft animate-fade-in-up ${className}`}
      >
        {title ? <div className="border-b border-primary/10 px-6 py-4 pr-12 text-lg font-semibold text-surface">{title}</div> : null}
        <button
          onClick={onClose}
          className="absolute right-3 top-3 z-20 rounded-full p-1.5 text-surface/60 hover:bg-surface/10 hover:text-surface transition-colors"
          aria-label="Close"
        >
          <X size={18} />
        </button>
        {children}
      </div>
    </div>
  );
}
