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
    @UserFunction
    @Description("example.traversable([n0,n1,...], [r0,r1,...], [m0,m1,...]) - is the graph (n,r) traversable given labeled nodes m.")
    public Boolean traversable(
            @Name("nodes") List<Node> nodes,
            @Name("edges") List<Relationship> edges,
            @Name("labeled") List<Node> labeled) {
        
        // Copy input arrays. Neo4j gets angry if we try to modify these.
        List<Node>reachable = new ArrayList<Node>(labeled);
        List<Node>unreachable = new ArrayList<Node>(nodes);
        unreachable.removeAll(reachable);

        // Initialize node->edges map.
        Map<Long, List<Relationship>> map = new HashMap();
        for (Node n : nodes) {
            map.put((Long)n.getId(), new ArrayList());
        }

        // Populate node->edges map, sorting edges by start node.
        for (Relationship e : edges) {
            map.get(e.getStartNodeId()).add(e);
        }

        // Loop through reachable nodes, including the ones we don't know about yet.
        int i = 0;
        while (i<reachable.size()) {
            // Get edges starting at node.
            List<Relationship> edges_from = map.get(reachable.get(i).getId());

            // Move edge targets from unreachable[] to reachable[].
            for (Relationship r : edges_from) {
                if (unreachable.contains(r.getEndNode())) {
                    reachable.add(r.getEndNode());
                    unreachable.remove(r.getEndNode());
                }
            }
            i++;
        }

        return unreachable.size()==0;
    }
}