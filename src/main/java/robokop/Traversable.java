package robokop;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
        
        List<Node>reachable = new ArrayList<Node>(labeled);
        List<Node>unreachable = new ArrayList<Node>(nodes);
        unreachable.removeAll(reachable);
        int i = 0;
        while (i<reachable.size()) {
            final long id = reachable.get(i).getId();
            List<Relationship> edges_from = edges
                .stream()
                .filter(p -> p.getStartNodeId() == id)
                .collect(Collectors.toList());
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