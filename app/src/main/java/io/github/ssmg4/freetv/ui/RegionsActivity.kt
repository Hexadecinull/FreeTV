package io.github.ssmg4.freetv.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.ssmg4.freetv.R

class RegionsActivity : AppCompatActivity() {

    private val linkList = ArrayList<Triple<String, Int, String>>()
    private lateinit var customAdapter: ItemAdapter
    private val filteredLinkList = ArrayList<Triple<String, Int, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_country)

        val tvPrimary: TextView = findViewById(R.id.tvPrimary)
        tvPrimary.text = getString(R.string.regions)

        val ivDrawable: AppCompatImageView = findViewById(R.id.ivDrawable)
        ivDrawable.setImageResource(R.drawable.region)

        val recyclerView: RecyclerView = findViewById(R.id.rvCountry)
        customAdapter =
            ItemAdapter(filteredLinkList, object : ItemAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    val (_, _, stringList2) = splitList(filteredLinkList)
                    val text = stringList2[position]
                    val parts = text.split(";")
                    sendActivity(parts[0], parts[1])
                }
            }, object : ItemAdapter.OnItemLongClickListener {
                override fun onItemLongClick(position: Int): Boolean {
                    val (_, _, stringList2) = splitList(filteredLinkList)
                    val text = stringList2[position]
                    val parts = text.split(";")
                    sendActivity(parts[0], parts[1])

                    return true
                }
            })

        val layoutManager = LinearLayoutManager(applicationContext)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = customAdapter
        prepareItems()

        val searchView: SearchView = findViewById(R.id.svCountry)

        val searchEditText =
            searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(ContextCompat.getColor(this, R.color.primary_text))
        searchView.setIconifiedByDefault(false)
        searchEditText.setHintTextColor(ContextCompat.getColor(this, R.color.primary_text))
        searchView.setQueryHint(getString(R.string.search_region))

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterList(query: String?) {
        filteredLinkList.clear()
        if (query.isNullOrBlank()) {
            filteredLinkList.addAll(linkList)
        } else {
            val lowerCaseQuery = query.lowercase()
            filteredLinkList.addAll(linkList.filter {
                it.first.lowercase().contains(lowerCaseQuery)
            })
        }
        customAdapter.notifyDataSetChanged()
    }

    private fun splitList(newsList: ArrayList<Triple<String, Int, String>>): Triple<ArrayList<String>, ArrayList<Int>, ArrayList<String>> {
        val stringList1 = ArrayList<String>()
        val intList = ArrayList<Int>()
        val stringList2 = ArrayList<String>()

        for (pair in newsList) {
            stringList1.add(pair.first)
            intList.add(pair.second)
            stringList2.add(pair.third)
        }
        return Triple(stringList1, intList, stringList2)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun prepareItems() {
        linkList.add(
            Triple(
                getString(R.string.africa),
                R.drawable.what,
                "africa;afr"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.americas),
                R.drawable.what,
                "americas;amer"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.arab_world),
                R.drawable.what,
                "arab_world;arab"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.asia),
                R.drawable.what,
                "asia;asia"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.asia_pacific),
                R.drawable.what,
                "asia_pacific;apac"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.asean),
                R.drawable.what,
                "asean;asean"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.balkan),
                R.drawable.what,
                "balkan;balkan"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.benelux),
                R.drawable.what,
                "benelux;benelux"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.caribbean),
                R.drawable.what,
                "caribbean;carib"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.central_america),
                R.drawable.what,
                "central_america;cenamer"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.central_and_eastern_europe),
                R.drawable.what,
                "central_and_eastern_europe;cee"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.central_asia),
                R.drawable.what,
                "central_asia;cas"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.cis),
                R.drawable.what,
                "cis;cis"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.europe),
                R.drawable.what,
                "europe;eur"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.emea),
                R.drawable.what,
                "emea;emea"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.european_union),
                R.drawable.what,
                "european_union;eu"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.hispanic_america),
                R.drawable.what,
                "hispanic_america;hispam"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.latin_america),
                R.drawable.what,
                "latin_america;latam"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.lac),
                R.drawable.what,
                "lac;lac"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.maghreb),
                R.drawable.what,
                "maghreb;maghreb"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.middle_east),
                R.drawable.what,
                "middle_east;mideast"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.mena),
                R.drawable.what,
                "mena;mena"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.nordics),
                R.drawable.what,
                "nordics;nord"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.north_america),
                R.drawable.what,
                "north_america;noram"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.northern_america),
                R.drawable.what,
                "northern_america;nam"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.northern_europe),
                R.drawable.what,
                "northern_europe;neur"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.oceania),
                R.drawable.what,
                "oceania;oce"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.south_america),
                R.drawable.what,
                "south_america;southam"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.south_asia),
                R.drawable.what,
                "south_asia;sas"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.southeast_asia),
                R.drawable.what,
                "southeast_asia;sea"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.southern_europe),
                R.drawable.what,
                "southern_europe;ser"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.sub_saharan_africa),
                R.drawable.what,
                "sub_saharan_africa;ssa"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.west_africa),
                R.drawable.what,
                "west_africa;wafr"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.western_europe),
                R.drawable.what,
                "western_europe;wer"
            )
        )

        filteredLinkList.addAll(linkList)
        customAdapter.notifyDataSetChanged()
    }

    @SuppressLint("DiscouragedApi")
    private fun sendActivity(country: String,code: String) {
        val intent = Intent(this, LinkActivity::class.java)
        intent.putExtra("type", "regions")
        intent.putExtra("country", code)
        intent.putExtra(
            "tvPrimary",
            getString(resources.getIdentifier(country, "string", packageName))
        )
        intent.putExtra("ivDrawable",R.drawable.region)
        startActivity(intent)
    }
}
