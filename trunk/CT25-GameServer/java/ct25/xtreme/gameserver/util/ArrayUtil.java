/* 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package ct25.xtreme.gameserver.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Random;

/**
 * @author Adonai
 */
public final class ArrayUtil {

	public static final byte[] arrayAdd(byte[] array, final byte value) {
		final int size = array.length;
		array = Arrays.copyOf(array, size + 1);
		array[size] = value;
		return array;
	}

	public static final char[] arrayAdd(char[] array, final char value) {
		final int size = array.length;
		array = Arrays.copyOf(array, size + 1);
		array[size] = value;
		return array;
	}

	public static final double[] arrayAdd(double[] array, final double value) {
		final int size = array.length;
		array = Arrays.copyOf(array, size + 1);
		array[size] = value;
		return array;
	}

	public static final float[] arrayAdd(float[] array, final float value) {
		final int size = array.length;
		array = Arrays.copyOf(array, size + 1);
		array[size] = value;
		return array;
	}

	public static final int[] arrayAdd(int[] array, final int value) {
		final int size = array.length;
		array = Arrays.copyOf(array, size + 1);
		array[size] = value;
		return array;
	}

	public static final long[] arrayAdd(long[] array, final long value) {
		final int size = array.length;
		array = Arrays.copyOf(array, size + 1);
		array[size] = value;
		return array;
	}

	public static final short[] arrayAdd(short[] array, final short value) {
		final int size = array.length;
		array = Arrays.copyOf(array, size + 1);
		array[size] = value;
		return array;
	}

	public static final <T> T[] arrayAdd(T[] array, final T value) {
		final int size = array.length;
		array = Arrays.copyOf(array, size + 1);
		array[size] = value;
		return array;
	}

	public static final byte[] arrayAddIfAbsent(byte[] array, final byte value) {
		if (!ArrayUtil.arrayContains(array, value))
			return ArrayUtil.arrayAdd(array, value);
		return array;
	}

	public static final char[] arrayAddIfAbsent(char[] array, final char value) {
		if (!ArrayUtil.arrayContains(array, value))
			return ArrayUtil.arrayAdd(array, value);
		return array;
	}

	public static final double[] arrayAddIfAbsent(double[] array, final double value) {
		if (!ArrayUtil.arrayContains(array, value))
			return ArrayUtil.arrayAdd(array, value);
		return array;
	}

	public static final float[] arrayAddIfAbsent(float[] array, final float value) {
		if (!ArrayUtil.arrayContains(array, value))
			return ArrayUtil.arrayAdd(array, value);
		return array;
	}

	public static final int[] arrayAddIfAbsent(int[] array, final int value) {
		if (!ArrayUtil.arrayContains(array, value))
			return ArrayUtil.arrayAdd(array, value);
		return array;
	}

	public static final long[] arrayAddIfAbsent(long[] array, final long value) {
		if (!ArrayUtil.arrayContains(array, value))
			return ArrayUtil.arrayAdd(array, value);
		return array;
	}

	public static final short[] arrayAddIfAbsent(short[] array, final short value) {
		if (!ArrayUtil.arrayContains(array, value))
			return ArrayUtil.arrayAdd(array, value);
		return array;
	}

	public static final void arraySwap(final int[] array, final int index1, final int index2) {
		final int swap = array[index1];
		array[index1] = array[index2];
		array[index2] = swap;
	}

	public static final <T> T[] arrayAddIfAbsent(T[] array, final T value) {
		if (!ArrayUtil.arrayContains(array, value))
			return ArrayUtil.arrayAdd(array, value);
		return array;
	}

	public static final boolean arrayContains(final Object array, final Object value) {
		return arrayLastIndexOf(array, value) != -1;
	}

	public static final <T> boolean arrayContains(final T[] array, final T value) {
		return arrayLastIndexOf(array, value) != -1;
	}

	public static final int arrayFirstIndexOf(final byte[] array, final byte value) {
		final int length = array.length;
		for (int i = 0; i < length; i++) {
			if (array[i] == value)
				return i;
		}
		return -1;
	}

	public static final int arrayFirstIndexOf(final char[] array, final char value) {
		final int length = array.length;
		for (int i = 0; i < length; i++) {
			if (array[i] == value)
				return i;
		}
		return -1;
	}

	public static final int arrayFirstIndexOf(final double[] array, final double value) {
		final int length = array.length;
		for (int i = 0; i < length; i++) {
			if (array[i] == value)
				return i;
		}
		return -1;
	}

	public static final int arrayFirstIndexOf(final float[] array, final float value) {
		final int length = array.length;
		for (int i = 0; i < length; i++) {
			if (array[i] == value)
				return i;
		}
		return -1;
	}

