export interface ValidationTestItem {
  type: 'method' | 'event';
  name: string;
  result?: boolean;
  expect?: number | string | string[] | number[];
}
