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
import ViewModels.UserViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.fragment_home.*
import java.lang.Exception

/**
 * Used to show current trip
 */
class FragmentHome : Fragment() {

    lateinit var mapFragment: SupportMapFragment
    lateinit var googleMap: GoogleMap
    var trip: TaxiTrip? = null
    lateinit var model: UserViewModel
    var started = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        model = activity?.run {
            ViewModelProviders.of(this).get(UserViewModel::class.java)
        } ?: throw Exception("Invalid activity")
        mapFragment = childFragmentManager.findFragmentById(R.id.home_view_map) as SupportMapFragment
        mapFragment.getMapAsync{
            googleMap = it
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(19.4362, -99.1373), 10f))
            setRoute()
        }

        home_button_refresh_trips.setOnClickListener {
            setRoute()
        }

        home_button_start_end_trip.setOnClickListener {
            if(trip != null) {
                if(!started) {
                    ApiClient(activity?.applicationContext!!).startTrip(trip?.id!!) {_, success, message ->
                        if(success) {
                            home_button_start_end_trip.setText(R.string.home_end_trip)
                            started = true
                        } else {
                            Toast.makeText(activity?.applicationContext, "Try again to start trip", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    val ratingIntent = Intent(activity, RatingActivity::class.java).apply {
                        putExtra(TRIP, trip)
                    }
                    startActivity(ratingIntent)
                }
            }
        }
    }

    /**
     * Display map containing the route belonging to current trip
     */
    private fun setRoute() {
        ApiClient(activity?.applicationContext!!).getTaxiCurrentOrNextTrip(model.taxi?.email!!) {trip, current, success, message ->
            this@FragmentHome.trip = trip
            if(success && trip != null) {
                setInfo(trip, current)
                val origin = "${trip?.origin?.address},${trip?.origin?.city},${trip?.origin?.state}"
                val destination = "${trip?.destination?.address},${trip?.destination?.city},${trip?.destination?.state}"
                ApiClient(activity?.applicationContext!!).getDirections(origin, destination){route, success, message ->
                    if(success) {
                        val lineOptions = PolylineOptions()
                        lineOptions.addAll(route?.points)
                        lineOptions.width(6f)
                        lineOptions.color(ContextCompat.getColor(activity?.applicationContext!!, R.color.gray))
                        googleMap.addPolyline(lineOptions)
                        val oLatLng = LatLng(trip?.origin?.latitue!!, trip.origin.longitude)
                        googleMap.addMarker(MarkerOptions().position(oLatLng).title(trip.origin.name))
                        val dLatLng = LatLng(trip.destination.latitue, trip.destination.longitude)
                        googleMap.addMarker(MarkerOptions().position(dLatLng).title(trip.destination.name))
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(oLatLng, 12f))
                    } else {
                        Toast.makeText(activity?.applicationContext!!, message, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                home_text_title.text = "No trip"
                home_button_start_end_trip.visibility = View.GONE
            }
        }
    }

    /**
     * Fill client information
     */
    private fun setInfo(trip: TaxiTrip, current: Boolean) {
        if(!current) {
            home_text_title.text = "Next trip"
            home_button_start_end_trip.visibility = View.GONE
        } else {
            home_button_start_end_trip.visibility = View.VISIBLE
        }
        home_text_src.text = "Source: " + trip.origin.name
        home_text_dest.text = "Destination: " + trip.destination.name
        home_text_driver_info_name.text = trip.user.name
        started = trip.status == "AC"
        if(started) {
            home_button_start_end_trip.setText(R.string.home_end_trip)
        } else {
            home_button_start_end_trip.setText(R.string.home_start_trip)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    companion object {
        const val TRIP = "Trip"
    }
}