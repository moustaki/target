package org.moustaki.target;

import com.google.android.maps.GeoPoint;

public class DistanceCalculator {

   private static double Radius = 6371000;

   public static double distance(GeoPoint StartP, GeoPoint EndP) {
      double lat1 = StartP.getLatitudeE6()/1E6;
      double lat2 = EndP.getLatitudeE6()/1E6;
      double lon1 = StartP.getLongitudeE6()/1E6;
      double lon2 = EndP.getLongitudeE6()/1E6;
      double dLat = Math.toRadians(lat2-lat1);
      double dLon = Math.toRadians(lon2-lon1);
      double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
      Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
      Math.sin(dLon/2) * Math.sin(dLon/2);
      double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
      return Radius * c;
   }
}
