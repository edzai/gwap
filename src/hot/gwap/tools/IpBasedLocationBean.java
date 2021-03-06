/*
 * This file is part of gwap, an open platform for games with a purpose
 *
 * Copyright (C) 2013
 * Project play4science
 * Lehr- und Forschungseinheit für Programmier- und Modellierungssprachen
 * Ludwig-Maximilians-Universität München
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package gwap.tools;

import gwap.game.AbstractGameSessionBean;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;

/**
 * Provides methods for looking up the country, region, and city
 * from an IP address.
 * 
 * @author Fabian Kneißl
 */
@Name("ipBasedLocationBean")
@Scope(ScopeType.APPLICATION)@AutoCreate
public class IpBasedLocationBean implements Serializable {

	@Logger
	private Log log;
	
	private LookupService lookupService;
	
	@Create
	public void startService() {
		URL url = AbstractGameSessionBean.class.getResource("/GeoLiteCity.dat");
		try {
			lookupService = new LookupService(url.getFile(), LookupService.GEOIP_MEMORY_CACHE);
			log.info("Started Lookup Service");
		} catch (IOException e) {
			log.warn("Could not start LookupService", e);
		}
		
	}
	
	@Destroy
	public void stopService() {
		if (lookupService != null)
			lookupService.close();
		log.info("Stopped Lookup Service");
	}
	
	/**
	 * Lookup the location (country, region, city) from an IP address.
	 * 
	 * @param ipAddress
	 * @return Location
	 */
	public Location findByIpAddress(String ipAddress) {
		if (lookupService == null)
			return null;
		else {
			Location location = lookupService.getLocation(ipAddress);
			return location;
		}
	}
}
