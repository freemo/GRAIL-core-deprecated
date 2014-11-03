package com.syncleus.grail.neural.backprop;

import com.syncleus.grail.neural.*;
import com.tinkerpop.frames.*;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerClass;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("BackpropSynapse")
@JavaHandlerClass(AbstractBackpropSynapse.class)
public interface BackpropSynapse extends Synapse {
    @InVertex
    BackpropNeuron getTarget();

    @OutVertex
    BackpropNeuron getSource();
}