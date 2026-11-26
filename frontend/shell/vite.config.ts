import { federation } from '@module-federation/vite';
import react from '@vitejs/plugin-react';
import { defineConfig, loadEnv } from 'vite';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '.', 'VITE_');
  return {
    plugins: [
      react(),
      federation({
        name: 'teamflow_shell',
        dts: false,
        remotes: {
          stakeholder: {
            type: 'module',
            name: 'teamflow_stakeholder',
            entry: env.VITE_STAKEHOLDER_REMOTE_URL ?? 'http://localhost:5174/remoteEntry.js',
          },
          team: {
            type: 'module',
            name: 'teamflow_team',
            entry: env.VITE_TEAM_REMOTE_URL ?? 'http://localhost:5175/remoteEntry.js',
          },
        },
        shared: {
          react: { singleton: true },
          'react/': { singleton: true },
          'react-router-dom': { singleton: true },
        },
      }),
    ],
    server: { host: '127.0.0.1', port: 5173 },
    build: { target: 'chrome89' },
  };
});
