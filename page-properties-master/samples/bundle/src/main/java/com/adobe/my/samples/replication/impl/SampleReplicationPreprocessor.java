package com.adobe.my.samples.replication.impl;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.my.samples.util.SessionService;
import com.day.cq.replication.Preprocessor;
import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.ReplicationOptions;

@Component(immediate = true, service = Preprocessor.class)
@Designate(ocd = SampleReplicationPreprocessor.Configuration.class)
public class SampleReplicationPreprocessor implements Preprocessor {
	private static final Logger log = LoggerFactory
			.getLogger(SampleReplicationPreprocessor.class);

	@ObjectClassDefinition(name = "Cedars-Sinai GTM Config", description = "Cedars-Sinai GTM Config Service")
	public @interface Configuration {
		@AttributeDefinition(name = "Page Properties", description = "Page Properties to validate during activation", type = AttributeType.STRING)
		String[] getRestrictedPageProperties() default { "bwprop" };
	}

	private Configuration config;

	@Reference
	private SessionService sessionService;

	public String[] getRestrictedPageProperties() {
		return config.getRestrictedPageProperties();
	}

	@Reference
	private ResourceResolverFactory resourceResolverFactory;

	@Activate
	@Modified
	protected void activate(Configuration config) {
		this.config = config;
	}

	

	@Override
	public void preprocess(final ReplicationAction replicationAction,
			final ReplicationOptions replicationOptions)
			throws ReplicationException {

		if (replicationAction == null
				|| !ReplicationActionType.ACTIVATE.equals(replicationAction
						.getType())) {
			return;
		}

		final String path = replicationAction.getPath();
		if (!path.trim().startsWith("/content/")) {
			log.info("path retrieved for publish" + path);
			return;
		}
		log.info("path checking for page properties" + path);

		ResourceResolver resourceResolver = null;

		try {
			resourceResolver = sessionService.getReadServiceResourceResolver();
			final Resource resource = resourceResolver.getResource(path)
					.getChild("jcr:content");

			if (resource == null) {
				log.warn("Could not find jcr:content node for resource to apply checksum!");
				return;
			}

			Node pageNode = resource.adaptTo(Node.class);

			String primaryType = pageNode.getProperty("jcr:primaryType")
					.getString();

			if (!primaryType.equals("cq:PageContent")) {
				return;
			}
			for (String property : getRestrictedPageProperties()) {
				if (!pageNode.hasProperty(property)) {
					resourceResolver.close();
					throw new ReplicationException(
							"Page should not be published without authoring property"
									+ property);
				}
			}
			resourceResolver.commit();
		} catch (LoginException e) {
			throw new ReplicationException(e);
		} catch (RepositoryException e) {
			throw new ReplicationException(e);
		} catch (PersistenceException e) {
			throw new ReplicationException(e);
		} finally {
			if (resourceResolver != null && resourceResolver.isLive()) {
				resourceResolver.close();
			}
		}
	}

}
