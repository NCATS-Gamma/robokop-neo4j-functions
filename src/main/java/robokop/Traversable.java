package robokop;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import java.util.HashMap;

import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

/**
 * Determine if a graph is traversable from a given set of 'labeled' nodes.
 */
public class Traversable
{
    private void recurse(
            Map<String, List<List<String>>> edges,
            Map<String, Boolean> reachable,
            String node_id) {

        // If node has already been visited, quit recursing.
        if (reachable.get(node_id)) {
            return;
        }

        // Mark node as reachable.
        reachable.put(node_id, true);

        // Get edges starting at node.
        List<List<String>> edges_from_node = edges.get(node_id);

        // Mark downstream neighbors as reachable.
        for (List<String> e : edges_from_node) {
            recurse(edges, reachable, e.get(1));
        }

        return;
    }
            
    @UserFunction
    @Description("robokop.traversable([n0,n1,...], [r0,r1,...], [m0,m1,...]) - is the graph (n,r) traversable given labeled nodes m.")
    public Boolean traversable(
            @Name("nodes") List<String> nodes,
            @Name("edges") List<List<String>> edges,
            @Name("labeled") List<String> labeled) {

        // Initialize node->edges map and node->reachable map.
        Map<String, List<List<String>>> edges_by_nodes = new HashMap();
        Map<String, Boolean> reachable = new HashMap();
        for (String n : nodes) {
            edges_by_nodes.put(n, new ArrayList());
            reachable.put(n, false);
        }

        // Populate node->edges map, sorting edges by start node.
        for (List<String> e : edges) {
            edges_by_nodes.get(e.get(0)).add(e);
        }

        // Run recursion to populate reachable
        for (String n : labeled) {
            recurse(edges_by_nodes, reachable, n);
        }

        // AND the reachability for all nodes
        return reachable.values().stream().reduce(true, (a,b)->a&&b);
    }
}