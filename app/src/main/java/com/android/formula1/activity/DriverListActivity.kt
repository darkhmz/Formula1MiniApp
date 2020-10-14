package com.android.formula1.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import com.android.formula1.misc.Driver
import com.android.formula1.R
import com.android.formula1.misc.Util
import com.android.formula1.adapter.DriverAdapter
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class DriverListActivity : AppCompatActivity() {
    lateinit var layout_summary: LinearLayout
    lateinit var listView_drivers: ListView
    lateinit var adapter_drivers: DriverAdapter
    lateinit var sharedPref: SharedPreferences
    lateinit var searchView: SearchView

    val listItems_drivers: ArrayList<Driver> = ArrayList()
    val listItems_drivers_filtered: ArrayList<Driver> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_driverlist)

        sharedPref = getSharedPreferences(applicationContext.packageName, Context.MODE_PRIVATE)
        Util.info_drivers = sharedPref.getBoolean("info_drivers", true)

        val selectedSeason = intent.extras?.getString("selectedSeason")
        supportActionBar?.setTitle(getString(R.string.header_drivers, selectedSeason))

        layout_summary = findViewById(R.id.layout_summary)
        listView_drivers = findViewById(R.id.list_drivers)

        Volley.newRequestQueue(this).add(
            Util.buildRequest("https://ergast.com/api/f1/" + selectedSeason + "/drivers.json?limit=" + Util.APIlimit, Response.Listener { response ->
                try {
                    val drivers = JSONObject(response).getJSONObject("MRData").getJSONObject("DriverTable").getJSONArray("Drivers")
                    setAdapter(drivers)
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

    override fun onBackPressed() {
        if(!searchView.isIconified){
            filter("")
            searchView.onActionViewCollapsed()
            layout_summary.visibility = View.VISIBLE
            return
        }
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        searchView = menu.findItem(R.id.searchView).actionView as SearchView
        searchView.queryHint = getString(R.string.label_search)
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if(layout_summary.visibility == View.VISIBLE) layout_summary.visibility = View.GONE
                filter(newText)
                return true
            }
        })

        searchView.setOnCloseListener ( object : SearchView.OnCloseListener {
            override fun onClose(): Boolean {
                layout_summary.visibility = View.VISIBLE
                return false
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    private fun setAdapter(a: JSONArray?) {
        if(a != null){
            try {
                Log.d(Util.TAG_DEBUG, a.toString())

                val emptystr = ""

                val hashMap: HashMap<String, Int> = HashMap()
                val countryCodes = Util.getCountryCodes(this)

                for(i in 0 until a.length()){
                    val o: JSONObject = a.getJSONObject(i)

                    val driverId = if(o.isNull("driverId")) emptystr else o.getString("driverId")
                    val givenName = if(o.isNull("givenName")) emptystr else o.getString("givenName")
                    val familyName = if(o.isNull("familyName")) emptystr else o.getString("familyName")
                    val nationality = if(o.isNull("nationality")) emptystr else o.getString("nationality")
                    val dateOfBirth = if(o.isNull("dateOfBirth")) emptystr else o.getString("dateOfBirth")
                    val permanentNumber = if(o.isNull("permanentNumber")) getString(
                        R.string.label_no_number
                    ) else o.getString("permanentNumber")
                    val url = if(o.isNull("url")) emptystr else o.getString("url")

                    val nationalityUpperCase = nationality.toUpperCase(Locale.ROOT)
                    val flagEmoji = if(countryCodes.containsKey(nationalityUpperCase)) Util.countryCodeToEmoji(countryCodes[nationalityUpperCase]!!) else nationality

                    listItems_drivers.add(Driver(driverId, permanentNumber, url, givenName, familyName, dateOfBirth, nationality, flagEmoji))

                    if(hashMap.containsKey(nationality)){
                        hashMap.replace(nationality, hashMap[nationality]!! + 1)
                    } else {
                        hashMap[nationality] = 1
                    }
                }

                listItems_drivers_filtered.addAll(listItems_drivers)

                adapter_drivers = DriverAdapter(this, listItems_drivers_filtered)
                listView_drivers.adapter = adapter_drivers
                listView_drivers.setOnItemClickListener{parent, view, position, id ->
                    intent = Intent(this, DriverActivity::class.java).apply {
                        putExtra("selectedDriver", (adapter_drivers.getItem(position) as Driver).driverId)
                    }
                    startActivity(intent)
                }

                val listItems_summary: ArrayList<String> = ArrayList()

                for(i in hashMap.toList().sortedBy { (_, value) -> value }.asReversed().toMap()){
                    listItems_summary.add(i.key + ": " + i.value)
                }

                findViewById<ListView>(R.id.list_summary).adapter = ArrayAdapter(this, R.layout.listitem_summary, R.id.tv_nationality, listItems_summary)

                if(Util.info_drivers){
                    AlertDialog.Builder(this).setMessage(R.string.info_drivers).setPositiveButton(android.R.string.ok, null).create().show()
                    Util.info_drivers = false
                    sharedPref.edit().putBoolean("info_drivers", false).apply()
                }

            } catch (e: JSONException){
                Log.d(Util.TAG_DEBUG, e.toString())
                AlertDialog.Builder(this).setMessage(R.string.label_error_adapter).setPositiveButton(android.R.string.ok, null).create().show()
            }
        } else {
            AlertDialog.Builder(this).setMessage(R.string.label_error_adapter).setPositiveButton(android.R.string.ok, null).create().show()
        }
    }

    private fun filter(s: String?) {
        if(s == null) return

        val trimmed = s.toUpperCase(Locale.ROOT).trim()

        listItems_drivers_filtered.clear()

        for(d in listItems_drivers){
            if(d.givenName.toUpperCase(Locale.ROOT).contains(trimmed) || d.familyName.toUpperCase(Locale.ROOT).contains(trimmed)){
                listItems_drivers_filtered.add(d)
            }
        }

        adapter_drivers.notifyDataSetChanged()
    }
}