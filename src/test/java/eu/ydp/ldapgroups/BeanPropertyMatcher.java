package eu.ydp.ldapgroups;

import static org.hamcrest.beans.PropertyUtil.NO_ARGUMENTS;
import static org.hamcrest.beans.PropertyUtil.propertyDescriptorsFor;
import static org.hamcrest.core.IsEqual.equalTo;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;

import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class BeanPropertyMatcher<T> extends TypeSafeDiagnosingMatcher<T> {
    private final T expectedBean;
    private final Map<String, PropertyMatcher> propertyMatchers;


    public BeanPropertyMatcher(T expectedBean, String... propertyNamesToMatch) {
        PropertyDescriptor[] descriptors = propertyDescriptorsFor(expectedBean, Object.class);
        this.expectedBean = expectedBean;
        Set<String> propertyNames = propertyNamesToMatch.length>0
                ? new LinkedHashSet<String>(Arrays.asList(propertyNamesToMatch))
                : propertyNamesFrom(descriptors);
        this.propertyMatchers = propertyMatchersFor(expectedBean, descriptors, propertyNames);
    }

    @Override
    public boolean matchesSafely(T item, Description mismatchDescription) {
        return isCompatibleType(item, mismatchDescription)
                && hasMatchingValues(item, mismatchDescription);
    }

    private boolean isCompatibleType(T item, Description mismatchDescription) {
        if (! expectedBean.getClass().isAssignableFrom(item.getClass())) {
            mismatchDescription.appendText("is incompatible type: " + item.getClass().getSimpleName());
            return false;
        }
        return true;
    }

    private boolean hasMatchingValues(T item, Description mismatchDescription) {
        for (Map.Entry<String, PropertyMatcher> matcherEntry : propertyMatchers.entrySet()) {
            PropertyMatcher propertyMatcher = matcherEntry.getValue();
            if (! propertyMatcher.matches(item)) {
                propertyMatcher.describeMismatch(item, mismatchDescription);
                return false;
            }
        }
        return true;
    }

    public void describeTo(Description description) {
        description.appendText("" + expectedBean.getClass().getSimpleName())
                .appendList("[", ", ", "]", propertyMatchers.values());
    }

    private static <T> Map<String, PropertyMatcher> propertyMatchersFor(T bean, PropertyDescriptor[] descriptors,
                                                                 Set<String> propertyNames) {

        Map<String, PropertyDescriptor> mappedDescriptors = new HashMap<String, PropertyDescriptor>(descriptors.length);
        for (PropertyDescriptor descriptor : descriptors) {
            mappedDescriptors.put(descriptor.getName(), descriptor);
        }

        Map<String, PropertyMatcher> result = new LinkedHashMap<String, PropertyMatcher>(descriptors.length);
        for (String propertyName : propertyNames) {
            PropertyDescriptor propertyDescriptor = mappedDescriptors.get(propertyName);
            result.put(propertyName, new PropertyMatcher(propertyDescriptor, bean));
        }
        return result;
    }

    private static Set<String> propertyNamesFrom(PropertyDescriptor[] descriptors) {
        HashSet<String> result = new HashSet<String>();
        for (PropertyDescriptor propertyDescriptor : descriptors) {
            result.add(propertyDescriptor.getDisplayName());
        }
        return result;
    }

    public static class PropertyMatcher extends DiagnosingMatcher<Object> {
        private final Method readMethod;
        private final Matcher<Object> matcher;
        private final String propertyName;

        public PropertyMatcher(PropertyDescriptor descriptor, Object expectedObject) {
            this.propertyName = descriptor.getDisplayName();
            this.readMethod = descriptor.getReadMethod();
            this.matcher = equalTo(readProperty(readMethod, expectedObject));
        }
        @Override
        public boolean matches(Object actual, Description mismatchDescription) {
            Object actualValue = readProperty(readMethod, actual);
            if (! matcher.matches(actualValue)) {
                mismatchDescription.appendText(propertyName + " ");
                matcher.describeMismatch(actualValue, mismatchDescription);
                return false;
            }
            return true;
        }

        public void describeTo(Description description) {
            description.appendText(propertyName + ": ").appendDescriptionOf(matcher);
        }
    }

    private static Object readProperty(Method method, Object target) {
        try {
            return method.invoke(target, NO_ARGUMENTS);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not invoke " + method + " on " + target, e);
        }
    }

    @Factory
    public static <T> Matcher<T> samePropertyValuesAs(T expectedBean) {
        return new BeanPropertyMatcher<T>(expectedBean);
    }

}