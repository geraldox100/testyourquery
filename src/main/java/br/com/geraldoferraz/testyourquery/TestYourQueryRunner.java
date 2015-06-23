package br.com.geraldoferraz.testyourquery;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import br.com.geraldoferraz.testyourquery.runner.Runner;
import br.com.geraldoferraz.testyourquery.runner.RunnerFactory;

public class TestYourQueryRunner extends BlockJUnit4ClassRunner {
	
	private Runner runner;

	public TestYourQueryRunner(Class<?> klass) throws InitializationError {
		super(klass);
		runner = new RunnerFactory(getTestClass().getJavaClass()).createRunner();
	}

	@Override
	protected void runChild(FrameworkMethod method, RunNotifier notifier) {
		Description description = describeChild(method);
		if (isIgnored(method)) {
            notifier.fireTestIgnored(description);
        } else {
        	runner.beforeRunTest();
        	runLeaf(methodBlock(method), description, notifier);
        	runner.afterRunTest();
        }
	}

	@Override
	protected Object createTest() throws Exception {
		Object testObject = getTestClass().getOnlyConstructor().newInstance();
		runner.testObjectCreated(testObject);
		return testObject;
	}
	

}
