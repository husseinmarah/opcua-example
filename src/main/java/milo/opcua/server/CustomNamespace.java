package milo.opcua.server;

import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.ManagedNamespace;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.model.nodes.objects.FolderTypeNode;
import org.eclipse.milo.opcua.sdk.server.nodes.*;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

import java.util.List;
import java.util.Optional;

import static milo.opcua.server.CallMe.createCallMeNode;

public class CustomNamespace extends ManagedNamespace {

    public static final String URI = "urn:my:custom:namespace";

    private final SubscriptionModel subscriptionModel;

    public CustomNamespace(final OpcUaServer server, final String uri) {
        super(server, uri);
        this.subscriptionModel = new SubscriptionModel(server, this);
        registerItems(getNodeContext());

    }

    private void registerItems(final UaNodeContext context) {
        System.out.println("Registering items");

        // create a folder

        final UaFolderNode folder = new UaFolderNode(
                context,
                newNodeId(1),
                newQualifiedName("QualifiedFolderName"),
                LocalizedText.english("This is the qualified name of the folder"));
        context.getNodeManager().addNode(folder);

        // add our folder to the objects folder

        final Optional<UaNode> objectsFolder = context.getServer()
                .getAddressSpaceManager()
                .getManagedNode(Identifiers.ObjectsFolder);

        objectsFolder.ifPresent(node -> {
            ((FolderTypeNode) node).addComponent(folder);
        });

        // add single variable

        {
            final UaVariableNode variable = new UaVariableNode(
                    context,
                    newNodeId("my-unique-identifier"),
                    newQualifiedName("OpcuaExample"),
                    LocalizedText.english("OPCUA Example")) {

                @Override
                public DataValue getValue() {
                    return new DataValue(new Variant(Math.sin(System.currentTimeMillis() / 1000)));
                }

            };

            variable.setDataType(Identifiers.Double);
            variable.setValue(new DataValue(new Variant(84)));

            folder.addOrganizes(variable);
            context.getNodeManager().addNode(variable);
        }

        // add method call

        {
            final UaMethodNode method = createCallMeNode(
                    context,
                    newNodeId("call-me-node"),
                    newQualifiedName("CallNode"));
            folder.addComponent(method);
            context.getNodeManager().addNode(method);
        }
    }


    protected void onStartup() {
        registerItems(getNodeContext());
    }

    @Override
    public void onDataItemsCreated(final List<DataItem> dataItems) {
        this.subscriptionModel.onDataItemsCreated(dataItems);
    }

    @Override
    public void onDataItemsModified(final List<DataItem> dataItems) {
        this.subscriptionModel.onDataItemsModified(dataItems);
    }

    @Override
    public void onDataItemsDeleted(final List<DataItem> dataItems) {
        this.subscriptionModel.onDataItemsDeleted(dataItems);
    }

    @Override
    public void onMonitoringModeChanged(final List<MonitoredItem> monitoredItems) {
        this.subscriptionModel.onMonitoringModeChanged(monitoredItems);
    }
}
