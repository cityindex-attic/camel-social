package org.apache.camel.component.social;

import java.util.concurrent.TimeUnit;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.camel.component.social.util.SocialUtils;
import org.junit.Test;

public class TestCalculateDelayBasedOnRateLimit extends TestCase {

	@Test
	public void testCalculation() {
		final int rateLimit = 150;
		final int period = 1;
		final TimeUnit periodUnit = TimeUnit.HOURS;
		final TimeUnit resultUnit = TimeUnit.MILLISECONDS;
		final long expected = resultUnit.convert(period, periodUnit) / rateLimit; // 24000ms

		final long result = SocialUtils.calculateDelatBasedOnRate(rateLimit, periodUnit, period, resultUnit);

		Assert.assertEquals(expected, result);

		System.out.println("Delay between requests: " + result + " " + resultUnit.toString());
	}

}
