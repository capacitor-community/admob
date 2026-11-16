require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name = 'CapacitorCommunityAdmob'
  s.version = package['version']
  s.summary = package['description']
  s.license = package['license']
  s.homepage = package['repository']['url']
  s.author = package['author']
  s.source = { :git => package['repository']['url'], :tag => s.version.to_s }
  s.source_files = 'ios/Sources/**/*.{swift,h,m,c,cc,mm,cpp}'
  s.resource_bundles = { 'CapacitorCommunityAdmobPrivacy' => ['ios/Sources/AdMobPlugin/PrivacyInfo.xcprivacy'] }
  s.ios.deployment_target = '15.0'
  s.swift_version = '5.1'
  s.static_framework = true
  s.dependency 'Capacitor'
  s.dependency 'WebViewNativeUIEngine', '>= 1.0.10', '< 2.0'
  s.dependency 'Google-Mobile-Ads-SDK', '13.11.0'
  s.dependency 'GoogleUserMessagingPlatform', '~> 3.1'
end
