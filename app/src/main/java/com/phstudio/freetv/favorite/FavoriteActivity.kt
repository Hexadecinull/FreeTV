package com.phstudio.freetv.favorite

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.phstudio.freetv.R
import com.phstudio.freetv.player.HTMLActivity
import com.phstudio.freetv.player.PlayerActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset

class FavoriteActivity : AppCompatActivity() {

    // SAF export launcher — replaces legacy WRITE_EXTERNAL_STORAGE
    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri ?: return@registerForActivityResult
        CoroutineScope(Dispatchers.IO).launch {
            val db = Database(this@FavoriteActivity, null)
            val stream = contentResolver.openOutputStream(uri)
            val success = stream?.use { db.exportToStream(it) } ?: false
            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(this@FavoriteActivity, getString(R.string.saveOkay), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@FavoriteActivity, getString(R.string.error), Toast.LENGTH_SHORT).show()
                }
                restart()
            }
        }
    }

    // SAF import launcher — replaces deprecated startActivityForResult
    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@registerForActivityResult
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val stream = contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(stream, Charset.forName("UTF-8")))
                val db = Database(this@FavoriteActivity, null)
                reader.readLines().forEach { line ->
                    val item = line.split(",")
                    if (item.size >= 4 && item[1] != "name") {
                        db.writeToDb(item[1], item[2], item[3])
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FavoriteActivity, getString(R.string.okay), Toast.LENGTH_SHORT).show()
                    restart()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FavoriteActivity, "${getString(R.string.error)}: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_favorite)

        val db = Database(this, null)
        val userList = db.getData()
        db.close()

        val lv = findViewById<ListView>(R.id.lvFavorite)
        val adapter: SimpleAdapter = object : SimpleAdapter(
            this, userList, R.layout.list_favorite,
            arrayOf("name", "logo"), intArrayOf(R.id.tvFavorite, R.id.ivFavorite)
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val imageView = view.findViewById<ImageView>(R.id.ivFavorite)
                val logoUrl = userList[position]["logo"].toString()
                loadImageWithCoil(logoUrl, imageView)
                return view
            }
        }
        lv.adapter = adapter

        lv.setOnItemClickListener { _, _, pos, _ -> openChannel(pos) }
        lv.setOnItemLongClickListener { _, _, pos, _ -> dialogDeleteRec(pos); true }

        findViewById<Button>(R.id.btExport).setOnClickListener { dialogExportDb() }
        findViewById<Button>(R.id.btImport).setOnClickListener { dialogImportDb() }
        findViewById<Button>(R.id.btDelete).setOnClickListener { dialogDeleteDb() }

        findViewById<TextView>(R.id.tvFavoriteEmpty).visibility =
            if (adapter.isEmpty) View.VISIBLE else View.GONE
    }

    private fun loadImageWithCoil(url: String, imageView: ImageView) {
        if (url.isBlank()) {
            imageView.setImageResource(R.drawable.image)
            return
        }
        val loader = ImageLoader(this)
        val request = ImageRequest.Builder(this)
            .data(url)
            .target(imageView)
            .placeholder(R.drawable.image)
            .error(R.drawable.image)
            .build()
        loader.enqueue(request)
    }

    @SuppressLint("Range")
    private fun openChannel(pos: Int) {
        val db = Database(this, null)
        val cursor = db.readFromDb()
        cursor?.moveToPosition(pos)
        val url = cursor?.getString(cursor.getColumnIndexOrThrow(Database.COL3)) ?: ""
        val name = cursor?.getString(cursor.getColumnIndexOrThrow(Database.COL1)) ?: ""
        cursor?.close(); db.close()

        if (url.contains("youtube.com") || url.contains("youtu.be")) {
            startActivity(Intent(this, HTMLActivity::class.java).putExtra("Url", url))
        } else {
            startActivity(Intent(this, PlayerActivity::class.java).putExtra("Url", url).putExtra("Name", name))
        }
    }

    @SuppressLint("Range")
    private fun dialogDeleteRec(pos: Int) {
        android.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.deleteRec))
            .setMessage(getString(R.string.really))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                val db = Database(this, null)
                val cursor = db.readFromDb()
                cursor?.moveToPosition(pos)
                val id = cursor?.getString(cursor.getColumnIndexOrThrow(Database.ID_COL)) ?: ""
                cursor?.close()
                db.deleteRec(id)
                db.close()
                Toast.makeText(this, getString(R.string.record) + id + getString(R.string.wasDeleted), Toast.LENGTH_SHORT).show()
                restart()
            }
            .setNegativeButton(getString(R.string.no)) { d, _ -> d.cancel() }
            .show()
    }

    private fun dialogImportDb() {
        android.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.importText))
            .setMessage(getString(R.string.really))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> importLauncher.launch("text/csv") }
            .setNegativeButton(getString(R.string.no)) { d, _ -> d.cancel() }
            .show()
    }

    private fun dialogExportDb() {
        android.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.exportText))
            .setMessage(getString(R.string.really))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> exportLauncher.launch("FreeTV_Favorites.csv") }
            .setNegativeButton(getString(R.string.no)) { d, _ -> d.cancel() }
            .show()
    }

    private fun dialogDeleteDb() {
        android.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.deleteText))
            .setMessage(getString(R.string.really))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                Database(this, null).also { it.deleteAllRec(); it.close() }
                restart()
            }
            .setNegativeButton(getString(R.string.no)) { d, _ -> d.cancel() }
            .show()
    }

    private fun restart() {
        finish()
        startActivity(intent)
    }
}
