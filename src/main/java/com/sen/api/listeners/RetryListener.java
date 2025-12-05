package com.sen.api.listeners;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.testng.IAnnotationTransformer;
import org.testng.IRetryAnalyzer;
import org.testng.annotations.ITestAnnotation;

public class RetryListener implements IAnnotationTransformer {
	@SuppressWarnings("rawtypes")
	public void transform(ITestAnnotation annotation, Class testClass,
			Constructor testConstructor, Method testMethod) {
		// TestNG 7.x 中 getRetryAnalyzer 方法被移除，使用 getRetryAnalyzerClass 代替
		Class<? extends IRetryAnalyzer> retry = annotation.getRetryAnalyzerClass();
		if (retry == null) {
			annotation.setRetryAnalyzer(TestngRetry.class);
		}
	}
}