	public static final int arrayFirstIndexOf(final int[] array, final int value) {
		final int length = array.length;
		for (int i = 0; i < length; i++) {
			if (array[i] == value)
				return i;
		}
		return -1;
	}

	public static final int arrayFirstIndexOf(final long[] array, final long value) {
		final int length = array.length;
		for (int i = 0; i < length; i++) {
			if (array[i] == value)
				return i;
		}
		return -1;
	}

	public static final int arrayFirstIndexOf(final Object array, final Object value) {
		final int length = Array.getLength(array);
		if (value == null) {
			for (int i = 0; i < length; i++) {
				if (Array.get(array, i) == null)
					return i;
			}
		}
		else {
			for (int i = 0; i < length; i++) {
				final Object obj = Array.get(array, i);
				if (value == obj || value.equals(obj))
					return i;
			}
		}
		return -1;
	}

	public static final int arrayFirstIndexOf(final short[] array, final short value) {
		final int length = array.length;
		for (int i = 0; i < length; i++) {
			if (array[i] == value)
				return i;
		}
		return -1;
	}

	public static final <T> int arrayFirstIndexOf(final T[] array, final T value) {
		if (value == null) {
			for (int i = 0; i < array.length; i++) {
				if (array[i] == null)
					return i;
			}
		}
		else {
			for (int i = 0; i < array.length; i++) {
				if (value == array[i] || value.equals(array[i]))
					return i;
			}
		}
		return -1;
	}

	public static final int arrayIndexOf(final Object array, final Object value) {
		return arrayFirstIndexOf(array, value);
	}

	public static final <T> int arrayIndexOf(final T[] array, final T value) {
		return arrayFirstIndexOf(array, value);
	}

	public static final byte[] arrayInsert(byte[] array, final byte value, final int pos) {
		final int size = array.length;
		if (pos < 0 || pos >= size)
			throw new IndexOutOfBoundsException("Pos " + pos + " < 0 or >= size");

		final byte[] temp = new byte[size + 1];
		temp[pos] = value;

		if (pos != 0)
			System.arraycopy(array, 0, temp, 0, pos);
		System.arraycopy(array, pos, temp, pos + 1, size - pos);
		return temp;
	}

	public static final char[] arrayInsert(char[] array, final char value, final int pos) {
		final int size = array.length;
		if (pos < 0 || pos >= size)
			throw new IndexOutOfBoundsException("Pos " + pos + " < 0 or >= size");

		final char[] temp = new char[size + 1];
		temp[pos] = value;

		if (pos != 0)
			System.arraycopy(array, 0, temp, 0, pos);
		System.arraycopy(array, pos, temp, pos + 1, size - pos);
		return temp;
	}

	public static final double[] arrayInsert(double[] array, final double value, final int pos) {
		final int size = array.length;
		if (pos < 0 || pos >= size)
			throw new IndexOutOfBoundsException("Pos " + pos + " < 0 or >= size");

		final double[] temp = new double[size + 1];
		temp[pos] = value;

		if (pos != 0)
			System.arraycopy(array, 0, temp, 0, pos);
		System.arraycopy(array, pos, temp, pos + 1, size - pos);
		return temp;
	}

	public static final float[] arrayInsert(float[] array, final float value, final int pos) {
		final int size = array.length;
		if (pos < 0 || pos >= size)
			throw new IndexOutOfBoundsException("Pos " + pos + " < 0 or >= size");

		final float[] temp = new float[size + 1];
		temp[pos] = value;

		if (pos != 0)
			System.arraycopy(array, 0, temp, 0, pos);
		System.arraycopy(array, pos, temp, pos + 1, size - pos);
		return temp;
	}

	public static final int[] arrayInsert(int[] array, final int value, final int pos) {
		final int size = array.length;
		if (pos < 0 || pos >= size)
			throw new IndexOutOfBoundsException("Pos " + pos + " < 0 or >= size");

		final int[] temp = new int[size + 1];
		temp[pos] = value;

		if (pos != 0)
			System.arraycopy(array, 0, temp, 0, pos);
		System.arraycopy(array, pos, temp, pos + 1, size - pos);
		return temp;
	}

	public static final long[] arrayInsert(long[] array, final long value, final int pos) {
		final int size = array.length;
		if (pos < 0 || pos >= size)
			throw new IndexOutOfBoundsException("Pos " + pos + " < 0 or >= size");

		final long[] temp = new long[size + 1];
		temp[pos] = value;

		if (pos != 0)
			System.arraycopy(array, 0, temp, 0, pos);
		System.arraycopy(array, pos, temp, pos + 1, size - pos);
		return temp;
	}

