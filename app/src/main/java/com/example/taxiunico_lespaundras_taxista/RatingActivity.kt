package com.example.taxiunico_lespaundras_taxista

import ApiUtility.ApiClient
import ApiUtility.TaxiTrip
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_rating.*

class RatingActivity : AppCompatActivity() {

    lateinit var trip: TaxiTrip

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating)

        if(intent.hasExtra(FragmentHome.TRIP)) {
            trip = intent.extras.getParcelable(FragmentHome.TRIP)
        }
        rating_text_driver_info.text = trip.user.name
        rating_button_rate.setOnClickListener {
            ApiClient(this@RatingActivity).rateUser(trip.id, rating_driver_stars.rating) { _, success, message ->
                if(success) {
                    val navIntent = Intent(this@RatingActivity, NavbarActivity::class.java).apply {
                        putExtra(LoginActivity.EMAIL, trip.taxi.email)
                    }
                    startActivity(navIntent)
                } else {
                    Toast.makeText(this@RatingActivity, message,Toast.LENGTH_SHORT).show()
                }
            }
        }
        rating_driver_stars.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->  }
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState?.putParcelable(FragmentHome.TRIP, trip)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        trip = savedInstanceState?.getParcelable(FragmentHome.TRIP)!!
    }
}
