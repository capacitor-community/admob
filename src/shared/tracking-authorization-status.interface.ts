/**
 * @url https://developer.apple.com/documentation/apptrackingtransparency/attrackingmanager/authorizationstatus
 */
export interface TrackingAuthorizationStatusInterface {
  status:  'authorized' | 'denied' | 'notDetermined' | 'restricted';
}
