package com.android.formula1.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import com.android.formula1.R
import com.android.formula1.misc.Season
import com.android.formula1.misc.Util
import com.android.formula1.adapter.SeasonAdapter
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.collections.ArrayList

class SeasonListActivity : AppCompatActivity() {
    lateinit var listView_seasons: ListView
    lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_seasonlist)

        sharedPref = getSharedPreferences(applicationContext.packageName, Context.MODE_PRIVATE)
        Util.info_seasons = sharedPref.getBoolean("info_seasons", true)

        listView_seasons = findViewById(R.id.list_seasons)

        Volley.newRequestQueue(this).add(
            Util.buildRequest(
            "https://ergast.com/api/f1/seasons.json?limit=" + Util.APIlimit,
            Response.Listener { response ->
                try {
                    val seasons = JSONObject(response).getJSONObject("MRData").getJSONObject("SeasonTable").getJSONArray("Seasons")
                    supportActionBar?.setTitle(getString(R.string.header_seasons, seasons.getJSONObject(0).getString("season"), seasons.getJSONObject(seasons.length() - 1).getString("season")))
                    setAdapter(seasons)
                } catch (e: JSONException) {
                    Log.d(Util.TAG_DEBUG, e.toString())
                    setAdapter(null)
                }
            },
            Response.ErrorListener {
                Log.d(Util.TAG_DEBUG, it.toString())
                setAdapter(null)
            }
        ))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setAdapter(a: JSONArray?) {
        if(a != null){
            try {
                Log.d(Util.TAG_DEBUG, a.toString())

                val listItems: ArrayList<Season> = ArrayList()

                for(i in 0 until a.length()){
                    val o: JSONObject = a.getJSONObject(i)
                    listItems.add(Season(o.getString("season"), o.getString("url")))
                }

                val adapter = SeasonAdapter(this, listItems)
                listView_seasons.adapter = adapter

                listView_seasons.setOnItemClickListener{parent, view, position, id ->
                    intent = Intent(this, DriverListActivity::class.java).apply {
                        putExtra("selectedSeason", (adapter.getItem(position) as Season).year)
                    }
                    startActivity(intent)
                }

                if(Util.info_seasons){
                    AlertDialog.Builder(this).setMessage(R.string.info_seasons).setPositiveButton(android.R.string.ok, null).create().show()
                    Util.info_seasons = false
                    sharedPref.edit().putBoolean("info_seasons", false).apply()
                }

            } catch (e: JSONException){
                Log.d(Util.TAG_DEBUG, e.toString())
                AlertDialog.Builder(this).setMessage(R.string.label_error_adapter).setPositiveButton(android.R.string.ok, null).create().show()
            }
        } else {
            AlertDialog.Builder(this).setMessage(R.string.label_error_adapter).setPositiveButton(android.R.string.ok, null).create().show()
        }
    }
}