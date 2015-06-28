package br.com.geraldoferraz.testyourquery.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.com.geraldoferraz.testyourquery.config.EntityManagerProvider;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface DefaultConfiguration {
	
	public Class<? extends EntityManagerProvider> value();

}
