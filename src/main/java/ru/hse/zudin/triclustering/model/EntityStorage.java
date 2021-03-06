package ru.hse.zudin.triclustering.model;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class for tricluster building and storing
 * Works only with tuples with 3 components, which, in this case, are called extent, intent and modus.
 *
 * @author Sergey Zudin
 * @since 12.04.15.
 */
public class EntityStorage {

    private static final Logger logger = Logger.getLogger(EntityStorage.class);

    private Map<MultiKey, ConcurrentHashSet<Entity>> map1;
    private Map<MultiKey, ConcurrentHashSet<Entity>> map2;
    private Map<MultiKey, ConcurrentHashSet<Entity>> map3;
    private Queue<EntityType> types;
    public AtomicInteger create = new AtomicInteger(0);
    public AtomicInteger add = new AtomicInteger(0);




    /**
     * Base constructor
     */
    public EntityStorage() {
        types = new ConcurrentLinkedQueue<>(EntityType.triclusteringEntities());
        map1 = new ConcurrentHashMap<>();
        map2 = new ConcurrentHashMap<>();
        map3 = new ConcurrentHashMap<>();
//        map1 = Collections.synchronizedMap(new HashMap<>());
//        map2 = Collections.synchronizedMap(new HashMap<>());
//        map3 = Collections.synchronizedMap(new HashMap<>());
    }

    /**
     * Add value with givan keys. If there 2 keys, just add given value into MultiKeyMap
     * (@see org.apache.commons.collections4.map.MultiKeyMap) with given keys. If there more than 2 keys,
     * it add given value for each pair of keys with different type (@see EntityType).
     *
     * Note that order of keys is important. Entities with the same type have to placed together, for example
     * "(EXTENT, EXTENT, MODUS, MODUS, MODUS)". Elements with the first type ("EXTENT" in example below)
     * in a row are always used as a first key.
     * @param value value to add
     * @param keys keys for map
     */
    public Set<Entity> add(Entity value, Entity[]... keys) {
        check(keys);
        Set<Entity> set = new ConcurrentHashSet<>();
        for (Entity first : keys[0]) {
            for (Entity second : keys[1]) {
                set.addAll(add(new MultiKey(first, second), value));
            }
        }
        return set;
    }

    private void check(Entity[][] keys) {
        if (keys == null || keys.length != 2
                || keys[0] == null || keys[0].length == 0
                || keys[1] == null || keys[1].length == 0)
            throw new IllegalArgumentException("Size is incorrect");
    }

    /**
     * Adds value for given pair of keys. Map to adding is selected by type of value
     * @param value value to add
     */
    public Set<Entity> add(MultiKey key, Entity value) {
        //boolean isNew = false;

        Map<MultiKey, ConcurrentHashSet<Entity>> map = getMap(value.getType());

        ConcurrentHashSet<Entity> set = map.get(key);
        if (set == null) {
            create.incrementAndGet();
            set = new ConcurrentHashSet<>();
            //isNew = true;
        } else {
            add.incrementAndGet();
        }
        set.add(value);
        map.putIfAbsent(key, set);
        return set;
    }

    private Map<MultiKey, ConcurrentHashSet<Entity>> getMap(EntityType type) {
        switch (EntityType.triclusteringEntities().indexOf(type)) {
            case 0:
                return map1;
            case 1:
                return map2;
            case 2:
                return map3;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Returns a set of entites for given keys.
     * @param keys
     * @return
     */
    public Set<Entity> get(Entity[]... keys) {
        check(keys);
        Set<Entity> set = new HashSet<>();
        for (Entity first : keys[0]) {
            for (Entity second : keys[1]) {
                set.addAll(get(new MultiKey(first, second)));
            }
        }
        return set;
    }

    private ConcurrentHashSet<Entity> get(MultiKey key) {
        List<EntityType> subtract = (List<EntityType>) CollectionUtils.subtract(types, key.types());
        return getMap(subtract.get(0)).get(key);
    }

    public static class MultiKey {
        private Entity key1;
        private Entity key2;

        public MultiKey(Entity key1, Entity key2) {
            this.key1 = key1;
            this.key2 = key2;
        }

        public List<EntityType> types() {
            return Arrays.asList(key1.getType(), key2.getType());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MultiKey multiKey = (MultiKey) o;

            return key1.equals(multiKey.key1) && key2.equals(multiKey.key2);
        }

        @Override
        public int hashCode() {
            return key1.hashCode() * 31 + key2.hashCode();
        }
    }
}
