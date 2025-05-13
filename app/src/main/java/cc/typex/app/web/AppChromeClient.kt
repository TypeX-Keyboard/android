package cc.typex.app.web

import android.graphics.Bitmap
import android.net.Uri
import android.os.Message
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.GeolocationPermissions
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebStorage
import android.webkit.WebView
import timber.log.Timber
import javax.inject.Inject

class AppChromeClient @Inject constructor() : WebChromeClient() {

    /**
     * Tell the host application the current progress of loading a page.
     * @param view The WebView that initiated the callback.
     * @param newProgress Current page loading progress, represented by
     * an integer between 0 and 100.
     */
    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        Timber.v("onProgressChanged: $newProgress")
    }

    /**
     * Notify the host application of a change in the document title.
     * @param view The WebView that initiated the callback.
     * @param title A String containing the new title of the document.
     */
    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
        Timber.v("onReceivedTitle: $title")
    }

    /**
     * Notify the host application of a new favicon for the current page.
     * @param view The WebView that initiated the callback.
     * @param icon A Bitmap containing the favicon for the current page.
     */
    override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
        super.onReceivedIcon(view, icon)
        Timber.v("onReceivedIcon")
    }

    /**
     * Notify the host application of the url for an apple-touch-icon.
     * @param view The WebView that initiated the callback.
     * @param url The icon url.
     * @param precomposed `true` if the url is for a precomposed touch icon.
     */
    override fun onReceivedTouchIconUrl(view: WebView?, url: String?, precomposed: Boolean) {
        super.onReceivedTouchIconUrl(view, url, precomposed)
        Timber.v("onReceivedTouchIconUrl: $url $precomposed")
    }

    /**
     * Notify the host application that the current page has entered full screen mode. After this
     * call, web content will no longer be rendered in the WebView, but will instead be rendered
     * in `view`. The host application should add this View to a Window which is configured
     * with [android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN] flag in order to
     * actually display this web content full screen.
     *
     *
     * The application may explicitly exit fullscreen mode by invoking `callback` (ex. when
     * the user presses the back button). However, this is generally not necessary as the web page
     * will often show its own UI to close out of fullscreen. Regardless of how the WebView exits
     * fullscreen mode, WebView will invoke [.onHideCustomView], signaling for the
     * application to remove the custom View.
     *
     *
     * If this method is not overridden, WebView will report to the web page it does not support
     * fullscreen mode and will not honor the web page's request to run in fullscreen mode.
     *
     *
     * **Note:** if overriding this method, the application must also override
     * [.onHideCustomView].
     *
     * @param view is the View object to be shown.
     * @param callback invoke this callback to request the page to exit
     * full screen mode.
     */
    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        super.onShowCustomView(view, callback)
        Timber.v("onShowCustomView $callback")
    }

    /**
     * Notify the host application that the current page would
     * like to show a custom View in a particular orientation.
     * @param view is the View object to be shown.
     * @param requestedOrientation An orientation constant as used in
     * [ActivityInfo.screenOrientation].
     * @param callback is the callback to be invoked if and when the view
     * is dismissed.
     */
    override fun onShowCustomView(
        view: View?,
        requestedOrientation: Int,
        callback: CustomViewCallback?
    ) {
        super.onShowCustomView(view, requestedOrientation, callback)
        Timber.v("onShowCustomView: $requestedOrientation $callback")
    }

    /**
     * Request the host application to create a new window. If the host
     * application chooses to honor this request, it should return `true` from
     * this method, create a new WebView to host the window, insert it into the
     * View system and send the supplied resultMsg message to its target with
     * the new WebView as an argument. If the host application chooses not to
     * honor the request, it should return `false` from this method. The default
     * implementation of this method does nothing and hence returns `false`.
     *
     *
     * Applications should typically not allow windows to be created when the
     * `isUserGesture` flag is false, as this may be an unwanted popup.
     *
     *
     * Applications should be careful how they display the new window: don't simply
     * overlay it over the existing WebView as this may mislead the user about which
     * site they are viewing. If your application displays the URL of the main page,
     * make sure to also display the URL of the new window in a similar fashion. If
     * your application does not display URLs, consider disallowing the creation of
     * new windows entirely.
     *
     * **Note:** There is no trustworthy way to tell which page
     * requested the new window: the request might originate from a third-party iframe
     * inside the WebView.
     *
     * @param view The WebView from which the request for a new window
     * originated.
     * @param isDialog `true` if the new window should be a dialog, rather than
     * a full-size window.
     * @param isUserGesture `true` if the request was initiated by a user gesture,
     * such as the user clicking a link.
     * @param resultMsg The message to send when once a new WebView has been
     * created. resultMsg.obj is a
     * [WebView.WebViewTransport] object. This should be
     * used to transport the new WebView, by calling
     * [                  WebView.WebViewTransport.setWebView(WebView)][WebView.WebViewTransport.setWebView].
     * @return This method should return `true` if the host application will
     * create a new window, in which case resultMsg should be sent to
     * its target. Otherwise, this method should return `false`. Returning
     * `false` from this method but also sending resultMsg will result in
     * undefined behavior.
     */
    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {
        Timber.v("onCreateWindow: $isDialog $isUserGesture $resultMsg")
        return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
    }

    /**
     * Request display and focus for this WebView. This may happen due to
     * another WebView opening a link in this WebView and requesting that this
     * WebView be displayed.
     * @param view The WebView that needs to be focused.
     */
    override fun onRequestFocus(view: WebView?) {
        super.onRequestFocus(view)
        Timber.v("onRequestFocus: $view")
    }

    /**
     * Notify the host application to close the given WebView and remove it
     * from the view system if necessary. At this point, WebCore has stopped
     * any loading in this window and has removed any cross-scripting ability
     * in javascript.
     *
     *
     * As with [.onCreateWindow], the application should ensure that any
     * URL or security indicator displayed is updated so that the user can tell
     * that the page they were interacting with has been closed.
     *
     * @param window The WebView that needs to be closed.
     */
    override fun onCloseWindow(window: WebView?) {
        super.onCloseWindow(window)
        Timber.v("onCloseWindow: $window")
    }

    /**
     * Notify the host application that the web page wants to display a
     * JavaScript `alert()` dialog.
     *
     * The default behavior if this method returns `false` or is not
     * overridden is to show a dialog containing the alert message and suspend
     * JavaScript execution until the dialog is dismissed.
     *
     * To show a custom dialog, the app should return `true` from this
     * method, in which case the default dialog will not be shown and JavaScript
     * execution will be suspended. The app should call
     * `JsResult.confirm()` when the custom dialog is dismissed such that
     * JavaScript execution can be resumed.
     *
     * To suppress the dialog and allow JavaScript execution to
     * continue, call `JsResult.confirm()` immediately and then return
     * `true`.
     *
     * Note that if the [WebChromeClient] is set to be `null`,
     * or if [WebChromeClient] is not set at all, the default dialog will
     * be suppressed and Javascript execution will continue immediately.
     *
     * Note that the default dialog does not inherit the [ ][android.view.Display.FLAG_SECURE] flag from the parent window.
     *
     * @param view The WebView that initiated the callback.
     * @param url The url of the page requesting the dialog.
     * @param message Message to be displayed in the window.
     * @param result A JsResult to confirm that the user closed the window.
     * @return boolean `true` if the request is handled or ignored.
     * `false` if WebView needs to show the default dialog.
     */
    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        Timber.v("onJsAlert: $url $message $result")
        return super.onJsAlert(view, url, message, result)
    }

    /**
     * Notify the host application that the web page wants to display a
     * JavaScript `confirm()` dialog.
     *
     * The default behavior if this method returns `false` or is not
     * overridden is to show a dialog containing the message and suspend
     * JavaScript execution until the dialog is dismissed. The default dialog
     * will return `true` to the JavaScript `confirm()` code when
     * the user presses the 'confirm' button, and will return `false` to
     * the JavaScript code when the user presses the 'cancel' button or
     * dismisses the dialog.
     *
     * To show a custom dialog, the app should return `true` from this
     * method, in which case the default dialog will not be shown and JavaScript
     * execution will be suspended. The app should call
     * `JsResult.confirm()` or `JsResult.cancel()` when the custom
     * dialog is dismissed.
     *
     * To suppress the dialog and allow JavaScript execution to continue,
     * call `JsResult.confirm()` or `JsResult.cancel()` immediately
     * and then return `true`.
     *
     * Note that if the [WebChromeClient] is set to be `null`,
     * or if [WebChromeClient] is not set at all, the default dialog will
     * be suppressed and the default value of `false` will be returned to
     * the JavaScript code immediately.
     *
     * Note that the default dialog does not inherit the [ ][android.view.Display.FLAG_SECURE] flag from the parent window.
     *
     * @param view The WebView that initiated the callback.
     * @param url The url of the page requesting the dialog.
     * @param message Message to be displayed in the window.
     * @param result A JsResult used to send the user's response to
     * javascript.
     * @return boolean `true` if the request is handled or ignored.
     * `false` if WebView needs to show the default dialog.
     */
    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        Timber.v("onJsConfirm: $url $message $result")
        return super.onJsConfirm(view, url, message, result)
    }

    /**
     * Notify the host application that the web page wants to display a
     * JavaScript `prompt()` dialog.
     *
     * The default behavior if this method returns `false` or is not
     * overridden is to show a dialog containing the message and suspend
     * JavaScript execution until the dialog is dismissed. Once the dialog is
     * dismissed, JavaScript `prompt()` will return the string that the
     * user typed in, or null if the user presses the 'cancel' button.
     *
     * To show a custom dialog, the app should return `true` from this
     * method, in which case the default dialog will not be shown and JavaScript
     * execution will be suspended. The app should call
     * `JsPromptResult.confirm(result)` when the custom dialog is
     * dismissed.
     *
     * To suppress the dialog and allow JavaScript execution to continue,
     * call `JsPromptResult.confirm(result)` immediately and then
     * return `true`.
     *
     * Note that if the [WebChromeClient] is set to be `null`,
     * or if [WebChromeClient] is not set at all, the default dialog will
     * be suppressed and `null` will be returned to the JavaScript code
     * immediately.
     *
     * Note that the default dialog does not inherit the [ ][android.view.Display.FLAG_SECURE] flag from the parent window.
     *
     * @param view The WebView that initiated the callback.
     * @param url The url of the page requesting the dialog.
     * @param message Message to be displayed in the window.
     * @param defaultValue The default value displayed in the prompt dialog.
     * @param result A JsPromptResult used to send the user's reponse to
     * javascript.
     * @return boolean `true` if the request is handled or ignored.
     * `false` if WebView needs to show the default dialog.
     */
    override fun onJsPrompt(
        view: WebView?,
        url: String?,
        message: String?,
        defaultValue: String?,
        result: JsPromptResult?
    ): Boolean {
        Timber.v("onJsPrompt: $url $message $defaultValue $result")
        return super.onJsPrompt(view, url, message, defaultValue, result)
    }

    /**
     * Notify the host application that the web page wants to confirm navigation
     * from JavaScript `onbeforeunload`.
     *
     * The default behavior if this method returns `false` or is not
     * overridden is to show a dialog containing the message and suspend
     * JavaScript execution until the dialog is dismissed. The default dialog
     * will continue the navigation if the user confirms the navigation, and
     * will stop the navigation if the user wants to stay on the current page.
     *
     * To show a custom dialog, the app should return `true` from this
     * method, in which case the default dialog will not be shown and JavaScript
     * execution will be suspended. When the custom dialog is dismissed, the
     * app should call `JsResult.confirm()` to continue the navigation or,
     * `JsResult.cancel()` to stay on the current page.
     *
     * To suppress the dialog and allow JavaScript execution to continue,
     * call `JsResult.confirm()` or `JsResult.cancel()` immediately
     * and then return `true`.
     *
     * Note that if the [WebChromeClient] is set to be `null`,
     * or if [WebChromeClient] is not set at all, the default dialog will
     * be suppressed and the navigation will be resumed immediately.
     *
     * Note that the default dialog does not inherit the [ ][android.view.Display.FLAG_SECURE] flag from the parent window.
     *
     * @param view The WebView that initiated the callback.
     * @param url The url of the page requesting the dialog.
     * @param message Message to be displayed in the window.
     * @param result A JsResult used to send the user's response to
     * javascript.
     * @return boolean `true` if the request is handled or ignored.
     * `false` if WebView needs to show the default dialog.
     */
    override fun onJsBeforeUnload(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        Timber.v("onJsBeforeUnload: $url $message $result")
        return super.onJsBeforeUnload(view, url, message, result)
    }

    /**
     * Tell the client that the quota has been exceeded for the Web SQL Database
     * API for a particular origin and request a new quota. The client must
     * respond by invoking the
     * [updateQuota(long)][WebStorage.QuotaUpdater.updateQuota]
     * method of the supplied [WebStorage.QuotaUpdater] instance. The
     * minimum value that can be set for the new quota is the current quota. The
     * default implementation responds with the current quota, so the quota will
     * not be increased.
     * @param url The URL of the page that triggered the notification
     * @param databaseIdentifier The identifier of the database where the quota
     * was exceeded.
     * @param quota The quota for the origin, in bytes
     * @param estimatedDatabaseSize The estimated size of the offending
     * database, in bytes
     * @param totalQuota The total quota for all origins, in bytes
     * @param quotaUpdater An instance of [WebStorage.QuotaUpdater] which
     * must be used to inform the WebView of the new quota.
     */
    override fun onExceededDatabaseQuota(
        url: String?,
        databaseIdentifier: String?,
        quota: Long,
        estimatedDatabaseSize: Long,
        totalQuota: Long,
        quotaUpdater: WebStorage.QuotaUpdater?
    ) {
        super.onExceededDatabaseQuota(
            url,
            databaseIdentifier,
            quota,
            estimatedDatabaseSize,
            totalQuota,
            quotaUpdater
        )
        Timber.v("onExceededDatabaseQuota: $url $databaseIdentifier $quota $estimatedDatabaseSize $totalQuota $quotaUpdater")
    }

    /**
     * Notify the host application that web content from the specified origin
     * is attempting to use the Geolocation API, but no permission state is
     * currently set for that origin. The host application should invoke the
     * specified callback with the desired permission state. See
     * [GeolocationPermissions] for details.
     *
     *
     * Note that for applications targeting Android N and later SDKs
     * (API level > [android.os.Build.VERSION_CODES.M])
     * this method is only called for requests originating from secure
     * origins such as https. On non-secure origins geolocation requests
     * are automatically denied.
     *
     * @param origin The origin of the web content attempting to use the
     * Geolocation API.
     * @param callback The callback to use to set the permission state for the
     * origin.
     */
    override fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        super.onGeolocationPermissionsShowPrompt(origin, callback)
        Timber.v("onGeolocationPermissionsShowPrompt: $origin $callback")
    }

    /**
     * Notify the host application that web content is requesting permission to
     * access the specified resources and the permission currently isn't granted
     * or denied. The host application must invoke [PermissionRequest.grant]
     * or [PermissionRequest.deny].
     *
     * If this method isn't overridden, the permission is denied.
     *
     * @param request the PermissionRequest from current web content.
     */
    override fun onPermissionRequest(request: PermissionRequest?) {
        super.onPermissionRequest(request)
        Timber.v("onPermissionRequest: $request")
    }

    /**
     * Notify the host application that the given permission request
     * has been canceled. Any related UI should therefore be hidden.
     *
     * @param request the PermissionRequest that needs be canceled.
     */
    override fun onPermissionRequestCanceled(request: PermissionRequest?) {
        super.onPermissionRequestCanceled(request)
        Timber.v("onPermissionRequestCanceled: $request")
    }

    /**
     * Report a JavaScript error message to the host application. The ChromeClient
     * should override this to process the log message as they see fit.
     * @param message The error message to report.
     * @param lineNumber The line number of the error.
     * @param sourceID The name of the source file that caused the error.
     */
    override fun onConsoleMessage(message: String?, lineNumber: Int, sourceID: String?) {
        super.onConsoleMessage(message, lineNumber, sourceID)
        Timber.v("onConsoleMessage: $message $lineNumber $sourceID")
    }

    /**
     * Report a JavaScript console message to the host application. The ChromeClient
     * should override this to process the log message as they see fit.
     * @param consoleMessage Object containing details of the console message.
     * @return `true` if the message is handled by the client.
     */
    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        Timber.v("onConsoleMessage: $consoleMessage")
        return super.onConsoleMessage(consoleMessage)
    }

    /** Obtains a list of all visited history items, used for link coloring
     */
    override fun getVisitedHistory(callback: ValueCallback<Array<String>>?) {
        super.getVisitedHistory(callback)
        Timber.v("getVisitedHistory: $callback")
    }

    /**
     * Tell the client to show a file chooser.
     *
     * This is called to handle HTML forms with 'file' input type, in response to the
     * user pressing the "Select File" button.
     * To cancel the request, call `filePathCallback.onReceiveValue(null)` and
     * return `true`.
     *
     *
     * **Note:** WebView does not enforce any restrictions on
     * the chosen file(s). WebView can access all files that your app can access.
     * In case the file(s) are chosen through an untrusted source such as a third-party
     * app, it is your own app's responsibility to check what the returned Uris
     * refer to before calling the `filePathCallback`. See
     * [.createIntent] and [.parseResult] for more details.
     *
     * @param webView The WebView instance that is initiating the request.
     * @param filePathCallback Invoke this callback to supply the list of paths to files to upload,
     * or `null` to cancel. Must only be called if the
     * [.onShowFileChooser] implementation returns `true`.
     * @param fileChooserParams Describes the mode of file chooser to be opened, and options to be
     * used with it.
     * @return `true` if filePathCallback will be invoked, `false` to use default
     * handling.
     *
     * @see FileChooserParams
     */
    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        Timber.v("onShowFileChooser: $filePathCallback $fileChooserParams")
        return super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
    }
}