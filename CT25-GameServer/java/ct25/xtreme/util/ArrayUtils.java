package ct25.xtreme.util;

import java.lang.reflect.Array;

public class ArrayUtils
{
	public static final int INDEX_NOT_FOUND = -1;

	public static boolean contains(Object[] array, Object objectToFind)
	{
		return indexOf(array, objectToFind) != INDEX_NOT_FOUND;
	}

	public static int indexOf(Object[] array, Object objectToFind)
	{
		return indexOf(array, objectToFind, 0);
	}

	public static int indexOf(Object[] array, Object objectToFind, int startIndex)
	{
		if (array == null)
		{
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0)
		{
			startIndex = 0;
		}
		if (objectToFind == null)
		{
			for (int i = startIndex; i < array.length; i++)
			{
				if (array[i] == null)
				{
					return i;
				}
			}
		}
		else
		{
			for (int i = startIndex; i < array.length; i++)
			{
				if (objectToFind.equals(array[i]))
				{
					return i;
				}
			}
		}
		return INDEX_NOT_FOUND;
	}

	public static boolean isIntInArray(int val, int[] array)
	{
		for (int elem : array)
		{
			if (val == elem)
				return true;
		}
		return false;
	}

	public static Object[] add(Object[] array, Object element)
	{
		Class<?> type;
		if (array != null)
		{
			type = array.getClass();
		}
		else if (element != null)
		{
			type = element.getClass();
		}
		else
		{
			type = Object.class;
		}
		Object[] newArray = (Object[]) copyArrayGrow1(array, type);
		newArray[newArray.length - 1] = element;
		return newArray;
	}

	private static Object copyArrayGrow1(Object array, Class<?> newArrayComponentType)
	{
		if (array != null)
		{
			int arrayLength = Array.getLength(array);
			Object newArray = Array.newInstance(array.getClass().getComponentType(), arrayLength + 1);
			System.arraycopy(array, 0, newArray, 0, arrayLength);
			return newArray;
		}
		return Array.newInstance(newArrayComponentType, 1);
	}

	public static Object[] remove(Object[] array, int index)
	{
		return (Object[]) remove((Object) array, index);
	}

	private static Object remove(Object array, int index)
	{
		int length = getLength(array);
		if (index < 0 || index >= length)
		{
			throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + length);
		}

		Object result = Array.newInstance(array.getClass().getComponentType(), length - 1);
		System.arraycopy(array, 0, result, 0, index);
		if (index < length - 1)
		{
			System.arraycopy(array, index + 1, result, index, length - index - 1);
		}

		return result;
	}

	public static int getLength(Object array)
	{
		if (array == null)
		{
			return 0;
		}
		return Array.getLength(array);
	}
}
