package me.oskar.microhaskell.ast;

import me.oskar.microhaskell.ast.visitor.Visitable;
import me.oskar.microhaskell.position.Span;

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

    private final Span span;

    protected Node(Span span) {
        this.span = span;
    }

    public Span getSpan() {
        return span;
    }

    @Override
    public String toString() {
        var fields = new ArrayList<String>();
        Class<?> clazz = this.getClass();

        while (clazz != null) {
            for (var field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                if (field.isAnnotationPresent(NoProperty.class)) continue;

                try {
                    fields.addFirst(field.get(this).toString());
                } catch (IllegalAccessException ignored) {
                }
            }

            clazz = clazz.getSuperclass();
        }

        return sExpression(this.getClass().getSimpleName(), fields.toArray(new String[0]));
    }

    protected String sExpression(String name, String... parts) {
        var joinedParts = String.join(" ", parts);
        return String.format("(%s%s)", name, (joinedParts.isEmpty() ? "" : " " + joinedParts));
    }
}
