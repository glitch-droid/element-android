/*
 * Copyright (c) 2021 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package varta.cdac.app.features.qrauth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.button.MaterialButton
import com.google.gson.GsonBuilder
import varta.cdac.app.R
import varta.cdac.app.features.login.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Timer
import java.util.TimerTask

class OTPActivity : AppCompatActivity() {
    private var editTextOtp : EditText? = null
    private var loginBtn : MaterialButton? = null
    private val timer = Timer()
    private lateinit var param1:String
    private lateinit var param2:String
    private var loader: LottieAnimationView?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otpactivity)
        val qrCode = intent.getStringExtra("qr_code")
        editTextOtp = findViewById(R.id.edit_text_otp)
        loader=findViewById(R.id.login_loading)
        param1= qrCode.toString()
        loginBtn = findViewById(R.id.login_btn)
        startUserSession();
        loginBtn!!.setOnClickListener{
            loader!!.visibility= View.VISIBLE
            param2=editTextOtp!!.text.toString()
            val gson = GsonBuilder().setLenient().create()
            val retrofitBuilder = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .baseUrl(ConfigValues.BASE_URL)
                    .build()

            val testApi = retrofitBuilder.create(ApiService::class.java)
            val call = testApi.login(param1,param2)

            call.enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if(response.code()==200){
                        timer.cancel()
                        Toast.makeText(applicationContext,"Login Success!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@OTPActivity,LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }else if(response.code()==400){
                        loader!!.visibility= View.INVISIBLE
                        Toast.makeText(applicationContext,"Please Enter Correct OTP", Toast.LENGTH_SHORT).show()
                    }else{
                        loader!!.visibility= View.INVISIBLE
                        Toast.makeText(applicationContext,"Error! Try Again", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    timer.cancel()
                    Toast.makeText(applicationContext,"An unexpected error occurred", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@OTPActivity,MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }

            })
        }

    }

    private fun startUserSession() {
        timer.schedule(object : TimerTask(){
            override fun run() {
                handleSessionExpired()
            }

        }, 25000)

    }

    private fun handleSessionExpired() {
        timer.cancel()
        val intent = Intent(this@OTPActivity,MainActivity::class.java);
        startActivity(intent)
        finish()

    }
}
