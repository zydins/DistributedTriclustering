package ru.hse.zudin.triclustering.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Formal context for clustering
 *
 * @author Sergey Zudin
 * @since 02.04.15.
 */
public class FormalContext {
    Set<Tuple> tuples;
    List<Set<Entity>> entities;
    ClusterConstructor constructor;

    public FormalContext(Set<Tuple> tuples, List<Set<Entity>> entities, ClusterConstructor constructor) {
        this.tuples = tuples;
        this.entities = entities;
        this.constructor = constructor;
    }

    /**
     * Base constructor
     */
    public FormalContext() {
        tuples = new HashSet<>();
        constructor = new TriclusterConstructor();
        entities = Utils.getFixedList(EntityType.size());
        for (int i = 0; i < EntityType.size(); i++) {
            entities.set(i, new HashSet<>());
        }
    }

    /**
     * Adds a tuple to formal context: into set of all tuples (@see Context#tuples), into cluster constructor
     * (@see ClusterConstructor) and its components into set of entities (@see Context#entities)
     * @param tuple tuple to add
     */
    public void add(Tuple tuple) {
        if (tuple.dimension() != EntityType.size())
            throw new IllegalArgumentException("Dimensions are different");
        tuples.add(tuple);
        for (int i = 0; i < EntityType.size(); i++) {
            entities.get(i).addAll(tuple.get(i));
            for (Entity elem : tuple.get(i)) {
                constructor.add(elem, tuple.getAllExcept(i).toArray(new Entity[0]));
            }
        }
    }

    /**
     * Returns result set of all existing clusters by using a cluster constructor (@see ClusterConstructor)
     * It iterates over tuples and build clusters based on their components.
     * @return set of all existing clusters
     */
    public Set<Tuple> getClusters() {
        Set<Tuple> result = new HashSet<>();
        for (Tuple tuple : tuples) {
            Tuple cluster = new Tuple();
            for (int i = 0; i < tuple.dimension(); i++) {
                //TODO: move to cluster constructor
                List<Entity> allExcept = tuple.getAllExcept(i);
                Set<Entity> set = constructor.get(allExcept.toArray(new Entity[allExcept.size()]));
                cluster.set(i, set);
            }
            result.add(cluster);
        }
        return result;
    }
}