	public static final short[] arrayInsert(short[] array, final short value, final int pos) {
		final int size = array.length;
		if (pos < 0 || pos >= size)
			throw new IndexOutOfBoundsException("Pos " + pos + " < 0 or >= size");

		final short[] temp = new short[size + 1];
		temp[pos] = value;

		if (pos != 0)
			System.arraycopy(array, 0, temp, 0, pos);
		System.arraycopy(array, pos, temp, pos + 1, size - pos);
		return temp;
	}

	public static final <T> T[] arrayInsert(T[] array, final T value, final int pos) {
		final int size = array.length;
		if (pos < 0 || pos >= size)
			throw new IndexOutOfBoundsException("Pos " + pos + " < 0 or >= size");

		@SuppressWarnings("unchecked")
		final T[] temp = (array.getClass() == Object[].class) ? (T[]) new Object[size + 1] : (T[]) Array.newInstance(array.getClass().getComponentType(), size + 1);
		temp[pos] = value;

		if (pos != 0)
			System.arraycopy(array, 0, temp, 0, pos);
		System.arraycopy(array, pos, temp, pos + 1, size - pos);
		return temp;
	}

	public static final int arrayLastIndexOf(final Object array, final Object value) {
		final int length = Array.getLength(array);
		if (value == null) {
			for (int i = length; i-- > 0;) {
				if (Array.get(array, i) == null)
					return i;
			}
		}
		else {
			for (int i = length; i-- > 0;) {
				final Object obj = Array.get(array, i);
				if (value == obj || value.equals(obj))
					return i;
			}
		}
		return -1;
	}

	public static final <T> int arrayLastIndexOf(final T[] array, final T value) {
		if (value == null) {
			for (int i = array.length; i-- > 0;) {
				if (array[i] == null)
					return i;
			}
		}
		else {
			for (int i = array.length; i-- > 0;) {
				if (value == array[i] || value.equals(array[i]))
					return i;
			}
		}
		return -1;
	}

	public static final <T> T[] arrayRemoveAll(T[] array, final T value) {
		int index;
		while ((index = arrayLastIndexOf(array, value)) != -1) {
			array = arrayRemoveAtUnsafe(array, index);
		}
		return array;
	}

	public static final byte[] arrayRemoveAtUnsafe(final byte[] array, final int index) {
		final byte[] newArray = new byte[array.length - 1];
		if (index != 0)
			System.arraycopy(array, 0, newArray, 0, index);
		System.arraycopy(array, index + 1, newArray, index, newArray.length - index);
		return newArray;
	}

	public static final char[] arrayRemoveAtUnsafe(final char[] array, final int index) {
		final char[] newArray = new char[array.length - 1];
		if (index != 0)
			System.arraycopy(array, 0, newArray, 0, index);
		System.arraycopy(array, index + 1, newArray, index, newArray.length - index);
		return newArray;
	}

	public static final double[] arrayRemoveAtUnsafe(final double[] array, final int index) {
		final double[] newArray = new double[array.length - 1];
		if (index != 0)
			System.arraycopy(array, 0, newArray, 0, index);
		System.arraycopy(array, index + 1, newArray, index, newArray.length - index);
		return newArray;
	}

	public static final float[] arrayRemoveAtUnsafe(final float[] array, final int index) {
		final float[] newArray = new float[array.length - 1];
		if (index != 0)
			System.arraycopy(array, 0, newArray, 0, index);
		System.arraycopy(array, index + 1, newArray, index, newArray.length - index);
		return newArray;
	}

	public static final int[] arrayRemoveAtUnsafe(final int[] array, final int index) {
		final int[] newArray = new int[array.length - 1];
		if (index != 0)
			System.arraycopy(array, 0, newArray, 0, index);
		System.arraycopy(array, index + 1, newArray, index, newArray.length - index);
		return newArray;
	}

	public static final long[] arrayRemoveAtUnsafe(final long[] array, final int index) {
		final long[] newArray = new long[array.length - 1];
		if (index != 0)
			System.arraycopy(array, 0, newArray, 0, index);
		System.arraycopy(array, index + 1, newArray, index, newArray.length - index);
		return newArray;
	}

	public static final short[] arrayRemoveAtUnsafe(final short[] array, final int index) {
		final short[] newArray = new short[array.length - 1];
		if (index != 0)
			System.arraycopy(array, 0, newArray, 0, index);
		System.arraycopy(array, index + 1, newArray, index, newArray.length - index);
		return newArray;
	}

