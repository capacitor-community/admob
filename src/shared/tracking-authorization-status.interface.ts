/**
 * The current iOS App Tracking Transparency authorization status.
 *
 * @see https://developer.apple.com/documentation/apptrackingtransparency/attrackingmanager/authorizationstatus
 */
export interface TrackingAuthorizationStatusInterface {
  /**
   * The authorization status reported by App Tracking Transparency.
   */
  status:  'authorized' | 'denied' | 'notDetermined' | 'restricted';
}
