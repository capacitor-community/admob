/**
 * The precision of an impression-level ad value.
 */
export enum AdValuePrecision {
  /**
   * The ad value precision is unknown.
   */
  Unknown = 0,

  /**
   * The ad value is estimated from aggregated data.
   */
  Estimated = 1,

  /**
   * The ad value was provided by the publisher.
   */
  PublisherProvided = 2,

  /**
   * The ad value is the precise value paid for this ad.
   */
  Precise = 3,
}

/**
 * Impression-level ad revenue data emitted by a paid event.
 */
export interface AdMobRevenueData {
  /**
   * The ad unit ID associated with the paid event.
   */
  adUnitId: string;

  /**
   * The ad value in micros, where 1,000,000 micros equals one currency unit.
   */
  valueMicros: number;

  /**
   * The ISO 4217 currency code for `valueMicros`.
   */
  currencyCode: string;

  /**
   * The precision of the reported ad value.
   */
  precision: AdValuePrecision;

  /**
   * The mediation adapter class name that served the impression, or an empty string when unavailable.
   */
  networkName: string;

  /**
   * The response identifier associated with the impression, or an empty string when unavailable.
   */
  impressionId: string;
}
