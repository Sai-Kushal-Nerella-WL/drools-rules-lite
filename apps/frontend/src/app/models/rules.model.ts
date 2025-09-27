export enum ColumnType {
  NAME = 'NAME',
  CONDITION = 'CONDITION',
  ACTION = 'ACTION'
}

export interface DecisionTableMeta {
  ruleSet: string;
  importTypes: string[];
  ruleTableName: string;
}

export interface TemplateCell {
  columnIndex: number;
  type: ColumnType;
  template: string;
}

export interface TableRow {
  name: string;
  values: any[];
}

export interface DecisionTable {
  meta: DecisionTableMeta;
  headers: ColumnType[];
  templates: TemplateCell[];
  rows: TableRow[];
}

export interface ValidationError {
  row?: number;
  col?: number;
  message: string;
}

export interface ValidationResult {
  ok: boolean;
  errors: ValidationError[];
}
