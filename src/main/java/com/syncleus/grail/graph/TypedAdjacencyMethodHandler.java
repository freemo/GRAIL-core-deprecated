package com.syncleus.grail.graph;

import com.tinkerpop.blueprints.*;
import com.tinkerpop.frames.*;
import com.tinkerpop.frames.annotations.AdjacencyAnnotationHandler;
import com.tinkerpop.frames.modules.MethodHandler;
import com.tinkerpop.frames.modules.typedgraph.*;
import com.tinkerpop.gremlin.java.GremlinPipeline;

import java.lang.reflect.Method;

public class TypedAdjacencyMethodHandler implements MethodHandler<TypedAdjacency> {

    @Override
    public Class<TypedAdjacency> getAnnotationType() {
        return TypedAdjacency.class;
    }

    @Override
    public Object processElement(final Object frame, final Method method, final Object[] arguments, final TypedAdjacency annotation, final FramedGraph<?> framedGraph, final Element element) {

        if( ! (element instanceof Vertex) )
            throw new IllegalStateException("element is not a type of VertexFrame " + element.getClass().getName());
        final Vertex vertex = (Vertex) element;

        if(annotation.label() == null)
            throw new IllegalStateException("method " + method.getName() + " label must be specified on @TypedAdjacency annotation");

        if( ClassUtilities.isAddMethod(method) ) {
            if( arguments == null )
                throw new IllegalStateException("method " + method.getName() + " was annotated with @TypedAdjacency but had no arguments.");
            else if( arguments.length == 1 ) {
                if( !(arguments[0] instanceof Class) )
                    throw new IllegalStateException("method " + method.getName() + " was annotated with @TypedAdjacency, had a single argument, but that argument was not of the type Class");

                final Class type = (Class) arguments[0];

                TypedAdjacencyMethodHandler.checkReturnType(method, type);

                return TypedAdjacencyMethodHandler.addNode(type, annotation.direction(), annotation.label(), framedGraph, vertex);
            }
            else
                throw new IllegalStateException("method " + method.getName() + " was annotated with @TypedAdjacency but had more than 1 arguments.");
        }
        else if( ClassUtilities.isGetMethod(method) ) {
            if( arguments == null )
                throw new IllegalStateException("method " + method.getName() + " was annotated with @TypedAdjacency but had no arguments.");
            else if( arguments.length == 1 ) {
                if( !(arguments[0] instanceof Class) )
                    throw new IllegalStateException("method " + method.getName() + " was annotated with @TypedAdjacency, had a single argument, but that argument was not of the type Class");

                final Class type = (Class) arguments[0];

                if( method.getReturnType().isAssignableFrom(Iterable.class))
                    return TypedAdjacencyMethodHandler.getNodes(type, annotation.direction(), annotation.label(), framedGraph, vertex);

                TypedAdjacencyMethodHandler.checkReturnType(method, type);
                return TypedAdjacencyMethodHandler.getNode(type, annotation.direction(), annotation.label(), framedGraph, vertex);
            }
            else
                throw new IllegalStateException("method " + method.getName() + " was annotated with @TypedAdjacency but had more than 1 arguments.");
        }
        else
            throw new IllegalStateException("method " + method.getName() + " was annotated with @TypedAdjacency but did not begin with either of the following keywords: add, get");
    }

    private static Iterable getNodes(final Class type, final Direction direction, final String label, final FramedGraph<?> framedGraph, final Vertex vertex) {
        final TypeValue typeValue = TypedAdjacencyMethodHandler.determineTypeValue(type);
        final TypeField typeField = TypedAdjacencyMethodHandler.determineTypeField(type);
        switch(direction) {
        case BOTH:
            return framedGraph.frameVertices((Iterable<Vertex>) new GremlinPipeline<Vertex, Vertex>(vertex).both(label).has(typeField.value(), typeValue.value()), type);
        case IN:
            return framedGraph.frameVertices((Iterable<Vertex>) new GremlinPipeline<Vertex, Vertex>(vertex).in(label).has(typeField.value(), typeValue.value()), type);
        //Assume out direction
        default:
            return framedGraph.frameVertices((Iterable<Vertex>) new GremlinPipeline<Vertex, Vertex>(vertex).out(label).has(typeField.value(), typeValue.value()), type);
        }

    }

    private static Object getNode(final Class type, final Direction direction, final String label, final FramedGraph<?> framedGraph, final Vertex vertex) {
        final TypeValue typeValue = TypedAdjacencyMethodHandler.determineTypeValue(type);
        final TypeField typeField = TypedAdjacencyMethodHandler.determineTypeField(type);
        switch(direction) {
            case BOTH:
                return framedGraph.frame(new GremlinPipeline<Vertex, Vertex>(vertex).both(label).has(typeField.value(), typeValue.value()).V().next(), type);
            case IN:
                return framedGraph.frame(new GremlinPipeline<Vertex, Vertex>(vertex).in(label).has(typeField.value(), typeValue.value()).V().next(), type);
            //Assume out direction
            default:
                return framedGraph.frame(new GremlinPipeline<Vertex, Vertex>(vertex).out(label).has(typeField.value(), typeValue.value()).V().next(), type);
        }
    }

    private static Object addNode(final Class type, final Direction direction, final String label, final FramedGraph<?> framedGraph, final Vertex vertex) {
        TypedAdjacencyMethodHandler.determineTypeValue(type);
        TypedAdjacencyMethodHandler.determineTypeField(type);

        final Object newNode = framedGraph.addVertex(null, type);
        assert type.isInstance(newNode);

        switch(direction) {
            case BOTH:
            case IN:
            //Assume out direction
            default:
        }

        return null;
    }

    private static void checkReturnType(final Method method, final Class type) {
        if( ! method.getReturnType().isAssignableFrom(type) )
            throw new IllegalArgumentException("The type is not a subtype of the return type.");
    }

    private static TypeValue determineTypeValue(final Class<?> type) {
        final TypeValue typeValue = type.getDeclaredAnnotation(TypeValue.class);
        if( typeValue == null )
            throw new IllegalArgumentException("The specified type does not have a TypeValue annotation");
        return typeValue;
    }

    private static TypeField determineTypeField(final Class<?> type) {
        final TypeField typeField = type.getAnnotation(TypeField.class);
        if( typeField == null )
            throw new IllegalArgumentException("The specified type does not have a parent with a typeField annotation");
        return typeField;
    }
}