export interface Credentials {
  email: string;
  password: string;
}

export interface AccessToken {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface CurrentUser {
  id: string;
  email: string;
  roles: string[];
}

export interface ApiRequestOptions extends RequestInit {
  token?: string;
}

export interface WorkspaceProps {
  user: CurrentUser;
  token: string;
  api: ApiClient;
}

export interface ApiClient {
  request<T>(path: string, options?: ApiRequestOptions): Promise<T>;
  login(credentials: Credentials): Promise<AccessToken>;
  register(credentials: Credentials): Promise<AccessToken>;
  currentUser(token: string): Promise<CurrentUser>;
}

export class ApiError extends Error {
  constructor(public readonly status: number, message: string) {
    super(message);
    this.name = 'ApiError';
  }
}

export function createApiClient(baseUrl: string) {
  const base = baseUrl.replace(/\/$/, '');

  async function request<T>(path: string, options: ApiRequestOptions = {}): Promise<T> {
    const { token, ...init } = options;
    const headers = new Headers(init.headers);
    if (init.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json');
    if (token) headers.set('Authorization', `Bearer ${token}`);

    const response = await fetch(`${base}${path}`, { ...init, headers });
    if (!response.ok) {
      let message = `Request failed (${response.status})`;
      try {
        const problem = (await response.json()) as { detail?: string; message?: string };
        message = problem.detail ?? problem.message ?? message;
      } catch {
        // Some gateway errors have no JSON response body.
      }
      throw new ApiError(response.status, message);
    }
    return (await response.json()) as T;
  }

  return {
    request,
    login: (credentials: Credentials) => request<AccessToken>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
    }),
    register: (credentials: Credentials) => request<AccessToken>('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(credentials),
    }),
    currentUser: (token: string) => request<CurrentUser>('/api/users/me', { token }),
  };
}
