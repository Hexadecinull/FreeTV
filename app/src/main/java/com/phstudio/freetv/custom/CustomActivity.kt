package com.phstudio.freetv.custom

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import coil.load
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

class CustomActivity : AppCompatActivity() {

    // SAF export launcher — replaces WRITE_EXTERNAL_STORAGE
    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri ?: return@registerForActivityResult
        CoroutineScope(Dispatchers.IO).launch {
            val db = DatabaseHelper(this@CustomActivity, null)
            val success = contentResolver.openOutputStream(uri)?.use { db.exportToStream(it) } ?: false
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@CustomActivity,
                    if (success) getString(R.string.saveOkay) else getString(R.string.error),
                    Toast.LENGTH_SHORT
                ).show()
                restart()
            }
        }
    }

    // SAF import launcher — replaces deprecated startActivityForResult
    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@registerForActivityResult
        importFromUri(uri)
    }

    // Import from M3U URL (new feature)
    private val m3uUrlLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@registerForActivityResult
        importFromUri(uri)
    }

    private fun importFromUri(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val stream = contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(stream, Charset.forName("UTF-8")))
                val db = DatabaseHelper(this@CustomActivity, null)
                val lines = reader.readLines()

                // Detect format: M3U or CSV
                if (lines.firstOrNull()?.startsWith("#EXTM3U") == true || lines.firstOrNull()?.startsWith("#EXTINF") == true) {
                    // M3U format
                    parseAndImportM3u(lines, db)
                } else {
                    // CSV format
                    lines.forEach { line ->
                        val item = line.split(",")
                        if (item.size >= 4 && item[1] != "name") {
                            db.writeToDb(item[1], item[2], item[3])
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CustomActivity, getString(R.string.okay), Toast.LENGTH_SHORT).show()
                    restart()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CustomActivity, "${getString(R.string.error)}: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun parseAndImportM3u(lines: List<String>, db: DatabaseHelper) {
        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("#EXTINF")) {
                val name = Regex("""tvg-name="([^"]+)"""").find(line)?.groupValues?.get(1)
                    ?: line.substringAfterLast(",").trim()
                val logo = Regex("""tvg-logo="([^"]+)"""").find(line)?.groupValues?.get(1) ?: ""
                val url = lines.drop(i + 1).firstOrNull { it.isNotBlank() && !it.startsWith("#") }?.trim() ?: ""
                if (url.startsWith("http") && name.isNotBlank()) {
                    db.writeToDb(name, logo, url)
                }
            }
            i++
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_custom)

        val db = DatabaseHelper(this, null)
        val userList = db.getData()
        db.close()

        val lv = findViewById<ListView>(R.id.lvCustom)
        val adapter: SimpleAdapter = object : SimpleAdapter(
            this, userList, R.layout.list_favorite,
            arrayOf("name", "logo"), intArrayOf(R.id.tvFavorite, R.id.ivFavorite)
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val imageView = view.findViewById<ImageView>(R.id.ivFavorite)
                val logoUrl = userList[position]["logo"].toString()
                if (logoUrl.isNotBlank()) {
                    imageView.load(logoUrl) {
                        placeholder(R.drawable.image)
                        error(R.drawable.image)
                    }
                } else {
                    imageView.setImageResource(R.drawable.image)
                }
                return view
            }
        }
        lv.adapter = adapter
        lv.setOnItemClickListener { _, _, pos, _ -> openChannel(pos) }
        lv.setOnItemLongClickListener { _, _, pos, _ -> openEditDialog(pos); true }

        val tvEmpty = findViewById<TextView>(R.id.tvCustomEmpty)
        val ivCustom = findViewById<ImageView>(R.id.ivCustom)
        val emptyVisibility = if (adapter.isEmpty) View.VISIBLE else View.GONE
        tvEmpty.visibility = emptyVisibility
        ivCustom.visibility = emptyVisibility

        tvEmpty.setOnClickListener { showAddDialog() }
        ivCustom.setOnClickListener { showAddDialog() }

        findViewById<Button>(R.id.btAdd).setOnClickListener { showAddDialog() }
        findViewById<Button>(R.id.btExport).setOnClickListener { dialogExportDb() }
        findViewById<Button>(R.id.btImport).setOnClickListener { dialogImportDb() }
        findViewById<Button>(R.id.btDelete).setOnClickListener { dialogDeleteDb() }
    }

    private fun showAddDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_custom)
        dialog.window?.apply {
            attributes = attributes.also { it.width = WindowManager.LayoutParams.MATCH_PARENT }
        }
        val etName = dialog.findViewById<EditText>(R.id.etName)
        val etLogo = dialog.findViewById<EditText>(R.id.etLogo)
        val etLink = dialog.findViewById<EditText>(R.id.etLink)

        dialog.findViewById<Button>(R.id.btOkay).setOnClickListener {
            val name = etName.text.toString().trim()
            val logo = etLogo.text.toString().trim()
            val url = etLink.text.toString().trim()
            if (name.isBlank() || url.isBlank()) {
                Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            DatabaseHelper(this, null).also { it.writeToDb(name, logo, url); it.close() }
            dialog.dismiss()
            restart()
        }
        dialog.show()
    }

    @SuppressLint("Range")
    private fun openEditDialog(pos: Int) {
        val db = DatabaseHelper(this, null)
        val cursor = db.readFromDb()
        cursor?.moveToPosition(pos)
        val id   = cursor?.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ID_COL)) ?: return
        var name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL1))
        var logo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL2))
        var url  = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL3))
        cursor.close(); db.close()

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_custom)
        dialog.window?.apply {
            attributes = attributes.also { it.width = WindowManager.LayoutParams.MATCH_PARENT }
        }
        val etName = dialog.findViewById<EditText>(R.id.etName).also { it.setText(name) }
        val etLogo = dialog.findViewById<EditText>(R.id.etLogo).also { it.setText(logo) }
        val etLink = dialog.findViewById<EditText>(R.id.etLink).also { it.setText(url) }
        val btDelete = dialog.findViewById<Button>(R.id.btDelete).also { it.visibility = View.VISIBLE }

        dialog.findViewById<Button>(R.id.btOkay).setOnClickListener {
            name = etName.text.toString().trim()
            logo = etLogo.text.toString().trim()
            url  = etLink.text.toString().trim()
            DatabaseHelper(this, null).also { it.editToDb(name, logo, url, id); it.close() }
            dialog.dismiss(); restart()
        }
        btDelete.setOnClickListener {
            dialog.dismiss()
            android.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.deleteRec))
                .setMessage(getString(R.string.really))
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    DatabaseHelper(this, null).also { it.deleteRec(id.toString()); it.close() }
                    Toast.makeText(this, getString(R.string.record) + id + getString(R.string.wasDeleted), Toast.LENGTH_SHORT).show()
                    restart()
                }
                .setNegativeButton(getString(R.string.no)) { d, _ -> d.cancel() }
                .show()
        }
        dialog.show()
    }

    @SuppressLint("Range")
    private fun openChannel(pos: Int) {
        val db = DatabaseHelper(this, null)
        val cursor = db.readFromDb()
        cursor?.moveToPosition(pos)
        val url  = cursor?.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL3)) ?: ""
        val name = cursor?.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL1)) ?: ""
        cursor?.close(); db.close()

        if (url.contains("youtube.com") || url.contains("youtu.be")) {
            startActivity(Intent(this, HTMLActivity::class.java).putExtra("Url", url))
        } else {
            startActivity(Intent(this, PlayerActivity::class.java).putExtra("Url", url).putExtra("Name", name))
        }
    }

    private fun dialogImportDb() {
        android.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.importText))
            .setMessage(getString(R.string.really))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> importLauncher.launch("*/*") }
            .setNegativeButton(getString(R.string.no)) { d, _ -> d.cancel() }
            .show()
    }

    private fun dialogExportDb() {
        android.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.exportText))
            .setMessage(getString(R.string.really))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> exportLauncher.launch("FreeTV_Custom.csv") }
            .setNegativeButton(getString(R.string.no)) { d, _ -> d.cancel() }
            .show()
    }

    private fun dialogDeleteDb() {
        android.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.deleteText))
            .setMessage(getString(R.string.really))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                DatabaseHelper(this, null).also { it.deleteAllRec(); it.close() }
                restart()
            }
            .setNegativeButton(getString(R.string.no)) { d, _ -> d.cancel() }
            .show()
    }

    private fun restart() { finish(); startActivity(intent) }
}
