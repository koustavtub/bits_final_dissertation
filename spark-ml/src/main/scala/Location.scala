case class Location(lat: Double, lon: Double)

trait DistanceCalcular {
  def calculateDistanceInKilometer(userLocation: Location, warehouseLocation: Location): Double

 // def calculateDistanceInMeter(userLocation: Location, warehouseLocation: Location): Int
}

class DistanceCalculatorImpl extends DistanceCalcular {

  private val AVERAGE_RADIUS_OF_EARTH_KM = 6371.0

  override def calculateDistanceInKilometer(userLocation: Location, warehouseLocation: Location): Double = {
    val latDistance = Math.toRadians(userLocation.lat - warehouseLocation.lat)
    val lngDistance = Math.toRadians(userLocation.lon - warehouseLocation.lon)
    val sinLat = Math.sin(latDistance / 2.0)
    val sinLng = Math.sin(lngDistance / 2.0)
    val a = sinLat * sinLat +
      (Math.cos(Math.toRadians(userLocation.lat)) *
        Math.cos(Math.toRadians(warehouseLocation.lat)) *
        sinLng * sinLng)
    val c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a))
    (AVERAGE_RADIUS_OF_EARTH_KM * c)
  }


}
