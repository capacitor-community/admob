// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorCommunityAdmob",
    platforms: [.iOS(.v14)],
    products: [
        .library(
            name: "CapacitorCommunityAdmob",
            targets: ["AdMobPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "7.0.0"),
        .package(url: "https://github.com/googleads/swift-package-manager-google-mobile-ads.git", from: "11.3.0")
    ],
    targets: [
        .target(
            name: "AdMobPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm"),
                .product(name: "GoogleMobileAds", package: "swift-package-manager-google-mobile-ads")
            ],
            path: "ios/Sources/AdMobPlugin"),
        .testTarget(
            name: "AdMobPluginTests",
            dependencies: ["AdMobPlugin"],
            path: "ios/Tests/AdMobPluginTests")
    ]
)
