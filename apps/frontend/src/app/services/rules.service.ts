import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DecisionTable, ValidationResult } from '../models/rules.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class RulesService {
  private apiUrl = environment.apiBase || 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  getRules(): Observable<DecisionTable> {
    return this.http.get<DecisionTable>(`${this.apiUrl}/rules`);
  }

  validateRules(table: DecisionTable): Observable<ValidationResult> {
    return this.http.post<ValidationResult>(`${this.apiUrl}/rules/validate`, table);
  }

  saveRules(table: DecisionTable): Observable<ValidationResult> {
    return this.http.post<ValidationResult>(`${this.apiUrl}/rules/save`, table);
  }
}
