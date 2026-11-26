import { afterEach, describe, expect, it, vi } from 'vitest';
import { createApiClient } from './api';

afterEach(() => vi.unstubAllGlobals());

describe('shared API client', () => {
  it('sends the access token as a bearer token for the current-user request', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(
      JSON.stringify({ id: 'user-id', email: 'student@example.com', roles: [] }),
      { status: 200, headers: { 'Content-Type': 'application/json' } },
    ));
    vi.stubGlobal('fetch', fetchMock);

    const client = createApiClient('http://localhost:8080/');
    const user = await client.currentUser('signed-token');

    expect(user.email).toBe('student@example.com');
    expect(fetchMock).toHaveBeenCalledWith('http://localhost:8080/api/users/me', {
      headers: new Headers({ Authorization: 'Bearer signed-token' }),
    });
  });

  it('allows remote workspaces to make authenticated API calls through the shared client', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(
      JSON.stringify({ id: 'project-id' }), { status: 200, headers: { 'Content-Type': 'application/json' } },
    ));
    vi.stubGlobal('fetch', fetchMock);

    const client = createApiClient('http://localhost:8080');
    await client.request('/api/projects', { method: 'GET', token: 'signed-token' });

    const requestOptions = fetchMock.mock.calls[0]?.[1] as RequestInit;
    expect((requestOptions.headers as Headers).get('Authorization')).toBe('Bearer signed-token');
  });

  it('turns an API problem response into a useful error', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(
      JSON.stringify({ detail: 'Invalid email or password' }), { status: 401 },
    )));

    await expect(createApiClient('http://localhost:8080').login({
      email: 'student@example.com',
      password: 'wrong-password',
    })).rejects.toMatchObject({
      name: 'ApiError',
      status: 401,
      message: 'Invalid email or password',
    });
  });
});
