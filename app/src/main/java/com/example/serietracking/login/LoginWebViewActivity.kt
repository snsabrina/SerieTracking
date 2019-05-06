package com.example.serietracking.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import com.example.serietracking.network.ApiClient
import com.example.serietracking.network.HttpConstants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.webkit.WebResourceRequest
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.serietracking.*
import com.example.serietracking.login.dto.CreateSessionRequest
import com.example.serietracking.login.dto.CreateSessionResponse
import com.example.serietracking.login.dto.RequestTokenResponse


class LoginWebViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_web_view)

        Log.d("log","calling create request token")
        ApiClient.apiInterface.createRequestToken(HttpConstants.API_KEY).enqueue(object: Callback<RequestTokenResponse> {
            override fun onResponse(call: Call<RequestTokenResponse>, response: Response<RequestTokenResponse>) {
                Log.d("log","create request token finished")
                response.body().let { requestTokenResponse ->
                    val myWebView: WebView = findViewById(R.id.webview)
                    myWebView.getSettings().setJavaScriptEnabled(true);
                    myWebView.webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                            view.loadUrl(request.url.toString())
                            return false
                        }

                        override fun onPageFinished(view: WebView, url: String) {
                            if (".*/authenticate/.*/allow".toRegex().matches(url)) {
                                ApiClient.apiInterface.createSession(HttpConstants.API_KEY,
                                    CreateSessionRequest(requestTokenResponse.requestToken)
                                ).enqueue(object: Callback<CreateSessionResponse> {
                                    override fun onResponse(call: Call<CreateSessionResponse>, response: Response<CreateSessionResponse>) {
                                        response.body().let { createSessionResponse ->
                                            createSessionResponse.sessionId
                                            val intent = Intent(this@LoginWebViewActivity, MainActivity::class.java)
                                            startActivity(intent)
                                        }
                                    }

                                    override fun onFailure(call: Call<CreateSessionResponse>, t: Throwable) {
                                        Log.e("log","failed to create session", t)
                                    }
                                })
                            }
                        }
                    }
                    Log.d("log","loading login")
                    myWebView.loadUrl("https://www.themoviedb.org/authenticate/" + requestTokenResponse.requestToken)
                }
            }

            override fun onFailure(call: Call<RequestTokenResponse>, t: Throwable) {
                //Toast.makeText(activity, "No tweets founds!", Toast.LENGTH_SHORT).show()
                Log.e("log","failed to create request token", t)
            }
        })
    }
}