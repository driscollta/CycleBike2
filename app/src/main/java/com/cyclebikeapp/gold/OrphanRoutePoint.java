package com.cyclebikeapp.gold;

class OrphanRoutePoint {
	GPXRoutePoint thePoint;
	/**
	 * presumed beginning location in merged array to place an orphan route
	 * point
	 */
	int startIndex;
	/**
	 * presumed ending location in merged array to place an orphan route point
	 */
	int endIndex;

	OrphanRoutePoint(GPXRoutePoint point, int start, int end) {
		// TODO Auto-generated constructor stub
		this.thePoint = point;
		this.startIndex = start;
		this.endIndex = end;
	}

	public OrphanRoutePoint() {
		// TODO Auto-generated constructor stub
	}

}
