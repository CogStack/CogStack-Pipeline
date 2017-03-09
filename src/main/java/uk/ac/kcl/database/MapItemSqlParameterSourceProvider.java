package uk.ac.kcl.database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.item.database.ItemSqlParameterSourceProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

/**
 * A convenient implementation for providing MapSqlParameterSource when the item has map keys
 * that correspond to names used for parameters in the SQL statement.
 *
 */
public class MapItemSqlParameterSourceProvider<T> implements ItemSqlParameterSourceProvider<T> {

  // TODO: change this to environment variables?
  private String mapPropertyName = "associativeArray";
  private boolean shouldFlattenMap = true;

  /**
   * Provide parameter values in an {@link MapSqlParameterSource} based on values from
   * the provided item.
   * Supports accessing nested maps or arrays.
   *  e.g. :map1.level1.level2
   *    or :map1.array1[10].level2
   * However it assume keys for the maps are all String.
   * @param item the item to use for parameter values
   */
  @Override
  public SqlParameterSource createSqlParameterSource(T item) {
    BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(item);
    if (beanWrapper.isReadableProperty(mapPropertyName)) {
        Map<String, Object> map = (Map<String, Object>) beanWrapper.getPropertyValue(mapPropertyName);
        if (shouldFlattenMap) {
          Map<String, Object> flattenMap = new HashMap<String, Object>();
          flatten(map, flattenMap, "");
          return new MapSqlParameterSource(flattenMap);
        } else {
          return new MapSqlParameterSource(map);
        }
    }
    return new MapSqlParameterSource();
  }

  private void flatten(Map<String, Object> inMap, Map<String, Object> outMap, String prefix) {
    for (Map.Entry<String, Object> entry : inMap.entrySet()) {

      if (entry.getValue() instanceof Map<?, ?>) {
        // Deal with map value
        // TODO there is a big assumption here that the key is String...
        flatten((Map<String, Object>) entry.getValue(), outMap, prefix + entry.getKey() + ".");

      } else if (entry.getValue() instanceof List<?>) {
        // Deal with arrays value
        int i = 0;
        for (Object o: (List<Object>) entry.getValue()) {
          if (o instanceof Map<?, ?>) {
            // TODO there is a big assumption here that the key is String...
            flatten((Map<String, Object>) o, outMap, prefix + entry.getKey() + "[" + Integer.toString(i) + "].");
          } else {
            outMap.put(prefix + entry.getKey() + "[" + Integer.toString(i) + "]", entry.getValue());
          }
          i++;
        }

      } else {
        // Simple value
        outMap.put(prefix + entry.getKey(), entry.getValue());
      } // end else

    } // end for

  } // end flatten()

}
