package cc.typex.app.web

import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Message
import android.view.KeyEvent
import android.webkit.ClientCertRequest
import android.webkit.HttpAuthHandler
import android.webkit.RenderProcessGoneDetail
import android.webkit.SafeBrowsingResponse
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import timber.log.Timber
import javax.inject.Inject

class AppWebViewClient @Inject constructor() : WebViewClient() {
    /**
     * Give the host application a chance to take control when a URL is about to be loaded in the
     * current WebView. If a WebViewClient is not provided, by default WebView will ask Activity
     * Manager to choose the proper handler for the URL. If a WebViewClient is provided, returning
     * `true` causes the current WebView to abort loading the URL, while returning
     * `false` causes the WebView to continue loading the URL as usual.
     *
     *
     * **Note:** Do not call [WebView.loadUrl] with the same
     * URL and then return `true`. This unnecessarily cancels the current load and starts a
     * new load with the same URL. The correct way to continue loading a given URL is to simply
     * return `false`, without calling [WebView.loadUrl].
     *
     *
     * **Note:** This method is not called for POST requests.
     *
     *
     * **Note:** This method may be called for subframes and with non-HTTP(S)
     * schemes; calling [WebView.loadUrl] with such a URL will fail.
     *
     * @param view The WebView that is initiating the callback.
     * @param url The URL to be loaded.
     * @return `true` to cancel the current load, otherwise return `false`.
     */
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        Timber.v("shouldOverrideUrlLoading: $url")
        return super.shouldOverrideUrlLoading(view, url)
    }

    /**
     * Give the host application a chance to take control when a URL is about to be loaded in the
     * current WebView. If a WebViewClient is not provided, by default WebView will ask Activity
     * Manager to choose the proper handler for the URL. If a WebViewClient is provided, returning
     * `true` causes the current WebView to abort loading the URL, while returning
     * `false` causes the WebView to continue loading the URL as usual.
     *
     *
     * This callback is not called for all page navigations. In particular, this is not called
     * for navigations which the app initiated with `loadUrl()`: this callback would not serve
     * a purpose in this case, because the app already knows about the navigation. This callback
     * lets the app know about navigations initiated by the web page (such as navigations initiated
     * by JavaScript code), by the user (such as when the user taps on a link), or by an HTTP
     * redirect (ex. if `loadUrl("foo.com")` redirects to `"bar.com"` because of HTTP
     * 301).
     *
     *
     * **Note:** Do not call [WebView.loadUrl] with the request's
     * URL and then return `true`. This unnecessarily cancels the current load and starts a
     * new load with the same URL. The correct way to continue loading a given URL is to simply
     * return `false`, without calling [WebView.loadUrl].
     *
     *
     * **Note:** This method is not called for POST requests.
     *
     *
     * **Note:** This method may be called for subframes and with non-HTTP(S)
     * schemes; calling [WebView.loadUrl] with such a URL will fail.
     *
     * @param view The WebView that is initiating the callback.
     * @param request Object containing the details of the request.
     * @return `true` to cancel the current load, otherwise return `false`.
     */
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        Timber.v("shouldOverrideUrlLoading: $request")
        return super.shouldOverrideUrlLoading(view, request)
    }

    /**
     * Notify the host application that a page has started loading. This method
     * is called once for each main frame load so a page with iframes or
     * framesets will call onPageStarted one time for the main frame. This also
     * means that onPageStarted will not be called when the contents of an
     * embedded frame changes, i.e. clicking a link whose target is an iframe,
     * it will also not be called for fragment navigations (navigations to
     * #fragment_id).
     *
     * @param view The WebView that is initiating the callback.
     * @param url The url to be loaded.
     * @param favicon The favicon for this page if it already exists in the
     * database.
     */
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        Timber.v("onPageStarted: $url $favicon")
    }

    /**
     * Notify the host application that a page has finished loading. This method
     * is called only for main frame. Receiving an `onPageFinished()` callback does not
     * guarantee that the next frame drawn by WebView will reflect the state of the DOM at this
     * point. In order to be notified that the current DOM state is ready to be rendered, request a
     * visual state callback with [WebView.postVisualStateCallback] and wait for the supplied
     * callback to be triggered.
     *
     * @param view The WebView that is initiating the callback.
     * @param url The url of the page.
     */
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        Timber.v("onPageFinished: $url")
    }

    /**
     * Notify the host application that the WebView will load the resource
     * specified by the given url.
     *
     * @param view The WebView that is initiating the callback.
     * @param url The url of the resource the WebView will load.
     */
    override fun onLoadResource(view: WebView?, url: String?) {
        super.onLoadResource(view, url)
        Timber.v("onLoadResource: $url")
    }

    /**
     * Notify the host application that [android.webkit.WebView] content left over from
     * previous page navigations will no longer be drawn.
     *
     *
     * This callback can be used to determine the point at which it is safe to make a recycled
     * [android.webkit.WebView] visible, ensuring that no stale content is shown. It is called
     * at the earliest point at which it can be guaranteed that [WebView.onDraw] will no
     * longer draw any content from previous navigations. The next draw will display either the
     * [background color][WebView.setBackgroundColor] of the [WebView], or some of the
     * contents of the newly loaded page.
     *
     *
     * This method is called when the body of the HTTP response has started loading, is reflected
     * in the DOM, and will be visible in subsequent draws. This callback occurs early in the
     * document loading process, and as such you should expect that linked resources (for example,
     * CSS and images) may not be available.
     *
     *
     * For more fine-grained notification of visual state updates, see [ ][WebView.postVisualStateCallback].
     *
     *
     * Please note that all the conditions and recommendations applicable to
     * [WebView.postVisualStateCallback] also apply to this API.
     *
     *
     * This callback is only called for main frame navigations.
     *
     * @param view The [android.webkit.WebView] for which the navigation occurred.
     * @param url  The URL corresponding to the page navigation that triggered this callback.
     */
    override fun onPageCommitVisible(view: WebView?, url: String?) {
        super.onPageCommitVisible(view, url)
        Timber.v("onPageCommitVisible: $url")
    }

    /**
     * Notify the host application of a resource request and allow the
     * application to return the data.  If the return value is `null`, the WebView
     * will continue to load the resource as usual.  Otherwise, the return
     * response and data will be used.
     *
     *
     * This callback is invoked for a variety of URL schemes (e.g., `http(s):`, `data:`, `file:`, etc.), not only those schemes which send requests over the network.
     * This is not called for `javascript:` URLs, `blob:` URLs, or for assets accessed
     * via `file:///android_asset/` or `file:///android_res/` URLs.
     *
     *
     * In the case of redirects, this is only called for the initial resource URL, not any
     * subsequent redirect URLs.
     *
     *
     * **Note:** This method is called on a thread
     * other than the UI thread so clients should exercise caution
     * when accessing private data or the view system.
     *
     *
     * **Note:** When Safe Browsing is enabled, these URLs still undergo Safe
     * Browsing checks. If this is undesired, you can use [WebView.setSafeBrowsingWhitelist]
     * to skip Safe Browsing checks for that host or dismiss the warning in [ ][.onSafeBrowsingHit] by calling [SafeBrowsingResponse.proceed].
     *
     * @param view The [android.webkit.WebView] that is requesting the
     * resource.
     * @param url The raw url of the resource.
     * @return A [android.webkit.WebResourceResponse] containing the
     * response information or `null` if the WebView should load the
     * resource itself.
     */
    override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
        Timber.v("shouldInterceptRequest: $url")
        return super.shouldInterceptRequest(view, url)
    }

    /**
     * Notify the host application of a resource request and allow the
     * application to return the data.  If the return value is `null`, the WebView
     * will continue to load the resource as usual.  Otherwise, the return
     * response and data will be used.
     *
     *
     * This callback is invoked for a variety of URL schemes (e.g., `http(s):`, `data:`, `file:`, etc.), not only those schemes which send requests over the network.
     * This is not called for `javascript:` URLs, `blob:` URLs, or for assets accessed
     * via `file:///android_asset/` or `file:///android_res/` URLs.
     *
     *
     * In the case of redirects, this is only called for the initial resource URL, not any
     * subsequent redirect URLs.
     *
     *
     * **Note:** This method is called on a thread
     * other than the UI thread so clients should exercise caution
     * when accessing private data or the view system.
     *
     *
     * **Note:** When Safe Browsing is enabled, these URLs still undergo Safe
     * Browsing checks. If this is undesired, you can use [WebView.setSafeBrowsingWhitelist]
     * to skip Safe Browsing checks for that host or dismiss the warning in [ ][.onSafeBrowsingHit] by calling [SafeBrowsingResponse.proceed].
     *
     * @param view The [android.webkit.WebView] that is requesting the
     * resource.
     * @param request Object containing the details of the request.
     * @return A [android.webkit.WebResourceResponse] containing the
     * response information or `null` if the WebView should load the
     * resource itself.
     */
    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        Timber.v("shouldInterceptRequest: $request")
        return super.shouldInterceptRequest(view, request)
    }

    /**
     * Notify the host application that there have been an excessive number of
     * HTTP redirects. As the host application if it would like to continue
     * trying to load the resource. The default behavior is to send the cancel
     * message.
     *
     * @param view The WebView that is initiating the callback.
     * @param cancelMsg The message to send if the host wants to cancel
     * @param continueMsg The message to send if the host wants to continue
     */
    override fun onTooManyRedirects(view: WebView?, cancelMsg: Message?, continueMsg: Message?) {
        super.onTooManyRedirects(view, cancelMsg, continueMsg)
        Timber.v("onTooManyRedirects: $cancelMsg $continueMsg")
    }

    /**
     * Report an error to the host application. These errors are unrecoverable
     * (i.e. the main resource is unavailable). The `errorCode` parameter
     * corresponds to one of the `ERROR_*` constants.
     * @param view The WebView that is initiating the callback.
     * @param errorCode The error code corresponding to an ERROR_* value.
     * @param description A String describing the error.
     * @param failingUrl The url that failed to load.
     */
    override fun onReceivedError(
        view: WebView?,
        errorCode: Int,
        description: String?,
        failingUrl: String?
    ) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        Timber.v("onReceivedError: $errorCode $description $failingUrl")
    }

    /**
     * Report web resource loading error to the host application. These errors usually indicate
     * inability to connect to the server. Note that unlike the deprecated version of the callback,
     * the new version will be called for any resource (iframe, image, etc.), not just for the main
     * page. Thus, it is recommended to perform minimum required work in this callback.
     * @param view The WebView that is initiating the callback.
     * @param request The originating request.
     * @param error Information about the error occurred.
     */
    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        Timber.v("onReceivedError: $request $error")
    }

    /**
     * Notify the host application that an HTTP error has been received from the server while
     * loading a resource.  HTTP errors have status codes &gt;= 400.  This callback will be called
     * for any resource (iframe, image, etc.), not just for the main page. Thus, it is recommended
     * to perform minimum required work in this callback. Note that the content of the server
     * response may not be provided within the `errorResponse` parameter.
     * @param view The WebView that is initiating the callback.
     * @param request The originating request.
     * @param errorResponse Information about the error occurred.
     */
    override fun onReceivedHttpError(
        view: WebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?
    ) {
        super.onReceivedHttpError(view, request, errorResponse)
        Timber.v("onReceivedHttpError: $request $errorResponse")
    }

    /**
     * As the host application if the browser should resend data as the
     * requested page was a result of a POST. The default is to not resend the
     * data.
     *
     * @param view The WebView that is initiating the callback.
     * @param dontResend The message to send if the browser should not resend
     * @param resend The message to send if the browser should resend data
     */
    override fun onFormResubmission(view: WebView?, dontResend: Message?, resend: Message?) {
        super.onFormResubmission(view, dontResend, resend)
        Timber.v("onFormResubmission: $dontResend $resend")
    }

    /**
     * Notify the host application to update its visited links database.
     *
     * @param view The WebView that is initiating the callback.
     * @param url The url being visited.
     * @param isReload `true` if this url is being reloaded.
     */
    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
        Timber.v("doUpdateVisitedHistory: $url $isReload")
    }

    /**
     * Notifies the host application that an SSL error occurred while loading a
     * resource. The host application must call either
     * [SslErrorHandler.cancel] or [SslErrorHandler.proceed].
     *
     *
     * **Warning:** Application overrides of this method
     * can be used to display custom error pages or to silently log issues, but
     * the host application should always call `SslErrorHandler#cancel()`
     * and never proceed past errors.
     *
     *
     * **Note:** Do not prompt the user about SSL errors.
     * Users are unlikely to be able to make an informed security decision, and
     * `WebView` does not provide a UI for showing the details of the
     * error in a meaningful way.
     *
     *
     * The decision to call `proceed()` or `cancel()` may be
     * retained to facilitate responses to future SSL errors. The default
     * behavior is to cancel the resource loading process.
     *
     *
     * This API is called only for recoverable SSL certificate errors. For
     * non-recoverable errors (such as when the server fails the client), the
     * `WebView` calls [onReceivedError(WebView,][.onReceivedError] with the
     * [.ERROR_FAILED_SSL_HANDSHAKE] argument.
     *
     * @param view `WebView` that initiated the callback.
     * @param handler [SslErrorHandler] that handles the user's response.
     * @param error SSL error object.
     */
    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        super.onReceivedSslError(view, handler, error)
        Timber.v("onReceivedSslError: $handler $error")
    }

    /**
     * Notify the host application to handle a SSL client certificate request. The host application
     * is responsible for showing the UI if desired and providing the keys. There are three ways to
     * respond: [ClientCertRequest.proceed], [ClientCertRequest.cancel], or [ ][ClientCertRequest.ignore]. Webview stores the response in memory (for the life of the
     * application) if [ClientCertRequest.proceed] or [ClientCertRequest.cancel] is
     * called and does not call `onReceivedClientCertRequest()` again for the same host and
     * port pair. Webview does not store the response if [ClientCertRequest.ignore]
     * is called. Note that, multiple layers in chromium network stack might be
     * caching the responses, so the behavior for ignore is only a best case
     * effort.
     *
     * This method is called on the UI thread. During the callback, the
     * connection is suspended.
     *
     * For most use cases, the application program should implement the
     * [android.security.KeyChainAliasCallback] interface and pass it to
     * [android.security.KeyChain.choosePrivateKeyAlias] to start an
     * activity for the user to choose the proper alias. The keychain activity will
     * provide the alias through the callback method in the implemented interface. Next
     * the application should create an async task to call
     * [android.security.KeyChain.getPrivateKey] to receive the key.
     *
     * An example implementation of client certificates can be seen at
     * <A href="https://android.googlesource.com/platform/packages/apps/Browser/+/android-5.1.1_r1/src/com/android/browser/Tab.java">
     * AOSP Browser</A>
     *
     * The default behavior is to cancel, returning no client certificate.
     *
     * @param view The WebView that is initiating the callback
     * @param request An instance of a [ClientCertRequest]
     */
    override fun onReceivedClientCertRequest(view: WebView?, request: ClientCertRequest?) {
        super.onReceivedClientCertRequest(view, request)
        Timber.v("onReceivedClientCertRequest: $request")
    }

    /**
     * Notifies the host application that the WebView received an HTTP
     * authentication request. The host application can use the supplied
     * [HttpAuthHandler] to set the WebView's response to the request.
     * The default behavior is to cancel the request.
     *
     *
     * **Note:** The supplied HttpAuthHandler must be used on
     * the UI thread.
     *
     * @param view the WebView that is initiating the callback
     * @param handler the HttpAuthHandler used to set the WebView's response
     * @param host the host requiring authentication
     * @param realm the realm for which authentication is required
     * @see WebView.getHttpAuthUsernamePassword
     */
    override fun onReceivedHttpAuthRequest(
        view: WebView?,
        handler: HttpAuthHandler?,
        host: String?,
        realm: String?
    ) {
        super.onReceivedHttpAuthRequest(view, handler, host, realm)
        Timber.v("onReceivedHttpAuthRequest: $handler $host $realm")
    }

    /**
     * Give the host application a chance to handle the key event synchronously.
     * e.g. menu shortcut key events need to be filtered this way. If return
     * true, WebView will not handle the key event. If return `false`, WebView
     * will always handle the key event, so none of the super in the view chain
     * will see the key event. The default behavior returns `false`.
     *
     * @param view The WebView that is initiating the callback.
     * @param event The key event.
     * @return `true` if the host application wants to handle the key event
     * itself, otherwise return `false`
     */
    override fun shouldOverrideKeyEvent(view: WebView?, event: KeyEvent?): Boolean {
        Timber.v("shouldOverrideKeyEvent: $event")
        return super.shouldOverrideKeyEvent(view, event)
    }

    /**
     * Notify the host application that a key was not handled by the WebView.
     * Except system keys, WebView always consumes the keys in the normal flow
     * or if [.shouldOverrideKeyEvent] returns `true`. This is called asynchronously
     * from where the key is dispatched. It gives the host application a chance
     * to handle the unhandled key events.
     *
     * @param view The WebView that is initiating the callback.
     * @param event The key event.
     */
    override fun onUnhandledKeyEvent(view: WebView?, event: KeyEvent?) {
        super.onUnhandledKeyEvent(view, event)
        Timber.v("onUnhandledKeyEvent: $event")
    }

    /**
     * Notify the host application that the scale applied to the WebView has
     * changed.
     *
     * @param view The WebView that is initiating the callback.
     * @param oldScale The old scale factor
     * @param newScale The new scale factor
     */
    override fun onScaleChanged(view: WebView?, oldScale: Float, newScale: Float) {
        super.onScaleChanged(view, oldScale, newScale)
        Timber.v("onScaleChanged: $oldScale $newScale")
    }

    /**
     * Notify the host application that a request to automatically log in the
     * user has been processed.
     * @param view The WebView requesting the login.
     * @param realm The account realm used to look up accounts.
     * @param account An optional account. If not `null`, the account should be
     * checked against accounts on the device. If it is a valid
     * account, it should be used to log in the user.
     * @param args Authenticator specific arguments used to log in the user.
     */
    override fun onReceivedLoginRequest(
        view: WebView?,
        realm: String?,
        account: String?,
        args: String?
    ) {
        super.onReceivedLoginRequest(view, realm, account, args)
        Timber.v("onReceivedLoginRequest: $realm $account $args")
    }

    /**
     * Notify host application that the given WebView's render process has exited.
     *
     * Multiple WebView instances may be associated with a single render process;
     * onRenderProcessGone will be called for each WebView that was affected.
     * The application's implementation of this callback should only attempt to
     * clean up the specific WebView given as a parameter, and should not assume
     * that other WebView instances are affected.
     *
     * The given WebView can't be used, and should be removed from the view hierarchy,
     * all references to it should be cleaned up, e.g any references in the Activity
     * or other classes saved using [android.view.View.findViewById] and similar calls, etc.
     *
     * To cause an render process crash for test purpose, the application can
     * call `loadUrl("chrome://crash")` on the WebView. Note that multiple WebView
     * instances may be affected if they share a render process, not just the
     * specific WebView which loaded chrome://crash.
     *
     * @param view The WebView which needs to be cleaned up.
     * @param detail the reason why it exited.
     * @return `true` if the host application handled the situation that process has
     * exited, otherwise, application will crash if render process crashed,
     * or be killed if render process was killed by the system.
     */
    override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
        Timber.v("onRenderProcessGone: $detail")
        return super.onRenderProcessGone(view, detail)
    }

    /**
     * Notify the host application that a loading URL has been flagged by Safe Browsing.
     *
     * The application must invoke the callback to indicate the preferred response. The default
     * behavior is to show an interstitial to the user, with the reporting checkbox visible.
     *
     * If the application needs to show its own custom interstitial UI, the callback can be invoked
     * asynchronously with [SafeBrowsingResponse.backToSafety] or [ ][SafeBrowsingResponse.proceed], depending on user response.
     *
     * @param view The WebView that hit the malicious resource.
     * @param request Object containing the details of the request.
     * @param threatType The reason the resource was caught by Safe Browsing, corresponding to a
     * `SAFE_BROWSING_THREAT_*` value.
     * @param callback Applications must invoke one of the callback methods.
     */
    override fun onSafeBrowsingHit(
        view: WebView?,
        request: WebResourceRequest?,
        threatType: Int,
        callback: SafeBrowsingResponse?
    ) {
        super.onSafeBrowsingHit(view, request, threatType, callback)
        Timber.v("onSafeBrowsingHit: $request $threatType $callback")
    }
}