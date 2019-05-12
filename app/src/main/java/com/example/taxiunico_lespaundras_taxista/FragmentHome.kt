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
    private lateinit var model: UserViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        model = activity?.run {
            ViewModelProviders.of(this).get(UserViewModel::class.java)
        } ?: throw Exception("Invalid activity")
        mapFragment = childFragmentManager.findFragmentById(R.id.home_view_map) as SupportMapFragment
        mapFragment.getMapAsync{
            googleMap = it
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(19.4362, -99.1373), 10f))
        }
    }

    private fun setInfo(trip: TaxiTrip, current: Boolean) {
        if(!current) {
            home_text_title.text = "Next trip"
        }
        home_text_src.text = trip.origin.name
        home_text_dest.text = trip.destination.name
        home_text_driver_info_name.text = trip.taxi.driverName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
}