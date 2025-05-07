package me.oskar.microhaskell.ast;

import me.oskar.microhaskell.ast.visitor.Visitable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;

public abstract class Node implements Visitable {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface NoProperty {
    }

    @Override
    public String toString() {
        var fields = new ArrayList<String>();

        for (var field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            if (!field.isAnnotationPresent(NoProperty.class)) {
                try {
                    fields.add(field.get(this).toString());
                } catch (IllegalAccessException ignored) {
                }
            }
        }

        return sExpression(this.getClass().getSimpleName(), fields.toArray(new String[0]));
    }

    protected String sExpression(String name, String... parts) {
        var joinedParts = String.join(" ", parts);
        return String.format("(%s%s)", name, (joinedParts.isEmpty() ? "" : " " + joinedParts));
    }
}
