package com.example.absensisiswa.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class GeofenceStatus(val label: String, val badgeText: String) {
    DI_SEKOLAH("Di Sekolah", "🟢 Di Sekolah"),
    DI_LUAR_AREA("Di Luar Area", "🔴 Di Luar Area"),
    BELUM_TERDETEKSI("Belum Terdeteksi", "⚪ Belum Terdeteksi"),
    PERLU_VERIFIKASI("Perlu Verifikasi", "🟡 Perlu Verifikasi")
}

data class LocationVerificationResult(
    val isSuccess: Boolean,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float? = null,
    val distanceFromSchoolMeters: Float? = null,
    val isInsideRadius: Boolean = false,
    val isMockLocation: Boolean = false,
    val geofenceStatus: GeofenceStatus = GeofenceStatus.BELUM_TERDETEKSI,
    val message: String = "",
    val errorType: LocationErrorType? = null
)

enum class LocationErrorType {
    PERMISSION_DENIED,
    GPS_DISABLED,
    POOR_ACCURACY,
    OUT_OF_BOUNDS,
    TIMEOUT,
    MOCK_LOCATION_DETECTED,
    UNKNOWN
}

object LocationUtils {

    // Default SMK TRITECH INFORMATIKA MEDAN Coordinates
    // Jl. Bhayangkara No. 434, Medan
    const val DEFAULT_SCHOOL_LAT = 3.606200
    const val DEFAULT_SCHOOL_LON = 98.697400
    const val DEFAULT_SCHOOL_RADIUS_METERS = 100f
    const val DEFAULT_MAX_ACCURACY_METERS = 80f
    const val DEFAULT_MONITORING_START = "07:00"
    const val DEFAULT_MONITORING_END = "13:30"

