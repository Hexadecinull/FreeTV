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

class CategoriesActivity : AppCompatActivity() {

    private val linkList = ArrayList<Triple<String, Int, String>>()
    private lateinit var customAdapter: ItemAdapter
    private val filteredLinkList = ArrayList<Triple<String, Int, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_country)

        val tvPrimary: TextView = findViewById(R.id.tvPrimary)
        tvPrimary.text = getString(R.string.categories)

        val ivDrawable: AppCompatImageView = findViewById(R.id.ivDrawable)
        ivDrawable.setImageResource(R.drawable.category)

        val recyclerView: RecyclerView = findViewById(R.id.rvCountry)
        customAdapter =
            ItemAdapter(filteredLinkList, object : ItemAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    val (_, _, text) = splitList(filteredLinkList)
                    val country = text[position]
                    sendActivity(country)
                }
            }, object : ItemAdapter.OnItemLongClickListener {
                override fun onItemLongClick(position: Int): Boolean {
                    val (_, _, text) = splitList(filteredLinkList)
                    val country = text[position]
                    sendActivity(country)

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
        searchView.setQueryHint(getString(R.string.search_category))

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
                getString(R.string.animation),
                R.drawable.animation,
                "animation"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.auto),
                R.drawable.auto,
                "auto"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.business),
                R.drawable.business,
                "business"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.classic),
                R.drawable.classic,
                "classic"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.comedy),
                R.drawable.comedy,
                "comedy"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.cooking),
                R.drawable.cooking,
                "cooking"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.culture),
                R.drawable.culture,
                "culture"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.documentary),
                R.drawable.documentary,
                "documentary"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.education),
                R.drawable.education,
                "education"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.entertainment),
                R.drawable.entertainment,
                "entertainment"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.family),
                R.drawable.family,
                "family"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.general),
                R.drawable.general,
                "general"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.kids),
                R.drawable.kids,
                "kids"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.legislative),
                R.drawable.legislative,
                "legislative"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.lifestyle),
                R.drawable.lifestyle,
                "lifestyle"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.movies),
                R.drawable.movies,
                "movies"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.music),
                R.drawable.music,
                "music"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.news),
                R.drawable.news,
                "news"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.outdoor),
                R.drawable.outdoor,
                "outdoor"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.relax),
                R.drawable.relax,
                "relax"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.religious),
                R.drawable.religious,
                "religious"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.science),
                R.drawable.science,
                "science"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.series),
                R.drawable.series,
                "series"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.shop),
                R.drawable.shop,
                "shop"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.sports),
                R.drawable.sports,
                "sports"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.travel),
                R.drawable.travel,
                "travel"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.weather),
                R.drawable.weather,
                "weather"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.undefined),
                R.drawable.what,
                "undefined"
            )
        )


        filteredLinkList.addAll(linkList)
        customAdapter.notifyDataSetChanged()
    }

    @SuppressLint("DiscouragedApi")
    private fun sendActivity(country: String) {
        val intent = Intent(this, LinkActivity::class.java)
        intent.putExtra("type", "categories")
        intent.putExtra("country", country)
        intent.putExtra(
            "tvPrimary",
            getString(resources.getIdentifier(country, "string", packageName))
        )
        intent.putExtra("ivDrawable", resources.getIdentifier(country, "drawable", packageName))
        startActivity(intent)
    }
}
