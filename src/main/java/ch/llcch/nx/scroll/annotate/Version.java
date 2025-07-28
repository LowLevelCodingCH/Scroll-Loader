package ch.llcch.nx.scroll.annotate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Version {
	String ver() default "1.0.0";
}