	public static final <T> T[] arrayRemoveAtUnsafe(final T[] array, final int index) {
		@SuppressWarnings("unchecked")
		final T[] newArray = (array.getClass() == Object[].class) ? (T[]) new Object[array.length - 1] : (T[]) Array.newInstance(array.getClass().getComponentType(), array.length - 1);
		if (index != 0)
			System.arraycopy(array, 0, newArray, 0, index);
		System.arraycopy(array, index + 1, newArray, index, newArray.length - index);
		return newArray;
	}

	public static final byte[] arrayRemoveFirst(final byte[] array, final byte value) {
		final int index = arrayFirstIndexOf(array, value);
		if (index != -1)
			return arrayRemoveAtUnsafe(array, index);

		return array;
	}

	public static final char[] arrayRemoveFirst(final char[] array, final char value) {
		final int index = arrayFirstIndexOf(array, value);
		if (index != -1)
			return arrayRemoveAtUnsafe(array, index);

		return array;
	}

	public static final double[] arrayRemoveFirst(final double[] array, final double value) {
		final int index = arrayFirstIndexOf(array, value);
		if (index != -1)
			return arrayRemoveAtUnsafe(array, index);

		return array;
	}

	public static final float[] arrayRemoveFirst(final float[] array, final float value) {
		final int index = arrayFirstIndexOf(array, value);
		if (index != -1)
			return arrayRemoveAtUnsafe(array, index);

		return array;
	}

	public static final int[] arrayRemoveFirst(final int[] array, final int value) {
		final int index = arrayFirstIndexOf(array, value);
		if (index != -1)
			return arrayRemoveAtUnsafe(array, index);

		return array;
	}

	public static final long[] arrayRemoveFirst(final long[] array, final long value) {
		final int index = arrayFirstIndexOf(array, value);
		if (index != -1)
			return arrayRemoveAtUnsafe(array, index);

		return array;
	}

	public static final short[] arrayRemoveFirst(final short[] array, final short value) {
		final int index = arrayFirstIndexOf(array, value);
		if (index != -1)
			return arrayRemoveAtUnsafe(array, index);

		return array;
	}

	public static final <T> T[] arrayRemoveFirst(final T[] array, final T value) {
		final int index = arrayFirstIndexOf(array, value);
		if (index != -1)
			return arrayRemoveAtUnsafe(array, index);

		return array;
	}

	public static final <T> T[] arrayRemoveLast(final T[] array, final T value) {
		final int index = arrayLastIndexOf(array, value);
		if (index != -1)
			return arrayRemoveAtUnsafe(array, index);

		return array;
	}

	public static final void arrayShuffle(final byte[] array) {
		byte value;
		final Random r = new Random();
		for (int i = array.length, j; i-- > 0;) {
			j = r.nextInt(array.length);
			value = array[i];
			array[i] = array[j];
			array[j] = value;
		}
	}

	public static final void arrayShuffle(final byte[] array, final Random random) {
		byte value;
		for (int i = array.length, j; i-- > 0;) {
			j = random.nextInt(array.length);
			value = array[i];
			array[i] = array[j];
			array[j] = value;
		}
	}

	public static final void arrayShuffle(final char[] array) {
		char value;
		final Random r = new Random();
		for (int i = array.length, j; i-- > 0;) {
			j = r.nextInt(array.length);
			value = array[i];
			array[i] = array[j];
			array[j] = value;
		}
	}

	public static final void arrayShuffle(final char[] array, final Random random) {
		char value;
		for (int i = array.length, j; i-- > 0;) {
			j = random.nextInt(array.length);
			value = array[i];
			array[i] = array[j];
			array[j] = value;
		}
	}

	public static final void arrayShuffle(final double[] array) {
		double value;
		final Random r = new Random();
		for (int i = array.length, j; i-- > 0;) {
			j = r.nextInt(array.length);
			value = array[i];
			array[i] = array[j];
			array[j] = value;
		}
	}

	public static final void arrayShuffle(final double[] array, final Random random) {
		double value;
		for (int i = array.length, j; i-- > 0;) {
			j = random.nextInt(array.length);
			value = array[i];
			array[i] = array[j];
			array[j] = value;
		}
	}

	public static final void arrayShuffle(final float[] array) {
		float value;
		final Random r = new Random();
		for (int i = array.length, j; i-- > 0;) {
			j = r.nextInt(array.length);
			value = array[i];
			array[i] = array[j];
			array[j] = value;
		}
	}

	public static final void arrayShuffle(final float[] array, final Random random) {
		float value;
		for (int i = array.length, j; i-- > 0;) {
			j = random.nextInt(array.length);
			value = array[i];
			array[i] = array[j];
			array[j] = value;
		}
	}

