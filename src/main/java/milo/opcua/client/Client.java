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
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.enumerated.*;
import org.eclipse.milo.opcua.stack.core.types.structured.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

public class Client {

    // private static final String ENDPOINT_URL = "opc.tcp://localhost:4840";
    private static final int PORT = 4840;
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

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
        QualifiedName qualifiedName = new QualifiedName(2, "WriteExample");

        NodeId nodeId = new NodeId(2, "1-unique-identifier");
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
            System.out.println("Item: " + item.getValue());
        }

//        ManagedSubscription subscription = ManagedSubscription.create(client);
////        subscription.addDataChangeListener((items, values) -> {
////            for (int i = 0; i < items.size(); i++) {
////                logger.info(
////                        "subscription value received: item={}, value={}",
////                        items.get(i).getNodeId(), values.get(i).getValue()
////                );
////            }
////        });
//
//        EventFilter eventFilter = new EventFilter(
//                new SimpleAttributeOperand[]{
//                        new SimpleAttributeOperand(
//                                Identifiers.BaseEventType,
//                                new QualifiedName[]{new QualifiedName(0, "EventId")},
//                                AttributeId.Value.uid(),
//                                null),
//                        new SimpleAttributeOperand(
//                                Identifiers.BaseEventType,
//                                new QualifiedName[]{new QualifiedName(0, "Time")},
//                                AttributeId.Value.uid(),
//                                null),
//                        new SimpleAttributeOperand(
//                                Identifiers.BaseEventType,
//                                new QualifiedName[]{new QualifiedName(0, "Message")},
//                                AttributeId.Value.uid(),
//                                null)
//                },
//                new ContentFilter(null)
//        );
//
//        ManagedEventItem eventItem = subscription.createEventItem(Identifiers.Server, eventFilter);
//        subscription.addChangeListener(new ManagedSubscription.ChangeListener() {
//            @Override
//            public void onEventReceived(List<ManagedEventItem> eventItems, List<Variant[]> eventFields) {
//
//                // Each item in the eventItems list has a corresponding set of
//                // event field values at the same index in the eventFields list.
//                // The number of fields and their meaning depend on the filter.
//                            for (int i = 0; i < eventItems.size(); i++) {
//                logger.info(
//                        "subscription value received: item={}, value={}",
//                        eventItems.get(i).getNodeId(), eventFields.get(i)
//                );
//            }
//            }
//        });
//
//        eventItem.addEventValueListener(new ManagedEventItem.EventValueListener() {
//            @Override
//            public void onEventValueReceived(ManagedEventItem item, Variant[] value) {
//
//                // A new event arrived, do something with it.
//            }
//        });
//
//
////        subscription.addChangeListener(new ManagedSubscription.ChangeListener() {
////            @Override
////            public void onDataReceived(List<ManagedDataItem> dataItems, List<DataValue> dataValues) {
////
////                // Each item in the dataItems list has a corresponding value at
////                // the same index in the dataValues list.
////                // Some items may appear multiple times if the item has a queue
////                // size greater than 1 and the value changed more than once within
////                // the publishing interval of the subscription.
////                // The items and values appear in the order of the changes.
////            }
////        });
//
//        ManagedDataItem dataItem = subscription.createDataItem(
//                new NodeId(2, "2-unique-identifier")
//        );
//
//        if (dataItem.getStatusCode().isGood()) {
//            logger.info("item created for nodeId={}", dataItem.getNodeId());
//
//            // let the example run for 5 seconds before completing
//            Thread.sleep(5000);
//            dataItem.delete();
//        } else {
//            logger.warn(
//                    "failed to create item for nodeId={} (status={})",
//                    dataItem.getNodeId(), dataItem.getStatusCode()
//            );
//        }



    }

}
