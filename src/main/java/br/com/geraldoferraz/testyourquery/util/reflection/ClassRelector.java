package br.com.geraldoferraz.testyourquery.util.reflection;

import static java.lang.reflect.Modifier.isStatic;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import net.vidageek.mirror.dsl.Mirror;
import net.vidageek.mirror.list.dsl.Matcher;
import net.vidageek.mirror.list.dsl.MirrorList;
import br.com.geraldoferraz.testyourquery.annotations.Configurator;

public class ClassRelector {

	private Mirror mirror = new Mirror();
	private Class<?> clazz;

	public ClassRelector(Class<?> clazz) {
		this.clazz = clazz;
	}

	public List<Field> getAnnotatedFields(Class<? extends Annotation> annotation) {
		return mirror.on(clazz).reflectAll().fields().matching((ClassRelector.<Field>annotedWith(annotation)));
	}

	

	public MirrorList<Method> getMethodsMatching(Matcher<Method> matcher) {
		return mirror.on(clazz).reflectAll().methods().matching(matcher);
	}

	public Object invokeStatic(Method method) {
		return mirror.on(clazz).invoke().method(method).withoutArgs();
	}

	public MirrorList<Method> getMethodsAnnotedWith(Class<Configurator> annotation) {
		return mirror.on(clazz).reflectAll().methods().matching(ClassRelector.<Method>annotedWith(annotation));
	}
	
	private static <T extends AccessibleObject> Matcher<T> annotedWith(final Class<? extends Annotation> annotation) {
		return new Matcher<T>() {
			public boolean accepts(T element) {
				return element.isAnnotationPresent(annotation);
			}
		};
	}

	public Method getConfiguratorMethod() {
		MirrorList<Method> methodsList = getMethodsAnnotedWith(Configurator.class);
		verificaQueNaoExisteMaisDeUmConfigurator(methodsList);
		Method method = methodsList.get(0);
		verificaQueConfiguratorEhStatic(method);
		return method;
	}
	
	private void verificaQueConfiguratorEhStatic(Method method) {
		if(!isStatic(method.getModifiers())){
			throw new RuntimeException("Configurator method must be static");
		}
	}

	private void verificaQueNaoExisteMaisDeUmConfigurator(MirrorList<Method> methdoList) {
		if (methdoList.size() > 1) {
			throw new RuntimeException("There can be only one configurator method");
		}
	}

}