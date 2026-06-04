import { useEffect, useState } from 'react';
import {
  startSeanimeServer,
  isSeanimeServerRunning,
  getSeanimeServerStatus,
} from '@/modules/expo-seanime-server/src';

export const useServerInitialization = () => {
  const [serverReady, setServerReady] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isInitializing, setIsInitializing] = useState(true);

  useEffect(() => {
    const initializeServer = async () => {
      try {
        setIsInitializing(true);
        setError(null);

        // Check if server is already running
        const isRunning = await isSeanimeServerRunning();
        if (!isRunning) {
          console.log('[Server] Starting embedded Seanime server...');
          const result = await startSeanimeServer();
          console.log('[Server] Start result:', result);

          if (!result.success) {
            throw new Error(result.message || 'Failed to start server');
          }
        } else {
          console.log('[Server] Server already running');
        }

        // Wait for server to be fully initialized
        await new Promise((resolve) => setTimeout(resolve, 2000));

        // Verify server status
        const status = await getSeanimeServerStatus();
        console.log('[Server] Server status:', status);

        if (status.running) {
          setServerReady(true);
          console.log('[Server] Server is ready on localhost:43211');
        } else {
          throw new Error('Server failed to initialize');
        }
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Unknown error';
        console.error('[Server] Initialization failed:', errorMessage);
        setError(errorMessage);
        // Still consider it ready after timeout to avoid infinite loading
        setTimeout(() => setServerReady(true), 5000);
      } finally {
        setIsInitializing(false);
      }
    };

    initializeServer();
  }, []);

  return { serverReady, error, isInitializing };
};
