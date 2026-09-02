import Foundation
import GoogleMobileAds
import UIKit

final class PluginNativeAdView: NativeAdView {
    private let isSmall: Bool
    private let attributionLabel = UILabel()
    private let headlineLabel = UILabel()
    private let bodyLabel = UILabel()
    private let advertiserLabel = UILabel()
    private let iconImageView = UIImageView()
    private let actionButton = UIButton(type: .system)
    private let nativeMediaView = MediaView()

    init(nativeAd: NativeAd, template: String, style: [String: Any]) {
        self.isSmall = template == "small"
        super.init(frame: .zero)
        clipsToBounds = true
        configure(nativeAd: nativeAd, style: style)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        let padding: CGFloat = 12
        let available = bounds.insetBy(dx: padding, dy: padding)

        attributionLabel.frame = CGRect(x: padding, y: padding, width: 28, height: 20)

        if isSmall {
            layoutDetails(
                in: CGRect(
                    x: available.minX,
                    y: attributionLabel.frame.maxY + 4,
                    width: available.width,
                    height: max(0, available.maxY - attributionLabel.frame.maxY - 4)
                )
            )
        } else {
            let mediaHeight = max(120, available.height * 0.58)
            nativeMediaView.frame = CGRect(
                x: available.minX,
                y: available.minY,
                width: available.width,
                height: min(mediaHeight, available.height)
            )
            layoutDetails(
                in: CGRect(
                    x: available.minX,
                    y: nativeMediaView.frame.maxY + 8,
                    width: available.width,
                    height: max(0, available.maxY - nativeMediaView.frame.maxY - 8)
                )
            )
        }
        bringSubviewToFront(attributionLabel)
    }

    private func layoutDetails(in rect: CGRect) {
        let headlineHeight = min(42, max(22, rect.height * 0.27))
        headlineLabel.frame = CGRect(x: rect.minX, y: rect.minY, width: rect.width, height: headlineHeight)
        let bodyHeight = bodyLabel.isHidden ? 0 : min(38, max(18, rect.height * 0.22))
        bodyLabel.frame = CGRect(x: rect.minX, y: headlineLabel.frame.maxY, width: rect.width, height: bodyHeight)

        let footerY = bodyLabel.frame.maxY + 4
        let footerHeight = max(0, rect.maxY - footerY)
        let iconSize = min(32, footerHeight)
        iconImageView.frame = CGRect(x: rect.minX, y: footerY, width: iconSize, height: iconSize)
        let actionWidth = actionButton.isHidden ? 0 : min(128, rect.width * 0.4)
        actionButton.frame = CGRect(x: rect.maxX - actionWidth, y: footerY, width: actionWidth, height: min(40, footerHeight))
        advertiserLabel.frame = CGRect(
            x: iconImageView.isHidden ? rect.minX : iconImageView.frame.maxX + 6,
            y: footerY,
            width: max(0, actionButton.frame.minX - (iconImageView.isHidden ? rect.minX : iconImageView.frame.maxX + 6) - 6),
            height: min(32, footerHeight)
        )
    }

    private func configure(nativeAd: NativeAd, style: [String: Any]) {
        backgroundColor = color(style["backgroundColor"] as? String) ?? .white
        layer.cornerRadius = number(style["cornerRadius"], fallback: 0)
        layer.borderWidth = number(style["borderWidth"], fallback: 0)
        layer.borderColor = (color(style["borderColor"] as? String) ?? .clear).cgColor

        configureAttribution()

        headlineLabel.text = nativeAd.headline
        headlineLabel.numberOfLines = 2
        headlineLabel.font = .boldSystemFont(
            ofSize: fontSize(style["headlineFontSize"], fallback: 16, range: 12 ... 24)
        )
        headlineLabel.textColor = color(style["headlineColor"] as? String) ?? .black
        addSubview(headlineLabel)
        headlineView = headlineLabel

        bodyLabel.text = nativeAd.body
        bodyLabel.numberOfLines = isSmall ? 1 : 2
        bodyLabel.font = .systemFont(ofSize: fontSize(style["bodyFontSize"], fallback: 13, range: 10 ... 18))
        bodyLabel.textColor = color(style["bodyColor"] as? String) ?? .darkGray
        bodyLabel.isHidden = isSmall || nativeAd.body == nil
        addSubview(bodyLabel)
        bodyView = bodyLabel

        advertiserLabel.text = nativeAd.advertiser
        advertiserLabel.font = .systemFont(ofSize: 11)
        advertiserLabel.numberOfLines = 1
        advertiserLabel.isHidden = nativeAd.advertiser == nil
        addSubview(advertiserLabel)
        advertiserView = advertiserLabel

        iconImageView.image = nativeAd.icon?.image
        iconImageView.contentMode = .scaleAspectFill
        iconImageView.clipsToBounds = true
        iconImageView.isHidden = nativeAd.icon == nil
        addSubview(iconImageView)
        iconView = iconImageView

        actionButton.setTitle(nativeAd.callToAction, for: .normal)
        actionButton.titleLabel?.font = .systemFont(
            ofSize: fontSize(style["callToActionFontSize"], fallback: 13, range: 12 ... 18)
        )
        actionButton.setTitleColor(color(style["callToActionTextColor"] as? String) ?? .white, for: .normal)
        actionButton.backgroundColor = color(style["callToActionBackgroundColor"] as? String) ?? UIColor(
            red: 33 / 255,
            green: 150 / 255,
            blue: 243 / 255,
            alpha: 1
        )
        actionButton.isUserInteractionEnabled = false
        actionButton.isHidden = nativeAd.callToAction == nil
        addSubview(actionButton)
        callToActionView = actionButton

        configureMedia(nativeAd)

        self.nativeAd = nativeAd
    }

    private func configureAttribution() {
        attributionLabel.text = "Ad"
        attributionLabel.textAlignment = .center
        attributionLabel.font = .boldSystemFont(ofSize: 10)
        attributionLabel.textColor = .white
        attributionLabel.backgroundColor = .darkGray
        addSubview(attributionLabel)
    }

    private func configureMedia(_ nativeAd: NativeAd) {
        guard !isSmall else { return }
        nativeMediaView.mediaContent = nativeAd.mediaContent
        nativeMediaView.contentMode = .scaleAspectFill
        nativeMediaView.clipsToBounds = true
        addSubview(nativeMediaView)
        mediaView = nativeMediaView
    }

    private func color(_ value: String?) -> UIColor? {
        guard var value = value?.trimmingCharacters(in: .whitespacesAndNewlines) else { return nil }
        if value.hasPrefix("#") { value.removeFirst() }
        guard value.count == 6 || value.count == 8, let number = UInt64(value, radix: 16) else { return nil }
        if value.count == 8 {
            return UIColor(
                red: CGFloat((number >> 24) & 0xff) / 255,
                green: CGFloat((number >> 16) & 0xff) / 255,
                blue: CGFloat((number >> 8) & 0xff) / 255,
                alpha: CGFloat(number & 0xff) / 255
            )
        }
        return UIColor(
            red: CGFloat((number >> 16) & 0xff) / 255,
            green: CGFloat((number >> 8) & 0xff) / 255,
            blue: CGFloat(number & 0xff) / 255,
            alpha: 1
        )
    }

    private func number(_ value: Any?, fallback: CGFloat) -> CGFloat {
        max(0, CGFloat((value as? NSNumber)?.doubleValue ?? Double(fallback)))
    }

    private func fontSize(_ value: Any?, fallback: CGFloat, range: ClosedRange<CGFloat>) -> CGFloat {
        min(range.upperBound, max(range.lowerBound, number(value, fallback: fallback)))
    }
}
