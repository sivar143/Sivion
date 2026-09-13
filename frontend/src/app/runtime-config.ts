export interface SivionRuntimeConfig {
  keycloakUrl?: string;
  keycloakRealm?: string;
  keycloakClientId?: string;
}

declare global {
  interface Window {
    __SIVION_CONFIG__?: SivionRuntimeConfig;
  }
}

const localHost = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';

export const SIVION_CONFIG: Required<SivionRuntimeConfig> = {
  keycloakUrl: window.__SIVION_CONFIG__?.keycloakUrl ?? (localHost ? 'http://localhost:8081' : `${window.location.origin}/auth`),
  keycloakRealm: window.__SIVION_CONFIG__?.keycloakRealm ?? 'sivion',
  keycloakClientId: window.__SIVION_CONFIG__?.keycloakClientId ?? 'sivion-web'
};
