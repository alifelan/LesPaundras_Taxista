/*
 * MIT License
 *
 * Copyright (c) 2019 José Luis Felán Villaseñor
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.example.taxiunico_lespaundras_taxista

import ApiUtility.ApiClient
import ApiUtility.TaxiTrip
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_rating.*

/**
 * Activity that shows stars to rate the client
 */
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
