package com.android.formula1.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.android.formula1.R
import com.android.formula1.misc.Util
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class DriverActivity : AppCompatActivity() {
    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        context = this
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(getString(R.string.label_driverdata))
        setContentView(R.layout.activity_driver)

        val selectedDriver = intent.extras?.getString("selectedDriver")

        Volley.newRequestQueue(this).add(
            Util.buildRequest("https://ergast.com/api/f1/drivers/" + selectedDriver + ".json", Response.Listener { response ->
                try {
                    val driver = JSONObject(response).getJSONObject("MRData").getJSONObject("DriverTable").getJSONArray("Drivers").getJSONObject(0)
                    setData(driver)
                } catch (e: JSONException) {
                    Log.d(Util.TAG_DEBUG, e.toString())
                    setData(null)
                }
            },
            Response.ErrorListener {
                Log.d(Util.TAG_DEBUG, it.toString())
                setData(null)
            }
        ))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setData(o: JSONObject?) {
        if(o != null){
            try {
                Log.d(Util.TAG_DEBUG, o.toString())

                val emptystr = ""

                val givenName = if(o.isNull("givenName")) emptystr else o.getString("givenName")
                val familyName = if(o.isNull("familyName")) emptystr else o.getString("familyName")
                val nationality = if(o.isNull("nationality")) emptystr else o.getString("nationality")
                val dateOfBirth = if(o.isNull("dateOfBirth")) emptystr else o.getString("dateOfBirth")
                val permanentNumber = if(o.isNull("permanentNumber")) getString(R.string.label_no_number) else o.getString("permanentNumber")
                val code = if(o.isNull("code")) getString(R.string.label_no_code) else o.getString("code")
                val url = if(o.isNull("url")) emptystr else o.getString("url")

                val nationalityUpperCase = nationality.toUpperCase(Locale.ROOT)
                val countryCodes = Util.getCountryCodes(this)
                val flagEmoji = if(countryCodes.containsKey(nationalityUpperCase)) Util.countryCodeToEmoji(countryCodes[nationalityUpperCase]!!) else nationality

                findViewById<TextView>(R.id.tv_name).setText(getString(
                    R.string.drivername, givenName, familyName))
                findViewById<TextView>(R.id.tv_nationality).setText(flagEmoji)
                findViewById<TextView>(R.id.tv_dob).setText(dateOfBirth)
                findViewById<TextView>(R.id.tv_number).setText(permanentNumber)
                findViewById<TextView>(R.id.tv_code).setText(code)

                findViewById<ImageView>(R.id.iv_info).setOnClickListener {
                    val openInBrowser = Intent(Intent.ACTION_VIEW)
                    openInBrowser.data = Uri.parse(url)
                    startActivity(openInBrowser)
                }

                val query = givenName + "+" + familyName
                val APIKey = "AIzaSyADx9HTfg1vEtKt2KllxBhwpjB5qUvO52k"
                val engineKey = "002476202830629165681:jda5k0tdizc"

                Volley.newRequestQueue(this).add(
                    Util.buildRequest(
                    "https://www.googleapis.com/customsearch/v1?key=" + APIKey + "&cx=" + engineKey + "&searchType=image&q=" + query,
                    Response.Listener { response ->
                        try{
                            val result = JSONObject(response)

                            if (!result.isNull("items")) {
                                Picasso.get().load(result.getJSONArray("items").getJSONObject(0).getString("link")).into(findViewById(R.id.iv_driver),
                                    object : Callback {
                                        override fun onSuccess() {
                                        }

                                        override fun onError(e: Exception) {
                                            Log.d(Util.TAG_DEBUG, e.toString())
                                            AlertDialog.Builder(context).setMessage(R.string.label_error_image).setPositiveButton(android.R.string.ok, null).create().show()
                                        }
                                    }
                                )
                            }
                        } catch (e: JSONException){
                            Log.d(Util.TAG_DEBUG, e.toString())
                            AlertDialog.Builder(this).setMessage(R.string.label_error_data).setPositiveButton(android.R.string.ok, null).create().show()
                        }
                    },
                    Response.ErrorListener {
                        Log.d(Util.TAG_DEBUG, it.toString())
                        AlertDialog.Builder(context).setMessage(R.string.label_error_image).setPositiveButton(android.R.string.ok, null).create().show()
                    }
                ))

            } catch (e: JSONException){
                Log.d(Util.TAG_DEBUG, e.toString())
                AlertDialog.Builder(this).setMessage(R.string.label_error_data).setPositiveButton(android.R.string.ok, null).create().show()
            }
        } else {
            AlertDialog.Builder(this).setMessage(R.string.label_error_data).setPositiveButton(android.R.string.ok, null).create().show()
        }
    }
}