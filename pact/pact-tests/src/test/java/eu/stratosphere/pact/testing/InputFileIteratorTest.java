package eu.stratosphere.pact.testing;

import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;

import eu.stratosphere.pact.common.io.SequentialInputFormat;
import eu.stratosphere.pact.common.io.SequentialOutputFormat;
import eu.stratosphere.pact.common.type.Key;
import eu.stratosphere.pact.common.type.KeyValuePair;
import eu.stratosphere.pact.common.type.Value;
import eu.stratosphere.pact.common.type.base.PactInteger;
import eu.stratosphere.pact.common.type.base.PactString;
import eu.stratosphere.pact.common.util.FormatUtil;
import eu.stratosphere.pact.testing.AssertUtil;
import eu.stratosphere.pact.testing.InputFileIterator;
import eu.stratosphere.pact.testing.TestPlan;

/**
 * Tests {@link InputFileIterator}.
 * 
 * @author Arvid Heise
 */
public class InputFileIteratorTest {
	/**
	 * Tests if a file iterator of an empty file returns any pairs at all.
	 * 
	 * @throws IOException
	 *         if an I/O exception occurred
	 */
	@Test
	public void emptyIteratorShouldReturnNoElements() throws IOException {
		InputFileIterator<Key, Value> inputFileIterator = createFileIterator(true);

		AssertUtil.assertIteratorEquals("input file iterator is not empty", Arrays.asList().iterator(),
			inputFileIterator);
	}

	/**
	 * Tests if a file iterator of an empty file returns any pairs at all.
	 * 
	 * @throws IOException
	 *         if an I/O exception occurred
	 */
	@Test
	public void filledIteratorShouldReturnExactlyTheGivenArguments() throws IOException {
		KeyValuePair<?, ?>[] pairs = { new KeyValuePair<Key, Value>(new PactInteger(1), new PactString("test1")),
			new KeyValuePair<Key, Value>(new PactInteger(2), new PactString("test2")) };
		InputFileIterator<Key, Value> inputFileIterator = createFileIterator(true, pairs);

		AssertUtil.assertIteratorEquals("input file iterator is does not return the right sequence of pairs", Arrays
			.asList(pairs).iterator(), inputFileIterator);
	}

	/**
	 * Tests if a file iterator of a non-existent file fails.
	 * 
	 * @throws IOException
	 *         if an I/O exception occurred
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void emptyIteratorIfInputFileDoesNotExists() throws IOException {
		String testPlanFile = TestPlan.getTestPlanFile("fileIteratorTest");
		SequentialInputFormat<Key, Value> inputFormat = FormatUtil.createInputFormat(
			SequentialInputFormat.class, testPlanFile, null);
		InputFileIterator<Key, Value> inputFileIterator = new InputFileIterator<Key, Value>(true, inputFormat);

		AssertUtil.assertIteratorEquals("input file iterator is not empty", Arrays.asList().iterator(),
			inputFileIterator);
	}

	/**
	 * Tests if a file iterator of a non-existent file fails.
	 * 
	 * @throws IOException
	 *         if an I/O exception occurred
	 */
	@Test
	public void failIfReadTwoManyItems() throws IOException {
		KeyValuePair<?, ?>[] pairs = { new KeyValuePair<Key, Value>(new PactInteger(1), new PactString("test1")),
			new KeyValuePair<Key, Value>(new PactInteger(2), new PactString("test2")) };
		InputFileIterator<Key, Value> inputFileIterator = createFileIterator(true, pairs);

		while (inputFileIterator.hasNext())
			Assert.assertNotNull(inputFileIterator.next());

		try {
			inputFileIterator.next();
			Assert.fail("should have thrown Exception");
		} catch (NoSuchElementException e) {
		}
	}

	/**
	 * Tests if a file iterator of an empty file returns any pairs at all.
	 * 
	 * @throws IOException
	 *         if an I/O exception occurred
	 */
	@Test
	public void iteratorShouldCreateNewPairsIfSpecified() throws IOException {
		KeyValuePair<?, ?>[] pairs = { new KeyValuePair<Key, Value>(new PactInteger(1), new PactString("test1")),
			new KeyValuePair<Key, Value>(new PactInteger(2), new PactString("test2")),
			new KeyValuePair<Key, Value>(new PactInteger(3), new PactString("test3")) };
		InputFileIterator<Key, Value> inputFileIterator = createFileIterator(false, pairs);

		AssertUtil.assertIteratorEquals("input file iterator is does not return the right sequence of pairs", Arrays
			.asList(pairs).iterator(), inputFileIterator);

		inputFileIterator = createFileIterator(false, pairs);
		KeyValuePair<Key, Value> lastPair = null;
		for (int index = 0; inputFileIterator.hasNext(); index++) {
			KeyValuePair<Key, Value> currentPair = inputFileIterator.next();
			Assert.assertNotSame("should have created different instances", currentPair, lastPair);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private InputFileIterator<Key, Value> createFileIterator(boolean reusePairs, KeyValuePair<?, ?>... pairs)
			throws IOException {
		String testPlanFile = TestPlan.getTestPlanFile("fileIteratorTest");
		SequentialOutputFormat output = FormatUtil.createOutputFormat(SequentialOutputFormat.class,
			testPlanFile, null);
		for (KeyValuePair keyValuePair : pairs)
			output.writePair(keyValuePair);
		output.close();
		SequentialInputFormat<Key, Value> inputFormat = FormatUtil.createInputFormat(
			SequentialInputFormat.class, testPlanFile, null);
		InputFileIterator<Key, Value> inputFileIterator = new InputFileIterator<Key, Value>(reusePairs, inputFormat);
		return inputFileIterator;
	}
}
