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

package gwap.game.memory;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Scope(ScopeType.SESSION)
@Name("gwapGameMemoryForceBean")
public class ForceBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String enteredId;
	
	private long forcedId=0;

	public long getForcedId() {
		return forcedId;
	}
	
	public void updateForcedId()
	{
		long v;
		try
		{
			v=Long.parseLong(enteredId);					
		}
		catch (Exception e)
		{
			forcedId=0;
			return;
		}
		
		forcedId=v;
	
	}


	public String getEnteredId() {
		return enteredId;
	}

	public void setEnteredId(String enteredId) {
		this.enteredId = enteredId;
	}
}
