package milo.opcua.server;

import org.eclipse.milo.opcua.sdk.core.AccessLevel;
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
import java.util.Timer;
import java.util.TimerTask;

import static milo.opcua.server.CallMe.createCallMeNode;

public class CustomNamespace extends ManagedNamespace {
    private static final int DELAY_MS = 2000;
    private static final int INTERVAL_MS = 5000;
    public static final String URI = "urn:my:custom:namespace";

    private final SubscriptionModel subscriptionModel;

    public CustomNamespace(final OpcUaServer server, final String uri) throws InterruptedException {
        super(server, uri);
        this.subscriptionModel = new SubscriptionModel(server, this);
        registerItems(getNodeContext());

    }

    private void registerItems(final UaNodeContext context) throws InterruptedException {
        System.out.println("Registering items");

        // create a folder
        final UaFolderNode folder = new UaFolderNode(
                context,
                newNodeId(1),
                newQualifiedName("QualifiedFolderName"),
                LocalizedText.english("This is the qualified name of the folder"));
        context.getNodeManager().addNode(folder);

        // add the folder to the objects folder
        final Optional<UaNode> objectsFolder = context.getServer()
                .getAddressSpaceManager()
                .getManagedNode(Identifiers.ObjectsFolder);

        objectsFolder.ifPresent(node -> {
            ((FolderTypeNode) node).addComponent(folder);
        });

        // add single variable
        {
            final UaVariableNode variableToBeWritten = new UaVariableNode(
                    context,
                    newNodeId("my-unique-identifier"),
                    newQualifiedName("OpcuaExample"),
                    LocalizedText.english("OPCUA Example")) {
//                @Override
//                public DataValue getValue() {
//                    return new DataValue(new Variant(Math.sin(System.currentTimeMillis() / 1000)));
//                }

            };
            // set the data type of the defined variable node
            variableToBeWritten.setDataType(Identifiers.Boolean);

            // define the variable that would be read and updated from the simulation "visual component"
            UaVariableNode variableToBeRead = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                    .setNodeId(newNodeId("my-unique-identifier2"))
                    .setAccessLevel(AccessLevel.READ_WRITE)
                    .setUserAccessLevel(AccessLevel.READ_WRITE)
                    .setBrowseName(newQualifiedName("OpcuaExample2"))
                    .setDisplayName(LocalizedText.english("OPCUA Example2"))
                    .setDataType(Identifiers.Boolean)
                    .build();

            // add all the variables to the main folder
            folder.addOrganizes(variableToBeWritten);
            context.getNodeManager().addNode(variableToBeWritten);
            folder.addOrganizes(variableToBeRead);
            context.getNodeManager().addNode(variableToBeRead);

            // initialize the variable to be true
            variableToBeWritten.setValue(new DataValue(new Variant(true)));


            // Create a timer
            Timer timer = new Timer();

            // Schedule a timer task to run at a fixed interval
            timer.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    // Run the application in "on" mode
                    runApp(true);
                    variableToBeWritten.setValue(new DataValue(new Variant(true)));
                    System.out.println("variableToBeRead.getValue() = " + variableToBeRead.getValue());
                    System.out.println("variableToBeWritten.getValue() = " + variableToBeWritten.getValue());

                    // Schedule a task to run after a delay
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            // Turn off the application
                            runApp(false);
                            variableToBeWritten.setValue(new DataValue(new Variant(false)));
                            System.out.println("variableToBeRead.getValue() = " + variableToBeRead.getValue());
                            System.out.println("variableToBeWritten.getValue() = " + variableToBeWritten.getValue());
                        }
                    }, DELAY_MS);
                }
            }, 0, INTERVAL_MS);



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


    protected void onStartup() throws InterruptedException {
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

    private static void runApp(boolean isOn) {
        if (isOn) {
            System.out.println("Application turned on");
        } else {
            System.out.println("Application turned off");
        }
    }
}
