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

class LanguagesActivity : AppCompatActivity() {

    private val linkList = ArrayList<Triple<String, Int, String>>()
    private lateinit var customAdapter: ItemAdapter
    private val filteredLinkList = ArrayList<Triple<String, Int, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_country)

        val tvPrimary: TextView = findViewById(R.id.tvPrimary)
        tvPrimary.text = getString(R.string.languages)

        val ivDrawable: AppCompatImageView = findViewById(R.id.ivDrawable)
        ivDrawable.setImageResource(R.drawable.language)

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
        searchView.setQueryHint(getString(R.string.search_language))

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
                getString(R.string.afghan_persian),
                R.drawable.what,
                "afghan_persian;prs"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.afrikaans),
                R.drawable.what,
                "afrikaans;afr"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.albanian),
                R.drawable.what,
                "albanian;sqi"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.amharic),
                R.drawable.what,
                "amharic;amh"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.arabic),
                R.drawable.what,
                "arabic;ara"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.armenian),
                R.drawable.what,
                "armenian;hye"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.assamese),
                R.drawable.what,
                "assamese;asm"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.assyrian_neo_aramaic),
                R.drawable.what,
                "assyrian_neo_aramaic;aii"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.aymara),
                R.drawable.what,
                "aymara;aym"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.azerbaijani),
                R.drawable.what,
                "azerbaijani;aze"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.bashkir),
                R.drawable.what,
                "bashkir;bak"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.basque),
                R.drawable.what,
                "basque;eus"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.belarusian),
                R.drawable.what,
                "belarusian;bel"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.bengali),
                R.drawable.what,
                "bengali;ben"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.bhojpuri),
                R.drawable.what,
                "bhojpuri;bho"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.bosnian),
                R.drawable.what,
                "bosnian;bos"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.bulgarian),
                R.drawable.what,
                "bulgarian;bul"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.burmese),
                R.drawable.what,
                "burmese;mya"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.catalan),
                R.drawable.what,
                "catalan;cat"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.central_kurdish),
                R.drawable.what,
                "central_kurdish;ckb"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.chhattisgarhi),
                R.drawable.what,
                "chhattisgarhi;hne"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.chinese),
                R.drawable.what,
                "chinese;zho"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.croatian),
                R.drawable.what,
                "croatian;hrv"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.czech),
                R.drawable.what,
                "czech;ces"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.danish),
                R.drawable.what,
                "danish;dan"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.dhanwar_nepal),
                R.drawable.what,
                "dhanwar_nepal;dhw"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.dhivehi),
                R.drawable.what,
                "dhivehi;div"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.dholuo),
                R.drawable.what,
                "dholuo;luo"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.dimili),
                R.drawable.what,
                "dimili;zza"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.dutch),
                R.drawable.what,
                "dutch;nld"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.english),
                R.drawable.what,
                "english;eng"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.estonian),
                R.drawable.what,
                "estonian;est"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.ewe),
                R.drawable.what,
                "ewe;ewe"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.faroese),
                R.drawable.what,
                "faroese;fao"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.fataleka),
                R.drawable.what,
                "fataleka;far"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.filipino),
                R.drawable.what,
                "filipino;fil"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.finnish),
                R.drawable.what,
                "finnish;fin"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.french),
                R.drawable.what,
                "french;fra"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.galician),
                R.drawable.what,
                "galician;glg"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.galolen),
                R.drawable.what,
                "galolen;gal"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.ganda),
                R.drawable.what,
                "ganda;lug"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.georgian),
                R.drawable.what,
                "georgian;kat"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.german),
                R.drawable.what,
                "german;deu"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.gikuyu),
                R.drawable.what,
                "gikuyu;gik"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.goan_konkani),
                R.drawable.what,
                "goan_konkani;kok"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.greek),
                R.drawable.what,
                "greek;gre"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.greenlandic),
                R.drawable.what,
                "greenlandic;kal"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.gujarati),
                R.drawable.what,
                "gujarati;guj"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.haitian),
                R.drawable.what,
                "haitian;hat"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.hausa),
                R.drawable.what,
                "hausa;hau"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.hebrew),
                R.drawable.what,
                "hebrew;heb"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.hindi),
                R.drawable.what,
                "hindi;hin"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.hungarian),
                R.drawable.what,
                "hungarian;hun"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.icelandic),
                R.drawable.what,
                "icelandic;isl"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.indonesian),
                R.drawable.what,
                "indonesian;ind"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.inuktitut),
                R.drawable.what,
                "inuktitut;iku"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.iranian_persian),
                R.drawable.what,
                "iranian_persian;fas"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.irish),
                R.drawable.what,
                "irish;gle"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.isekiri),
                R.drawable.what,
                "isekiri;isek"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.italian),
                R.drawable.what,
                "italian;ita"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.japanese),
                R.drawable.what,
                "japanese;jpn"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.javanese),
                R.drawable.what,
                "javanese;jav"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.kannada),
                R.drawable.what,
                "kannada;kan"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.kapampangan),
                R.drawable.what,
                "kapampangan;kpm"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.kazakh),
                R.drawable.what,
                "kazakh;kaz"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.khmer),
                R.drawable.what,
                "khmer;khm"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.kinyarwanda),
                R.drawable.what,
                "kinyarwanda;kin"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.kirghiz),
                R.drawable.what,
                "kirghiz;kir"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.konkani_macrolanguage),
                R.drawable.what,
                "konkani_macrolanguage;kon"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.korean),
                R.drawable.what,
                "korean;kor"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.kurdish),
                R.drawable.what,
                "kurdish;kur"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.lahnda),
                R.drawable.what,
                "lahnda;lhn"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.lao),
                R.drawable.what,
                "lao;lao"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.latin),
                R.drawable.what,
                "latin;lat"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.latvian),
                R.drawable.what,
                "latvian;lav"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.letzeburgesch),
                R.drawable.what,
                "letzeburgesch;lux"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.lingala),
                R.drawable.what,
                "lingala;lin"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.lithuanian),
                R.drawable.what,
                "lithuanian;lit"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.macedonian),
                R.drawable.what,
                "macedonian;mkd"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.malay),
                R.drawable.what,
                "malay;may"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.malayalam),
                R.drawable.what,
                "malayalam;mal"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.maltese),
                R.drawable.what,
                "maltese;mla"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.mandarin_chinese),
                R.drawable.what,
                "mandarin_chinese;cmn"
            )
        )
        linkList.add(Triple(getString(R.string.marathi), R.drawable.what, "marathi;mar"))
        linkList.add(
            Triple(
                getString(R.string.min_nan_chinese),
                R.drawable.what,
                "min_nan_chinese;nan"
            )
        )
        linkList.add(Triple(getString(R.string.mongolian), R.drawable.what, "mongolian;mon"))
        linkList.add(Triple(getString(R.string.montenegrin), R.drawable.what, "montenegrin;cnr"))
        linkList.add(
            Triple(
                getString(R.string.mycenaean_greek),
                R.drawable.what,
                "mycenaean_greek;gmy"
            )
        )
        linkList.add(Triple(getString(R.string.nepali), R.drawable.what, "nepali;nep"))
        linkList.add(
            Triple(
                getString(R.string.northern_kurdish),
                R.drawable.what,
                "northern_kurdish;kmr"
            )
        )
        linkList.add(Triple(getString(R.string.norwegian), R.drawable.what, "norwegian;nor"))
        linkList.add(
            Triple(
                getString(R.string.norwegian_bokmaal),
                R.drawable.what,
                "norwegian_bokmaal;nob"
            )
        )
        linkList.add(
            Triple(
                getString(R.string.oriya_macrolanguage),
                R.drawable.what,
                "oriya_macrolanguage;ori"
            )
        )
        linkList.add(Triple(getString(R.string.panjabi), R.drawable.what, "panjabi;pan"))
        linkList.add(Triple(getString(R.string.papiamento), R.drawable.what, "papiamento;pap"))
        linkList.add(Triple(getString(R.string.parsi_dari), R.drawable.what, "parsi_dari;prd"))
        linkList.add(Triple(getString(R.string.pashto), R.drawable.what, "pashto;pus"))
        linkList.add(Triple(getString(R.string.persian), R.drawable.what, "persian;fas"))
        linkList.add(Triple(getString(R.string.polish), R.drawable.what, "polish;pol"))
        linkList.add(Triple(getString(R.string.portuguese), R.drawable.what, "portuguese;por"))
        linkList.add(Triple(getString(R.string.quechua), R.drawable.what, "quechua;que"))
        linkList.add(Triple(getString(R.string.romanian), R.drawable.what, "romanian;ron"))
        linkList.add(Triple(getString(R.string.romany), R.drawable.what, "romany;rom"))
        linkList.add(Triple(getString(R.string.russian), R.drawable.what, "russian;rus"))
        linkList.add(Triple(getString(R.string.saint_lucian_creole_french), R.drawable.what, "saint_lucian_creole_french;acf"))
        linkList.add(Triple(getString(R.string.samoan), R.drawable.what, "samoan;smo"))
        linkList.add(Triple(getString(R.string.santali), R.drawable.what, "santali;sat"))
        linkList.add(Triple(getString(R.string.serbian), R.drawable.what, "serbian;srp"))
        linkList.add(Triple(getString(R.string.serbo_croatian), R.drawable.what, "serbo_croatian;hbs"))
        linkList.add(Triple(getString(R.string.sindhi), R.drawable.what, "sindhi;snd"))
        linkList.add(Triple(getString(R.string.sinhala), R.drawable.what, "sinhala;sin"))
        linkList.add(Triple(getString(R.string.slovak), R.drawable.what, "slovak;slk"))
        linkList.add(Triple(getString(R.string.slovenian), R.drawable.what, "slovenian;slv"))
        linkList.add(Triple(getString(R.string.somali), R.drawable.what, "somali;som"))
        linkList.add(Triple(getString(R.string.southern_kurdish), R.drawable.what, "southern_kurdish;sdh"))
        linkList.add(Triple(getString(R.string.spanish), R.drawable.what, "spanish;spa"))
        linkList.add(Triple(getString(R.string.swahili), R.drawable.what, "swahili;swa"))
        linkList.add(Triple(getString(R.string.swedish), R.drawable.what, "swedish;swe"))
        linkList.add(Triple(getString(R.string.tagalog), R.drawable.what, "tagalog;tgl"))
        linkList.add(Triple(getString(R.string.tajik), R.drawable.what, "tajik;tgk"))
        linkList.add(Triple(getString(R.string.tamil), R.drawable.what, "tamil;tam"))
        linkList.add(Triple(getString(R.string.tatar), R.drawable.what, "tatar;tat"))
        linkList.add(Triple(getString(R.string.telugu), R.drawable.what, "telugu;tel"))
        linkList.add(Triple(getString(R.string.tetum), R.drawable.what, "tetum;tet"))
        linkList.add(Triple(getString(R.string.thai), R.drawable.what, "thai;tha"))
        linkList.add(Triple(getString(R.string.tibetan), R.drawable.what, "tibetan;bod"))
        linkList.add(Triple(getString(R.string.tigrinya), R.drawable.what, "tigrinya;tir"))
        linkList.add(Triple(getString(R.string.turkish), R.drawable.what, "turkish;tur"))
        linkList.add(Triple(getString(R.string.turkmen), R.drawable.what, "turkmen;tuk"))
        linkList.add(Triple(getString(R.string.ukrainian), R.drawable.what, "ukrainian;ukr"))
        linkList.add(Triple(getString(R.string.urdu), R.drawable.what, "urdu;urd"))
        linkList.add(Triple(getString(R.string.uzbek), R.drawable.what, "uzbek;uzb"))
        linkList.add(Triple(getString(R.string.vietnamese), R.drawable.what, "vietnamese;vie"))
        linkList.add(Triple(getString(R.string.welsh), R.drawable.what, "welsh;cym"))
        linkList.add(Triple(getString(R.string.western_frisian), R.drawable.what, "western_frisian;fry"))
        linkList.add(Triple(getString(R.string.wolof), R.drawable.what, "wolof;wol"))
        linkList.add(Triple(getString(R.string.yucatec_maya), R.drawable.what, "yucatec_maya;yua"))
        linkList.add(Triple(getString(R.string.yue_chinese), R.drawable.what, "yue_chinese;yue"))
        linkList.add(Triple(getString(R.string.undefined), R.drawable.what, "undefined;undefined"))

        filteredLinkList.addAll(linkList)
        customAdapter.notifyDataSetChanged()
    }

    @SuppressLint("DiscouragedApi")
    private fun sendActivity(country: String, code: String) {
        val intent = Intent(this, LinkActivity::class.java)
        intent.putExtra("type", "languages")
        intent.putExtra("country", code)
        intent.putExtra(
            "tvPrimary",
            getString(resources.getIdentifier(country, "string", packageName))
        )
        intent.putExtra("ivDrawable",R.drawable.language)
        startActivity(intent)
    }
}
