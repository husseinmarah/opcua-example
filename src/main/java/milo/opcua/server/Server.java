/*******************************************************************************
 *
 * OPC UA example
 *
 *******************************************************************************/
package milo.opcua.server;

import org.eclipse.milo.opcua.sdk.client.DataTypeTreeBuilder;
import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.core.DataTypeTree;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.AddressSpace;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfigBuilder;
import org.eclipse.milo.opcua.sdk.server.identity.AnonymousIdentityValidator;
import org.eclipse.milo.opcua.sdk.server.identity.CompositeValidator;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.server.util.HostnameUtil;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.security.DefaultCertificateManager;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.structured.BuildInfo;
import org.eclipse.milo.opcua.stack.server.EndpointConfiguration;
import org.eclipse.milo.opcua.stack.server.security.ServerCertificateValidator;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singleton;
import static org.eclipse.milo.opcua.stack.core.Identifiers.ReadValueId;
import static org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText.english;

public class Server {
    private static final int PORT = 4840;
    private static final String SERVER_NAME = "Simple OPC UA Server";

    public static void main(final String[] args) throws Exception {

        final OpcUaServerConfigBuilder builder = new OpcUaServerConfigBuilder();

        builder.setIdentityValidator(new CompositeValidator(
                AnonymousIdentityValidator.INSTANCE // You should better ask who knocked, right?
        ));

        final EndpointConfiguration.Builder endpointBuilder = new EndpointConfiguration.Builder();

        endpointBuilder.addTokenPolicies(
                OpcUaServerConfig.USER_TOKEN_POLICY_ANONYMOUS // You wouldn't leave you door open, would you?
        );

        endpointBuilder.setSecurityPolicy(SecurityPolicy.None); // ... or give everyone access to your fridge ...
        endpointBuilder.setBindPort(PORT);

        builder.setEndpoints(singleton(endpointBuilder.build()));
        builder.setApplicationName(english(SERVER_NAME));
        builder.setApplicationUri("urn:" + HostnameUtil.getHostname() + ":" + PORT + "/" + SERVER_NAME);
        builder.setBuildInfo(new BuildInfo("", "", "", "", "", new DateTime()));
        builder.setCertificateManager(new DefaultCertificateManager()); // ... don't to this at home! ...

        builder.setCertificateValidator(new ServerCertificateValidator() {
            @Override
            public void validateCertificateChain(List<X509Certificate> list, String s) throws UaException {
            }

            @Override
            public void validateCertificateChain(List<X509Certificate> list) throws UaException {

            }
        });

        final OpcUaServer server = new OpcUaServer(builder.build());

        // register namespace
        server.getAddressSpaceManager().register(new CustomNamespace(server, CustomNamespace.URI));

        // start it up
        server.startup().get();

        System.out.println("server.getSubscriptions() = " + server.getSubscriptions());

        // don't wait for me
        Thread.sleep(Long.MAX_VALUE);


    }
}
