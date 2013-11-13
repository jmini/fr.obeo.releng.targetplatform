/*******************************************************************************
 * Copyright (c) 2013 Jeremie Bresson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremie Bresson - initial API and implementation
 *******************************************************************************/
package fr.obeo.releng.targetplatform.resolved.export;

import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.w3c.dom.Element;

import fr.obeo.releng.targetplatform.Option;
import fr.obeo.releng.targetplatform.resolved.ResolvedLocation;

public class Exporter38 extends AbstractExporter {
	private static final String ATTR_INCLUDE_ALL_PLATFORMS = "includeAllPlatforms";
	private static final String ATTR_INCLUDE_CONFIGURE_PHASE = "includeConfigurePhase";
	private static final String ATTR_INCLUDE_MODE = "includeMode";
	private static final String ATTR_INCLUDE_SOURCE = "includeSource";
	private static final String ATTR_LOCATION_TYPE = "type";
	private static final String INSTALLABLE_UNIT_TYPE = "InstallableUnit";
	private static final String MODE_PLANNER = "planner";
	private static final String MODE_SLICER = "slicer";

	public Exporter38(IMetadataRepositoryManager metadataRepositoryManager, SubMonitor subMonitor) {
		super(metadataRepositoryManager, subMonitor);
	}

	@Override
	protected String getTargetVersion() {
		return "3.8";
	}

	@Override
	protected void handleLocationElementAttribute(Element locationElement, ResolvedLocation location) {
		locationElement.setAttribute(ATTR_LOCATION_TYPE, INSTALLABLE_UNIT_TYPE);
		locationElement.setAttribute(ATTR_INCLUDE_MODE, isIncludeAllRequired(location) ? MODE_PLANNER : MODE_SLICER);
		locationElement.setAttribute(ATTR_INCLUDE_ALL_PLATFORMS, Boolean.toString(isIncludeAllEnvironments(location)));
		locationElement.setAttribute(ATTR_INCLUDE_SOURCE, Boolean.toString(isIncludeSource(location)));
		locationElement.setAttribute(ATTR_INCLUDE_CONFIGURE_PHASE, Boolean.toString(isIncludeConfigurePhase(location)));
	}

	private static boolean isIncludeAllRequired(ResolvedLocation location) {
		return location.getOptions().contains(Option.INCLUDE_REQUIRED);
	}

	private boolean isIncludeAllEnvironments(ResolvedLocation location) {
		return location.getOptions().contains(Option.INCLUDE_ALL_ENVIRONMENTS);
	}
	
	private boolean isIncludeSource(ResolvedLocation location) {
		return location.getOptions().contains(Option.INCLUDE_SOURCE);
	}
	
	private boolean isIncludeConfigurePhase(ResolvedLocation location) {
		return location.getOptions().contains(Option.INCLUDE_CONFIGURE_PHASE);
	}
}
