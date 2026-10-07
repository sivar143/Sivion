import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient } from '@angular/common/http';
import { provideAppInitializer, inject } from '@angular/core';
import { AppComponent } from '../app.component';
import { AuthService } from '../services/auth.service';
bootstrapApplication(AppComponent,{providers:[provideHttpClient(),provideAppInitializer(()=>inject(AuthService).init())]}).catch(err=>console.error(err));
