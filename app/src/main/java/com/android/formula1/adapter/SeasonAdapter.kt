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
import com.android.formula1.R
import com.android.formula1.misc.Season

class SeasonAdapter(private val context: Context, private val source: ArrayList<Season>) : BaseAdapter() {
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val v = if(convertView == null) inflater.inflate(R.layout.listitem_season, parent, false) else convertView

        v.findViewById<TextView>(R.id.tv_year).setText(source.get(position).year)

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
