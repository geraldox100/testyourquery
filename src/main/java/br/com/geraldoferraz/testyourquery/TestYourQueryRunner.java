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
//	private Logger log = Logger.getLogger(TestYourQueryRunner.class);

	public TestYourQueryRunner(Class<?> klass) throws InitializationError {
		super(klass);
//		log.debug("Initializing");
		runner = new RunnerFactory(getTestClass().getJavaClass()).createRunner();
	}

	@Override
	protected void runChild(FrameworkMethod method, RunNotifier notifier) {
		Description description = describeChild(method);
		if (isIgnored(method)) {
//			log.debug("Test: "+method.getName()+" ignored");
            notifier.fireTestIgnored(description);
        } else {
        	runner.beforeRunTest(method);
//        	log.debug("Start Test");
        	runLeaf(methodBlock(method), description, notifier);
        	runner.afterRunTest();
        }
	}

	@Override
	protected Object createTest() throws Exception {
		return runner.createTestObject();
	}
	

}
