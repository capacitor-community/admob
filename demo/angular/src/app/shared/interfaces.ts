export interface ITestItems  {
  type: 'method' | 'event'
  name: string;
  result?: boolean;
  expect?: number | string | string[] | number[];
}
