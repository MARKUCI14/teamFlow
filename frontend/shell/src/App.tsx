import { Component, lazy, Suspense, useEffect, useState, type ReactNode } from 'react';
import { Link, Navigate, Route, Routes, useNavigate } from 'react-router-dom';
import { connectNotifications, createApiClient, type CurrentUser, type NotificationConnectionState } from '@teamflow/shared';
import { AuthPage } from './AuthPage';

const StakeholderRemote = lazy(() => import('stakeholder/RemoteApp'));
const TeamRemote = lazy(() => import('team/RemoteApp'));
const api = createApiClient(import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080');
const notificationUrl = import.meta.env.VITE_NOTIFICATION_URL ?? 'ws://localhost:8083/ws';

export function App() {
  const [token, setToken] = useState<string | null>(null);
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [authError, setAuthError] = useState('');

  async function authenticate(accessToken: string) {
    setAuthError('');
    try {
      const currentUser = await api.currentUser(accessToken);
      setToken(accessToken);
      setUser(currentUser);
    } catch (error) {
      setAuthError(error instanceof Error ? error.message : 'Could not load the current user.');
    }
  }

  function logout() {
    setToken(null);
    setUser(null);
  }

  if (!token || !user) {
    return <AuthPage onAuthenticated={authenticate} error={authError} />;
  }

  return (
    <AuthenticatedApp
      user={user}
      token={token}
      notificationUrl={notificationUrl}
      onLogout={logout}
    />
  );
}

function AuthenticatedApp({
  user,
  token,
  notificationUrl: socketUrl,
  onLogout,
}: {
  user: CurrentUser;
  token: string;
  notificationUrl: string;
  onLogout: () => void;
}) {
  const navigate = useNavigate();
  const [connection, setConnection] = useState<NotificationConnectionState>('connecting');
  const [latestMessage, setLatestMessage] = useState('');

  useEffect(() => connectNotifications({
    url: socketUrl,
    token,
    onStateChange: setConnection,
    onMessage: setLatestMessage,
  }), [socketUrl, token]);

  function handleLogout() {
    onLogout();
    navigate('/');
  }

  return (
    <div className="app-shell">
      <header className="top-bar">
        <Link className="brand" to="/">TeamFlow</Link>
        <nav aria-label="Main navigation">
          <Link to="/stakeholder">Stakeholder</Link>
          <Link to="/team">Team</Link>
        </nav>
        <div className="account">
          <span>{user.email}</span>
          <button className="button button-secondary" onClick={handleLogout}>Log out</button>
        </div>
      </header>

      <main className="content">
        <div className="connection-status" role="status">
          Notifications: {connection}
          {latestMessage && <span className="notification-message">Latest: {latestMessage}</span>}
        </div>
        <Routes>
          <Route path="/" element={<Navigate to="/stakeholder" replace />} />
          <Route path="/stakeholder/*" element={(
            <RemoteBoundary key="stakeholder">
              <Suspense fallback={<LoadingState /> }>
                <StakeholderRemote user={user} token={token} api={api} />
              </Suspense>
            </RemoteBoundary>
          )} />
          <Route path="/team/*" element={(
            <RemoteBoundary key="team">
              <Suspense fallback={<LoadingState /> }>
                <TeamRemote user={user} token={token} api={api} />
              </Suspense>
            </RemoteBoundary>
          )} />
          <Route path="*" element={<section className="panel"><h1>Page not found</h1><Link to="/stakeholder">Return to the workspace</Link></section>} />
        </Routes>
      </main>
    </div>
  );
}

function LoadingState() {
  return <section className="panel" role="status">Loading workspace…</section>;
}

class RemoteBoundary extends Component<{ children: ReactNode }, { error: boolean }> {
  state = { error: false };

  static getDerivedStateFromError(): { error: boolean } {
    return { error: true };
  }

  render() {
    if (this.state.error) {
      return <section className="panel error-state" role="alert">
        <h1>Workspace unavailable</h1>
        <p>Check that the remote application is running, then reload this page.</p>
      </section>;
    }
    return this.props.children;
  }
}
