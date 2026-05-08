export enum AdValuePrecision {
  Unknown = 0,
  Estimated = 1,
  PublisherProvided = 2,
  Precise = 3,
}

export interface AdMobRevenueData {
  adUnitId: string;
  valueMicros: number;
  currencyCode: string;
  precision: AdValuePrecision;
  networkName: string;
  impressionId: string;
}
