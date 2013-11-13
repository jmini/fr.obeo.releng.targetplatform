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

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import fr.obeo.releng.targetplatform.resolved.ResolvedLocation;
import fr.obeo.releng.targetplatform.resolved.ResolvedTargetPlatform;


public abstract class AbstractExporter {
	private static final String SEQUENCE_NUMBER_VALUE = "0";
	
	private static final String ATTR_ID = "id";
	private static final String ATTR_LOCATION = "location";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_SEQUENCE_NUMBER = "sequenceNumber";
	private static final String ATTR_VERSION = "version";
	private static final String INSTALLABLE_UNIT = "unit";
	private static final String LOCATION = "location";
	private static final String LOCATIONS = "locations";
	private static final String PDE_INSTRUCTION = "pde";
	private static final String REPOSITORY = "repository";
	private static final String ROOT = "target";

	private IMetadataRepositoryManager metadataRepositoryManager;
	private SubMonitor subMonitor;
	
	public AbstractExporter(IMetadataRepositoryManager metadataRepositoryManager, SubMonitor subMonitor) {
		this.metadataRepositoryManager = metadataRepositoryManager;
		this.subMonitor = subMonitor;
	}

	public void persistXML(ResolvedTargetPlatform definition, OutputStream output) throws IOException, ParserConfigurationException, TransformerException, ProvisionException, OperationCanceledException {
		DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();

		ProcessingInstruction instruction = doc.createProcessingInstruction(PDE_INSTRUCTION, ATTR_VERSION + "=\"" + getTargetVersion() + "\""); //$NON-NLS-2$
		doc.appendChild(instruction);

		Element rootElement = doc.createElement(ROOT);

		if (definition.getName() != null) {
			rootElement.setAttribute(ATTR_NAME, definition.getName());
		}
		
		rootElement.setAttribute(ATTR_SEQUENCE_NUMBER, SEQUENCE_NUMBER_VALUE);

		List<ResolvedLocation> locations = definition.getLocations();
		if (locations != null && locations.size() > 0) {
			Element locationsElement = doc.createElement(LOCATIONS);
			for (ResolvedLocation location : locations) {
				convertLocation(doc, locationsElement, location);
			}
			rootElement.appendChild(locationsElement);
		}

		doc.appendChild(rootElement);
		DOMSource source = new DOMSource(doc);

		StreamResult outputTarget = new StreamResult(output);
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.transform(source, outputTarget);
	}

	private void convertLocation(Document doc, Element locationsElement, ResolvedLocation location) throws ProvisionException, OperationCanceledException {
		Element locationElement = doc.createElement(LOCATION);
		handleLocationElementAttribute(locationElement, location);
		
		Element repositoryElement = doc.createElement(REPOSITORY);
		if(location.getId() != null && location.getId().length() > 0) {
			repositoryElement.setAttribute(ATTR_ID, location.getId());
		}
		repositoryElement.setAttribute(ATTR_LOCATION, location.getURI().toASCIIString());
		locationElement.appendChild(repositoryElement);

		List<IInstallableUnit> installableUnits = location.getIUToBeInstalled(metadataRepositoryManager, subMonitor);
		for (IInstallableUnit unit : installableUnits) {
			Element unitElement = doc.createElement(INSTALLABLE_UNIT);
			unitElement.setAttribute(ATTR_ID, unit.getId());
			unitElement.setAttribute(ATTR_VERSION, unit.getVersion().toString());
			locationElement.appendChild(unitElement);
		}
		
		locationsElement.appendChild(locationElement);
	}

	protected abstract void handleLocationElementAttribute(Element locationElement, ResolvedLocation location);

	protected abstract String getTargetVersion();
}
