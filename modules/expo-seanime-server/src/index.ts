import { NativeModule, requireNativeModule } from 'expo';

declare class ExpoSeanimeServerModule extends NativeModule {
  startServer(): Promise<{ success: boolean; message: string }>;
  stopServer(): Promise<{ success: boolean; message: string }>;
  isServerRunning(): Promise<boolean>;
  getServerStatus(): Promise<{ running: boolean; processId: number }>;
}

// This calls the native module we defined above
const ExpoSeanimeServer = requireNativeModule('ExpoSeanimeServer') as ExpoSeanimeServerModule;

export async function startSeanimeServer() {
  return ExpoSeanimeServer.startServer();
}

export async function stopSeanimeServer() {
  return ExpoSeanimeServer.stopServer();
}

export async function isSeanimeServerRunning() {
  return ExpoSeanimeServer.isServerRunning();
}

export async function getSeanimeServerStatus() {
  return ExpoSeanimeServer.getServerStatus();
}
