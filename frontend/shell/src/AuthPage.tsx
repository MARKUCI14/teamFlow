import { useState, type FormEvent } from 'react';
import { createApiClient } from '@teamflow/shared';

const api = createApiClient(import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080');

export function AuthPage({
  onAuthenticated,
  error: sessionError,
}: {
  onAuthenticated: (token: string) => Promise<void>;
  error: string;
}) {
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);
    setError('');
    try {
      const credentials = { email, password };
      const result = mode === 'login' ? await api.login(credentials) : await api.register(credentials);
      await onAuthenticated(result.accessToken);
    } catch (failure) {
      setError(failure instanceof Error ? failure.message : 'Authentication failed.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-card">
        <p className="eyebrow">TeamFlow</p>
        <h1>{mode === 'login' ? 'Welcome back' : 'Create your account'}</h1>
        <p className="muted">Sign in to access your project workspaces.</p>
        <form onSubmit={submit}>
          <label htmlFor="email">Email</label>
          <input id="email" type="email" autoComplete="email" required value={email} onChange={(event) => setEmail(event.target.value)} />
          <label htmlFor="password">Password</label>
          <input id="password" type="password" autoComplete={mode === 'login' ? 'current-password' : 'new-password'} minLength={8} required value={password} onChange={(event) => setPassword(event.target.value)} />
          {(error || sessionError) && <p className="error-message" role="alert">{error || sessionError}</p>}
          <button className="button button-primary" type="submit" disabled={busy}>
            {busy ? 'Please wait…' : mode === 'login' ? 'Log in' : 'Register'}
          </button>
        </form>
        <button className="text-button" type="button" onClick={() => { setMode(mode === 'login' ? 'register' : 'login'); setError(''); }}>
          {mode === 'login' ? 'Need an account? Register' : 'Already have an account? Log in'}
        </button>
      </section>
    </main>
  );
}
