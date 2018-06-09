/* AT4J -- Archive file tools for Java -- http://www.at4j.org
 * Copyright (C) 2009 Karl Gustafsson
 *
 * This file is a part of AT4J
 *
 * AT4J is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * AT4J is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.at4j.comp.lzma;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Test;

/**
 * @author Karl Gustafsson
 * @since 1.0
 */
public class LzmaEncoderInputStreamTest
{
	private static class TestReadSingleByteWriter implements Runnable
	{
		private LinkedBlockingQueue<byte[]> m_q;
		private CountDownLatch m_l1;
		private CountDownLatch m_l2;
		private Throwable m_exception = new RuntimeException("Not finished");

		public void run()
		{
			try
			{
				// Wait for the other thread to start
				Thread.sleep(300);

				m_q.put(new byte[] { (byte) (254 & 0xFF) });
				m_l1.countDown();
				m_l1.await();

				Thread.sleep(300);

				m_q.put(new byte[] { (byte) 12, (byte) 23 });
				m_l2.countDown();
				m_l2.await();

				// Write an EOF
				m_q.put(new byte[0]);
				m_exception = null;
			}
			catch (Throwable t)
			{
				m_l1.countDown();
				m_l2.countDown();
				m_exception = t;
			}
		}
	}

	private static class TestReadSingleByteReader implements Runnable
	{
		private InputStream m_is;
		private CountDownLatch m_l1;
		private CountDownLatch m_l2;
		private Throwable m_exception = new RuntimeException("Not finished");

		public void run()
		{
			try
			{
				assertEquals(254, m_is.read());
				m_l1.countDown();
				m_l1.await();

				assertEquals(12, m_is.read());
				assertEquals(23, m_is.read());
				m_l2.countDown();
				m_l2.await();

				assertEquals(-1, m_is.read());
				m_exception = null;
			}
			catch (Throwable t)
			{
				m_l1.countDown();
				m_l2.countDown();
				m_exception = t;
			}
		}
	}

	@Test
	public void testReadSingleByte() throws InterruptedException
	{
		LinkedBlockingQueue<byte[]> q = new LinkedBlockingQueue<byte[]>();
		TestReadSingleByteWriter wt = new TestReadSingleByteWriter();
		wt.m_q = q;
		wt.m_l1 = new CountDownLatch(2);
		wt.m_l2 = new CountDownLatch(2);
		TestReadSingleByteReader rt = new TestReadSingleByteReader();
		rt.m_is = new LzmaEncoderInputStream(q);
		rt.m_l1 = wt.m_l1;
		rt.m_l2 = wt.m_l2;

		Thread t1 = new Thread(rt);
		t1.start();

		Thread t2 = new Thread(wt);
		t2.start();

		t1.join();
		t2.join();
		if (rt.m_exception != null)
		{
			throw new RuntimeException(rt.m_exception);
		}
		if (wt.m_exception != null)
		{
			throw new RuntimeException(wt.m_exception);
		}
	}

	private static class TestReadByteArrayWriter implements Runnable
	{
		private LinkedBlockingQueue<byte[]> m_q;
		private CountDownLatch m_l1;
		private CountDownLatch m_l2;
		private Throwable m_exception = new RuntimeException("Not finished");

		public void run()
		{
			try
			{
				// Wait for the other thread to start
				Thread.sleep(300);

				m_q.put(new byte[] { (byte) (254 & 0xFF), (byte) 12, (byte) 23, (byte) 34 });
				m_l1.countDown();
				m_l1.await();

				Thread.sleep(300);

				m_q.put(new byte[] { (byte) 45, (byte) 56 });
				m_q.put(new byte[] { (byte) 67, (byte) 78, (byte) 89 });
				m_l2.countDown();
				m_l2.await();

				Thread.sleep(300);

				m_q.put(new byte[] { (byte) 45, (byte) 56 });
				m_q.put(new byte[] { (byte) 67, (byte) 78, (byte) 89 });

				Thread.sleep(300);

				// EOF
				m_q.put(new byte[0]);

				m_exception = null;
			}
			catch (Throwable t)
			{
				m_l1.countDown();
				m_l2.countDown();
				m_exception = t;
			}
		}
	}

	private static class TestReadByteArrayReader implements Runnable
	{
		private InputStream m_is;
		private CountDownLatch m_l1;
		private CountDownLatch m_l2;
		private Throwable m_exception = new RuntimeException("Not finished");