	public static final void arrayShuffle(final int[] array) {
		int value;
		final Random r = new Random();
		for (int i = array.length, j; i-- > 0;) {
			j = r.nextInt(array.length);
			value = array[i];
			array[i] = array[j];
			array[j] = value;
		}
	}

	public static final void arrayShuffle(final int[] array, final Random random) {
		int value;
		for (int i = array.length, j; i-- > 0;) {
			j = random.nextInt(array.length);
			value = array[i];
			array[i] = array[j];
			array[j] = value;
		}
	}

	public static final void arrayShuffle(final long[] array) {
		long value;
		final Random r = new Random();
		for (int i = array.length, j; i-- > 0;) {
			j = r.nextInt(array.length);
			value = array[i];
			array[i] = array[j];
			array[j] = value;
		}
	}

	public static final void arrayShuffle(final long[] array, final Random random) {
		long value;
		for (int i = array.length, j; i-- > 0;) {
			j = random.nextInt(array.length);
			value = array[i];
			array[i] = array[j];
			array[j] = value;
		}
	}

	public static final void arrayShuffle(final short[] array) {
		short value;
		final Random r = new Random();
		for (int i = array.length, j; i-- > 0;) {
			j = r.nextInt(array.length);
			value = array[i];
			array[i] = array[j];
			array[j] = value;
		}
	}

	public static final void arrayShuffle(final short[] array, final Random random) {
		short value;
		for (int i = array.length, j; i-- > 0;) {
			j = random.nextInt(array.length);
			value = array[i];
			array[i] = array[j];
			array[j] = value;
		}
	}

	public static final <T> void arrayShuffle(final T[] array) {
		T value;
		final Random r = new Random();
		for (int i = array.length, j; i-- > 0;) {
			j = r.nextInt(array.length);
			value = array[i];
			array[i] = array[j];
			array[j] = value;
		}
	}

	public static final <T> void arrayShuffle(final T[] array, final Random random) {
		T value;
		for (int i = array.length, j; i-- > 0;) {
			j = random.nextInt(array.length);
			value = array[i];
			array[i] = array[j];
			array[j] = value;
		}
	}

	public static final int getC(final byte[] array, final int pos) {
		return array[pos] & 0x000000FF;
	}

	public static final int getD(final byte[] array, final int pos) {
		return array[pos] & 0x000000FF | array[pos + 1] << 8 & 0x0000FF00 | array[pos + 2] << 16 & 0x00FF0000 | array[pos + 3] << 24 & 0xFF000000;
	}

	public static final double getF(final byte[] array, final int pos) {
		return Double.longBitsToDouble(getQ(array, pos));
	}

	public static final int getH(final byte[] array, final int pos) {
		return array[pos] & 0x000000FF | array[pos + 1] << 8 & 0x0000FF00;
	}

	public static final long getQ(final byte[] array, final int pos) {
		return (getD(array, pos) & 0xFFFFFFFFL) | (getD(array, pos + 4) & 0xFFFFFFFFL) << 32;
	}

	public static final void putC(final byte[] array, final int pos, final int value) {
		array[pos] = (byte) (value & 0x000000FF);
	}

	public static final void putD(final byte[] array, final int pos, final int value) {
		array[pos] = (byte) (value & 0x000000FF);
		array[pos + 1] = (byte) (value >> 8 & 0x000000FF);
		array[pos + 2] = (byte) (value >> 16 & 0x000000FF);
		array[pos + 3] = (byte) (value >> 24 & 0x000000FF);
	}

	public static final void putF(final byte[] array, final int pos, final double value) {
		putQ(array, pos, Double.doubleToRawLongBits(value));
	}

	public static final void putH(final byte[] array, final int pos, final int value) {
		array[pos] = (byte) (value & 0x000000FF);
		array[pos + 1] = (byte) (value >> 8 & 0x000000FF);
	}

	public static final void putQ(final byte[] array, final int pos, final long value) {
		array[pos] = (byte) (value & 0x000000FF);
		array[pos + 1] = (byte) (value >> 8 & 0x000000FF);
		array[pos + 2] = (byte) (value >> 16 & 0x000000FF);
		array[pos + 3] = (byte) (value >> 24 & 0x000000FF);
		array[pos + 4] = (byte) (value >> 32 & 0x000000FF);
		array[pos + 5] = (byte) (value >> 40 & 0x000000FF);
		array[pos + 6] = (byte) (value >> 48 & 0x000000FF);
		array[pos + 7] = (byte) (value >> 56 & 0x000000FF);
	}
}