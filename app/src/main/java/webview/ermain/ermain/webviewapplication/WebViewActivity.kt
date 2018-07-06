package webview.ermain.ermain.webviewapplication

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.location.GnssNavigationMessage
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation

import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_web_view.*

class WebViewActivity : AppCompatActivity(){

    private val URL = "https://steemit.com"
    private var isAlreadyCreated = false

    // Entry-Point for our application
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)  // Specify the activity we want to use

        startLoaderAnimate()

        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportZoom(false)

        webView.webViewClient = object : WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                endLoaderAnimate()
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                endLoaderAnimate()

                // Show an error to the user if no connection is available
                showErrorDialog("Error", "No internet connection available.", this@WebViewActivity)
            }
        }

        webView.loadUrl(URL)
    }

    // This function pertains to how the app behaves when you exit it but you don't close it
    override fun onResume() {
        super.onResume()

        if(isAlreadyCreated && !isNetworkAvailable()){
            isAlreadyCreated = false
            showErrorDialog("Error", "No internet connection.", this@WebViewActivity)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Check that the user can go back in the life cycle of the app
        if(keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()){
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun isNetworkAvailable() : Boolean{
        val connectionManager =
                this@WebViewActivity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkInfo = connectionManager.activeNetworkInfo

        return networkInfo != null && networkInfo.isConnectedOrConnecting
    }

    private fun showErrorDialog(title: String, message: String, context: Context){
        val dialog = AlertDialog.Builder(context)
        dialog.setTitle(title)
        dialog.setMessage(message)
        dialog.setNegativeButton("Cancel", {_, _ ->
            this@WebViewActivity.finish()
        })
        dialog.setNeutralButton("Settings", {_, _ ->
            // This opens the wifi menu for the user to find another network
            startActivity(Intent(Settings.ACTION_SETTINGS))
        })
        dialog.setPositiveButton("Retry", {_, _ ->
            this@WebViewActivity.recreate()
        })
        dialog.create().show()
    }

    private fun endLoaderAnimate(){
        loader_image.clearAnimation()
        loader_image.visibility = View.GONE
    }
    private fun startLoaderAnimate(){
        val objectAnimator = object : Animation(){
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                val startHeight = 170
                val newHeight = (startHeight * (startHeight + 40) * interpolatedTime).toInt()
                loader_image.layoutParams.height = newHeight
                loader_image.requestLayout()
            }

            override fun initialize(width: Int, height: Int, parentWidth: Int, parentHeight: Int) {
                super.initialize(width, height, parentWidth, parentHeight)
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }

        objectAnimator.repeatCount = -1
        objectAnimator.repeatMode = ValueAnimator.REVERSE
        objectAnimator.duration = 1000
        loader_image.startAnimation(objectAnimator)
    }
}