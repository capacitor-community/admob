import UIKit
import WebKit
import WebViewNativeUIEngine

/// Research-preview binding to a single HTML overflow scroll container.
final class NativeAdScrollTracker {
    private weak var webView: WKWebView?
    private weak var scrollView: UIScrollView?
    private let placements: [NativeAdPlacementValue]
    private let clip: CGRect
    private let render: ([NativeAdPlacementValue]) -> Void
    private var observation: NSKeyValueObservation?
    let hosting: NativeUIScrollHosting

    init?(
        container: [String: Any],
        webView: WKWebView,
        placements: [NativeAdPlacementValue],
        previousHosting: NativeUIScrollHosting? = nil,
        render: @escaping ([NativeAdPlacementValue]) -> Void
    ) {
        guard
            let rectValue = container["rect"] as? [String: Any],
            let clipValue = container["clipRect"] as? [String: Any],
            let rect = NativeAdPlacementValue.rect(rectValue),
            let clip = NativeAdPlacementValue.rect(clipValue),
            let width = container["contentWidth"] as? Double,
            let height = container["contentHeight"] as? Double,
            [rect.minX, rect.minY, rect.width, rect.height,
             clip.minX, clip.minY, clip.width, clip.height, width, height].allSatisfy({ $0.isFinite }),
            rect.width > 0, rect.height > 0
        else { return nil }

        let candidates = Self.scrollViews(in: webView).filter { scroll in
            let frame = scroll.convert(scroll.bounds, to: webView)
            return scroll !== webView.scrollView && !scroll.isHidden && scroll.window != nil &&
                abs(scroll.zoomScale - 1) < 0.001 &&
                abs(frame.minX - rect.minX) < 2 && abs(frame.minY - rect.minY) < 2 &&
                abs(frame.width - rect.width) < 2 && abs(frame.height - rect.height) < 2 &&
                abs(scroll.contentSize.width - width) < 3 && abs(scroll.contentSize.height - height) < 3
        }
        guard candidates.count == 1, let scroll = candidates.first else { return nil }
        guard let hosting = previousHosting.flatMap({ $0.matches(scroll, in: webView) ? $0 : nil }) ??
            NativeUIScrollHosting(webView: webView, scrollView: scroll) else { return nil }
        self.hosting = hosting
        self.webView = webView
        self.scrollView = scroll
        self.placements = placements
        self.clip = clip
        self.render = render
        observation = scroll.observe(\.contentOffset, options: [.new]) { [weak self] _, _ in
            self?.update()
        }
    }

    func host(_ view: UIView, frame: CGRect) -> Bool {
        hosting.place(view, frame: frame)
    }

    func update() {
        guard let webView, let scrollView, scrollView.isDescendant(of: webView) else {
            render([])
            return
        }
        let frame = scrollView.convert(scrollView.bounds, to: webView)
        let viewport = clip.intersection(webView.bounds).intersection(frame)
        let origin = CGPoint(x: frame.minX - scrollView.contentOffset.x, y: frame.minY - scrollView.contentOffset.y)
        render(placements.compactMap { $0.projected(origin: origin, viewport: viewport) })
    }

    private static func scrollViews(in view: UIView) -> [UIScrollView] {
        view.subviews.flatMap { child in
            ((child as? UIScrollView).map { [$0] } ?? []) + scrollViews(in: child)
        }
    }
}
