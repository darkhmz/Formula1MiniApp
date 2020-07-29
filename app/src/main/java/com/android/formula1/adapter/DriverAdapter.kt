package com.android.formula1.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.android.formula1.misc.Driver
import com.android.formula1.R

class DriverAdapter(private val context: Context, private val source: ArrayList<Driver>) : BaseAdapter() {
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val v = if(convertView == null) inflater.inflate(R.layout.listitem_driver, parent, false) else convertView

        val driver = source.get(position)

        v.findViewById<TextView>(R.id.tv_name).setText(context.getString(
            R.string.drivername, driver.givenName, driver.familyName))
        v.findViewById<TextView>(R.id.tv_nationality).setText(driver.flagEmoji)
        v.findViewById<TextView>(R.id.tv_dob).setText(driver.dateOfBirth)
        v.findViewById<TextView>(R.id.tv_number).setText(driver.permanentNumber)

        v.findViewById<ImageView>(R.id.iv_info).setTag(position)

        v.findViewById<ImageView>(R.id.iv_info).setOnClickListener {
            val openInBrowser = Intent(Intent.ACTION_VIEW)
            openInBrowser.data = Uri.parse(source.get(it.getTag() as Int).url)
            context.startActivity(openInBrowser)
        }

        return v
    }

    override fun getItem(position: Int): Any {
        return source[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return source.size
    }
}
