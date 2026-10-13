import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../../services/auth.service';

export interface EmployeeDocumentRecord {
  id: number;
  employeeId: number;
  originalName: string;
  contentType: string;
  fileSize: number;
  description?: string;
  uploadedBy: string;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class EmployeeDocumentsApi {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly url = '/api/v1/hr/documents';

  private async headers(): Promise<HttpHeaders> {
    return new HttpHeaders({
      Authorization: `Bearer ${await this.auth.token()}`,
      'X-Tenant-ID': '1'
    });
  }

  async list(): Promise<EmployeeDocumentRecord[]> {
    return firstValueFrom(this.http.get<EmployeeDocumentRecord[]>(this.url, { headers: await this.headers() }));
  }

  async upload(employeeId: number, description: string, file: File): Promise<EmployeeDocumentRecord> {
    const form = new FormData();
    form.append('employeeId', String(employeeId));
    form.append('description', description);
    form.append('file', file, file.name);
    return firstValueFrom(this.http.post<EmployeeDocumentRecord>(this.url, form, { headers: await this.headers() }));
  }

  async download(id: number): Promise<Blob> {
    return firstValueFrom(this.http.get(`${this.url}/${id}/download`, {
      headers: await this.headers(),
      responseType: 'blob'
    }));
  }
}