    /**
     * Calculates geodesic distance between two coordinates in meters using the Haversine formula.
     */
    fun calculateDistanceMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val earthRadius = 6371000.0 // in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (earthRadius * c).toFloat()
    }

    /**
     * Checks if location permission is granted.
     */
    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if GPS location provider is enabled.
     */
    fun isGpsEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
    }

    /**
     * Detects if location is mocked or simulated.
     */
    fun isLocationMocked(location: Location): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location.isMock
        } else {
            @Suppress("DEPRECATION")
            location.isFromMockProvider
        }
    }

    /**
     * Checks if current time is within school monitoring hours (e.g. 07:00 to 13:30).
     */
    fun isWithinMonitoringHours(startTime: String, endTime: String): Boolean {
        return try {
            val now = java.util.Calendar.getInstance()
            val currentMinutes = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE)

            val startParts = startTime.split(":")
            val startMinutes = startParts[0].toInt() * 60 + startParts.getOrElse(1) { "00" }.toInt()

            val endParts = endTime.split(":")
            val endMinutes = endParts[0].toInt() * 60 + endParts.getOrElse(1) { "00" }.toInt()

            currentMinutes in startMinutes..endMinutes
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Retrieves single-shot device location safely without continuous background tracking.
     */
    @SuppressLint("MissingPermission")
    suspend fun getSingleLocation(context: Context, timeoutMs: Long = 6000L): Location? {
        if (!hasLocationPermission(context)) return null

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null

        // Try getting last known location first if fresh (< 60s)
        val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        val bestLastKnown = when {
            gpsLocation != null && networkLocation != null -> {
                if (gpsLocation.time > networkLocation.time) gpsLocation else networkLocation
            }
            gpsLocation != null -> gpsLocation
            else -> networkLocation
        }

        val currentTime = System.currentTimeMillis()
        if (bestLastKnown != null && (currentTime - bestLastKnown.time) < 45_000L) {
            return bestLastKnown
        }

        // Request single update with coroutine timeout
        return withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine { continuation ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        locationManager.removeUpdates(this)
                        if (continuation.isActive) {
                            continuation.resume(location)
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }

                try {
                    val provider = when {
                        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                        else -> null
                    }

                    if (provider != null) {
                        locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                        continuation.invokeOnCancellation {
                            locationManager.removeUpdates(listener)
                        }
                    } else {
                        continuation.resume(bestLastKnown)
                    }
                } catch (e: Exception) {
                    continuation.resume(bestLastKnown)
                }
            }
        } ?: bestLastKnown
    }

    /**
     * Verifies student location against school geofence and parameters.
     */
    suspend fun verifyAttendanceLocation(
        context: Context,
        schoolLat: Double,
        schoolLon: Double,
        schoolRadiusMeters: Float,
        maxAccuracyMeters: Float,
        simulatedLocationMode: Boolean = false,
        simulateInsideSchool: Boolean = true
    ): LocationVerificationResult {
        // If simulation mode enabled for testing/emulator
        if (simulatedLocationMode) {
            val dist = if (simulateInsideSchool) 38f else 320f
            val acc = 8f
            val isInside = dist <= schoolRadiusMeters
            return LocationVerificationResult(
                isSuccess = isInside,
                latitude = schoolLat,
                longitude = schoolLon,
                accuracyMeters = acc,
                distanceFromSchoolMeters = dist,
                isInsideRadius = isInside,
                isMockLocation = false,
                geofenceStatus = if (isInside) GeofenceStatus.DI_SEKOLAH else GeofenceStatus.DI_LUAR_AREA,
                message = if (isInside) "Anda berada di area sekolah (${dist.toInt()} m, Akurasi: ${acc.toInt()} m)"
                else "Di luar area sekolah: ${dist.toInt()} m (radius maksimal: ${schoolRadiusMeters.toInt()} m)",
                errorType = if (isInside) null else LocationErrorType.OUT_OF_BOUNDS
            )
        }

        if (!hasLocationPermission(context)) {
            return LocationVerificationResult(
                isSuccess = false,
                geofenceStatus = GeofenceStatus.PERLU_VERIFIKASI,
                message = "Izin lokasi diperlukan untuk memverifikasi kehadiran Anda di area sekolah.",
                errorType = LocationErrorType.PERMISSION_DENIED
            )
        }

        if (!isGpsEnabled(context)) {
            return LocationVerificationResult(
                isSuccess = false,
                geofenceStatus = GeofenceStatus.PERLU_VERIFIKASI,
                message = "GPS / Layanan Lokasi sedang non-aktif. Mohon aktifkan GPS perangkat Anda.",
                errorType = LocationErrorType.GPS_DISABLED
            )
        }

        val location = getSingleLocation(context)
            ?: return LocationVerificationResult(
                isSuccess = false,
                geofenceStatus = GeofenceStatus.PERLU_VERIFIKASI,
                message = "Tidak dapat mengambil lokasi GPS saat ini. Pastikan Anda berada di area terbuka dan coba lagi.",
                errorType = LocationErrorType.TIMEOUT
            )

        val isMock = isLocationMocked(location)
        val distance = calculateDistanceMeters(location.latitude, location.longitude, schoolLat, schoolLon)
        val accuracy = location.accuracy

        // Check mock location
        if (isMock) {
            return LocationVerificationResult(
                isSuccess = false,
                latitude = location.latitude,
                longitude = location.longitude,
                accuracyMeters = accuracy,
                distanceFromSchoolMeters = distance,
                isInsideRadius = distance <= schoolRadiusMeters,
                isMockLocation = true,
                geofenceStatus = GeofenceStatus.PERLU_VERIFIKASI,
                message = "Terdeteksi lokasi tiruan (Mock Location). Harap matikan aplikasi pemalsu GPS.",
                errorType = LocationErrorType.MOCK_LOCATION_DETECTED
            )
        }

        // Check accuracy threshold
        if (accuracy > maxAccuracyMeters) {
            return LocationVerificationResult(
                isSuccess = false,
                latitude = location.latitude,
                longitude = location.longitude,
                accuracyMeters = accuracy,
                distanceFromSchoolMeters = distance,
                isInsideRadius = distance <= schoolRadiusMeters,
                isMockLocation = false,
                geofenceStatus = GeofenceStatus.PERLU_VERIFIKASI,
                message = "Akurasi GPS belum cukup (${accuracy.toInt()} m > batas ${maxAccuracyMeters.toInt()} m). Coba aktifkan mode akurasi tinggi.",
                errorType = LocationErrorType.POOR_ACCURACY
            )
        }

        val isInside = distance <= schoolRadiusMeters

        return if (isInside) {
            LocationVerificationResult(
                isSuccess = true,
                latitude = location.latitude,
                longitude = location.longitude,
                accuracyMeters = accuracy,
                distanceFromSchoolMeters = distance,
                isInsideRadius = true,
                isMockLocation = false,
                geofenceStatus = GeofenceStatus.DI_SEKOLAH,
                message = "✓ Anda berada di area sekolah (${distance.toInt()} m, Akurasi: ${accuracy.toInt()} m)"
            )
        } else {
            LocationVerificationResult(
                isSuccess = false,
                latitude = location.latitude,
                longitude = location.longitude,
                accuracyMeters = accuracy,
                distanceFromSchoolMeters = distance,
                isInsideRadius = false,
                isMockLocation = false,
                geofenceStatus = GeofenceStatus.DI_LUAR_AREA,
                message = "Di luar area sekolah: Berjarak ${distance.toInt()} m dari sekolah (Maksimal ${schoolRadiusMeters.toInt()} m). Anda harus berada di area sekolah untuk melakukan absensi.",
                errorType = LocationErrorType.OUT_OF_BOUNDS
            )
        }
    }
}
