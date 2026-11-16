import UIKit

/// UIKit can expand a presented controller's root WebView when returning from an ad.
/// Preserve the host application's frame, including a non-overlaying status bar.
final class FullScreenAdWebViewLayout {
    private weak var webView: UIView?
    private weak var parent: UIView?
    private let frame: CGRect
    private let parentSize: CGSize

    init?(webView: UIView?) {
        guard let webView = webView, let parent = webView.superview else { return nil }
        self.webView = webView
        self.parent = parent
        self.frame = webView.frame
        self.parentSize = parent.bounds.size
    }

    func restore() {
        guard let webView = webView, let parent = parent,
              webView.superview === parent, parent.bounds.size == parentSize else { return }
        webView.frame = frame
    }
}
