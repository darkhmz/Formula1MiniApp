package com.android.formula1.misc

import android.content.Context
import com.android.formula1.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import java.util.*
import kotlin.collections.HashMap

object Util {
    //A limitet beállítjuk az API által támogatott legmagasabb értékre, ami 1000
    val APIlimit = 1000
    val TAG_DEBUG = "debug"

    var info_seasons = true
    var info_drivers = true

    fun buildRequest(url: String, responseListener: Response.Listener<String>, errorListener: Response.ErrorListener): StringRequest{
        val request = object: StringRequest(Method.GET, url, responseListener, errorListener)
        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }
        return request
    }

    //https://en.wikipedia.org/wiki/Regional_Indicator_Symbol
    //A szimbólumok 0x1F1E6 és 0x1F1FF között vannak kódolva, A-tól Z-ig
    //A 0x1F1E6-ból kivonjuk az 'A' értékét ami 0x41, ez lesz a 0x1F1A5
    //Ehhez később hozzá kell adni az országkód betűjét, és helyes értéket kapunk (pl 'A' esetén ismét 0x1F1E6)
    fun countryCodeToEmoji(s: String): String{
        val countryCode = s.toUpperCase(Locale.ROOT)

        if (countryCode.length != 2 || !countryCode[0].isLetter() || !countryCode[1].isLetter()) {
            return s
        }

        return String(Character.toChars(Character.codePointAt(countryCode, 0) + 0x1F1A5)) + String(Character.toChars(Character.codePointAt(countryCode, 1) + 0x1F1A5))
    }

    fun getCountryCodes(c: Context): HashMap<String, String>{
        val array = c.resources.getStringArray(R.array.countrycodes)
        val hashMap: HashMap<String, String> = HashMap()

        for (s in array) {
            val kv = s.split(",")
            hashMap.put(kv[1].toUpperCase(Locale.ROOT), kv[0])
        }

        return hashMap
    }
}