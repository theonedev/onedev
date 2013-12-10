package com.pmease.gitop.web.common.quantity;

import com.pmease.gitop.web.common.datatype.DataTypes;

/**
 * Provides a unit to allow conversions and unambiguous passing around of data
 * {@link Amount}s. The kilo/mega/giga/... hierarchy is built on base 2 so that
 * the hierarchy increases by a factor of 1024 instead of 1000 as typical in
 * metric units. Additionally, units are divided in 2 hierarchies one based on
 * bits and the other on bytes. Thus {@link #Kb} represents kilobits; so 1 Kb =
 * 1024 bits, and {@link #KB} represents kilobytes so 1 KB = 1024 bytes or 8192
 * bits.
 * 
 * @author John Sirois
 */
public enum Data implements Unit<Data> {
	BITS(1), 
	Kb(1024, BITS), 
	Mb(1024, Kb), 
	Gb(1024, Mb), 
	BYTES(8, BITS), 
	KB(1024, BYTES), 
	MB(1024, KB), 
	GB(1024, MB), 
	TB(1024, GB), 
	PB(1024, TB);

	private final double multiplier;

	private Data(double multiplier) {
		this.multiplier = multiplier;
	}

	private Data(double multiplier, Data base) {
		this(multiplier * base.multiplier);
	}

	@Override
	public double multiplier() {
		return multiplier;
	}

	public static String formatBytes(double bytes) {
		return formatBytes(bytes, BYTES);
	}

	public static String formatBytes(double bytes, Data smallestUnit) {
		return formatBytes(bytes, smallestUnit, "0.000");
	}
	
	public static String formatBytes(double bytes, Data smallestUnit, String pattern) {
		Amount<Double, Data> amount = Amount.of(bytes, Data.BYTES);
		double d = 0d;
		Data unit = smallestUnit;
		for (int i = Data.values().length - 1; i >= smallestUnit.ordinal(); i--) {
			d = amount.as(Data.values()[i]);
			if (d >= 1) {
				unit = Data.values()[i];
				break;
			}
		}

		String p = pattern;
		if (unit == Data.BYTES) {
			p = "0";
		}
		
		return DataTypes.DOUBLE.asString(amount.as(unit), p) + " " + unit.name().toLowerCase();
	}
}