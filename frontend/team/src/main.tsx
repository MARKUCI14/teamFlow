import { createRoot } from 'react-dom/client';
import { createApiClient, type CurrentUser } from '@teamflow/shared';
import TeamRemote from './RemoteApp';

const previewUser: CurrentUser = { id: 'local-preview', email: 'student@example.com', roles: [] };
const previewApi = createApiClient('http://localhost:8080');

createRoot(document.getElementById('root')!).render(<TeamRemote user={previewUser} token="local-preview" api={previewApi} />);
