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
package ApiUtility

import android.content.Context
import com.android.volley.*
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import org.json.JSONObject

/**
 * Class in charge of actually halding the http requests
 */
class ApiClient(private val ctx: Context) {

    /***
     * Make api call
     */
    private fun performRequest(route: ApiRoute, completion: (success: Boolean, apiResponse: ApiResponse) -> Unit) {
        val request: JsonObjectRequest =
            object : JsonObjectRequest(route.httpMethod, route.url, route.body, { response ->
                this.handle(response, completion)
            }, {
                it.printStackTrace()
                if (it.networkResponse != null && it.networkResponse.data != null)
                    this.handle(JSONObject().apply {
                        put("message", JSONObject(String(it.networkResponse.data)).optString("message"))
                        put("status", "false")
                    }, completion)
                else
                    this.handle(JSONObject().apply {
                        put("message", getStringError(it))
                        put("status", "false")
                    }, completion)
            }) {
                override fun getHeaders(): MutableMap<String, String> = route.headers
            }
        request.retryPolicy = DefaultRetryPolicy(
            route.timeOut,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        getRequestQueue().add(request)
    }

    /**
     * Creates ApiResponse to be used
     **/
    private fun handle(response: JSONObject, completion: (success: Boolean, apiResponse: ApiResponse) -> Unit) {
        val ar = ApiResponse(response)
        completion.invoke(ar.success, ar)
    }

    /**
     * This method will return the error as String
     **/
    private fun getStringError(volleyError: VolleyError): String {
        return when (volleyError) {
            is TimeoutError -> "The conection timed out."
            is NoConnectionError -> "The conection couldn´t be established."
            is AuthFailureError -> "There was an authentication failure in your request."
            is ServerError -> "Error while prosessing the server response."
            is NetworkError -> "Network error, please verify your conection."
            is ParseError -> "Error while prosessing the server response."
            else -> "Internet error"
        }
    }

    /**
     * We create and return a new instance for the queue of Volley requests.
     **/
    private fun getRequestQueue(): RequestQueue {
        val maxCacheSize = 20 * 1024 * 1024
        val cache = DiskBasedCache(ctx.cacheDir, maxCacheSize)
        val netWork = BasicNetwork(HurlStack())
        val mRequestQueue = RequestQueue(cache, netWork)
        mRequestQueue.start()
        System.setProperty("http.keepAlive", "false")
        return mRequestQueue
    }

    /**
     * Login user to the application
     */
    fun login(email: String, password: String, completion: (logged: Boolean, message: String) -> Unit) {
        val route = ApiRoute.Login(email, password, ctx)
        this.performRequest(route) { success, response ->
            completion.invoke(success, response.message)
        }
    }

    /**
     * Call google maps api to get coordinates from an addres
     */
    fun getCoordinates(address: String, completion: (coord: LatLng?, status: Boolean, message: String) -> Unit) {
        val route = ApiRoute.GetGeoCoding(address, ctx)
        this.performRequest(route) { success, response ->
            val results = response.json.getJSONArray("results")
            if (success && response.json.getString("status") == "OK") {
                val geometry = results.getJSONObject(0).getJSONObject("geometry")
                val location = geometry.getJSONObject("location")
                val lat = location.getDouble("lat")
                val lng = location.getDouble("lng")
                val coord = LatLng(lat, lng)
                completion.invoke(coord, success, "Geocoding complete")
            } else {
                completion.invoke(null, false, response.message)
            }
        }
    }

    /**
     * Used to decode polylines received from the google directions api
     */
    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(p)
        }

        return poly
    }

    /**
     * Call to the google directions api
     */
    fun getDirections(
        origin: String,
        destination: String,
        completion: (route: Route?, status: Boolean, message: String) -> Unit
    ) {
        val route = ApiRoute.GetDirections(origin, destination, ctx)
        this.performRequest(route) { success, response ->
            if (success) {
                val routes = response.json.getJSONArray("routes")
                if (routes.length() == 0) {
                    completion.invoke(null, false, "No routes found")
                } else {
                    val route = routes.getJSONObject(0)
                    val legs = route.getJSONArray("legs")
                    var du = 0
                    var di = 0
                    var du_text = ""
                    var di_text = ""
                    val points: MutableList<LatLng> = mutableListOf()
                    for (i in 0 until legs.length()) {
                        val leg = legs.getJSONObject(i)
                        val steps = leg.getJSONArray("steps")
                        val duration: ValueText =
                            Gson().fromJson(leg.getJSONObject("duration").toString(), ValueText::class.java)
                        du += duration.value
                        du_text = duration.text
                        val distance: ValueText =
                            Gson().fromJson(leg.getJSONObject("distance").toString(), ValueText::class.java)
                        di += distance.value
                        di_text = distance.text
                        for (j in 0 until steps.length()) {
                            val poly = decodePoly(steps.getJSONObject(j).getJSONObject("polyline").getString("points"))
                            points.addAll(poly)
                        }
                    }
                    completion.invoke(Route(points, ValueText(du, du_text), ValueText(di, di_text)), success, "Route found")
                }
            } else {
                completion.invoke(null, success, response.message)
            }
        }
    }
}