import Keycloak from 'keycloak-js';
import { Injectable } from '@angular/core';
import { SIVION_CONFIG } from './runtime-config';

@Injectable({providedIn:'root'})
export class AuthService {
  private readonly keycloak = new Keycloak({url:SIVION_CONFIG.keycloakUrl,realm:SIVION_CONFIG.keycloakRealm,clientId:SIVION_CONFIG.keycloakClientId});
  private ready=false;

  async init(): Promise<void> {
    if(this.ready) return;
    await this.keycloak.init({onLoad:'login-required',pkceMethod:'S256',checkLoginIframe:false});
    this.ready=true;
  }

  async token(): Promise<string> {
    if(!this.ready) await this.init();
    await this.keycloak.updateToken(30);
    if(!this.keycloak.token) throw new Error('Authentication token is unavailable');
    return this.keycloak.token;
  }

  logout(){return this.keycloak.logout({redirectUri:window.location.origin});}
  get username(){return this.keycloak.tokenParsed?.['preferred_username'] ?? 'User';}
  get roles(): string[] {return (this.keycloak.tokenParsed?.['realm_access'] as {roles?: string[]}|undefined)?.roles ?? [];}
  hasRole(role: string): boolean { return this.roles.includes(role); }
}
