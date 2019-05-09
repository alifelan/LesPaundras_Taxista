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