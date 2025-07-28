package ch.llcch.nx.scroll.annotate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Todo {
	String what() default "This";
	String due() default "Now";
}
