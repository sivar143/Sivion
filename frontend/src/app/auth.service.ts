import Keycloak from 'keycloak-js';
import { Injectable } from '@angular/core';

@Injectable({providedIn:'root'})
export class AuthService {
  private readonly keycloak = new Keycloak({url:'http://localhost:8081',realm:'sivion',clientId:'sivion-web'});
  private ready=false;
  async init(): Promise<void> { if(this.ready) return; await this.keycloak.init({onLoad:'login-required',pkceMethod:'S256',checkLoginIframe:false}); this.ready=true; }
  async token(): Promise<string> { await this.keycloak.updateToken(30); return this.keycloak.token ?? ''; }
  logout(){return this.keycloak.logout({redirectUri:window.location.origin});}
  get username(){return this.keycloak.tokenParsed?.['preferred_username'] ?? 'User';}
}
