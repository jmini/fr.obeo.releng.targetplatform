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
package fr.obeo.releng.targetplatform.internal.pde;

import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.inject.Inject;
import com.google.inject.Provider;

import fr.obeo.releng.TargetPlatformBundleActivator;
import fr.obeo.releng.targetplatform.TargetPlatform;
import fr.obeo.releng.targetplatform.pde.IConverter;
import fr.obeo.releng.targetplatform.resolved.ResolvedTargetPlatform;
import fr.obeo.releng.targetplatform.resolved.export.AbstractExporter;
import fr.obeo.releng.targetplatform.resolved.export.Exporter38;

public class Converter implements IConverter {

	@Inject
	private Provider<XtextResourceSet> resourceSetProvider;
	
	public void generateTargetDefinitionFile(org.eclipse.emf.common.util.URI targetPlatformLocation, IProgressMonitor monitor) throws Exception {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		if (!"targetplatform".equals(targetPlatformLocation.fileExtension())) {
			System.err.println("The target platform file must ends with .targetplatform extension");
			return;
		}
		
		TargetPlatform targetPlatform = loadTargetPlatform(targetPlatformLocation);
		if (targetPlatform == null) {
			return;
		}
		
		ResolvedTargetPlatform targetDefinition = convertToTargetDefinition(
				targetPlatform, null, subMonitor.newChild(80));

		org.eclipse.emf.common.util.URI targetDefinitionLocation = targetPlatformLocation.trimFileExtension().appendFileExtension("target");
		
		URI agentLocation = getAgentLocationURI(targetPlatformLocation);
		IProvisioningAgent agent = TargetPlatformBundleActivator.getInstance().getProvisioningAgentProvider().createAgent(agentLocation );
		IMetadataRepositoryManager metadataRepositoryManager = (IMetadataRepositoryManager) agent.getService(IMetadataRepositoryManager.SERVICE_NAME);

		AbstractExporter exporter = new Exporter38(metadataRepositoryManager, subMonitor.newChild(90));
		exporter.persistXML(targetDefinition, new FileOutputStream(targetDefinitionLocation.toFileString()));
		agent.stop();
		
		subMonitor.worked(20);
	}

	public ResolvedTargetPlatform convertToTargetDefinition(
			TargetPlatform targetPlatform,
			URI agentLocation, IProgressMonitor monitor)
			throws URISyntaxException, ProvisionException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		ResolvedTargetPlatform resolvedTargetPlatform = ResolvedTargetPlatform.resolve(targetPlatform, subMonitor.newChild(33));
		return resolvedTargetPlatform;
	}

	private URI getAgentLocationURI(org.eclipse.emf.common.util.URI fileLocation) throws URISyntaxException {
		String fileName = fileLocation.lastSegment();
		URI agentLocation = new URI(fileLocation.trimSegments(1).appendSegment("." + fileName).toString());
		return agentLocation;
	}

	private TargetPlatform loadTargetPlatform(org.eclipse.emf.common.util.URI fileLocation) {
		ResourceSet resourceSet = resourceSetProvider.get();
		Resource resource = resourceSet.getResource(fileLocation, true);
		EList<Diagnostic> errors = resource.getErrors();
		if (!errors.isEmpty()) {
			for (Diagnostic diagnostic : errors) {
				System.err.println(diagnostic);
			}
			return null;
		}
		
		TargetPlatform targetPlatform = (TargetPlatform) resource.getContents().get(0);
		return targetPlatform;
	}
}
