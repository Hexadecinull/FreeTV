package com.phstudio.freetv.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.phstudio.freetv.R
import com.phstudio.freetv.favorite.Database
import com.phstudio.freetv.player.HTMLActivity
import com.phstudio.freetv.player.PlayerActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class LinkActivity : AppCompatActivity() {

    private val linkList = ArrayList<Triple<String, String, String>>()
    private val filteredLinkList = ArrayList<Triple<String, String, String>>()
    private lateinit var customAdapter: ItemAdapter2

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    // ── Connectivity (fixed for API 34+) ──────────────────────────
    private fun internet(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_country)

        val type = intent.getStringExtra("type")
        val country = intent.getStringExtra("country")

        findViewById<TextView>(R.id.tvPrimary).text = intent.getStringExtra("tvPrimary")
        val image = intent.getIntExtra("ivDrawable", 0)
        if (image != 0) findViewById<AppCompatImageView>(R.id.ivDrawable).setImageResource(image)

        val recyclerView: RecyclerView = findViewById(R.id.rvCountry)
        customAdapter = ItemAdapter2(
            this,
            filteredLinkList,
            object : ItemAdapter2.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    val (names, _, urls) = splitList(filteredLinkList)
                    val url = urls[position]
                    val name = names[position]
                    if (url.contains("youtube.com") || url.contains("youtu.be")) {
                        startActivity(Intent(this@LinkActivity, HTMLActivity::class.java)
                            .putExtra("Url", url))
                    } else {
                        startActivity(Intent(this@LinkActivity, PlayerActivity::class.java)
                            .putExtra("Name", name)
                            .putExtra("Url", url))
                    }
                }
            },
            object : ItemAdapter2.OnItemLongClickListener {
                override fun onItemLongClick(position: Int): Boolean {
                    val (names, logos, urls) = splitList(filteredLinkList)
                    favorite(names[position], logos[position], urls[position])
                    return true
                }
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = customAdapter

        val searchView: SearchView = findViewById(R.id.svCountry)
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(ContextCompat.getColor(this, R.color.primary_text))
        searchEditText.setHintTextColor(ContextCompat.getColor(this, R.color.primary_text))
        searchView.setIconifiedByDefault(false)
        searchView.setQueryHint(getString(R.string.search_channel))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText); return true
            }
        })

        // ── Load content ──────────────────────────────────────────
        when (type) {
            "countries" -> {
                val code = intent.getStringExtra("code")
                // Primary source: iptv-org (actively maintained, auto-checked daily)
                fetchPlaylist("https://iptv-org.github.io/iptv/countries/$code.m3u")
                // Secondary source: Free-TV/IPTV (kept as supplemental only)
                fetchPlaylist("https://raw.githubusercontent.com/Free-TV/IPTV/master/playlists/playlist_$country.m3u8")
            }
            "categories" -> {
                fetchPlaylist("https://iptv-org.github.io/iptv/categories/$country.m3u")
                when (country) {
                    "news" -> {
                        loadCuratedChannels(curatedNews)
                        fetchPlaylist("https://raw.githubusercontent.com/Free-TV/IPTV/master/playlists/playlist_zz_news_en.m3u8")
                    }
                    "music" -> {
                        loadCuratedChannels(curatedMusic)
                    }
                    "movies" -> {
                        fetchPlaylist("https://raw.githubusercontent.com/Free-TV/IPTV/master/playlists/playlist_zz_movies.m3u8")
                    }
                }
            }
            "regions" -> fetchPlaylist("https://iptv-org.github.io/iptv/regions/$country.m3u")
            "languages" -> fetchPlaylist("https://iptv-org.github.io/iptv/languages/$country.m3u")
        }
    }

    // ── Robust M3U parser (attribute-order-agnostic) ─────────────
    private fun parseM3u(content: String): List<Triple<String, String, String>> {
        val result = mutableListOf<Triple<String, String, String>>()
        val lines = content.lines()
        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("#EXTINF")) {
                val name = Regex("""tvg-name="([^"]+)"""").find(line)?.groupValues?.get(1)
                    ?: line.substringAfterLast(",").trim()
                val logo = Regex("""tvg-logo="([^"]+)"""").find(line)?.groupValues?.get(1) ?: ""
                // Find the next non-blank, non-comment line as the URL
                val url = lines.drop(i + 1)
                    .firstOrNull { it.isNotBlank() && !it.startsWith("#") }
                    ?.trim() ?: ""
                if ((url.startsWith("http://") || url.startsWith("https://")) && name.isNotBlank()) {
                    result.add(Triple(name, logo, url))
                }
            }
            i++
        }
        return result
    }

    // ── Fetch playlist via OkHttp + coroutines ───────────────────
    private fun fetchPlaylist(url: String) {
        if (!internet()) {
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val body = httpClient.newCall(request).execute().use { it.body?.string() } ?: return@launch
                val items = parseM3u(body)
                withContext(Dispatchers.Main) {
                    addItems(items)
                }
            } catch (e: Exception) {
                // Silently skip failed sources — other sources may still load
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadCuratedChannels(channels: List<Triple<String, String, String>>) {
        linkList.addAll(channels)
        filteredLinkList.addAll(channels)
        customAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addItems(items: List<Triple<String, String, String>>) {
        // Deduplicate by URL
        val existingUrls = linkList.map { it.third }.toSet()
        val newItems = items.filter { it.third !in existingUrls }
        linkList.addAll(newItems)
        filteredLinkList.addAll(newItems.filter { item ->
            val q = (customAdapter as? ItemAdapter2)?.currentQuery ?: ""
            q.isBlank() || item.first.lowercase().contains(q.lowercase())
        })
        customAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterList(query: String?) {
        filteredLinkList.clear()
        if (query.isNullOrBlank()) {
            filteredLinkList.addAll(linkList)
        } else {
            val q = query.lowercase()
            filteredLinkList.addAll(linkList.filter { it.first.lowercase().contains(q) })
        }
        customAdapter.notifyDataSetChanged()
    }

    private fun splitList(list: ArrayList<Triple<String, String, String>>):
            Triple<ArrayList<String>, ArrayList<String>, ArrayList<String>> {
        val names = ArrayList<String>()
        val logos = ArrayList<String>()
        val urls = ArrayList<String>()
        for ((n, l, u) in list) { names.add(n); logos.add(l); urls.add(u) }
        return Triple(names, logos, urls)
    }

    private fun favorite(name: String, logo: String, url: String) {
        Database(this, null).writeToDb(name, logo, url)
        Toast.makeText(this, getString(R.string.addedToFav), Toast.LENGTH_SHORT).show()
    }

    // ── Curated channels (high-quality, verified live streams) ────
    // These replace the old hardcoded lists. Update here or host as JSON for OTA updates.
    private val curatedNews = listOf(
        Triple("Al Jazeera English", "https://i.imgur.com/BB93NQP.png", "https://live-hls-apps-aje-fa.getaj.net/AJE/index.m3u8"),
        Triple("DW English", "https://i.imgur.com/A1xzjOI.png", "https://dwamdstream102.akamaized.net/hls/live/2015525/dwstream102/index.m3u8"),
        Triple("DW German", "https://i.imgur.com/A1xzjOI.png", "https://dwamdstream106.akamaized.net/hls/live/2017965/dwstream106/index.m3u8"),
        Triple("DW Spanish", "https://i.imgur.com/A1xzjOI.png", "https://dwamdstream104.akamaized.net/hls/live/2015530/dwstream104/index.m3u8"),
        Triple("DW Arabic", "https://i.imgur.com/A1xzjOI.png", "https://dwamdstream103.akamaized.net/hls/live/2015526/dwstream103/index.m3u8"),
        Triple("BBC News", "https://raw.githubusercontent.com/tv-logo/tv-logos/main/countries/united-kingdom/bbc-news-uk.png", "https://vs-hls-push-uk.live.fastly.md.bbci.co.uk/x=4/i=urn:bbc:pips:service:bbc_news_channel_hd/iptv_hd_abr_v1.m3u8"),
        Triple("CGTN", "https://i.imgur.com/fMsJYzl.png", "https://news.cgtn.com/resource/live/english/cgtn-news.m3u8"),
        Triple("NBC News NOW", "https://raw.githubusercontent.com/tv-logo/tv-logos/main/countries/united-kingdom/nbc-news-now-uk.png", "https://dai2.xumo.com/amagi_hls_data_xumo1212A-xumo-nbcnewsnow/CDN/master.m3u8"),
        Triple("Reuters", "https://i.imgur.com/6eQ2nCJ.png", "https://reuters-reutersnow-1-nl.samsung.wurl.tv/playlist.m3u8"),
        Triple("CBS News", "https://raw.githubusercontent.com/tv-logo/tv-logos/main/countries/united-states/cbs-news-us.png", "https://dai.google.com/linear/hls/event/Sid4xiTQTkCT1SLu6rjUSQ/master.m3u8"),
        Triple("ABC News Live", "https://raw.githubusercontent.com/tv-logo/tv-logos/main/countries/united-states/abc-news-live-hz-us.png", "https://lnc-abc-news.tubi.video/index.m3u8"),
        Triple("France 24 English", "https://i.imgur.com/61MSiq9.png", "https://www.youtube.com/france24english/live"),
        Triple("France 24 French", "https://i.imgur.com/61MSiq9.png", "https://www.youtube.com/france24/live"),
        Triple("Euronews", "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9c/Euronews_2022.svg/640px-Euronews_2022.svg.png", "https://www.youtube.com/euronews/live"),
        Triple("Sky News (UK)", "https://d2n0069hmnqmmx.cloudfront.net/epgdata/1.0/newchanlogos/512/512/skychb1404.png", "https://ythls.armelin.one/channel/UCoMdktPbSTixAyNGwb-UYkQ.m3u8"),
        Triple("Africanews", "https://i.imgur.com/xocvePC.png", "https://www.youtube.com/africanews/live"),
    )

    private val curatedMusic = listOf(
        Triple("Óčko", "https://upload.wikimedia.org/wikipedia/commons/2/20/%C3%93%C4%8Dko_logo_2012.png", "https://ocko-live.ssl.cdn.cra.cz/channels/ocko/playlist.m3u8"),
        Triple("Óčko Expres", "https://upload.wikimedia.org/wikipedia/commons/2/2b/%C3%93%C4%8Dko_Expres_logo.png", "https://ocko-live.ssl.cdn.cra.cz/channels/ocko_expres/playlist.m3u8"),
        Triple("Óčko Gold", "https://upload.wikimedia.org/wikipedia/commons/b/b5/%C3%93%C4%8Dko_Star_logo.png", "https://ocko-live.ssl.cdn.cra.cz/channels/ocko_gold/playlist.m3u8"),
        Triple("DELUXE MUSIC", "https://i.imgur.com/E65GQN9.png", "https://sdn-global-live-streaming-packager-cache.3qsdn.com/13456/13456_264_live.m3u8"),
        Triple("DELUXE MUSIC DANCE", "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c2/Deluxe_Dance_by_Kontor_Logo_2023.svg/666px-Deluxe_Dance_by_Kontor_Logo_2023.svg.png", "https://sdn-global-live-streaming-packager-cache.3qsdn.com/64733/64733_264_live.m3u8"),
        Triple("DELUXE MUSIC RAP", "https://upload.wikimedia.org/wikipedia/commons/thumb/0/07/Deluxe_Rap_Logo_2023.svg/666px-Deluxe_Rap_Logo_2023.svg.png", "https://sdn-global-live-streaming-packager-cache.3qsdn.com/65183/65183_264_live.m3u8"),
        Triple("SCHLAGER DELUXE", "https://i.imgur.com/YPpgUOg.png", "https://sdn-global-live-streaming-packager-cache.3qsdn.com/26658/26658_264_live.m3u8"),
        Triple("Kronehit", "", "https://bitcdn-kronehit.bitmovin.com/v2/hls/playlist.m3u8"),
        Triple("Rotana Music", "", "https://rotanastudios-rotanamusic-1-eu.xiaomi.wurl.tv/playlist.m3u8"),
        Triple("Trace Urban (Australia)", "", "https://lightning-traceurban-samsungau.amagi.tv/playlist.m3u8"),
        Triple("The Country Network 4K", "", "https://endpnt.com/hls/tcn4k/playlist.m3u8"),
    )
}
