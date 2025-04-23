package sortpom.wrapper.content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import sortpom.parameter.DependencySortOrder;

/**
 * @author bjorn
 * @since 2012-09-20
 */
public class ChildElementSorter {
  static final ChildElementSorter EMPTY_SORTER = new ChildElementSorter();
  private static final String GROUP_ID_NAME = "GROUPID";
  private static final String EMPTY_PLUGIN_GROUP_ID_VALUE = "org.apache.maven.plugins";

  private final List<String> priorityGroupIds = new ArrayList<>();

  private final LinkedHashMap<String, String> childElementTextMappedBySortedNames =
      new LinkedHashMap<>();

  public ChildElementSorter(DependencySortOrder dependencySortOrder, List<Element> children) {
    this(dependencySortOrder, Collections.emptyList(), children);
  }

  public ChildElementSorter(
      DependencySortOrder dependencySortOrder,
      Collection<String> priorityGroupIds,
      List<Element> children) {
    this.priorityGroupIds.addAll(priorityGroupIds);
    var childElementNames = dependencySortOrder.getChildElementNames();

    childElementNames.forEach(
        name -> childElementTextMappedBySortedNames.put(name.toUpperCase(), ""));

    children.forEach(
        element ->
            childElementTextMappedBySortedNames.replace(
                element.getName().toUpperCase(), element.getTextTrim()));
  }

  private ChildElementSorter() {}

  boolean compareTo(ChildElementSorter otherChildElementSorter) {
    Function<Map.Entry<String, String>, String> getOtherTextFunc =
        entry -> otherChildElementSorter.childElementTextMappedBySortedNames.get(entry.getKey());

    int compare =
        childElementTextMappedBySortedNames.entrySet().stream()
            .map(
                entry ->
                    compareTexts(entry.getKey(), entry.getValue(), getOtherTextFunc.apply(entry)))
            .filter(i -> i != 0)
            .findFirst()
            .orElse(0);

    return compare < 0;
  }

  private int compareTexts(String key, String text, String otherText) {
    if ("scope".equalsIgnoreCase(key)) {
      return compareScope(text, otherText);
    }
    if ("groupId".equalsIgnoreCase(key)) {
      return compareGroupId(text, otherText);
    }
    return text.compareToIgnoreCase(otherText);
  }

  private int compareGroupId(String childElementText, String otherChildElementText) {
    String groupId = getResolvedGroupId(childElementText);
    String otherGroupId = getResolvedGroupId(otherChildElementText);
    return groupId.compareToIgnoreCase(otherGroupId);
  }

  private String getResolvedGroupId(String groupId) {
    for (int ii = 0; ii < priorityGroupIds.size(); ii++) {
      String pg = priorityGroupIds.get(ii);
      if (pg.equals(groupId) || StringUtils.startsWith(groupId, pg + ".")) {
        return String.format("!%02d:%s", ii, groupId);
      }
    }
    return groupId;
  }

  private int compareScope(String childElementText, String otherChildElementText) {
    return Scope.getScope(childElementText).compareTo(Scope.getScope(otherChildElementText));
  }

  void emptyGroupIdIsFilledWithDefaultMavenGroupId() {
    childElementTextMappedBySortedNames.computeIfPresent(
        GROUP_ID_NAME,
        (k, oldValue) -> oldValue.isEmpty() ? EMPTY_PLUGIN_GROUP_ID_VALUE : oldValue);
  }

  @Override
  public String toString() {
    return "ChildElementSorter{"
        + "childElementTexts="
        + childElementTextMappedBySortedNames.values()
        + '}';
  }

  private enum Scope {
    IMPORT,
    COMPILE,
    PROVIDED,
    SYSTEM,
    RUNTIME,
    TEST,
    OTHER;

    static Scope getScope(String scope) {
      if (scope == null || scope.isEmpty()) {
        return COMPILE;
      }
      var values = Scope.values();
      for (var value : values) {
        if (scope.equalsIgnoreCase(value.name())) {
          return value;
        }
      }
      return OTHER;
    }
  }
}
