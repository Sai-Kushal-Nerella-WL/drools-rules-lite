import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatInputModule } from '@angular/material/input';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';

import { RulesService } from '../../services/rules.service';
import { DecisionTable, ColumnType, TableRow, TemplateCell, ValidationError } from '../../models/rules.model';

@Component({
  selector: 'app-rules-editor',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatToolbarModule,
    MatButtonModule,
    MatTableModule,
    MatInputModule,
    MatSlideToggleModule,
    MatSidenavModule,
    MatListModule,
    MatChipsModule,
    MatIconModule,
    MatCardModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './rules-editor.component.html',
  styleUrls: ['./rules-editor.component.scss']
})
export class RulesEditorComponent implements OnInit {
  decisionTable: DecisionTable | null = null;
  validationErrors: ValidationError[] = [];
  hasUnsavedChanges = false;
  isLoading = false;
  
  displayedColumns: string[] = [];
  ColumnType = ColumnType;

  constructor(
    private rulesService: RulesService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.loadRules();
  }

  loadRules() {
    this.isLoading = true;
    this.rulesService.getRules().subscribe({
      next: (table) => {
        this.decisionTable = table;
        this.updateDisplayedColumns();
        this.hasUnsavedChanges = false;
        this.snackBar.open('Rules loaded successfully', 'Close', { duration: 3000 });
      },
      error: (error) => {
        console.error('Error loading rules:', error);
        this.snackBar.open('Error loading rules', 'Close', { duration: 5000 });
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  validateRules() {
    if (!this.decisionTable) return;
    
    this.isLoading = true;
    this.rulesService.validateRules(this.decisionTable).subscribe({
      next: (result) => {
        this.validationErrors = result.errors;
        if (result.ok) {
          this.snackBar.open('Validation passed', 'Close', { duration: 3000 });
        } else {
          this.snackBar.open(`Validation failed: ${result.errors.length} errors`, 'Close', { duration: 5000 });
        }
      },
      error: (error) => {
        console.error('Error validating rules:', error);
        this.snackBar.open('Error validating rules', 'Close', { duration: 5000 });
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  saveRules() {
    if (!this.decisionTable) return;
    
    this.isLoading = true;
    this.rulesService.saveRules(this.decisionTable).subscribe({
      next: (result) => {
        if (result.ok) {
          this.hasUnsavedChanges = false;
          this.validationErrors = [];
          this.snackBar.open('Rules saved successfully', 'Close', { duration: 3000 });
        } else {
          this.validationErrors = result.errors;
          this.snackBar.open(`Save failed: ${result.errors.length} validation errors`, 'Close', { duration: 5000 });
        }
      },
      error: (error) => {
        console.error('Error saving rules:', error);
        this.snackBar.open('Error saving rules', 'Close', { duration: 5000 });
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  addRow() {
    if (!this.decisionTable) return;
    
    const newRow: TableRow = {
      name: `Rule ${this.decisionTable.rows.length + 1}`,
      values: new Array(this.decisionTable.headers.length - 1).fill(null)
    };
    
    this.decisionTable.rows.push(newRow);
    this.hasUnsavedChanges = true;
  }

  cloneRow(index: number) {
    if (!this.decisionTable || index < 0 || index >= this.decisionTable.rows.length) return;
    
    const originalRow = this.decisionTable.rows[index];
    const clonedRow: TableRow = {
      name: `${originalRow.name} (Copy)`,
      values: [...originalRow.values]
    };
    
    this.decisionTable.rows.splice(index + 1, 0, clonedRow);
    this.hasUnsavedChanges = true;
  }

  deleteRow(index: number) {
    if (!this.decisionTable || index < 0 || index >= this.decisionTable.rows.length) return;
    
    this.decisionTable.rows.splice(index, 1);
    this.hasUnsavedChanges = true;
  }

  onCellValueChange() {
    this.hasUnsavedChanges = true;
  }

  getInputType(columnIndex: number): string {
    if (!this.decisionTable) return 'text';
    
    const template = this.decisionTable.templates.find(t => t.columnIndex === columnIndex);
    if (!template) return 'text';
    
    const templateStr = template.template.toLowerCase();
    
    if (templateStr.includes('>=') || templateStr.includes('<=') || 
        templateStr.includes('>') || templateStr.includes('<')) {
      return 'number';
    } else if (templateStr.includes('== $param') && 
               (templateStr.includes('true') || templateStr.includes('false'))) {
      return 'boolean';
    }
    
    return 'text';
  }

  isBooleanInput(columnIndex: number): boolean {
    return this.getInputType(columnIndex) === 'boolean';
  }

  isNumericInput(columnIndex: number): boolean {
    return this.getInputType(columnIndex) === 'number';
  }

  getTemplateForColumn(columnIndex: number): string {
    if (!this.decisionTable) return '';
    
    const template = this.decisionTable.templates.find(t => t.columnIndex === columnIndex);
    return template ? template.template : '';
  }

  getErrorsForCell(rowIndex: number, colIndex: number): ValidationError[] {
    return this.validationErrors.filter(error => 
      error.row === rowIndex && error.col === colIndex
    );
  }

  getGeneralErrors(): ValidationError[] {
    return this.validationErrors.filter(error => 
      error.row === null || error.row === undefined
    );
  }

  getRowSpecificErrors(): ValidationError[] {
    return this.validationErrors.filter(error => 
      error.row !== null && error.row !== undefined
    );
  }

  private updateDisplayedColumns() {
    if (!this.decisionTable) return;
    
    this.displayedColumns = ['actions'];
    this.decisionTable.headers.forEach((header, index) => {
      this.displayedColumns.push(`col_${index}`);
    });
  }

  trackByIndex(index: number): number {
    return index;
  }
}
