package com.syncleus.titangraph.example.titangods;

import com.syncleus.grail.graph.GrailModule;
import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.*;
import com.tinkerpop.frames.*;
import com.tinkerpop.frames.modules.*;
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerModule;
import org.junit.*;

public class FramesTest {
    @Test
    public void testFramesGetGod() {
        final TitanGraph godGraph = TitanGods.create("./target/TitanTestDB");
        final FramedGraphFactory factory = new FramedGraphFactory(new GremlinGroovyModule());

        final FramedGraph framedGraph = factory.create(godGraph);

        final Iterable<God> gods = (Iterable<God>) framedGraph.getVertices("name", "saturn", God.class);
        final God god = gods.iterator().next();
        Assert.assertEquals(god.getName(), "saturn");
        Assert.assertTrue(god.getAge().equals(10000));
    }

    @Test
    public void testFramesGetChildren() {
        final TitanGraph godGraph = TitanGods.create("./target/TitanTestDB");
        final FramedGraphFactory factory = new FramedGraphFactory(new GrailModule(), new GremlinGroovyModule());

        final FramedGraph framedGraph = factory.create(godGraph);

        final Iterable<God> gods = (Iterable<God>) framedGraph.getVertices("name", "jupiter", God.class);
        final God father = gods.iterator().next();
        Assert.assertEquals(father.getName(), "jupiter");

        final Iterable<? extends God> children = father.getSons(God.class);
        final God child = children.iterator().next();
        System.out.println("child: " + child.getName());
        Assert.assertEquals(child.getName(), "hercules");
    }

    @Test
    public void testFramesHandler() {
        final TitanGraph godGraph = TitanGods.create("./target/TitanTestDB");
        final FramedGraphFactory factory = new FramedGraphFactory(new GremlinGroovyModule(), new JavaHandlerModule());

        final FramedGraph framedGraph = factory.create(godGraph);

        final Iterable<God> gods = (Iterable<God>) framedGraph.getVertices("name", "saturn", God.class);
        final God god = gods.iterator().next();
        Assert.assertEquals(god.getName(), "saturn");
        Assert.assertTrue(god.isAgeEven());
    }

    @Test
    public void testFramesTypeResolver() {
        final TypeResolver resolver = new TypeResolver() {

            @Override
            public Class<?>[] resolveTypes(final Vertex v, final Class<?> defaultType) {
                if( v.getPropertyKeys().contains("other") ) {
                    return new Class<?>[]{LocationExtended.class};
                }
                return new Class<?>[0];
            }

            @Override
            public Class<?>[] resolveTypes(final Edge e, final Class<?> defaultType) {
                return new Class<?>[0];
            }
        };

        final Module resolverModule = new AbstractModule() {
            public void doConfigure(FramedGraphConfiguration config) {
                config.addTypeResolver(resolver);
            }
        };

        final FramedGraphFactory factory = new FramedGraphFactory(new JavaHandlerModule(), new GremlinGroovyModule(), resolverModule);

        final TitanGraph godGraph = TitanGods.create("./target/TitanTestDB");
        final FramedGraph framedGraph = factory.create(godGraph);

        final Iterable<Location> skys = (Iterable<Location>) framedGraph.getVertices("name", "sky", Location.class);
        final Location sky = skys.iterator().next();
        Assert.assertTrue(sky instanceof LocationExtended);
    }
}
