package de.binaerebauten.gleichklang.core.utils;

import de.binaerebauten.gleichklang.core.model.locatable.Zip;

public class DistanceCalculator
{

	public static Double getDistance(Zip zip1, Zip zip2){
		double lat1 = zip1.getLatitude();
		double long1 = zip1.getLongitude();
		double lat2 = zip2.getLatitude();
		double long2 = zip2.getLongitude();
		return (3956 * 2 *
				Math.asin(
						Math.sqrt(
								Math.pow(Math.sin((lat1 - lat2) * Math.PI / 180 / 2), 2) +
								Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
								Math.pow(Math.sin((long1 - long2) * Math.PI / 180 / 2), 2)
						)
				)
		);
	}
}
