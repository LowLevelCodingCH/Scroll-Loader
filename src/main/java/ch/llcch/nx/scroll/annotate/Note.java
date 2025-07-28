package ch.llcch.nx.scroll.annotate;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Notes.class)
public @interface Note {
    String what();
}