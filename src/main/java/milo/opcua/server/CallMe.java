package milo.opcua.server;

import org.eclipse.milo.opcua.sdk.server.annotations.UaInputArgument;
import org.eclipse.milo.opcua.sdk.server.annotations.UaMethod;
import org.eclipse.milo.opcua.sdk.server.annotations.UaOutputArgument;
import org.eclipse.milo.opcua.sdk.server.api.methods.AbstractMethodInvocationHandler;
import org.eclipse.milo.opcua.sdk.server.api.methods.Out;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext;
import org.eclipse.milo.opcua.sdk.server.annotations.UaInputArgument;
import org.eclipse.milo.opcua.sdk.server.annotations.UaMethod;
import org.eclipse.milo.opcua.sdk.server.annotations.UaOutputArgument;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.structured.Annotation;

public class CallMe {

    @UaMethod
    public void call(
            final AbstractMethodInvocationHandler.InvocationContext context,
            @UaInputArgument(name = "me") final String me,
            @UaOutputArgument(name = "result") final Out<String> result) {

        System.err.println("Someone called me: " + me);

        if ("Al".equals(me)) {
            result.set("You did it!");
        } else {
            result.set("Try again");
        }
    }

    public static UaMethodNode createCallMeNode(final UaNodeContext context, final NodeId nodeId,
            final QualifiedName qualifiedName) {
        final UaMethodNode method = new UaMethodNode(
                context,
                nodeId,
                qualifiedName,
                LocalizedText.english("Al"),
                LocalizedText.english("Call me Al"),
                UInteger.MIN, UInteger.MIN, true, true);

        try {

        } catch (final Exception e) {
            e.printStackTrace();
        }
        return method;
    }
}
