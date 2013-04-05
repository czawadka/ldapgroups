package eu.ydp.ldapgroups;

import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.List;

public class IsToStringEqual<T> extends org.hamcrest.core.IsEqual<String> {
    public IsToStringEqual(T equalArg) {
        super(equalArg!=null ? equalArg.toString() : null);
    }

    @Override
    public boolean matches(Object arg) {
        return super.matches(arg!=null ? arg.toString() : null);
    }

    @Factory
    public static <T> Matcher<T> toStringEqualTo(T operand) {
        return (Matcher<T>)new IsToStringEqual<T>(operand);
    }

    public static  <T> List<Matcher<? extends T>> toStringEqualTo(T... operands) {
        List<Matcher<? extends T>> matchers = new ArrayList<Matcher<? extends T>>(operands.length);
        for(T o : operands) {
            matchers.add(IsToStringEqual.toStringEqualTo(o));
        }
        return matchers;
    }

}
