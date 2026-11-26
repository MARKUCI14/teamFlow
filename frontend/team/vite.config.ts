import { federation } from '@module-federation/vite';
import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

export default defineConfig({
  plugins: [
    react(),
    federation({
      name: 'teamflow_team',
      dts: false,
      filename: 'remoteEntry.js',
      exposes: { './RemoteApp': './src/RemoteApp.tsx' },
      shared: { react: { singleton: true }, 'react/': { singleton: true } },
    }),
  ],
  server: { host: '127.0.0.1', port: 5175, cors: true },
  build: { target: 'chrome89' },
});
