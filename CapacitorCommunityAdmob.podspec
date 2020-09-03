
  Pod::Spec.new do |s|
    s.name = 'CapacitorCommunityAdmob'
    s.version = '0.0.1'
    s.summary = 'A native plugin for AdMob'
    s.license = 'MIT'
    s.homepage = 'git@github.com:capacitor-community/admob.git'
    s.author = 'Masahiko Sakakibara <sakakibara@rdlabo.jp>'
    s.source = { :git => 'git@github.com:capacitor-community/admob.git', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '11.0'
    s.static_framework = true
    s.dependency 'Capacitor'
    s.dependency 'Google-Mobile-Ads-SDK', '>= 7.64.0'
  end
