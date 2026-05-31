package io.github.ssmg4.freetv.favorite

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import io.github.ssmg4.freetv.R
import android.widget.Toast
import java.io.OutputStream
import java.io.PrintWriter

class Database(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "PH studio"
        // Bump version only when schema changes — use migrations, never DROP TABLE
        private const val DATABASE_VERSION = 4
        const val TABLE_NAME = "Favorite"
        const val ID_COL = "id"
        const val COL1 = "name"
        const val COL2 = "logo"
        const val COL3 = "url"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
                    "$ID_COL INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COL1 TEXT, " +
                    "$COL2 TEXT, " +
                    "$COL3 TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Safe migrations — NEVER DROP TABLE (users lose all favorites!)
        // Add future column migrations here:
        // if (oldVersion < 5) db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN category TEXT DEFAULT ''")
    }

    // onOpen intentionally does NOT call onCreate — SQLite handles table creation automatically
    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
    }

    @SuppressLint("Recycle", "Range")
    fun getData(): ArrayList<HashMap<String, Any>> {
        val db = this.readableDatabase
        val userList = ArrayList<HashMap<String, Any>>()
        val cursor = db.rawQuery("SELECT $COL1, $COL2, $COL3 FROM $TABLE_NAME", null)
        while (cursor.moveToNext()) {
            userList.add(hashMapOf(
                "name" to cursor.getString(cursor.getColumnIndexOrThrow(COL1)),
                "logo" to cursor.getString(cursor.getColumnIndexOrThrow(COL2)),
                "url" to cursor.getString(cursor.getColumnIndexOrThrow(COL3))
            ))
        }
        cursor.close()
        db.close()
        return userList
    }

    fun writeToDb(name: String, logo: String, url: String) {
        val db = this.writableDatabase
        db.insert(TABLE_NAME, null, ContentValues().apply {
            put(COL1, name); put(COL2, logo); put(COL3, url)
        })
        db.close()
    }

    fun readFromDb(): Cursor? = this.readableDatabase.rawQuery("SELECT * FROM $TABLE_NAME", null)

    fun getSum(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_NAME", null)
        val count = if (cursor.moveToFirst()) cursor.getInt(0) else 0
        cursor.close()
        db.close()
        return count
    }

    fun deleteRec(id: String): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_NAME, "$ID_COL = ?", arrayOf(id)).also { db.close() }
    }

    fun deleteAllRec() {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, null, null)
        db.close()
    }

    /**
     * Export favorites to an OutputStream (caller provides via SAF).
     * No longer uses legacy getExternalStoragePublicDirectory which breaks on Android 13+.
     */
    @SuppressLint("Range")
    fun exportToStream(stream: OutputStream): Boolean {
        return try {
            val db = this.readableDatabase
            val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)
            val writer = PrintWriter(stream)
            writer.println("id,name,logo,url")
            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(ID_COL))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COL1)).replace(",", ";")
                val logo = cursor.getString(cursor.getColumnIndexOrThrow(COL2)).replace(",", ";")
                val url = cursor.getString(cursor.getColumnIndexOrThrow(COL3))
                writer.println("$id,$name,$logo,$url")
            }
            writer.flush()
            cursor.close()
            db.close()
            true
        } catch (e: Exception) {
            false
        }
    }
}
