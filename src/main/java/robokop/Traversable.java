package robokop;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import java.util.HashMap;

import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * Determine if a graph is traversable from a given set of 'labeled' nodes.
 */
public class Traversable
{
    private void recurse(
            Map<Long, List<Relationship>> edges,
            Map<Long, Boolean> reachable,
            Long node_id) {

        // If node has already been visited, quit recursing.
        if (reachable.get(node_id)) {
            return;
        }

        // Mark node as reachable.
        reachable.put(node_id, true);

        // Get edges starting at node.
        List<Relationship> edges_from_node = edges.get(node_id);

        // Mark downstream neighbors as reachable.
        for (Relationship e : edges_from_node) {
            recurse(edges, reachable, e.getEndNode().getId());
        }

        return;
    }
            
    @UserFunction
    @Description("robokop.traversable([n0,n1,...], [r0,r1,...], [m0,m1,...]) - is the graph (n,r) traversable given labeled nodes m.")
    public Boolean traversable(
            @Name("nodes") List<Node> nodes,
            @Name("edges") List<Relationship> edges,
            @Name("labeled") List<Node> labeled) {

        // Initialize node->edges map and node->reachable map.
        Map<Long, List<Relationship>> edges_by_nodes = new HashMap();
        Map<Long, Boolean> reachable = new HashMap();
        for (Node n : nodes) {
            edges_by_nodes.put((Long)n.getId(), new ArrayList());
            reachable.put((Long)n.getId(), false);
        }

        // Populate node->edges map, sorting edges by start node.
        for (Relationship e : edges) {
            edges_by_nodes.get(e.getStartNodeId()).add(e);
        }

        // Run recursion to populate reachable
        for (Node n : labeled) {
            recurse(edges_by_nodes, reachable, n.getId());
        }

        return reachable.values().stream().reduce(true, (a,b)->a&&b);
    }
}