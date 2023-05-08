/*******************************************************************************
 *
 * OPC UA example
 *
 *******************************************************************************/
package milo.opcua.client;

import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseResultMask;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.*;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

public class Client {

    //    private static final String ENDPOINT_URL = "opc.tcp://localhost:4840";
    private static final int PORT = 4840;

    public Client() throws UnknownHostException {
    }

    public static void main(String[] args) throws Exception {
        ////////// DISCOVER ENDPOINT /////////
        // Discover server's endpoints, and choose one
//        final String publicHostname = InetAddress.getLocalHost().getHostName();
        final String publicHostname = "localhost";
        final String ENDPOINT_URL = "opc.tcp://" + publicHostname + ":" + PORT; // ServerExample1

        List<EndpointDescription> endpoints = DiscoveryClient.getEndpoints(ENDPOINT_URL).get();

        OpcUaClientConfigBuilder clientConfigBuilder = new OpcUaClientConfigBuilder();
        clientConfigBuilder.setEndpoint(endpoints.get(0)); // please do better, and not only pick the first entry

        OpcUaClient client = OpcUaClient.create(clientConfigBuilder.build());
        client.connect().get();
        System.out.println("client.getNamespaceTable() = " + client.getNamespaceTable());
        System.out.println("client = " + endpoints.get(0));
        // Browse for forward hierarchical references from the Objects' folder
        // that lead to other Object and Variable nodes.
        BrowseDescription browse = new BrowseDescription(
                Identifiers.ObjectsFolder,
                BrowseDirection.Forward,
                Identifiers.References,
                true,
                uint(NodeClass.Object.getValue() | NodeClass.Variable.getValue()),
                uint(BrowseResultMask.All.getValue())
        );

        BrowseResult browseResult = client.browse(browse).get();
        System.out.println("Browse Result = " + browseResult);

        for (ReferenceDescription referenceDescription : browseResult.getReferences()) {
            System.out.println("Reference: " + referenceDescription.getNodeId());
        }

        AddressSpace addressSpace = client.getAddressSpace();
        UaNode serverNode = addressSpace.getNode(Identifiers.Server);
        List<? extends UaNode> nodes = addressSpace.browseNodes(serverNode);

        for (UaNode uaNode : nodes) {
            System.out.println("Node: " + uaNode.getNodeId());

        }
        QualifiedName qualifiedName = new QualifiedName(2, "MyObject");


        NodeId nodeId = new NodeId(2, "my-unique-identifier");
        List<ReadValueId> readValueIds = new ArrayList<>();
        readValueIds.add(
                new ReadValueId(
                        nodeId,
                        AttributeId.Value.uid(),
                        null, // indexRange
                        QualifiedName.NULL_VALUE
                )
        );

        System.out.println("readValueIds = " + readValueIds);

        ReadResponse readResponse = client.read(
                0.0, // maxAge
                TimestampsToReturn.Both,
                readValueIds
        ).get();


        for (DataValue item : readResponse.getResults()) {
            System.out.println(item.getValue());
        }

    }
}
