package rangequeries;

import java.util.List;

public interface RangeMap<K, V> {
  int size();
  boolean isEmpty();
  void add(K key, V value);
  V remove(K key);
  boolean contains(K key);
  V lookup(K key);
  List<V> lookupRange(K from, K to);
}
