package io.github.ssmg4.freetv

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import io.github.ssmg4.freetv.R.id
import io.github.ssmg4.freetv.R.layout
import io.github.ssmg4.freetv.custom.CustomActivity
import io.github.ssmg4.freetv.favorite.Database
import io.github.ssmg4.freetv.favorite.FavoriteActivity
import io.github.ssmg4.freetv.ui.AboutActivity
import io.github.ssmg4.freetv.ui.CategoriesActivity
import io.github.ssmg4.freetv.ui.CountriesActivity
import io.github.ssmg4.freetv.ui.LanguagesActivity
import io.github.ssmg4.freetv.ui.RegionsActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        val clCountries = findViewById<ConstraintLayout>(id.clCountries)
        val clCategories = findViewById<ConstraintLayout>(id.clCategories)
        val clRegions = findViewById<ConstraintLayout>(id.clRegions)
        val clLanguages = findViewById<ConstraintLayout>(id.clLanguages)
        val clCustom = findViewById<ConstraintLayout>(id.clCustom)
        val ivAbout = findViewById<AppCompatImageView>(id.ivAbout)
        val llCountries = findViewById<LinearLayout>(id.llCountries)
        val llStreams = findViewById<LinearLayout>(id.llStreams)
        val llFavorites = findViewById<LinearLayout>(id.llFavorites)

        llCountries.setOnClickListener {
            sendActivity(CountriesActivity())
        }

        llStreams.setOnClickListener {
            sendActivity(CustomActivity())
        }

        llFavorites.setOnClickListener {
            sendActivity(FavoriteActivity())
        }

        ivAbout.setOnClickListener {
            sendActivity(AboutActivity())
        }

        clCountries.setOnClickListener {
            sendActivity(CountriesActivity())
        }

        clCategories.setOnClickListener {
            sendActivity(CategoriesActivity())
        }

        clRegions.setOnClickListener {
            sendActivity(RegionsActivity())
        }
        clLanguages.setOnClickListener {
            sendActivity(LanguagesActivity())
        }

        clCustom.setOnClickListener {
            sendActivity(CustomActivity())
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val dialogBuilder = android.app.AlertDialog.Builder(this@MainActivity)
                dialogBuilder.setIcon(R.mipmap.ic_launcher_round)
                dialogBuilder.setMessage(getString(R.string.closeApp))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                        finishAffinity()
                    }
                    .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                        dialog.cancel()
                    }
                val alert = dialogBuilder.create()
                alert.setTitle(getString(R.string.exitApp))
                alert.show()
            }
        })
    }

    private fun sendActivity(where: AppCompatActivity) {
        val intent = Intent(this, where::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        val tvFavoriteSum = findViewById<TextView>(id.tvFavoriteSum)
        val db = Database(this, null)
        val sum = db.getSum()
        tvFavoriteSum.text = sum.toString()
    }
}