package com.example.framgiabuivanhieu.loginauthentication

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import com.bumptech.glide.Glide
import com.facebook.*
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginResult
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.net.URL
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var mCallBackManager: CallbackManager
    private lateinit var mDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        printKeyHash()
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
        setContentView(R.layout.activity_main)
        mCallBackManager = CallbackManager.Factory.create()
        buttonLogin.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday", "user_friends"))
        buttonLogin.registerCallback(mCallBackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                mDialog = ProgressDialog(this@MainActivity)
                mDialog.setMessage("Retrieving ... data")
                mDialog.show()

                var accessToken: String = result!!.accessToken.token
                var request: GraphRequest =
                    GraphRequest.newMeRequest(result.accessToken, object : GraphRequest.GraphJSONObjectCallback {
                        override fun onCompleted(jsonObject: JSONObject?, response: GraphResponse?) {
                            mDialog.dismiss()
                            Log.d("response", jsonObject.toString())
                            Log.d("response", jsonObject?.getString("email"))
                            Log.d("response", jsonObject?.getString("id"))
                            Log.d("response", jsonObject?.getString("name"))
                            Log.d("response", jsonObject?.getString("birthday"))
                            getData(jsonObject)

                        }

                    })

                //Request Graph API
                var parameters: Bundle = Bundle()
                parameters.putString("fields", "id,name,email,birthday")
                request.parameters = parameters
                request.executeAsync()


            }

            override fun onCancel() {
            }

            override fun onError(error: FacebookException?) {
            }
        })

        //If already Login
        if (AccessToken.getCurrentAccessToken() != null) {
            textViewEmail.text = AccessToken.getCurrentAccessToken().userId

        }
    }

    private fun getData(jsonObject: JSONObject?) {
        try {
            var avatarURL =
                URL("https://graph.facebook.com/" + jsonObject?.getString("id") + "/picture?width=263&height=263")
            Glide.with(applicationContext).load(avatarURL).into(imageViewAvatar)
            textViewName.text = jsonObject?.getString("name")
            textViewEmail.text = jsonObject?.getString("email")
            textViewBirthDay.text = jsonObject?.getString("birthday")
        } catch (e: Exception) {

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mCallBackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun printKeyHash() {
        try {
            var info: PackageInfo = packageManager.getPackageInfo(
                "com.example.framgiabuivanhieu.loginauthentication",
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {

        } catch (e: NoSuchAlgorithmException) {

        }
    }
}