		public void run()
		{
			try
			{
				// Read less data than is available
				assertEquals(4, m_is.available());
				byte[] barr = new byte[2];
				assertEquals(2, m_is.read(barr));
				assertEquals((byte) (254 & 0xFF), barr[0]);
				assertEquals((byte) 12, barr[1]);

				// Read exactly as much data as is available
				assertEquals(2, m_is.available());
				barr = new byte[5];
				assertEquals(2, m_is.read(barr, 2, 2));
				assertEquals((byte) 23, barr[2]);
				assertEquals((byte) 34, barr[3]);

				m_l1.countDown();
				m_l1.await();

				// Read data that spans two incoming arrays
				assertEquals(2, m_is.available());
				assertEquals(4, m_is.read(barr, 1, 4));
				assertEquals((byte) 45, barr[1]);
				assertEquals((byte) 56, barr[2]);
				assertEquals((byte) 67, barr[3]);
				assertEquals((byte) 78, barr[4]);
				m_is.skip(1);
				m_l2.countDown();
				m_l2.await();

				barr = new byte[10];

				// Try to read data, but reach EOF some way through
				assertEquals(2, m_is.available());
				assertEquals(5, m_is.read(barr));
				assertEquals((byte) 45, barr[0]);
				assertEquals((byte) 56, barr[1]);
				assertEquals((byte) 67, barr[2]);
				assertEquals((byte) 78, barr[3]);
				assertEquals((byte) 89, barr[4]);

				// Try to read past EOF
				assertEquals(-1, m_is.read(barr));
				assertEquals(0, m_is.available());

				m_exception = null;
			}
			catch (Throwable t)
			{
				m_l1.countDown();
				m_l2.countDown();
				m_exception = t;
			}
		}
	}

	@Test
	public void testReadByteArray() throws InterruptedException
	{
		LinkedBlockingQueue<byte[]> q = new LinkedBlockingQueue<byte[]>();
		TestReadByteArrayWriter wt = new TestReadByteArrayWriter();
		wt.m_q = q;
		wt.m_l1 = new CountDownLatch(2);
		wt.m_l2 = new CountDownLatch(2);
		TestReadByteArrayReader rt = new TestReadByteArrayReader();
		rt.m_is = new LzmaEncoderInputStream(q);
		rt.m_l1 = wt.m_l1;
		rt.m_l2 = wt.m_l2;

		Thread t1 = new Thread(rt);
		t1.start();

		Thread t2 = new Thread(wt);
		t2.start();

		t1.join();
		t2.join();
		if (rt.m_exception != null)
		{
			throw new RuntimeException(rt.m_exception);
		}
		if (wt.m_exception != null)
		{
			throw new RuntimeException(wt.m_exception);
		}
	}

	private static class TestSkipBlocksWriter implements Runnable
	{
		private LinkedBlockingQueue<byte[]> m_q;
		private CountDownLatch m_l1;
		private Throwable m_exception = new RuntimeException("Not finished");

		public void run()
		{
			try
			{
				m_l1.countDown();
				m_l1.await();

				m_q.put(new byte[] { (byte) 12, (byte) 23 });
				Thread.sleep(300);
				m_q.put(new byte[] { (byte) 34, (byte) 45, (byte) 56 });
				m_q.put(new byte[0]);

				m_exception = null;
			}
			catch (Throwable t)
			{
				m_l1.countDown();
				m_exception = t;
			}
		}
	}

	private static class TestSkipBlocksReader implements Runnable
	{
		private InputStream m_is;
		private CountDownLatch m_l1;
		private Throwable m_exception = new RuntimeException("Not finished");

		public void run()
		{
			try
			{
				m_l1.countDown();
				m_l1.await();

				assertEquals(3, m_is.skip(3));
				assertEquals(45, m_is.read());
				assertEquals(1, m_is.skip(10));
				assertEquals(-1, m_is.read());
				assertEquals(0, m_is.skip(10));
				assertEquals(0, m_is.skip(-2));

				m_exception = null;
			}
			catch (Throwable t)
			{
				m_l1.countDown();
				m_exception = t;
			}
		}
	}

	@Test
	public void testSkipAcrossTwoIncomingDataBlocks() throws InterruptedException
	{
		LinkedBlockingQueue<byte[]> q = new LinkedBlockingQueue<byte[]>();
		TestSkipBlocksWriter wt = new TestSkipBlocksWriter();
		wt.m_q = q;
		wt.m_l1 = new CountDownLatch(2);
		TestSkipBlocksReader rt = new TestSkipBlocksReader();
		rt.m_is = new LzmaEncoderInputStream(q);
		rt.m_l1 = wt.m_l1;

		Thread t1 = new Thread(rt);
		t1.start();

		Thread t2 = new Thread(wt);
		t2.start();

		t1.join();
		t2.join();
		if (rt.m_exception != null)
		{
			throw new RuntimeException(rt.m_exception);
		}
		if (wt.m_exception != null)
		{
			throw new RuntimeException(wt.m_exception);
		}
	}
}
