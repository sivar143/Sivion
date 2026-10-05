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

// Local Docker Compose exposes Keycloak on the host at 8081. Keep this value
// authoritative for local browser sessions so a stale/deployment runtime
// configuration cannot accidentally send OAuth requests to the backend (8080).
// Runtime configuration remains available for non-local deployments.
export const SIVION_CONFIG: Required<SivionRuntimeConfig> = {
  keycloakUrl: localHost ? 'http://localhost:8081' : (window.__SIVION_CONFIG__?.keycloakUrl ?? `${window.location.origin}/auth`),
  keycloakRealm: window.__SIVION_CONFIG__?.keycloakRealm ?? 'sivion',
  keycloakClientId: window.__SIVION_CONFIG__?.keycloakClientId ?? 'sivion-web'
};
