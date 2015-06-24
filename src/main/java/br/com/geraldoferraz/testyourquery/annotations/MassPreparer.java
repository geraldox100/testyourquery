package br.com.geraldoferraz.testyourquery.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.com.geraldoferraz.testyourquery.util.ScriptRunner;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MassPreparer {

	public Class<? extends ScriptRunner> value();
	public String script() default "";
}
