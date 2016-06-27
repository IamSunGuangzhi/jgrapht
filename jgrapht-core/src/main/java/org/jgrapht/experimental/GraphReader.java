/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
/* -------------------
 * GraphReader.java
 * -------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 16-Sep-2003 : Initial revision (BN);
 *
 */
package org.jgrapht.experimental;

import java.io.*;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.generate.*;

/**
 * Imports a graph specified in DIMACS format (http://mat.gsia.cmu.edu/COLOR/general/ccformat.ps).
 * In summary, graphs specified in DIMACS format adhere to the following structure:
 * <pre><code>
 *
 * DIMACS G {
 *    c <comments; ignored during parsing of the graph
 *    p edge <number of nodes> <number of edges>
 *    e <edge source 1> <edge target 1>
 *    e <edge source 2> <edge target 2>
 *    e <edge source 3> <edge target 3>
 *    e <edge source 4> <edge target 4>
 *    ...
 * }
 *
 * </code></pre>
 *
 * Note: the current implementation does not fully implement the DIMACS specifications! Special (rarely used) fields
 * specified as 'Optional Descriptors' are currently not supported.
 *
 * @author unknown
 *
 * @param <V>
 * @param <E>
 */
public class GraphReader<V, E>
    implements GraphGenerator<V, E, V>
{
    // ~ Static fields/initializers --------------------------------------------

    // ~ Instance fields -------------------------------------------------------

    // ~ Static fields/initializers --------------------------------------------

    // ~ Instance fields -------------------------------------------------------

    private final BufferedReader _in;
    private final boolean _isWeighted;
    private final double _defaultWeight;

    // ~ Constructors ----------------------------------------------------------

    /**
     * Construct a new GraphReader.
     */
    private GraphReader(Reader input, boolean isWeighted, double defaultWeight)
        throws IOException
    {
        if (input instanceof BufferedReader) {
            _in = (BufferedReader) input;
        } else {
            _in = new BufferedReader(input);
        }
        _isWeighted = isWeighted;
        _defaultWeight = defaultWeight;
    }

    /**
     * Construct a new GraphReader.
     */
    public GraphReader(Reader input)
        throws IOException
    {
        this(input, false, 1);
    }

    /**
     * Construct a new GraphReader.
     */
    public GraphReader(Reader input, double defaultWeight)
        throws IOException
    {
        this(input, true, defaultWeight);
    }

    // ~ Methods ---------------------------------------------------------------

    private String [] split(final String src)
    {
        if (src == null) {
            return null;
        }
        return src.split("\\s+");
    }

    private String [] skipComments()
    {
        String [] cols = null;
        try {
            cols = split(_in.readLine());
            while (
                (cols != null)
                && ((cols.length == 0)
                    || cols[0].equals("c")
                    || cols[0].startsWith("%")))
            {
                cols = split(_in.readLine());
            }
        } catch (IOException e) {
        }
        return cols;
    }

    private int readNodeCount()
    {
        final String [] cols = skipComments();
        if (cols[0].equals("p")) {
            return Integer.parseInt(cols[1]);
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override public void generateGraph(
        Graph<V, E> target,
        VertexFactory<V> vertexFactory,
        Map<String, V> resultMap)
    {
        final int size = readNodeCount();
        if (resultMap == null) {
            resultMap = new HashMap<>();
        }

        for (int i = 0; i < size; i++) {
            V newVertex = vertexFactory.createVertex();
            target.addVertex(newVertex);
            resultMap.put(Integer.toString(i + 1), newVertex);
        }
        String [] cols = skipComments();
        while (cols != null) {
            if (cols[0].equals("e")) {
                E edge =
                    target.addEdge(
                        resultMap.get(cols[1]),
                        resultMap.get(cols[2]));
                if (_isWeighted && (edge != null)) {
                    double weight = _defaultWeight;
                    if (cols.length > 3) {
                        weight = Double.parseDouble(cols[3]);
                    }
                    ((WeightedGraph<V, E>) target).setEdgeWeight(edge, weight);
                }
            }
            cols = skipComments();
        }
    }
}

// End GraphReader.java
