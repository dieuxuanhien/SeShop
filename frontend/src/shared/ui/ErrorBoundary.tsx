import { Component, ErrorInfo, ReactNode } from 'react';
import { EmptyState } from './EmptyState';

type ErrorBoundaryProps = {
  children: ReactNode;
};

type ErrorBoundaryState = {
  hasError: boolean;
};

export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  state: ErrorBoundaryState = { hasError: false };

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error(error, info);
  }

  render() {
    if (this.state.hasError) {
      return (
        <main className="min-h-screen bg-ink p-6">
          <EmptyState title="Something went wrong" description="Reload the page or return to the previous screen." />
        </main>
      );
    }

    return this.props.children;
  }
}
